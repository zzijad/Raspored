package com.zi.raspored.data

import com.zi.raspored.model.Kalendar
import java.time.LocalDate

/** Greška pri čitanju kalendara, sa porukom za korisnika. */
class KalendarGreska(poruka: String) : Exception(poruka)

/**
 * Čita školski kalendar sa stranice "Školski kalendar" školskog portala (platforma ministarstva).
 *
 * Stranica ima dve tabele (polugodišta). Svaki dan je ćelija sa brojem dana; neradni dani imaju
 * oznaku (simbol) u ćeliji: obojenu pozadinu (raspust, državni praznik) ili okvir (Sveti Sava / Vidovdan,
 * kraj polugodišta, ispiti, praznici koji se obeležavaju radno). Uz svaki mesec stranica ispisuje broj
 * nastavnih dana; kalendar se prihvata samo ako se naše brojanje slaže sa tim brojevima u svakom mesecu.
 */
object KalendarCitac {

    private val meseci = mapOf(
        "септембар" to 9, "октобар" to 10, "новембар" to 11, "децембар" to 12,
        "јануар" to 1, "фебруар" to 2, "март" to 3, "април" to 4, "мај" to 5, "јун" to 6, "јул" to 7, "август" to 8,
        "septembar" to 9, "oktobar" to 10, "novembar" to 11, "decembar" to 12,
        "januar" to 1, "februar" to 2, "mart" to 3, "april" to 4, "maj" to 5, "jun" to 6, "jul" to 7, "avgust" to 8,
    )

    private val tabelaRegex = Regex("<table.*?</table>", RegexOption.DOT_MATCHES_ALL)
    private val godinaRegex = Regex("""(\d{4})""")
    private val tokenRegex = Regex(
        """>\s*(""" + meseci.keys.flatMap { listOf(it, it.replaceFirstChar { c -> c.uppercase() }, it.uppercase()) }
            .joinToString("|") { Regex.escape(it) } + """)\s*<""" +
            """|<td class="relative[^"]*"[^>]*>(.*?)</td>""" +
            """|rounded-full[^"]*">(\d+)<""",
        RegexOption.DOT_MATCHES_ALL,
    )
    private val danRegex = Regex("""pointer-events-none">(\d+)<""")
    private val simbolRegex = Regex(
        """<span class="simbol (simbol-fill|simbol-stroke)[^"]*" style="([^"]*)">(.*?)</svg>""",
        RegexOption.DOT_MATCHES_ALL,
    )

    private enum class Oznaka { RASPUST, DRZAVNI, BEZ_NASTAVE, KRAJ, OSTALO }

    private class Mesec(val mesec: Int) {
        var naStranici: Int? = null
        var izbrojano = 0
    }

    /** Baca KalendarGreska ako stranica nije prepoznata ili se brojevi ne slažu. */
    fun procitaj(html: String, izvor: String?, danas: LocalDate?): Kalendar {
        val tabele = tabelaRegex.findAll(html).map { it.value }.toList()
        if (tabele.isEmpty()) throw KalendarGreska("Na stranici nema tabele sa kalendarom.")

        // godina iz naslova prve tabele ("Прво полугодиште - 2026.")
        val zaglavlje = tabele.first().substringBefore("</th>")
        val pocetnaGodina = godinaRegex.findAll(zaglavlje).map { it.value.toInt() }.lastOrNull { it in 2020..2100 }
            ?: throw KalendarGreska("Na stranici nije pronađena školska godina.")

        val nastavni = mutableSetOf<LocalDate>()
        val opisi = mutableMapOf<LocalDate, String>()
        val krajevi = mutableListOf<LocalDate>()
        val poMesecima = mutableListOf<Mesec>()
        var tekuci: Mesec? = null

        for (tabela in tabele) {
            for (m in tokenRegex.findAll(tabela)) {
                val nazivMeseca = m.groups[1]?.value
                val celija = m.groups[2]?.value
                val broj = m.groups[3]?.value
                when {
                    nazivMeseca != null -> {
                        tekuci = Mesec(meseci.getValue(nazivMeseca.lowercase()))
                        poMesecima += tekuci!!
                    }
                    broj != null -> tekuci?.let { if (it.naStranici == null) it.naStranici = broj.toInt() }
                    celija != null -> {
                        val mes = tekuci ?: continue
                        val dan = danRegex.find(celija)?.groupValues?.get(1)?.toIntOrNull() ?: continue
                        val godina = if (mes.mesec >= 8) pocetnaGodina else pocetnaGodina + 1
                        val datum = runCatching { LocalDate.of(godina, mes.mesec, dan) }.getOrNull()
                            ?: throw KalendarGreska("Neispravan datum u tabeli ($dan. ${mes.mesec}.).")
                        val oznake = oznake(celija)
                        if (Oznaka.KRAJ in oznake) krajevi += datum
                        if (datum.dayOfWeek.value > 5) continue
                        val opis = when {
                            Oznaka.DRZAVNI in oznake -> "Državni praznik"
                            Oznaka.BEZ_NASTAVE in oznake -> when {
                                datum.monthValue == 1 && datum.dayOfMonth == 27 -> "Sveti Sava"
                                datum.monthValue == 6 && datum.dayOfMonth == 28 -> "Vidovdan"
                                else -> "Praznik, bez nastave"
                            }
                            Oznaka.RASPUST in oznake -> "Raspust"
                            else -> null
                        }
                        if (opis == null) {
                            nastavni += datum
                            mes.izbrojano++
                        } else {
                            opisi[datum] = opis
                        }
                    }
                }
            }
        }

        if (poMesecima.size < 8) throw KalendarGreska("Tabela kalendara nije prepoznata (pronađeno meseci: ${poMesecima.size}).")
        for (m in poMesecima) {
            val naStranici = m.naStranici ?: throw KalendarGreska("Za ${m.mesec}. mesec nije pronađen broj nastavnih dana.")
            if (naStranici != m.izbrojano) {
                throw KalendarGreska(
                    "Kalendar nije prepoznat: za ${m.mesec}. mesec stranica kaže $naStranici nastavnih dana, a izbrojano je ${m.izbrojano}.",
                )
            }
        }
        if (nastavni.size < 100) throw KalendarGreska("Premalo nastavnih dana (${nastavni.size}).")

        // dve oznake kraja u drugom polugodištu: ranija je kraj za VIII razred
        val krajeviDrugog = krajevi.filter { it.monthValue in 1..8 }.sorted()
        val krajOsmi = if (krajeviDrugog.size >= 2) krajeviDrugog.first() else null

        return Kalendar(nastavni, opisi, krajOsmi, izvor, danas)
    }

    private fun oznake(celija: String): Set<Oznaka> {
        val rezultat = mutableSetOf<Oznaka>()
        for (s in simbolRegex.findAll(celija)) {
            val vrsta = s.groupValues[1]
            val stil = s.groupValues[2]
            val svg = s.groupValues[3]
            val brojPravougaonika = Regex("<rect").findAll(svg).count()
            rezultat += when {
                vrsta == "simbol-fill" && "tertiary" in stil -> Oznaka.RASPUST
                vrsta == "simbol-fill" && "secondary" in stil && brojPravougaonika > 0 -> Oznaka.DRZAVNI
                vrsta == "simbol-stroke" && brojPravougaonika >= 2 -> Oznaka.KRAJ
                vrsta == "simbol-stroke" && brojPravougaonika == 1 && "dasharray" !in svg -> Oznaka.BEZ_NASTAVE
                else -> Oznaka.OSTALO
            }
        }
        return rezultat
    }
}
