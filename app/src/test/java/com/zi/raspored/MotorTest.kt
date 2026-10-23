package com.zi.raspored

import com.zi.raspored.logic.Motor
import com.zi.raspored.logic.Stanje
import com.zi.raspored.model.Izmena
import com.zi.raspored.model.TipIzmene
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalDate
import java.time.LocalDateTime

class MotorTest {

    private fun d(g: Int, m: Int, dan: Int) = LocalDate.of(g, m, dan)

    @Test
    fun smene() {
        assertEquals(2, Motor.smena(d(2026, 9, 1)))   // septembar = II
        assertEquals(2, Motor.smena(d(2026, 10, 2)))  // petak, ponedeljak je 28.9. -> još septembar
        assertEquals(1, Motor.smena(d(2026, 10, 5)))  // prvi ponedeljak oktobra -> I
        assertEquals(1, Motor.smena(d(2026, 10, 23))) // danas
        assertEquals(2, Motor.smena(d(2026, 11, 2)))
        assertEquals(1, Motor.smena(d(2026, 12, 7)))
        assertEquals(2, Motor.smena(d(2027, 1, 18)))  // posle zimskog raspusta
        assertEquals(1, Motor.smena(d(2027, 2, 1)))
        assertEquals(1, Motor.smena(d(2027, 6, 7)))
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
        val s = Motor.stanje(LocalDateTime.of(2026, 10, 23, 10, 18), emptyList())
        assertTrue(s is Stanje.NaCasu)
        s as Stanje.NaCasu
        assertEquals(4, s.cas.broj)
        assertEquals("V5", s.cas.oznaka)
        assertEquals(12L, s.ostaloMin)
        assertEquals("VII5 [1]", s.sledeci?.oznaka)
    }

    @Test
    fun prePrvogCasa() {
        val s = Motor.stanje(LocalDateTime.of(2026, 10, 23, 8, 40), emptyList())
        assertTrue(s is Stanje.Ceka)
        s as Stanje.Ceka
        assertEquals(3, s.cas.broj)
        assertTrue(s.prviDanas)
        assertEquals(15L, s.zaMin)
    }

    @Test
    fun vikend() {
        val s = Motor.stanje(LocalDateTime.of(2026, 10, 24, 12, 0), emptyList())
        assertTrue(s is Stanje.Slobodno)
        s as Stanje.Slobodno
        assertEquals("Vikend", s.razlog)
        assertEquals(d(2026, 10, 26), s.datum)
        assertEquals("V3 [1]", s.cas?.oznaka)
    }

    @Test
    fun otkazanCas() {
        val izmene = listOf(Izmena(d(2026, 10, 23), 4, TipIzmene.OTKAZI))
        val s = Motor.stanje(LocalDateTime.of(2026, 10, 23, 10, 18), izmene)
        assertTrue(s is Stanje.Ceka)
        s as Stanje.Ceka
        assertEquals(5, s.cas.broj)
        assertFalse(s.prviDanas)
        assertEquals(17L, s.zaMin)
    }

    @Test
    fun dodatCas() {
        val izmene = listOf(Izmena(d(2026, 10, 27), 3, TipIzmene.DODAJ, "VII2", null))
        val casovi = Motor.casoviZaDan(d(2026, 10, 27), izmene)
        assertEquals(listOf(3, 5, 6, 7), casovi.map { it.broj })
        assertTrue(casovi.first().izmenjen)
    }

    @Test
    fun promenaSmene() {
        assertEquals(d(2026, 11, 2) to 2, Motor.promenaSmene(d(2026, 10, 23)))
    }
}
