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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.zi.raspored.logic.Motor
import com.zi.raspored.model.CasUDanu
import com.zi.raspored.model.Podaci
import com.zi.raspored.model.SMENA_A
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.temporal.TemporalAdjusters

@Composable
fun NedeljaScreen(
    dan: LocalDate,
    onIzaberi: (LocalDate) -> Unit,
    sada: LocalDateTime,
    podaci: Podaci,
    onIzmena: (LocalDate, Int, Int) -> Unit,
) {
    val post = podaci.postavke ?: return
    var tab by rememberSaveable { mutableStateOf(SMENA_A) }
    val smena = if (post.smena(tab) != null) tab else SMENA_A
    val ponedeljak = dan.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY))
    val posle = Motor.posle(ponedeljak, smena, post)
    val termini = Motor.termini(ponedeljak, smena, podaci)
    val ukupno = podaci.raspored.size

    Column(
        Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        Zaglavlje("${brojCasova(ukupno)} nedeljno", "Nedelja") {
            DugmeIkona(Icons.AutoMirrored.Filled.KeyboardArrowLeft, "Prethodna nedelja") { onIzaberi(dan.minusWeeks(1)) }
            Spacer(Modifier.width(6.dp))
            DugmeIkona(Icons.AutoMirrored.Filled.KeyboardArrowRight, "Sledeća nedelja") { onIzaberi(dan.plusWeeks(1)) }
        }

        if (post.b != null) {
            Box(Modifier.padding(top = 6.dp)) {
                Segment(listOf("Smena A", "Smena B"), smena) { tab = it }
            }
        }

        Row(
            Modifier.fillMaxWidth().padding(vertical = 8.dp).clip(RoundedCornerShape(16.dp)).background(Color.White)
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                "${ponedeljak.dayOfMonth}.–${ponedeljak.plusDays(4).kratko()}",
                Modifier.weight(1f),
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                color = Tekst,
            )
            Znacka("${dobaTekst(posle)} · ${termini.first().pocetak.hm()}–${termini.last().kraj.hm()}")
        }

        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(4.dp)) {
            Spacer(Modifier.width(34.dp))
            termini.forEachIndexed { i, t ->
                Column(Modifier.weight(1f).padding(vertical = 4.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("${i + 1}.", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Tekst)
                    Text(t.pocetak.hm(), fontSize = 10.sp, color = Siva, maxLines = 1)
                }
            }
        }

        for (i in 0..4) {
            val d = ponedeljak.plusDays(i.toLong())
            val nastavni = Motor.jeNastavniDan(d)
            val casovi = Motor.casoviSmene(d, smena, podaci)
            val jeDanas = d == sada.toLocalDate()
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                Column(Modifier.width(34.dp).height(72.dp), verticalArrangement = Arrangement.Center) {
                    Text(
                        daniKratko[i],
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (jeDanas) Plava else if (nastavni) Tekst else SivaSvetla,
                    )
                    Text("${d.dayOfMonth}.", fontSize = 11.sp, color = Siva)
                }
                if (!nastavni) {
                    Box(
                        Modifier.weight(1f).height(72.dp).clip(RoundedCornerShape(10.dp)).background(Color(0xFFE9ECEF)),
                        contentAlignment = Alignment.Center,
                    ) {
                        Text(Motor.razlogNeradnog(d) ?: "", fontSize = 13.sp, color = Siva, fontWeight = FontWeight.SemiBold)
                    }
                } else {
                    val dtermini = Motor.termini(d, smena, podaci)
                    for (b in 1..dtermini.size) {
                        val c = casovi.firstOrNull { it.broj == b }
                        val trenutni = jeDanas && run {
                            val v = sada.toLocalTime()
                            !v.isBefore(dtermini[b - 1].pocetak) && v.isBefore(dtermini[b - 1].kraj)
                        }
                        Celija(c, trenutni, Modifier.weight(1f)) { onIzmena(d, b, smena) }
                    }
                }
            }
        }

        Row(
            Modifier.fillMaxWidth().padding(start = 4.dp, end = 4.dp, top = 12.dp),
            horizontalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            Legenda(NarandzastaPozadina, "izmena")
            Legenda(Plava, "sada")
        }
        Text(
            "Dodirni polje da izmeniš čas tog dana.",
            Modifier.padding(horizontal = 4.dp, vertical = 8.dp),
            fontSize = 13.sp,
            color = Siva,
        )
    }
}

@Composable
private fun Celija(cas: CasUDanu?, trenutni: Boolean, modifier: Modifier, onClick: () -> Unit) {
    val oblik = RoundedCornerShape(10.dp)
    if (cas == null) {
        Box(modifier.height(72.dp).clip(oblik).border(1.dp, Granica, oblik).clickable(onClick = onClick))
        return
    }
    val (razredPozadina, razredBoja) = bojaRazreda(cas.odeljenje)
    val pozadina = when {
        trenutni && !cas.otkazan -> Plava
        cas.izmenjen -> NarandzastaPozadina
        else -> razredPozadina
    }
    val boja = when {
        trenutni && !cas.otkazan -> Color.White
        cas.izmenjen -> NarandzastaTekst
        else -> razredBoja
    }
    Column(
        modifier.height(72.dp).clip(oblik).background(pozadina)
            .border(if (cas.izmenjen) 1.dp else 0.dp, if (cas.izmenjen) NarandzastaIvica else Color.Transparent, oblik)
            .clickable(onClick = onClick),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Text(
            cas.odeljenje,
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            color = boja,
            maxLines = 1,
            softWrap = false,
            textDecoration = if (cas.otkazan) TextDecoration.LineThrough else TextDecoration.None,
        )
        Text(
            if (cas.grupa == null) "cela" else "[${cas.grupa}]",
            fontSize = 10.sp,
            fontWeight = FontWeight.SemiBold,
            color = boja,
            maxLines = 1,
        )
    }
}

@Composable
private fun Legenda(boja: Color, tekst: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(Modifier.size(12.dp).clip(RoundedCornerShape(3.dp)).background(boja))
        Spacer(Modifier.width(5.dp))
        Text(tekst, fontSize = 12.sp, color = Siva)
    }
}
