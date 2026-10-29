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

/** Izgled widgeta: veličina slova, tamna pozadina i boja oznake (indeksi u WidgetOpcije). */
data class WidgetIzgled(val velicina: Int = 1, val tamna: Boolean = false, val akcenat: Int = 0)

object WidgetOpcije {
    val velicine = listOf("Malo", "Normalno", "Veliko", "Vrlo veliko")
    val faktori = listOf(0.85f, 1f, 1.25f, 1.5f)
    val akcentiNazivi = listOf("Plava", "Zelena", "Ljubičasta", "Narandžasta", "Crvena")
    val akcenti: List<Long> = listOf(0xFF1D4ED8, 0xFF0F766E, 0xFF7C3AED, 0xFFC2410C, 0xFFBE123C)

    fun faktor(i: WidgetIzgled): Float = faktori.getOrElse(i.velicina) { 1f }
    fun akcenat(i: WidgetIzgled): Long = akcenti.getOrElse(i.akcenat) { akcenti[0] }
}

/** Sve što aplikacija čuva. postavke == null znači da podešavanje još nije urađeno. */
data class Podaci(
    val postavke: Postavke?,
    val raspored: List<Cas>,
    val izmene: List<Izmena>,
    val satnica: Satnica,
    val widget: WidgetIzgled = WidgetIzgled(),
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
