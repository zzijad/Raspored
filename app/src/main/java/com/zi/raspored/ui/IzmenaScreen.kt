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
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.zi.raspored.model.CasUDanu
import com.zi.raspored.model.Izmena
import com.zi.raspored.model.Smena
import com.zi.raspored.model.Termin
import com.zi.raspored.model.TipIzmene
import java.time.LocalDate

private fun TipIzmene.naziv(): String = when (this) {
    TipIzmene.DODAJ -> "Dodaj čas"
    TipIzmene.ZAMENI -> "Zameni"
    TipIzmene.OTKAZI -> "Otkaži čas"
}

/**
 * Jednokratna izmena jednog časa za konkretan datum (preko celog ekrana, dugmad na dnu).
 * original = čas po osnovnom rasporedu (null ako je termin slobodan).
 */
@Composable
fun IzmenaScreen(
    datum: LocalDate,
    broj: Int,
    smenaIndeks: Int,
    oznakaSmene: String?,
    smena: Smena,
    termin: Termin,
    original: CasUDanu?,
    postojeca: Izmena?,
    onSacuvaj: (Izmena) -> Unit,
    onVrati: () -> Unit,
    onNazad: () -> Unit,
) {
    val tipovi = if (original != null) listOf(TipIzmene.ZAMENI, TipIzmene.OTKAZI) else listOf(TipIzmene.DODAJ)

    var tip by remember { mutableStateOf(postojeca?.tip?.takeIf { it in tipovi } ?: tipovi.first()) }
    var odeljenje by remember { mutableStateOf(postojeca?.odeljenje ?: original?.odeljenje) }
    var grupa by remember { mutableStateOf(if (postojeca != null) postojeca.grupa else original?.grupa) }
    val izabrano = odeljenje
    val imaGrupe = izabrano != null && smena.imaGrupe(izabrano)

    Column(
        Modifier.fillMaxSize().background(Color.White).systemBarsPadding().padding(horizontal = 20.dp, vertical = 20.dp),
    ) {
        Column(
            Modifier.weight(1f).verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(18.dp),
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(
                    "${daniPuno[datum.dayOfWeek.value - 1]} ${datum.kratko()} · $broj. čas · ${termin.tekst()}" +
                        (oznakaSmene?.let { " · $it" } ?: ""),
                    fontSize = 13.sp,
                    color = Siva,
                    fontWeight = FontWeight.SemiBold,
                )
                Text("Izmena časa", fontSize = 22.sp, fontWeight = FontWeight.ExtraBold, color = Tekst)
                Text("Po rasporedu: ${original?.oznaka ?: "slobodno"}", fontSize = 13.sp, color = Siva)
            }

            if (tipovi.size > 1) {
                Segment(tipovi.map { it.naziv() }, tipovi.indexOf(tip)) { tip = tipovi[it] }
            }

            if (tip == TipIzmene.OTKAZI) {
                Text(
                    "Čas će biti prikazan kao otkazan i neće se računati u odbrojavanju.",
                    fontSize = 15.sp,
                    color = Siva,
                )
            } else {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Odeljenje", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Siva)
                    smena.odeljenja.chunked(6).forEach { red ->
                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            red.forEach { o ->
                                Cip(o, izabran = o == odeljenje, modifier = Modifier.weight(1f)) {
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
                            Text("Grupa", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Siva)
                            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                listOf<Int?>(null, 1, 2).forEach { g ->
                                    Cip(
                                        if (g == null) "Celo odeljenje" else "Grupa [$g]",
                                        izabran = g == grupa,
                                        modifier = Modifier.weight(1f),
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

            if (postojeca != null) {
                TextButton(onClick = onVrati) {
                    Text("Vrati po rasporedu", color = NarandzastaTekst, fontWeight = FontWeight.Bold)
                }
            }
        }

        Spacer(Modifier.height(12.dp))
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            DugmeSporedno("Odustani", Modifier.weight(1f), onClick = onNazad)
            DugmeGlavno(
                "Sačuvaj",
                Modifier.weight(1f),
                omoguceno = tip == TipIzmene.OTKAZI || izabrano != null,
            ) {
                val otkazi = tip == TipIzmene.OTKAZI
                onSacuvaj(
                    Izmena(
                        datum = datum,
                        broj = broj,
                        smena = smenaIndeks,
                        tip = tip,
                        odeljenje = if (otkazi) null else izabrano,
                        grupa = if (otkazi || !imaGrupe) null else grupa,
                    ),
                )
            }
        }
    }
}
