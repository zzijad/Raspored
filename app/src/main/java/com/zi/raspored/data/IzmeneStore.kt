package com.zi.raspored.data

import android.content.Context
import com.zi.raspored.model.Izmena
import com.zi.raspored.model.TipIzmene
import org.json.JSONArray
import org.json.JSONObject
import java.time.LocalDate

/** Čuva jednokratne izmene u SharedPreferences kao JSON niz. */
class IzmeneStore(context: Context) {

    private val prefs = context.getSharedPreferences("raspored", Context.MODE_PRIVATE)

    fun ucitaj(): List<Izmena> {
        val tekst = prefs.getString(KLJUC, null) ?: return emptyList()
        return try {
            val niz = JSONArray(tekst)
            (0 until niz.length()).map { i ->
                val o = niz.getJSONObject(i)
                Izmena(
                    datum = LocalDate.parse(o.getString("datum")),
                    broj = o.getInt("cas"),
                    tip = TipIzmene.valueOf(o.getString("tip")),
                    odeljenje = o.optString("odeljenje").ifEmpty { null },
                    grupa = if (o.has("grupa")) o.getInt("grupa") else null,
                )
            }
        } catch (e: Exception) {
            emptyList()
        }
    }

    fun sacuvaj(izmene: List<Izmena>) {
        val niz = JSONArray()
        izmene.forEach { iz ->
            val o = JSONObject()
            o.put("datum", iz.datum.toString())
            o.put("cas", iz.broj)
            o.put("tip", iz.tip.name)
            iz.odeljenje?.let { o.put("odeljenje", it) }
            iz.grupa?.let { o.put("grupa", it) }
            niz.put(o)
        }
        prefs.edit().putString(KLJUC, niz.toString()).apply()
    }

    private companion object {
        const val KLJUC = "izmene"
    }
}
