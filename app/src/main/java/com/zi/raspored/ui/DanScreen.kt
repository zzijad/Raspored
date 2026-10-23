package com.zi.raspored.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.zi.raspored.logic.Motor
import com.zi.raspored.model.CasUDanu
import com.zi.raspored.model.Podaci
import com.zi.raspored.model.SMENA_A
import com.zi.raspored.model.Termin
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.temporal.TemporalAdjusters

@Composable
fun DanScreen(
    dan: LocalDate,
    onIzaberi: (LocalDate) -> Unit,
    sada: LocalDateTime,
    podaci: Podaci,
    onIzmena: (LocalDate, Int, Int) -> Unit,
) {
    val post = podaci.postavke ?: return
    val ponedeljak = dan.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY))
    val petak = ponedeljak.plusDays(4)
    val nadnaslov = if (post.b == null) {
        "${ponedeljak.dayOfMonth}.–${petak.kratko()} · ${dobaTekst(Motor.posle(dan, SMENA_A, post))}"
    } else {
        "${ponedeljak.dayOfMonth}.–${petak.kratko()}"
    }

    Column(
        Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Zaglavlje(nadnaslov, "Dan") {
            DugmeIkona(Icons.AutoMirrored.Filled.KeyboardArrowLeft, "Prethodna nedelja") { onIzaberi(dan.minusWeeks(1)) }
            Spacer(Modifier.width(6.dp))
            DugmeIkona(Icons.AutoMirrored.Filled.KeyboardArrowRight, "Sledeća nedelja") { onIzaberi(dan.plusWeeks(1)) }
        }

        Row(Modifier.fillMaxWidth().padding(bottom = 6.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            for (i in 0..4) {
                val d = ponedeljak.plusDays(i.toLong())
                DanDugme(d, izabran = d == dan, danas = d == sada.toLocalDate(), Modifier.weight(1f)) { onIzaberi(d) }
            }
        }

        if (!Motor.jeNastavniDan(dan)) {
            BelaKartica {
                Text(Motor.razlogNeradnog(dan) ?: "", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = Tekst)
                Text("Nema nastave ovog dana.", fontSize = 15.sp, color = Siva)
            }
        } else {
            post.aktivne.forEach { smena ->
                val casovi = Motor.casoviSmene(dan, smena, podaci)
                val termini = Motor.termini(dan, smena, podaci)
                if (post.b != null) {
                    Text(
                        "Smena ${smenaSlovo(smena)} · ${dobaTekst(Motor.posle(dan, smena, post))}",
                        Modifier.padding(start = 4.dp, top = 8.dp),
                        fontSize = 15.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = Tekst,
                    )
                }
                termini.forEachIndexed { idx, termin ->
                    val broj = idx + 1
                    val cas = casovi.firstOrNull { it.broj == broj }
                    SlotRed(broj, termin, cas, dan, sada) { onIzmena(dan, broj, smena) }
                    if (broj == 2 && termini.size > 2) {
                        Text(
                            "VELIKI ODMOR · ${termini[1].kraj.hm()}–${termini[2].pocetak.hm()}",
                            Modifier.fillMaxWidth().padding(vertical = 2.dp),
                            fontSize = 12.sp,
                            color = SivaSvetla,
                            fontWeight = FontWeight.SemiBold,
                            textAlign = TextAlign.Center,
                        )
                    }
                }
            }
            Text(
                "Dodirni čas da ga zameniš ili otkažeš, ili prazno polje da dodaš čas.",
                Modifier.padding(horizontal = 4.dp, vertical = 6.dp),
                fontSize = 13.sp,
                color = Siva,
            )
        }
        Spacer(Modifier.height(8.dp))
    }
}

@Composable
private fun DanDugme(datum: LocalDate, izabran: Boolean, danas: Boolean, modifier: Modifier, onClick: () -> Unit) {
    val oblik = RoundedCornerShape(16.dp)
    Column(
        modifier.height(60.dp).clip(oblik)
            .background(if (izabran) Plava else Color.White)
            .border(if (danas && !izabran) 2.dp else 0.dp, if (danas && !izabran) Plava else Color.Transparent, oblik)
            .clickable(onClick = onClick),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Text(
            daniKratko[datum.dayOfWeek.value - 1],
            fontSize = 12.sp,
            fontWeight = FontWeight.SemiBold,
            color = if (izabran) Color.White else Siva,
        )
        Text(
            "${datum.dayOfMonth}",
            fontSize = 17.sp,
            fontWeight = FontWeight.Bold,
            color = if (izabran) Color.White else Tekst,
        )
    }
}

@Composable
private fun SlotRed(broj: Int, termin: Termin, cas: CasUDanu?, dan: LocalDate, sada: LocalDateTime, onClick: () -> Unit) {
    val oblik = RoundedCornerShape(16.dp)
    val danas = dan == sada.toLocalDate()
    val vreme = sada.toLocalTime()
    val prosao = dan.isBefore(sada.toLocalDate()) || (danas && !vreme.isBefore(termin.kraj))
    val traje = danas && !vreme.isBefore(termin.pocetak) && vreme.isBefore(termin.kraj)

    if (cas == null) {
        Row(
            Modifier.fillMaxWidth().clip(oblik).border(1.dp, Granica, oblik).clickable(onClick = onClick)
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text("$broj.", Modifier.width(28.dp), fontSize = 14.sp, color = SivaSvetla, fontWeight = FontWeight.SemiBold)
            Text("slobodno", Modifier.weight(1f), fontSize = 14.sp, color = SivaSvetla)
            Text(termin.tekst(), fontSize = 13.sp, color = SivaSvetla)
        }
        return
    }

    val (pozadina, ivica, debljina) = when {
        cas.izmenjen -> Triple(NarandzastaPozadina, NarandzastaIvica, 1.dp)
        traje -> Triple(Color.White, Plava, 2.dp)
        else -> Triple(Color.White, Color.Transparent, 0.dp)
    }
    val podnaslov = when {
        cas.otkazan -> "Otkazan"
        cas.izmenjen -> "Izmena"
        traje -> "Sada"
        prosao -> "Završeno"
        else -> null
    }

    Row(
        Modifier.fillMaxWidth().alpha(if (prosao && !traje) 0.6f else 1f).clip(oblik).background(pozadina)
            .border(debljina, ivica, oblik).clickable(onClick = onClick).padding(16.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            "$broj.",
            Modifier.width(28.dp),
            fontSize = 15.sp,
            fontWeight = FontWeight.Bold,
            color = if (traje) Plava else if (cas.izmenjen) NarandzastaTekst else Siva,
        )
        Column(Modifier.weight(1f)) {
            Text(
                cas.oznaka,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = Tekst,
                textDecoration = if (cas.otkazan) TextDecoration.LineThrough else TextDecoration.None,
            )
            if (podnaslov != null) {
                Text(
                    podnaslov,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = if (cas.izmenjen) NarandzastaTekst else if (traje) Plava else Siva,
                )
            }
        }
        Text(termin.tekst(), fontSize = 14.sp, color = if (traje) Plava else Siva)
    }
}
