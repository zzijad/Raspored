package com.zi.raspored.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.zi.raspored.logic.Motor
import com.zi.raspored.logic.Stanje
import com.zi.raspored.model.CasUDanu
import com.zi.raspored.model.Podaci
import com.zi.raspored.model.SMENA_A
import java.time.LocalDateTime

@Composable
fun SadaScreen(sada: LocalDateTime, podaci: Podaci) {
    val post = podaci.postavke ?: return
    val danas = sada.toLocalDate()
    val stanje = Motor.stanje(sada, podaci)
    val casovi = Motor.casoviZaDan(danas, podaci)
    val obeSmene = post.b != null

    Column(
        Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Zaglavlje(danas.lepo(), "Sada") {
            if (Motor.jeNastavniDan(danas)) {
                Znacka(if (obeSmene) "Obe smene" else dobaTekst(Motor.posle(danas, SMENA_A, post)))
            }
        }

        if (podaci.raspored.isEmpty()) {
            BelaKartica {
                Text("Raspored je prazan", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = Tekst)
                Text("Unesi časove u Podešavanja → Osnovni raspored.", fontSize = 15.sp, color = Siva)
            }
        }

        when (stanje) {
            is Stanje.NaCasu -> {
                PlavaKartica(
                    gore = "NA ČASU · ${stanje.cas.broj}. ČAS",
                    goreDesno = stanje.cas.termin.tekst(),
                    velik = stanje.cas.oznaka,
                ) {
                    Traka(stanje.napredak, Color.White, Color.White.copy(alpha = 0.28f))
                    Row(Modifier.fillMaxWidth()) {
                        Text(
                            "Još ${trajanje(stanje.ostaloMin)}",
                            Modifier.weight(1f),
                            color = Color.White,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.SemiBold,
                        )
                        Text("zvono u ${stanje.cas.termin.kraj.hm()}", color = Color.White.copy(alpha = 0.9f), fontSize = 15.sp)
                    }
                }
                stanje.sledeci?.let { SledeciRed("SLEDEĆE · ${it.broj}. ČAS", it.oznaka, it.termin.pocetak.hm()) }
            }

            is Stanje.Ceka -> {
                val gore = if (stanje.prviDanas) "PRVI ČAS DANAS" else "ODMOR · SLEDEĆI ČAS"
                PlavaKartica(
                    gore = "$gore · ${stanje.cas.broj}.",
                    goreDesno = stanje.cas.termin.tekst(),
                    velik = stanje.cas.oznaka,
                ) {
                    Text(
                        "Počinje za ${trajanje(stanje.zaMin)}",
                        color = Color.White,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.SemiBold,
                    )
                }
            }

            is Stanje.Slobodno -> {
                if (podaci.raspored.isNotEmpty()) {
                    BelaKartica {
                        Text(stanje.razlog, fontSize = 20.sp, fontWeight = FontWeight.Bold, color = Tekst)
                        val d = stanje.datum
                        val c = stanje.cas
                        if (d != null && c != null) {
                            Text(
                                "Sledeći čas: ${d.lepo()}, ${c.broj}. čas, ${c.oznaka} u ${c.termin.pocetak.hm()}",
                                fontSize = 15.sp,
                                color = Siva,
                            )
                        } else {
                            Text("Nema više nastave ove školske godine.", fontSize = 15.sp, color = Siva)
                        }
                    }
                }
            }
        }

        if (casovi.isNotEmpty()) DanasLista(casovi, sada, obeSmene)

        Row(Modifier.fillMaxWidth().padding(horizontal = 6.dp, vertical = 4.dp)) {
            Text(
                "Još ${brojNastavnihDana(Motor.preostaloNastavnihDana(danas))}",
                Modifier.weight(1f),
                fontSize = 13.sp,
                color = Siva,
            )
            if (!obeSmene) {
                Motor.promenaSmene(danas, post)?.let { (d, posle) ->
                    Text("${dobaTekst(posle)} od ${d.kratko()}", fontSize = 13.sp, color = Siva)
                }
            }
        }
        Spacer(Modifier.height(8.dp))
    }
}

@Composable
private fun PlavaKartica(
    gore: String,
    goreDesno: String,
    velik: String,
    sadrzaj: @Composable ColumnScope.() -> Unit,
) {
    Column(
        Modifier.fillMaxWidth().clip(RoundedCornerShape(24.dp)).background(Plava).padding(22.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        Row(Modifier.fillMaxWidth()) {
            Text(gore, Modifier.weight(1f), color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
            Text(goreDesno, color = Color.White, fontSize = 13.sp)
        }
        Text(velik, color = Color.White, fontSize = 56.sp, lineHeight = 58.sp, fontWeight = FontWeight.ExtraBold)
        sadrzaj()
    }
}

@Composable
private fun SledeciRed(nadnaslov: String, naziv: String, vreme: String) {
    Row(
        Modifier.fillMaxWidth().clip(RoundedCornerShape(20.dp)).background(Color.White).padding(horizontal = 18.dp, vertical = 16.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            Modifier.size(44.dp).clip(RoundedCornerShape(14.dp)).background(Color(0xFFEEF1F5)),
            contentAlignment = Alignment.Center,
        ) {
            Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = null, tint = Tekst)
        }
        Spacer(Modifier.width(14.dp))
        Column(Modifier.weight(1f)) {
            Text(nadnaslov, fontSize = 12.sp, color = Siva, fontWeight = FontWeight.SemiBold)
            Text(naziv, fontSize = 20.sp, fontWeight = FontWeight.Bold, color = Tekst)
        }
        Text(vreme, fontSize = 15.sp, fontWeight = FontWeight.Bold, color = Tekst)
    }
}

@Composable
private fun DanasLista(casovi: List<CasUDanu>, sada: LocalDateTime, obeSmene: Boolean) {
    val vreme = sada.toLocalTime()
    val aktivni = casovi.filter { !it.otkazan }
    BelaKartica {
        Row(Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
            Text("Danas", Modifier.weight(1f), fontSize = 14.sp, color = Siva, fontWeight = FontWeight.SemiBold)
            aktivni.lastOrNull()?.let {
                Text("${brojCasova(aktivni.size)} · do ${it.termin.kraj.hm()}", fontSize = 14.sp, color = Siva)
            }
        }
        HorizontalDivider(color = Linija)
        casovi.forEach { c ->
            val prosao = !vreme.isBefore(c.termin.kraj)
            val traje = !vreme.isBefore(c.termin.pocetak) && vreme.isBefore(c.termin.kraj) && !c.otkazan
            val boja = when {
                traje -> Plava
                c.otkazan || prosao -> SivaSvetla
                c.izmenjen -> NarandzastaTekst
                else -> Tekst
            }
            val dekoracija = if (prosao || c.otkazan) TextDecoration.LineThrough else TextDecoration.None
            val opis = when {
                c.otkazan -> "${c.oznaka} · otkazan"
                traje -> "${c.oznaka} · sada"
                c.izmenjen -> "${c.oznaka} · izmena"
                else -> c.oznaka
            }
            Row(Modifier.fillMaxWidth().padding(vertical = 6.dp)) {
                Text(
                    if (obeSmene) "${c.broj}${smenaSlovo(c.smena)}" else "${c.broj}.",
                    Modifier.width(32.dp),
                    fontSize = 14.sp,
                    color = boja,
                    fontWeight = FontWeight.SemiBold,
                )
                Text(
                    opis,
                    Modifier.weight(1f),
                    fontSize = 15.sp,
                    color = boja,
                    fontWeight = if (traje) FontWeight.Bold else FontWeight.Medium,
                    textDecoration = dekoracija,
                )
                Text(c.termin.pocetak.hm(), fontSize = 14.sp, color = boja)
            }
        }
    }
}
