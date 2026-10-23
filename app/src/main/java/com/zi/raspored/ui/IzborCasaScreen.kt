package com.zi.raspored.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBarsPadding
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.zi.raspored.model.Cas
import com.zi.raspored.model.Smena

/**
 * Izbor odeljenja i grupe za jedno polje osnovnog rasporeda.
 * nedavno = poslednje unete kombinacije (odeljenje, grupa).
 */
@Composable
fun IzborCasaScreen(
    dan: Int,
    broj: Int,
    oznakaSmene: String?,
    smena: Smena,
    postojeci: Cas?,
    nedavno: List<Pair<String, Int?>>,
    onSacuvaj: (odeljenje: String, grupa: Int?) -> Unit,
    onIsprazni: () -> Unit,
    onNazad: () -> Unit,
) {
    var odeljenje by remember { mutableStateOf(postojeci?.odeljenje) }
    var grupa by remember { mutableStateOf(postojeci?.grupa) }
    val nedavnoOvde = nedavno.filter { it.first in smena.odeljenja }.distinct().take(3)
    val izabrano = odeljenje
    val imaGrupe = izabrano != null && smena.imaGrupe(izabrano)

    Column(Modifier.fillMaxSize().background(Color.White).systemBarsPadding().padding(horizontal = 20.dp, vertical = 20.dp)) {
        Column(
            Modifier.weight(1f).verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(18.dp),
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(
                    "${daniPuno[dan - 1]} · $broj. čas" + (oznakaSmene?.let { " · $it" } ?: ""),
                    fontSize = 13.sp,
                    color = Siva,
                    fontWeight = FontWeight.SemiBold,
                )
                Text("Koje odeljenje?", fontSize = 22.sp, fontWeight = FontWeight.ExtraBold, color = Tekst)
            }

            if (nedavnoOvde.isNotEmpty()) {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Nedavno", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Siva)
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        nedavnoOvde.forEach { (o, g) ->
                            Cip(
                                if (g == null) o else "$o [$g]",
                                izabran = odeljenje == o && grupa == g,
                                modifier = Modifier.weight(1f),
                                visina = 48.dp,
                            ) {
                                odeljenje = o
                                grupa = if (smena.imaGrupe(o)) g else null
                            }
                        }
                        repeat(3 - nedavnoOvde.size) { Spacer(Modifier.weight(1f)) }
                    }
                }
            }

            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("Moja odeljenja", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Siva)
                smena.odeljenja.chunked(6).forEach { red ->
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        red.forEach { o ->
                            Cip(o, izabran = o == odeljenje, modifier = Modifier.weight(1f), visina = 48.dp) {
                                if (odeljenje != o) {
                                    odeljenje = o
                                    grupa = if (smena.imaGrupe(o)) (grupa ?: 1) else null
                                }
                            }
                        }
                        repeat(6 - red.size) { Spacer(Modifier.weight(1f)) }
                    }
                }
            }

            if (izabrano != null) {
                if (imaGrupe) {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text("Grupa u $izabrano", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Siva)
                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            listOf<Int?>(null, 1, 2).forEach { g ->
                                Cip(
                                    if (g == null) "Celo" else "Grupa [$g]",
                                    izabran = g == grupa,
                                    modifier = Modifier.weight(1f),
                                    visina = 48.dp,
                                ) { grupa = g }
                            }
                        }
                    }
                } else {
                    Text(
                        "$izabrano nema grupe, čas je za celo odeljenje.",
                        Modifier.fillMaxWidth().clip(RoundedCornerShape(12.dp)).background(Pozadina)
                            .padding(horizontal = 14.dp, vertical = 12.dp),
                        fontSize = 14.sp,
                        color = Siva,
                    )
                }
            }
        }

        Spacer(Modifier.height(12.dp))
        Row(Modifier.fillMaxWidth().padding(horizontal = 4.dp), verticalAlignment = Alignment.CenterVertically) {
            val rezultat = when {
                izabrano == null -> "ništa"
                imaGrupe && grupa != null -> "$izabrano [$grupa]"
                else -> izabrano
            }
            Text("Upisuje se: ", fontSize = 15.sp, color = Siva)
            Text(rezultat, Modifier.weight(1f), fontSize = 15.sp, color = Tekst, fontWeight = FontWeight.Bold)
            if (postojeci != null) {
                TextButton(onClick = onIsprazni) {
                    Text("Isprazni polje", color = NarandzastaTekst, fontWeight = FontWeight.Bold)
                }
            }
        }
        Spacer(Modifier.height(8.dp))
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            DugmeSporedno("Odustani", Modifier.weight(1f), onClick = onNazad)
            DugmeGlavno("Sačuvaj", Modifier.weight(1f), omoguceno = izabrano != null) {
                val o = izabrano
                if (o != null) onSacuvaj(o, if (imaGrupe) grupa else null)
            }
        }
    }
}
