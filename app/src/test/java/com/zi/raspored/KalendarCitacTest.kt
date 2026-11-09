package com.zi.raspored

import com.zi.raspored.data.KalendarCitac
import com.zi.raspored.data.KalendarGreska
import com.zi.raspored.model.Kalendari
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalDate

class KalendarCitacTest {

    private fun stranica(): String =
        javaClass.classLoader!!.getResourceAsStream("kalendar-2026-2027.html")!!.readBytes().decodeToString()

    @Test
    fun citaStranicuSkole() {
        val k = KalendarCitac.procitaj(stranica(), "test", null)
        assertEquals("2026/2027", k.godina)
        assertEquals(180, k.nastavni.size)
        assertEquals(LocalDate.of(2026, 9, 1), k.prvi)
        assertEquals(LocalDate.of(2027, 6, 11), k.poslednji)
        assertEquals(LocalDate.of(2027, 5, 28), k.krajOsmi)
        // isti nastavni dani kao u ugrađenom kalendaru
        assertEquals(Kalendari.ugradjen.nastavni, k.nastavni)
        assertEquals("Državni praznik", k.opisi[LocalDate.of(2026, 11, 11)])
        assertEquals("Raspust", k.opisi[LocalDate.of(2026, 11, 12)])
        assertEquals("Sveti Sava", k.opisi[LocalDate.of(2027, 1, 27)])
        assertTrue(LocalDate.of(2026, 10, 21) in k.nastavni) // obeležava se radno
        assertTrue(LocalDate.of(2026, 12, 30) in k.nastavni) // kraj polugodišta je nastavni dan
    }

    @Test
    fun odbijaPogresanBroj() {
        val pokvarena = stranica().replaceFirst(">22</div>", ">23</div>")
        var greska: String? = null
        try {
            KalendarCitac.procitaj(pokvarena, "test", null)
        } catch (e: KalendarGreska) {
            greska = e.message
        }
        assertTrue(greska != null && greska.contains("9. mesec"))
    }

    @Test
    fun odbijaStranicuBezKalendara() {
        var odbijena = false
        try {
            KalendarCitac.procitaj("<html><body>Nema</body></html>", "test", null)
        } catch (e: KalendarGreska) {
            odbijena = true
        }
        assertTrue(odbijena)
    }
}
