package com.zi.raspored

import com.zi.raspored.logic.Motor
import com.zi.raspored.logic.Stanje
import com.zi.raspored.model.Cas
import com.zi.raspored.model.Izmena
import com.zi.raspored.model.Podaci
import com.zi.raspored.model.Postavke
import com.zi.raspored.model.SMENA_A
import com.zi.raspored.model.SMENA_B
import com.zi.raspored.model.SkolskiPodaci
import com.zi.raspored.model.Smena
import com.zi.raspored.model.TipIzmene
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalDate
import java.time.LocalDateTime

class MotorTest {

    private fun d(g: Int, m: Int, dan: Int) = LocalDate.of(g, m, dan)

    private val odeljenjaZi = listOf(
        "V1", "V2", "V3", "V4", "V5", "V6", "VII1", "VII2", "VII3", "VII4", "VII5", "VII6",
    )
    private val grupeZi = setOf("V1", "V2", "V3", "V6", "VII1", "VII3", "VII4", "VII5")

    private val rasporedZi = listOf(
        Cas(1, 4, SMENA_A, "V3", 1), Cas(1, 5, SMENA_A, "VII6"), Cas(1, 6, SMENA_A, "V6", 1), Cas(1, 7, SMENA_A, "V3", 2),
        Cas(2, 5, SMENA_A, "VII5", 2), Cas(2, 6, SMENA_A, "V2", 2), Cas(2, 7, SMENA_A, "V1", 2),
        Cas(3, 3, SMENA_A, "V4"), Cas(3, 5, SMENA_A, "VII1", 1), Cas(3, 6, SMENA_A, "VII3", 1), Cas(3, 7, SMENA_A, "VII4", 1),
        Cas(4, 4, SMENA_A, "V6", 2), Cas(4, 5, SMENA_A, "VII1", 2), Cas(4, 6, SMENA_A, "VII2"), Cas(4, 7, SMENA_A, "V1", 1),
        Cas(5, 3, SMENA_A, "V2", 1), Cas(5, 4, SMENA_A, "V5"), Cas(5, 5, SMENA_A, "VII5", 1), Cas(5, 6, SMENA_A, "VII4", 2),
        Cas(5, 7, SMENA_A, "VII3", 2),
    )

    private val zi = Podaci(
        postavke = Postavke(Smena(odeljenjaZi, grupeZi), aPosleUSeptembru = true, b = null),
        raspored = rasporedZi,
        izmene = emptyList(),
        satnica = SkolskiPodaci.podrazumevanaSatnica,
    )

    @Test
    fun smene() {
        val post = zi.postavke!!
        assertTrue(Motor.posle(d(2026, 9, 1), SMENA_A, post))    // septembar = posle podne
        assertTrue(Motor.posle(d(2026, 10, 2), SMENA_A, post))   // petak, ponedeljak je 28.9.
        assertFalse(Motor.posle(d(2026, 10, 5), SMENA_A, post))  // oktobar = pre podne
        assertFalse(Motor.posle(d(2026, 10, 23), SMENA_A, post))
        assertTrue(Motor.posle(d(2026, 10, 23), SMENA_B, post))  // B je uvek suprotna
        assertTrue(Motor.posle(d(2026, 11, 2), SMENA_A, post))
        assertFalse(Motor.posle(d(2026, 12, 7), SMENA_A, post))
        assertTrue(Motor.posle(d(2027, 1, 18), SMENA_A, post))
        assertFalse(Motor.posle(d(2027, 2, 1), SMENA_A, post))
        assertFalse(Motor.posle(d(2027, 6, 7), SMENA_A, post))
    }

    @Test
    fun nastavniDaniPoMesecima() {
        val ocekivano = mapOf(
            d(2026, 9, 1) to 22, d(2026, 10, 1) to 22, d(2026, 11, 1) to 18, d(2026, 12, 1) to 22,
            d(2027, 1, 1) to 9, d(2027, 2, 1) to 15, d(2027, 3, 1) to 23, d(2027, 4, 1) to 21,
            d(2027, 5, 1) to 19, d(2027, 6, 1) to 9,
        )
        ocekivano.forEach { (pocetak, broj) ->
            val dani = (0 until pocetak.lengthOfMonth()).count { Motor.jeNastavniDan(pocetak.plusDays(it.toLong())) }
            assertEquals("Mesec ${pocetak.monthValue}", broj, dani)
        }
        assertEquals(142, Motor.preostaloNastavnihDana(d(2026, 10, 23)))
    }

    @Test
    fun naCasu() {
        val s = Motor.stanje(LocalDateTime.of(2026, 10, 23, 10, 18), zi)
        assertTrue(s is Stanje.NaCasu)
        s as Stanje.NaCasu
        assertEquals(4, s.cas.broj)
        assertEquals("V5", s.cas.oznaka)
        assertEquals(12L, s.ostaloMin)
        assertEquals("VII5 [1]", s.sledeci?.oznaka)
    }

    @Test
    fun prePrvogCasa() {
        val s = Motor.stanje(LocalDateTime.of(2026, 10, 23, 8, 40), zi)
        assertTrue(s is Stanje.Ceka)
        s as Stanje.Ceka
        assertEquals(3, s.cas.broj)
        assertTrue(s.prviDanas)
        assertEquals(15L, s.zaMin)
    }

    @Test
    fun vikend() {
        val s = Motor.stanje(LocalDateTime.of(2026, 10, 24, 12, 0), zi)
        assertTrue(s is Stanje.Slobodno)
        s as Stanje.Slobodno
        assertEquals("Vikend", s.razlog)
        assertEquals(d(2026, 10, 26), s.datum)
        assertEquals("V3 [1]", s.cas?.oznaka)
    }

    @Test
    fun praznaAplikacija() {
        val prazno = Podaci(null, emptyList(), emptyList(), SkolskiPodaci.podrazumevanaSatnica)
        assertTrue(Motor.casoviZaDan(d(2026, 10, 23), prazno).isEmpty())
        assertTrue(Motor.stanje(LocalDateTime.of(2026, 10, 23, 10, 0), prazno) is Stanje.Slobodno)
    }

    @Test
    fun otkazanCas() {
        val p = zi.copy(izmene = listOf(Izmena(d(2026, 10, 23), 4, SMENA_A, TipIzmene.OTKAZI)))
        val s = Motor.stanje(LocalDateTime.of(2026, 10, 23, 10, 18), p)
        assertTrue(s is Stanje.Ceka)
        s as Stanje.Ceka
        assertEquals(5, s.cas.broj)
        assertFalse(s.prviDanas)
        assertEquals(17L, s.zaMin)
    }

    @Test
    fun dodatCas() {
        val p = zi.copy(izmene = listOf(Izmena(d(2026, 10, 27), 3, SMENA_A, TipIzmene.DODAJ, "VII2", null)))
        val casovi = Motor.casoviZaDan(d(2026, 10, 27), p)
        assertEquals(listOf(3, 5, 6, 7), casovi.map { it.broj })
        assertTrue(casovi.first().izmenjen)
    }

    @Test
    fun obeSmene() {
        val post = zi.postavke!!.copy(b = Smena(listOf("VI2", "VIII1"), setOf("VI2")))
        val p = zi.copy(postavke = post, raspored = rasporedZi + Cas(1, 4, SMENA_B, "VI2", 1))
        // ponedeljak 26.10: A pre podne (V3 [1] u 09:45), B posle podne (VI2 [1] u 16:35)
        val casovi = Motor.casoviZaDan(d(2026, 10, 26), p)
        assertEquals(listOf("V3 [1]", "VII6", "V6 [1]", "V3 [2]", "VI2 [1]"), casovi.map { it.oznaka })
        assertEquals("16:35", casovi.last().termin.pocetak.toString())
        assertTrue(casovi.last().posle)
    }

    @Test
    fun promenaPostavki() {
        val post = zi.postavke!!
        val nove = post.copy(a = Smena(odeljenjaZi - "VII6", grupeZi - "V3"))
        val p = Motor.primeniPostavke(zi, nove)
        assertFalse(p.raspored.any { it.odeljenje == "VII6" })
        assertTrue(p.raspored.filter { it.odeljenje == "V3" }.all { it.grupa == null })
        assertEquals(rasporedZi.size - 1, p.raspored.size)
    }

    @Test
    fun promenaSmene() {
        assertEquals(d(2026, 11, 2) to true, Motor.promenaSmene(d(2026, 10, 23), zi.postavke!!))
    }
}
