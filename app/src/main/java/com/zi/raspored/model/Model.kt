package com.zi.raspored.model

import java.time.LocalDate
import java.time.LocalTime

const val SMENA_A = 0
const val SMENA_B = 1

/** Jedan čas iz osnovnog rasporeda. dan: 1 = ponedeljak ... 5 = petak. smena: SMENA_A / SMENA_B. grupa: null = celo odeljenje. */
data class Cas(val dan: Int, val broj: Int, val smena: Int, val odeljenje: String, val grupa: Int? = null)

enum class TipIzmene { DODAJ, ZAMENI, OTKAZI }

/** Jednokratna izmena za konkretan datum, broj časa i smenu. */
data class Izmena(
    val datum: LocalDate,
    val broj: Int,
    val smena: Int,
    val tip: TipIzmene,
    val odeljenje: String? = null,
    val grupa: Int? = null,
)

data class Termin(val pocetak: LocalTime, val kraj: LocalTime)

data class Period(val od: LocalDate, val doDana: LocalDate)

/** Odeljenja kojima nastavnik predaje u jednoj smeni i koja od njih imaju grupe. */
data class Smena(val odeljenja: List<String>, val grupe: Set<String>) {
    fun imaGrupe(odeljenje: String): Boolean = odeljenje in grupe
}

/**
 * Podešavanja nastavnika.
 * aPosleUSeptembru: da li smena A radi posle podne u septembru. Smena B je uvek suprotna od A.
 */
data class Postavke(val a: Smena, val aPosleUSeptembru: Boolean, val b: Smena?) {
    fun smena(indeks: Int): Smena? = if (indeks == SMENA_A) a else b
    val aktivne: List<Int> get() = if (b == null) listOf(SMENA_A) else listOf(SMENA_A, SMENA_B)
}

/** Satnica: termini časova pre podne (I smena) i posle podne (II smena). */
data class Satnica(val pre: List<Termin>, val posle: List<Termin>)

/** Sve što aplikacija čuva. postavke == null znači da podešavanje još nije urađeno. */
data class Podaci(
    val postavke: Postavke?,
    val raspored: List<Cas>,
    val izmene: List<Izmena>,
    val satnica: Satnica,
)

/** Čas kakav stvarno važi za određeni dan (posle primene izmena). */
data class CasUDanu(
    val broj: Int,
    val smena: Int,
    val posle: Boolean,
    val odeljenje: String,
    val grupa: Int?,
    val termin: Termin,
    val izmenjen: Boolean,
    val otkazan: Boolean,
) {
    val oznaka: String get() = if (grupa == null) odeljenje else "$odeljenje [$grupa]"
}
