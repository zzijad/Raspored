package com.zi.raspored.logic

import com.zi.raspored.model.CasUDanu
import com.zi.raspored.model.Podaci
import com.zi.raspored.model.Postavke
import com.zi.raspored.model.SMENA_A
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

    /** Danas više nema časova. datum/cas = sledeći čas (null ako ga nema do kraja godine). */
    data class Slobodno(val razlog: String, val datum: LocalDate?, val cas: CasUDanu?) : Stanje
}

object Motor {

    /**
     * Da li je nedelja kojoj pripada datum u "septembarskom" rasporedu smena.
     * Pravilo: odlučuje mesec u kome je ponedeljak te nedelje; smene se menjaju svakog meseca.
     */
    fun kaoUSeptembru(datum: LocalDate): Boolean {
        val ponedeljak = datum.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY))
        // prva nedelja škole može početi u avgustu (npr. 31.8.2026)
        val mesec = if (ponedeljak.monthValue == 8) 9 else ponedeljak.monthValue
        val odSeptembra = (mesec - 9 + 12) % 12
        return odSeptembra % 2 == 0
    }

    /** Da li smena (A ili B) radi posle podne na taj datum. */
    fun posle(datum: LocalDate, smena: Int, postavke: Postavke): Boolean {
        val a = if (kaoUSeptembru(datum)) postavke.aPosleUSeptembru else !postavke.aPosleUSeptembru
        return if (smena == SMENA_A) a else !a
    }

    fun termini(datum: LocalDate, smena: Int, p: Podaci): List<Termin> {
        val post = p.postavke ?: return p.satnica.pre
        return if (posle(datum, smena, post)) p.satnica.posle else p.satnica.pre
    }

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

    /** Časovi jedne smene za dan, sa primenjenim izmenama. Otkazani su uključeni (otkazan = true). */
    fun casoviSmene(datum: LocalDate, smena: Int, p: Podaci): List<CasUDanu> {
        val post = p.postavke ?: return emptyList()
        if (!jeNastavniDan(datum) || post.smena(smena) == null) return emptyList()
        val termini = termini(datum, smena, p)
        val posle = posle(datum, smena, post)
        val mapa = sortedMapOf<Int, Slot>()

        p.raspored
            .filter { it.dan == datum.dayOfWeek.value && it.smena == smena }
            .forEach { mapa[it.broj] = Slot(it.odeljenje, it.grupa, izmenjen = false, otkazan = false) }

        p.izmene.filter { it.datum == datum && it.smena == smena }.forEach { iz ->
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
            .map { (broj, s) ->
                CasUDanu(broj, smena, posle, s.odeljenje, s.grupa, termini[broj - 1], s.izmenjen, s.otkazan)
            }
    }

    /** Svi časovi za dan (obe smene), poređani po vremenu. */
    fun casoviZaDan(datum: LocalDate, p: Podaci): List<CasUDanu> {
        val post = p.postavke ?: return emptyList()
        return post.aktivne.flatMap { casoviSmene(datum, it, p) }.sortedBy { it.termin.pocetak }
    }

    private fun minutaDo(od: LocalTime, doVremena: LocalTime): Long =
        (Duration.between(od, doVremena).seconds + 59) / 60

    fun stanje(sada: LocalDateTime, p: Podaci): Stanje {
        val danas = sada.toLocalDate()
        val vreme = sada.toLocalTime()
        val casovi = casoviZaDan(danas, p).filter { !it.otkazan }

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
        val sledeci = sledeciCas(danas.plusDays(1), p)
        return Stanje.Slobodno(razlog, sledeci?.first, sledeci?.second)
    }

    /** Prvi sledeći dan (počev od datuma) koji ima bar jedan čas. */
    fun sledeciCas(od: LocalDate, p: Podaci): Pair<LocalDate, CasUDanu>? {
        var d = od
        repeat(250) {
            val prvi = casoviZaDan(d, p).firstOrNull { !it.otkazan }
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

    /** Sledeći nastavni dan kada smena A menja doba dana: (datum, posle podne?). */
    fun promenaSmene(od: LocalDate, postavke: Postavke): Pair<LocalDate, Boolean>? {
        val trenutno = posle(od, SMENA_A, postavke)
        var d = od.plusDays(1)
        repeat(120) {
            if (jeNastavniDan(d) && posle(d, SMENA_A, postavke) != trenutno) return d to !trenutno
            d = d.plusDays(1)
        }
        return null
    }

    /**
     * Primenjuje nove postavke: iz rasporeda i izmena uklanja odeljenja koja više nisu izabrana,
     * a grupe briše tamo gde odeljenje više nema grupe.
     */
    fun primeniPostavke(p: Podaci, nove: Postavke): Podaci {
        fun ok(smena: Int, odeljenje: String?): Boolean =
            odeljenje == null || nove.smena(smena)?.odeljenja?.contains(odeljenje) == true

        val raspored = p.raspored
            .filter { ok(it.smena, it.odeljenje) }
            .map { if (nove.smena(it.smena)?.imaGrupe(it.odeljenje) == true) it else it.copy(grupa = null) }
        val izmene = p.izmene
            .filter { nove.smena(it.smena) != null && ok(it.smena, it.odeljenje) }
            .map { iz ->
                val od = iz.odeljenje
                if (od != null && nove.smena(iz.smena)?.imaGrupe(od) != true) iz.copy(grupa = null) else iz
            }
        return p.copy(postavke = nove, raspored = raspored, izmene = izmene)
    }
}
