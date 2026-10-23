package com.zi.raspored.data

import android.content.Context
import com.zi.raspored.model.Podaci
import com.zi.raspored.model.SkolskiPodaci

/** Čuva sve podatke u telefonu (SharedPreferences) kao jedan JSON. */
class Skladiste(context: Context) {

    private val prefs = context.getSharedPreferences("raspored", Context.MODE_PRIVATE)

    fun ucitaj(): Podaci {
        val tekst = prefs.getString(KLJUC, null)
        if (tekst != null) {
            try {
                return Kodek.izJsona(tekst)
            } catch (e: Exception) {
                // oštećen zapis: krećemo od praznog
            }
        }
        return prazno()
    }

    fun sacuvaj(p: Podaci) {
        prefs.edit().putString(KLJUC, Kodek.uJson(p)).apply()
    }

    companion object {
        private const val KLJUC = "podaci"

        fun prazno() = Podaci(
            postavke = null,
            raspored = emptyList(),
            izmene = emptyList(),
            satnica = SkolskiPodaci.podrazumevanaSatnica,
        )
    }
}
