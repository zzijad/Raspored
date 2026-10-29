package com.zi.raspored.ui

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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.zi.raspored.model.WidgetIzgled
import com.zi.raspored.model.WidgetOpcije

/** Podešavanje izgleda widgeta: veličina slova, pozadina i boja oznake, sa pregledom. */
@Composable
fun WidgetScreen(izgled: WidgetIzgled, onSacuvaj: (WidgetIzgled) -> Unit, onNazad: () -> Unit) {
    var velicina by remember { mutableStateOf(izgled.velicina) }
    var tamna by remember { mutableStateOf(izgled.tamna) }
    var akcenat by remember { mutableStateOf(izgled.akcenat) }
    val trenutni = WidgetIzgled(velicina, tamna, akcenat)

    Column(Modifier.fillMaxSize().background(Pozadina).systemBarsPadding().padding(horizontal = 16.dp, vertical = 12.dp)) {
        Column(
            Modifier.weight(1f).verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            KorakZaglavlje(null, 0, "Izgled widgeta", "Promene se vide na widgetu odmah posle čuvanja.")

            Pregled(trenutni)

            BelaKartica {
                Text("Veličina slova", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Siva)
                WidgetOpcije.velicine.chunked(2).forEachIndexed { red, par ->
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        par.forEachIndexed { k, naziv ->
                            val i = red * 2 + k
                            Cip(naziv, izabran = i == velicina, modifier = Modifier.weight(1f)) { velicina = i }
                        }
                    }
                }
            }

            BelaKartica {
                Text("Pozadina", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Siva)
                Segment(listOf("Svetla", "Tamna"), if (tamna) 1 else 0) { tamna = it == 1 }
            }

            BelaKartica {
                Text("Boja oznake (danas i trenutni čas)", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Siva)
                Row(Modifier.fillMaxWidth().padding(vertical = 4.dp), horizontalArrangement = Arrangement.SpaceBetween) {
                    WidgetOpcije.akcenti.forEachIndexed { i, boja ->
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Box(
                                Modifier.size(48.dp).clip(CircleShape).background(Color(boja))
                                    .border(if (i == akcenat) 3.dp else 0.dp, if (i == akcenat) Tekst else Color.Transparent, CircleShape)
                                    .clickable { akcenat = i },
                                contentAlignment = Alignment.Center,
                            ) {
                                if (i == akcenat) Icon(Icons.Filled.Check, contentDescription = null, tint = Color.White)
                            }
                            Spacer(Modifier.height(4.dp))
                            Text(WidgetOpcije.akcentiNazivi[i], fontSize = 11.sp, color = Siva)
                        }
                    }
                }
            }
        }

        Spacer(Modifier.height(12.dp))
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            DugmeSporedno("Odustani", Modifier.weight(1f), onClick = onNazad)
            DugmeGlavno("Sačuvaj", Modifier.weight(1f)) { onSacuvaj(trenutni) }
        }
    }
}

/** Umanjeni pregled widgeta sa dva primer dana. */
@Composable
private fun Pregled(i: WidgetIzgled) {
    val f = WidgetOpcije.faktor(i)
    val akcenat = Color(WidgetOpcije.akcenat(i))
    val pozadina = if (i.tamna) Color(0xFF111827) else Color.White
    val tekst = if (i.tamna) Color(0xFFF9FAFB) else Tekst
    val prazno = if (i.tamna) Color(0xFF1F2937) else Pozadina
    val redovi = listOf(
        Triple("Sre", false, listOf("", "", "V4", "", "VII1 [1]", "VII3 [1]", "VII4 [1]")),
        Triple("Čet", true, listOf("", "", "", "V6 [2]", "VII1 [2]", "VII2", "V1 [1]")),
    )

    Column(
        Modifier.fillMaxWidth().clip(RoundedCornerShape(20.dp)).background(Color(0xFF374151)).padding(14.dp),
    ) {
        Column(
            Modifier.fillMaxWidth().clip(RoundedCornerShape(18.dp)).background(pozadina).padding(10.dp),
            verticalArrangement = Arrangement.spacedBy(3.dp),
        ) {
            Row(Modifier.fillMaxWidth().padding(bottom = 4.dp), verticalAlignment = Alignment.CenterVertically) {
                Text("Nedelja 26.–30.10.", Modifier.weight(1f), fontSize = (13 * f).sp, fontWeight = FontWeight.Bold, color = tekst)
                Text(
                    "I smena",
                    Modifier.clip(RoundedCornerShape(10.dp)).background(akcenat.copy(alpha = if (i.tamna) 0.35f else 0.15f))
                        .padding(horizontal = 8.dp, vertical = 3.dp),
                    fontSize = (11 * f).sp,
                    fontWeight = FontWeight.Bold,
                    color = if (i.tamna) Color.White else akcenat,
                )
            }
            redovi.forEach { (dan, danas, celije) ->
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(2.dp), verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        dan,
                        Modifier.width((30 * f).dp),
                        fontSize = (11 * f).sp,
                        fontWeight = FontWeight.Bold,
                        color = if (danas) akcenat else tekst,
                    )
                    celije.forEachIndexed { k, v ->
                        val sada = danas && k == 3
                        val (rp, rb) = if (v.isEmpty()) prazno to tekst else bojaRazreda(v)
                        Column(
                            Modifier.weight(1f).height((46 * f).dp).clip(RoundedCornerShape(6.dp))
                                .background(if (sada) akcenat else rp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center,
                        ) {
                            if (v.isNotEmpty()) {
                                val delovi = v.split(" ")
                                Text(
                                    delovi[0] + "\n" + (delovi.getOrNull(1) ?: ""),
                                    fontSize = (10 * f).sp,
                                    lineHeight = (12 * f).sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (sada) Color.White else rb,
                                    textAlign = TextAlign.Center,
                                    maxLines = 2,
                                    softWrap = false,
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
