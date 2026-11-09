package com.zi.raspored.model

import java.time.LocalDate

/**
 * Školski kalendar jedne školske godine.
 * nastavni = svi nastavni dani (za I–VII razred), opisi = naziv neradnog dana (praznik, raspust...).
 * krajOsmi = poslednji nastavni dan za VIII razred (ako se razlikuje).
 * izvor = adresa sajta sa kog je preuzet, ili null za ugrađeni kalendar.
 */
data class Kalendar(
    val nastavni: Set<LocalDate>,
    val opisi: Map<LocalDate, String>,
    val krajOsmi: LocalDate?,
    val izvor: String?,
    val preuzet: LocalDate?,
) {
    val prvi: LocalDate = nastavni.minOrNull() ?: LocalDate.of(2026, 9, 1)
    val poslednji: LocalDate = nastavni.maxOrNull() ?: LocalDate.of(2027, 6, 11)
    val godina: String get() = "${prvi.year}/${poslednji.year}"
}

/** Kalendar koji aplikacija trenutno koristi (preuzeti ili ugrađeni). */
object Kalendari {

    /** Ugrađeni kalendar 2026/2027, pravljen iz SkolskiPodaci. */
    val ugradjen: Kalendar by lazy {
        val nastavni = mutableSetOf<LocalDate>()
        val opisi = mutableMapOf<LocalDate, String>()
        var d = SkolskiPodaci.polugodista.first().od
        val kraj = SkolskiPodaci.polugodista.last().doDana
        while (!d.isAfter(kraj)) {
            val uPolugodistu = SkolskiPodaci.polugodista.any { !d.isBefore(it.od) && !d.isAfter(it.doDana) }
            if (d.dayOfWeek.value <= 5) {
                when {
                    !uPolugodistu -> opisi[d] = "Raspust"
                    d in SkolskiPodaci.neradni -> opisi[d] = "Neradni dan"
                    else -> nastavni += d
                }
            }
            d = d.plusDays(1)
        }
        Kalendar(nastavni, opisi, SkolskiPodaci.krajOsmi, izvor = null, preuzet = null)
    }

    @Volatile
    var aktivan: Kalendar = ugradjen
}
