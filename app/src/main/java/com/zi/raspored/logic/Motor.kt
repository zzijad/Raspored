package com.zi.raspored.logic

import com.zi.raspored.model.CasUDanu
import com.zi.raspored.model.Izmena
import com.zi.raspored.model.SkolskiPodaci
import com.zi.raspored.model.Termin
import com.zi.raspored.model.TipIzmene
import java.time.DayOfWeek
import java.time.Duration
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.temporal.TemporalAdjusters

sealed interface Stanje {
    /** Trenutno traje čas. */
    data class NaCasu(
        val cas: CasUDanu,
        val ostaloMin: Long,
        val napredak: Float,
        val sledeci: CasUDanu?,
    ) : Stanje

    /** Danas ima još časova, ali trenutno se čeka (pre prvog ili na odmoru). */
    data class Ceka(val cas: CasUDanu, val zaMin: Long, val prviDanas: Boolean) : Stanje

    /** Danas više nema časova. datum/cas = sledeći čas (null ako je kraj godine). */
    data class Slobodno(val razlog: String, val datum: LocalDate?, val cas: CasUDanu?) : Stanje
}

object Motor {

    /**
     * Smena za V i VII razred. Pravilo: smena se određuje po mesecu u kome je ponedeljak te nedelje.
     * Septembar = II smena, oktobar = I, novembar = II... (naizmenično).
     */
    fun smena(datum: LocalDate): Int {
        val ponedeljak = datum.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY))
        // prva nedelja škole može početi u avgustu (npr. 31.8.2026)
        val mesec = if (ponedeljak.monthValue == 8) 9 else ponedeljak.monthValue
        val odSeptembra = (mesec - 9 + 12) % 12
        return if (odSeptembra % 2 == 0) 2 else 1
    }

    fun termini(datum: LocalDate): List<Termin> = SkolskiPodaci.satnica.getValue(smena(datum))

    private fun uPolugodistu(datum: LocalDate): Boolean =
        SkolskiPodaci.polugodista.any { !datum.isBefore(it.od) && !datum.isAfter(it.doDana) }

    fun jeNastavniDan(datum: LocalDate): Boolean =
        datum.dayOfWeek.value <= 5 && uPolugodistu(datum) && datum !in SkolskiPodaci.neradni

    fun razlogNeradnog(datum: LocalDate): String? = when {
        datum.dayOfWeek.value > 5 -> "Vikend"
        !uPolugodistu(datum) -> "Raspust"
        datum in SkolskiPodaci.neradni -> "Neradni dan"
        else -> null
    }

    private data class Slot(val odeljenje: String, val grupa: Int?, val izmenjen: Boolean, val otkazan: Boolean)

    /** Časovi za dan sa primenjenim izmenama. Otkazani časovi su uključeni (otkazan = true). */
    fun casoviZaDan(datum: LocalDate, izmene: List<Izmena>): List<CasUDanu> {
        if (!jeNastavniDan(datum)) return emptyList()
        val termini = termini(datum)
        val mapa = sortedMapOf<Int, Slot>()

        SkolskiPodaci.raspored
            .filter { it.dan == datum.dayOfWeek.value }
            .forEach { mapa[it.broj] = Slot(it.odeljenje, it.grupa, izmenjen = false, otkazan = false) }

        izmene.filter { it.datum == datum }.forEach { iz ->
            when (iz.tip) {
                TipIzmene.OTKAZI -> {
                    val postojeci = mapa[iz.broj]
                    if (postojeci != null) mapa[iz.broj] = postojeci.copy(izmenjen = true, otkazan = true)
                }
                TipIzmene.DODAJ, TipIzmene.ZAMENI -> {
                    val od = iz.odeljenje
                    if (od != null) mapa[iz.broj] = Slot(od, iz.grupa, izmenjen = true, otkazan = false)
                }
            }
        }

        return mapa
            .filterKeys { it in 1..termini.size }
            .map { (broj, s) -> CasUDanu(broj, s.odeljenje, s.grupa, termini[broj - 1], s.izmenjen, s.otkazan) }
    }

    private fun minutaDo(od: LocalTime, doVremena: LocalTime): Long =
        (Duration.between(od, doVremena).seconds + 59) / 60

    fun stanje(sada: LocalDateTime, izmene: List<Izmena>): Stanje {
        val danas = sada.toLocalDate()
        val vreme = sada.toLocalTime()
        val casovi = casoviZaDan(danas, izmene).filter { !it.otkazan }

        casovi.forEachIndexed { i, c ->
            if (!vreme.isBefore(c.termin.pocetak) && vreme.isBefore(c.termin.kraj)) {
                val ukupno = Duration.between(c.termin.pocetak, c.termin.kraj).seconds.toFloat()
                val proslo = Duration.between(c.termin.pocetak, vreme).seconds.toFloat()
                return Stanje.NaCasu(c, minutaDo(vreme, c.termin.kraj), proslo / ukupno, casovi.getOrNull(i + 1))
            }
            if (vreme.isBefore(c.termin.pocetak)) {
                return Stanje.Ceka(c, minutaDo(vreme, c.termin.pocetak), prviDanas = i == 0)
            }
        }

        val razlog = when {
            casovi.isNotEmpty() -> "Za danas je gotovo"
            else -> razlogNeradnog(danas) ?: "Danas nemaš časova"
        }
        val sledeci = sledeciCas(danas.plusDays(1), izmene)
        return Stanje.Slobodno(razlog, sledeci?.first, sledeci?.second)
    }

    /** Prvi sledeći dan (počev od datuma) koji ima bar jedan čas. */
    fun sledeciCas(od: LocalDate, izmene: List<Izmena>): Pair<LocalDate, CasUDanu>? {
        var d = od
        repeat(250) {
            val prvi = casoviZaDan(d, izmene).firstOrNull { !it.otkazan }
            if (prvi != null) return d to prvi
            d = d.plusDays(1)
        }
        return null
    }

    fun preostaloNastavnihDana(od: LocalDate): Int {
        val kraj = SkolskiPodaci.polugodista.last().doDana
        var d = od
        var broj = 0
        while (!d.isAfter(kraj)) {
            if (jeNastavniDan(d)) broj++
            d = d.plusDays(1)
        }
        return broj
    }

    /** Sledeći nastavni dan u kome je druga smena: (datum, nova smena). */
    fun promenaSmene(od: LocalDate): Pair<LocalDate, Int>? {
        val trenutna = smena(od)
        var d = od.plusDays(1)
        repeat(120) {
            if (jeNastavniDan(d) && smena(d) != trenutna) return d to smena(d)
            d = d.plusDays(1)
        }
        return null
    }
}
