package com.zi.raspored.model

import java.time.LocalDate
import java.time.LocalTime

/** Jedan čas iz osnovnog rasporeda. dan: 1 = ponedeljak ... 5 = petak. grupa: null = celo odeljenje. */
data class Cas(val dan: Int, val broj: Int, val odeljenje: String, val grupa: Int? = null)

enum class TipIzmene { DODAJ, ZAMENI, OTKAZI }

/** Jednokratna izmena za konkretan datum i broj časa. */
data class Izmena(
    val datum: LocalDate,
    val broj: Int,
    val tip: TipIzmene,
    val odeljenje: String? = null,
    val grupa: Int? = null,
)

data class Termin(val pocetak: LocalTime, val kraj: LocalTime)

data class Period(val od: LocalDate, val doDana: LocalDate)

/** Čas kakav stvarno važi za određeni dan (posle primene izmena). */
data class CasUDanu(
    val broj: Int,
    val odeljenje: String,
    val grupa: Int?,
    val termin: Termin,
    val izmenjen: Boolean,
    val otkazan: Boolean,
) {
    val oznaka: String get() = if (grupa == null) odeljenje else "$odeljenje [$grupa]"
}
