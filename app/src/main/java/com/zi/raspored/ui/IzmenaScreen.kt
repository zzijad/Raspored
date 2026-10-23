package com.zi.raspored.ui

import androidx.compose.foundation.background
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.OutlinedButton
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
import com.zi.raspored.logic.Motor
import com.zi.raspored.model.CasUDanu
import com.zi.raspored.model.Izmena
import com.zi.raspored.model.SkolskiPodaci
import com.zi.raspored.model.TipIzmene
import java.time.LocalDate

private fun TipIzmene.naziv(): String = when (this) {
    TipIzmene.DODAJ -> "Dodaj čas"
    TipIzmene.ZAMENI -> "Zameni"
    TipIzmene.OTKAZI -> "Otkaži čas"
}

/**
 * Ekran preko celog ekrana za izmenu jednog časa.
 * original = čas po osnovnom rasporedu (null ako je termin slobodan).
 */
@Composable
fun IzmenaScreen(
    datum: LocalDate,
    broj: Int,
    original: CasUDanu?,
    postojeca: Izmena?,
    onSacuvaj: (Izmena) -> Unit,
    onVrati: () -> Unit,
    onNazad: () -> Unit,
) {
    val termin = Motor.termini(datum)[broj - 1]
    val tipovi = if (original != null) listOf(TipIzmene.ZAMENI, TipIzmene.OTKAZI) else listOf(TipIzmene.DODAJ)

    var tip by remember { mutableStateOf(postojeca?.tip?.takeIf { it in tipovi } ?: tipovi.first()) }
    var odeljenje by remember { mutableStateOf(postojeca?.odeljenje ?: original?.odeljenje) }
    var grupa by remember { mutableStateOf(if (postojeca != null) postojeca.grupa else original?.grupa) }

    Column(
        Modifier.fillMaxSize().background(Color.White).systemBarsPadding().padding(horizontal = 20.dp, vertical = 20.dp),
    ) {
        Column(
            Modifier.weight(1f).verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(18.dp),
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(
                    "${daniPuno[datum.dayOfWeek.value - 1]} ${datum.kratko()} · $broj. čas · ${termin.tekst()}",
                    fontSize = 13.sp,
                    color = Siva,
                    fontWeight = FontWeight.SemiBold,
                )
                Text("Izmena časa", fontSize = 22.sp, fontWeight = FontWeight.ExtraBold, color = Tekst)
                Text("Po rasporedu: ${original?.oznaka ?: "slobodno"}", fontSize = 13.sp, color = Siva)
            }

            if (tipovi.size > 1) {
                Row(
                    Modifier.fillMaxWidth().clip(RoundedCornerShape(14.dp)).background(Pozadina).padding(4.dp),
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                ) {
                    tipovi.forEach { t ->
                        Box(
                            Modifier.weight(1f).height(44.dp).clip(RoundedCornerShape(11.dp))
                                .background(if (t == tip) Color.White else Color.Transparent)
                                .clickable { tip = t },
                            contentAlignment = Alignment.Center,
                        ) {
                            Text(
                                t.naziv(),
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (t == tip) Tekst else Siva,
                            )
                        }
                    }
                }
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
                    SkolskiPodaci.odeljenja.chunked(6).forEach { red ->
                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            red.forEach { o ->
                                Cip(o, izabran = o == odeljenje, Modifier.weight(1f)) { odeljenje = o }
                            }
                        }
                    }
                }
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Grupa", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Siva)
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        listOf<Int?>(null, 1, 2).forEach { g ->
                            Cip(
                                if (g == null) "Celo odeljenje" else "Grupa [$g]",
                                izabran = g == grupa,
                                Modifier.weight(1f),
                            ) { grupa = g }
                        }
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
            OutlinedButton(
                onClick = onNazad,
                modifier = Modifier.weight(1f).height(56.dp),
                shape = RoundedCornerShape(16.dp),
            ) {
                Text("Odustani", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = Tekst)
            }
            Button(
                onClick = {
                    val otkazi = tip == TipIzmene.OTKAZI
                    onSacuvaj(
                        Izmena(
                            datum = datum,
                            broj = broj,
                            tip = tip,
                            odeljenje = if (otkazi) null else odeljenje,
                            grupa = if (otkazi) null else grupa,
                        ),
                    )
                },
                enabled = tip == TipIzmene.OTKAZI || odeljenje != null,
                modifier = Modifier.weight(1f).height(56.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Plava),
            ) {
                Text("Sačuvaj", fontSize = 15.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}
