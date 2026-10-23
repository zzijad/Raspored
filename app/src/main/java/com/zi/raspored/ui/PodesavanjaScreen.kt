package com.zi.raspored.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.zi.raspored.logic.Motor
import com.zi.raspored.model.Izmena
import com.zi.raspored.model.SkolskiPodaci
import com.zi.raspored.model.TipIzmene
import java.time.LocalDate

@Composable
fun PodesavanjaScreen(
    danas: LocalDate,
    izmene: List<Izmena>,
    onObrisi: (Izmena) -> Unit,
    onObrisiStare: () -> Unit,
) {
    val kraj = SkolskiPodaci.polugodista.last().doDana
    Column(
        Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        Zaglavlje("Školska ${SkolskiPodaci.polugodista.first().od.year}/${kraj.year}.", "Podešavanja")

        BelaKartica {
            InfoRed("Smena (V i VII)", smenaTekst(Motor.smena(danas)))
            Motor.promenaSmene(danas)?.let { (d, s) -> InfoRed("Sledeća promena", "${smenaTekst(s)} od ${d.kratko()}") }
            InfoRed("Kraj nastave", "${kraj.dayOfMonth}. ${meseci[kraj.monthValue - 1]} ${kraj.year}.")
            InfoRed("Preostalo", brojNastavnihDana(Motor.preostaloNastavnihDana(danas)))
        }

        Text(
            "Izmene",
            Modifier.padding(start = 4.dp, top = 8.dp),
            fontSize = 17.sp,
            fontWeight = FontWeight.Bold,
            color = Tekst,
        )

        if (izmene.isEmpty()) {
            Text(
                "Nema unetih izmena. Izmenu dodaješ dodirom na čas u prikazu Dan ili Nedelja.",
                Modifier.padding(horizontal = 4.dp),
                fontSize = 14.sp,
                color = Siva,
            )
        } else {
            izmene.sortedWith(compareBy<Izmena>({ it.datum }, { it.broj })).forEach { iz ->
                BelaKartica {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Column(Modifier.weight(1f)) {
                            Text(
                                "${daniKratko[iz.datum.dayOfWeek.value - 1]} ${iz.datum.kratko()} · ${iz.broj}. čas",
                                fontSize = 13.sp,
                                color = Siva,
                                fontWeight = FontWeight.SemiBold,
                            )
                            Text(opisIzmene(iz), fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Tekst)
                        }
                        IconButton(onClick = { onObrisi(iz) }) {
                            Icon(Icons.Filled.Delete, contentDescription = "Obriši izmenu", tint = Siva)
                        }
                    }
                }
            }
            if (izmene.any { it.datum.isBefore(danas.minusDays(30)) }) {
                TextButton(onClick = onObrisiStare) {
                    Text("Obriši izmene starije od 30 dana", color = Plava, fontWeight = FontWeight.Bold)
                }
            }
        }
        Spacer(Modifier.height(12.dp))
    }
}

private fun opisIzmene(iz: Izmena): String {
    val oznaka = when {
        iz.odeljenje == null -> ""
        iz.grupa == null -> iz.odeljenje
        else -> "${iz.odeljenje} [${iz.grupa}]"
    }
    return when (iz.tip) {
        TipIzmene.OTKAZI -> "Otkazan čas"
        TipIzmene.ZAMENI -> "Zamena: $oznaka"
        TipIzmene.DODAJ -> "Dodat čas: $oznaka"
    }
}

@Composable
private fun InfoRed(naziv: String, vrednost: String) {
    Row(Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
        Text(naziv, Modifier.weight(1f), fontSize = 15.sp, color = Siva)
        Text(vrednost, fontSize = 15.sp, fontWeight = FontWeight.SemiBold, color = Tekst)
    }
}
