package com.zi.raspored.model

import java.time.LocalDate
import java.time.LocalTime

/**
 * Podaci zajednički za celu školu: školski kalendar 2026/2027. i podrazumevana satnica.
 * Za novu školsku godinu menjaju se polugodišta i neradni dani.
 */
object SkolskiPodaci {

    private fun t(od: String, `do`: String) = Termin(LocalTime.parse(od), LocalTime.parse(`do`))

    /** Podrazumevana satnica (može da se promeni u Podešavanjima). Veliki odmor je posle 2. časa. */
    val podrazumevanaSatnica = Satnica(
        pre = listOf(
            t("07:00", "07:45"), t("07:50", "08:35"), t("08:55", "09:40"), t("09:45", "10:30"),
            t("10:35", "11:20"), t("11:25", "12:10"), t("12:15", "13:00"),
        ),
        posle = listOf(
            t("13:50", "14:35"), t("14:40", "15:25"), t("15:45", "16:30"), t("16:35", "17:20"),
            t("17:25", "18:10"), t("18:15", "19:00"), t("19:05", "19:50"),
        ),
    )

    const val BROJ_CASOVA = 7

    /** Polugodišta (za V–VII razred nastava traje do 11. juna). */
    val polugodista: List<Period> = listOf(
        Period(LocalDate.of(2026, 9, 1), LocalDate.of(2026, 12, 30)),
        Period(LocalDate.of(2027, 1, 18), LocalDate.of(2027, 6, 11)),
    )

    /** Neradni dani unutar polugodišta (praznici, prolećni raspust...). */
    val neradni: Set<LocalDate> = listOf(
        "2026-11-11", "2026-11-12", "2026-11-13",
        "2027-01-27",
        "2027-02-15", "2027-02-16", "2027-02-17", "2027-02-18", "2027-02-19",
        "2027-04-30",
        "2027-05-03", "2027-05-04",
    ).map { LocalDate.parse(it) }.toSet()

    val razredi: List<String> = listOf("V", "VI", "VII", "VIII")
    const val MAKS_ODELJENJA = 8

    /** Redosled za sortiranje: V1, V2 ... VI1 ... VIII8. */
    fun redosled(odeljenje: String): Int {
        val razred = odeljenje.takeWhile { !it.isDigit() }
        val broj = odeljenje.dropWhile { !it.isDigit() }.toIntOrNull() ?: 0
        return razredi.indexOf(razred) * 100 + broj
    }

    fun sortiraj(odeljenja: Collection<String>): List<String> = odeljenja.sortedBy { redosled(it) }
}
