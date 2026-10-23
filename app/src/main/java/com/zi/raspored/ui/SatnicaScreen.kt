package com.zi.raspored.ui

import android.app.TimePickerDialog
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.zi.raspored.model.Satnica
import com.zi.raspored.model.SkolskiPodaci
import com.zi.raspored.model.Termin
import java.time.LocalTime

/** Izmena satnice zvona. Dodir na vreme otvara izbor sata. */
@Composable
fun SatnicaScreen(satnica: Satnica, onSacuvaj: (Satnica) -> Unit, onNazad: () -> Unit) {
    var pre by remember { mutableStateOf(satnica.pre) }
    var posle by remember { mutableStateOf(satnica.posle) }
    val context = LocalContext.current

    fun izaberi(vreme: LocalTime, postavi: (LocalTime) -> Unit) {
        TimePickerDialog(context, { _, sat, minut -> postavi(LocalTime.of(sat, minut)) }, vreme.hour, vreme.minute, true).show()
    }

    fun promeni(lista: List<Termin>, i: Int, pocetak: LocalTime? = null, kraj: LocalTime? = null): List<Termin> =
        lista.mapIndexed { j, t -> if (j == i) Termin(pocetak ?: t.pocetak, kraj ?: t.kraj) else t }

    Column(Modifier.fillMaxSize().background(Pozadina).systemBarsPadding().padding(horizontal = 16.dp, vertical = 12.dp)) {
        Column(
            Modifier.weight(1f).verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            KorakZaglavlje(null, 0, "Satnica zvona", "Dodirni vreme da ga promeniš. Važi za sve časove u toj smeni.")

            listOf("I smena · pre podne" to true, "II smena · posle podne" to false).forEach { (naslov, jePre) ->
                val lista = if (jePre) pre else posle
                BelaKartica {
                    Text(naslov, fontSize = 16.sp, fontWeight = FontWeight.ExtraBold, color = Tekst)
                    lista.forEachIndexed { i, t ->
                        Row(Modifier.fillMaxWidth().padding(vertical = 2.dp), verticalAlignment = Alignment.CenterVertically) {
                            Text("${i + 1}. čas", Modifier.weight(1f), fontSize = 15.sp, color = Siva, fontWeight = FontWeight.SemiBold)
                            VremeDugme(t.pocetak) {
                                izaberi(t.pocetak) { v ->
                                    if (jePre) pre = promeni(pre, i, pocetak = v) else posle = promeni(posle, i, pocetak = v)
                                }
                            }
                            Text("–", Modifier.padding(horizontal = 8.dp), color = Siva)
                            VremeDugme(t.kraj) {
                                izaberi(t.kraj) { v ->
                                    if (jePre) pre = promeni(pre, i, kraj = v) else posle = promeni(posle, i, kraj = v)
                                }
                            }
                        }
                    }
                }
            }

            TextButton(onClick = {
                pre = SkolskiPodaci.podrazumevanaSatnica.pre
                posle = SkolskiPodaci.podrazumevanaSatnica.posle
            }) {
                Text("Vrati podrazumevanu satnicu", color = Plava, fontWeight = FontWeight.Bold)
            }
        }

        Spacer(Modifier.height(12.dp))
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            DugmeSporedno("Odustani", Modifier.weight(1f), onClick = onNazad)
            DugmeGlavno("Sačuvaj", Modifier.weight(1f)) { onSacuvaj(Satnica(pre, posle)) }
        }
    }
}

@Composable
private fun VremeDugme(vreme: LocalTime, onClick: () -> Unit) {
    val oblik = RoundedCornerShape(10.dp)
    Box(
        Modifier.width(76.dp).height(44.dp).clip(oblik).background(Color.White).border(1.dp, Granica, oblik).clickable(onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        Text(vreme.hm(), fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Tekst)
    }
}
