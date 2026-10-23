package com.zi.raspored.data

import com.zi.raspored.model.Cas
import com.zi.raspored.model.Izmena
import com.zi.raspored.model.Podaci
import com.zi.raspored.model.Postavke
import com.zi.raspored.model.SMENA_A
import com.zi.raspored.model.SMENA_B
import com.zi.raspored.model.Satnica
import com.zi.raspored.model.SkolskiPodaci
import com.zi.raspored.model.Smena
import com.zi.raspored.model.Termin
import com.zi.raspored.model.TipIzmene
import org.json.JSONArray
import org.json.JSONObject
import java.time.LocalDate
import java.time.LocalTime

/**
 * Pretvara Podaci u JSON i nazad. Isti format se koristi za čuvanje u telefonu i za rezervnu kopiju.
 */
object Kodek {

    private const val VERZIJA = 1

    private fun smenaSlovo(s: Int) = if (s == SMENA_B) "B" else "A"
    private fun smenaIndeks(s: String) = if (s == "B") SMENA_B else SMENA_A

    fun uJson(p: Podaci): String {
        val o = JSONObject()
        o.put("aplikacija", "Raspored")
        o.put("verzija", VERZIJA)

        p.postavke?.let { post ->
            val po = JSONObject()
            po.put("a", smenaUJson(post.a))
            po.put("aPosleUSeptembru", post.aPosleUSeptembru)
            post.b?.let { po.put("b", smenaUJson(it)) }
            o.put("postavke", po)
        }

        val ras = JSONArray()
        p.raspored.forEach { c ->
            val co = JSONObject()
            co.put("dan", c.dan)
            co.put("cas", c.broj)
            co.put("smena", smenaSlovo(c.smena))
            co.put("odeljenje", c.odeljenje)
            c.grupa?.let { co.put("grupa", it) }
            ras.put(co)
        }
        o.put("raspored", ras)

        val iz = JSONArray()
        p.izmene.forEach { i ->
            val io = JSONObject()
            io.put("datum", i.datum.toString())
            io.put("cas", i.broj)
            io.put("smena", smenaSlovo(i.smena))
            io.put("tip", i.tip.name)
            i.odeljenje?.let { io.put("odeljenje", it) }
            i.grupa?.let { io.put("grupa", it) }
            iz.put(io)
        }
        o.put("izmene", iz)

        val sat = JSONObject()
        sat.put("pre", terminiUJson(p.satnica.pre))
        sat.put("posle", terminiUJson(p.satnica.posle))
        o.put("satnica", sat)

        return o.toString(2)
    }

    /** Baca izuzetak ako tekst nije ispravan JSON ove aplikacije. */
    fun izJsona(tekst: String): Podaci {
        val o = JSONObject(tekst)

        val postavke = o.optJSONObject("postavke")?.let { po ->
            Postavke(
                a = smenaIzJsona(po.getJSONObject("a")),
                aPosleUSeptembru = po.optBoolean("aPosleUSeptembru", false),
                b = po.optJSONObject("b")?.let { smenaIzJsona(it) },
            )
        }

        val raspored = mutableListOf<Cas>()
        o.optJSONArray("raspored")?.let { niz ->
            for (i in 0 until niz.length()) {
                val c = niz.getJSONObject(i)
                raspored += Cas(
                    dan = c.getInt("dan"),
                    broj = c.getInt("cas"),
                    smena = smenaIndeks(c.optString("smena", "A")),
                    odeljenje = c.getString("odeljenje"),
                    grupa = if (c.has("grupa")) c.getInt("grupa") else null,
                )
            }
        }

        val izmene = mutableListOf<Izmena>()
        o.optJSONArray("izmene")?.let { niz ->
            for (i in 0 until niz.length()) {
                val c = niz.getJSONObject(i)
                val od = c.optString("odeljenje", "")
                izmene += Izmena(
                    datum = LocalDate.parse(c.getString("datum")),
                    broj = c.getInt("cas"),
                    smena = smenaIndeks(c.optString("smena", "A")),
                    tip = TipIzmene.valueOf(c.getString("tip")),
                    odeljenje = if (od.isEmpty()) null else od,
                    grupa = if (c.has("grupa")) c.getInt("grupa") else null,
                )
            }
        }

        val satnica = o.optJSONObject("satnica")?.let { s ->
            val pre = terminiIzJsona(s.optJSONArray("pre"))
            val posle = terminiIzJsona(s.optJSONArray("posle"))
            if (pre.size == SkolskiPodaci.BROJ_CASOVA && posle.size == SkolskiPodaci.BROJ_CASOVA) Satnica(pre, posle) else null
        } ?: SkolskiPodaci.podrazumevanaSatnica

        return Podaci(postavke, raspored, izmene, satnica)
    }

    private fun smenaUJson(s: Smena): JSONObject {
        val o = JSONObject()
        o.put("odeljenja", JSONArray(s.odeljenja))
        o.put("grupe", JSONArray(SkolskiPodaci.sortiraj(s.grupe)))
        return o
    }

    private fun smenaIzJsona(o: JSONObject): Smena {
        val od = o.getJSONArray("odeljenja")
        val gr = o.optJSONArray("grupe") ?: JSONArray()
        val odeljenja = (0 until od.length()).map { od.getString(it) }
        val grupe = (0 until gr.length()).map { gr.getString(it) }.toSet()
        return Smena(SkolskiPodaci.sortiraj(odeljenja), grupe.intersect(odeljenja.toSet()))
    }

    private fun terminiUJson(lista: List<Termin>): JSONArray {
        val niz = JSONArray()
        lista.forEach { t ->
            val par = JSONArray()
            par.put(t.pocetak.toString())
            par.put(t.kraj.toString())
            niz.put(par)
        }
        return niz
    }

    private fun terminiIzJsona(niz: JSONArray?): List<Termin> {
        if (niz == null) return emptyList()
        return (0 until niz.length()).map {
            val par = niz.getJSONArray(it)
            Termin(LocalTime.parse(par.getString(0)), LocalTime.parse(par.getString(1)))
        }
    }
}
