package com.zi.raspored.data

import android.content.Context
import com.zi.raspored.model.Kalendari
import com.zi.raspored.model.Podaci
import com.zi.raspored.model.SkolskiPodaci

/** Čuva sve podatke u telefonu (SharedPreferences) kao jedan JSON. */
class Skladiste(context: Context) {

    private val prefs = context.getSharedPreferences("raspored", Context.MODE_PRIVATE)

    fun ucitaj(): Podaci {
        val tekst = prefs.getString(KLJUC, null)
        val p = tekst?.let { runCatching { Kodek.izJsona(it) }.getOrNull() } ?: prazno()
        primeniKalendar(p)
        return p
    }

    fun sacuvaj(p: Podaci) {
        prefs.edit().putString(KLJUC, Kodek.uJson(p)).apply()
    }

    companion object {
        private const val KLJUC = "podaci"

        /** Postavlja kalendar koji koristi cela aplikacija (i widget). */
        fun primeniKalendar(p: Podaci) {
            Kalendari.aktivan = p.kalendar ?: Kalendari.ugradjen
        }

        fun prazno() = Podaci(
            postavke = null,
            raspored = emptyList(),
            izmene = emptyList(),
            satnica = SkolskiPodaci.podrazumevanaSatnica,
        )
    }
}
