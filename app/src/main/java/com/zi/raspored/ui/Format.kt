package com.zi.raspored.ui

import com.zi.raspored.model.Termin
import java.time.LocalDate
import java.time.LocalTime

val daniKratko = listOf("Pon", "Uto", "Sre", "Čet", "Pet", "Sub", "Ned")
val daniPuno = listOf("Ponedeljak", "Utorak", "Sreda", "Četvrtak", "Petak", "Subota", "Nedelja")
val meseci = listOf(
    "januar", "februar", "mart", "april", "maj", "jun",
    "jul", "avgust", "septembar", "oktobar", "novembar", "decembar",
)

fun LocalDate.lepo(): String = "${daniPuno[dayOfWeek.value - 1]}, $dayOfMonth. ${meseci[monthValue - 1]}"

fun LocalDate.kratko(): String = "$dayOfMonth.$monthValue."

fun LocalTime.hm(): String = "%02d:%02d".format(hour, minute)

fun Termin.tekst(): String = "${pocetak.hm()}–${kraj.hm()}"

/** I smena = pre podne, II smena = posle podne. */
fun dobaTekst(posle: Boolean): String = if (posle) "II smena" else "I smena"

fun smenaSlovo(smena: Int): String = if (smena == 0) "A" else "B"

fun trajanje(min: Long): String =
    if (min >= 60) "${min / 60} h ${min % 60} min" else "$min min"

fun brojCasova(n: Int): String = when {
    n % 10 == 1 && n % 100 != 11 -> "$n čas"
    n % 10 in 2..4 && n % 100 !in 12..14 -> "$n časa"
    else -> "$n časova"
}

fun brojNastavnihDana(n: Int): String = when {
    n % 10 == 1 && n % 100 != 11 -> "$n nastavni dan"
    n % 10 in 2..4 && n % 100 !in 12..14 -> "$n nastavna dana"
    else -> "$n nastavnih dana"
}
