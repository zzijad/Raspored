package com.zi.raspored.data

import com.zi.raspored.model.Kalendar
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.net.HttpURLConnection
import java.net.URL
import java.time.LocalDate

/** Preuzima stranicu sa kalendarom i čita je. */
object KalendarPreuzimanje {

    suspend fun preuzmi(adresa: String): Kalendar = withContext(Dispatchers.IO) {
        val url = runCatching { URL(adresa.trim()) }.getOrNull()
            ?: throw KalendarGreska("Adresa nije ispravna.")
        val html = try {
            val veza = url.openConnection() as HttpURLConnection
            veza.connectTimeout = 15_000
            veza.readTimeout = 20_000
            veza.instanceFollowRedirects = true
            veza.setRequestProperty("User-Agent", "Mozilla/5.0 (Linux; Android) Raspored")
            veza.setRequestProperty("Accept", "text/html")
            try {
                if (veza.responseCode !in 200..299) {
                    throw KalendarGreska("Sajt je vratio grešku ${veza.responseCode}.")
                }
                veza.inputStream.use { it.readBytes().decodeToString() }
            } finally {
                veza.disconnect()
            }
        } catch (e: KalendarGreska) {
            throw e
        } catch (e: Exception) {
            throw KalendarGreska("Sajt nije dostupan. Proveri internet i pokušaj ponovo.")
        }
        KalendarCitac.procitaj(html, adresa.trim(), LocalDate.now())
    }
}
