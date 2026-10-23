package com.zi.raspored.model

import java.time.LocalDate
import java.time.LocalTime

/**
 * Podaci za školsku 2026/2027. godinu.
 * Za novu školsku godinu menjaju se samo polugodišta i neradni dani (i raspored ako se promeni).
 */
object SkolskiPodaci {

    private fun t(od: String, `do`: String) = Termin(LocalTime.parse(od), LocalTime.parse(`do`))

    /** Satnica po smenama: 1 = I smena, 2 = II smena. Veliki odmor je posle 2. časa. */
    val satnica: Map<Int, List<Termin>> = mapOf(
        1 to listOf(
            t("07:00", "07:45"), t("07:50", "08:35"), t("08:55", "09:40"), t("09:45", "10:30"),
            t("10:35", "11:20"), t("11:25", "12:10"), t("12:15", "13:00"),
        ),
        2 to listOf(
            t("13:50", "14:35"), t("14:40", "15:25"), t("15:45", "16:30"), t("16:35", "17:20"),
            t("17:25", "18:10"), t("18:15", "19:00"), t("19:05", "19:50"),
        ),
    )

    /** Osnovni raspored: dan, broj časa, odeljenje, grupa. */
    val raspored: List<Cas> = listOf(
        // Ponedeljak
        Cas(1, 4, "V3", 1), Cas(1, 5, "VII6"), Cas(1, 6, "V6", 1), Cas(1, 7, "V3", 2),
        // Utorak
        Cas(2, 5, "VII5", 2), Cas(2, 6, "V2", 2), Cas(2, 7, "V1", 2),
        // Sreda
        Cas(3, 3, "V4"), Cas(3, 5, "VII1", 1), Cas(3, 6, "VII3", 1), Cas(3, 7, "VII4", 1),
        // Četvrtak
        Cas(4, 4, "V6", 2), Cas(4, 5, "VII1", 2), Cas(4, 6, "VII2"), Cas(4, 7, "V1", 1),
        // Petak
        Cas(5, 3, "V2", 1), Cas(5, 4, "V5"), Cas(5, 5, "VII5", 1), Cas(5, 6, "VII4", 2), Cas(5, 7, "VII3", 2),
    )

    /** Polugodišta (za V i VII razred nastava traje do 11. juna). */
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

    val odeljenja: List<String> = listOf(
        "V1", "V2", "V3", "V4", "V5", "V6",
        "VII1", "VII2", "VII3", "VII4", "VII5", "VII6",
    )
}
