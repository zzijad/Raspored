package com.zi.raspored.ui

import androidx.compose.foundation.clickable
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
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.zi.raspored.logic.Motor
import com.zi.raspored.model.Izmena
import com.zi.raspored.model.Podaci
import com.zi.raspored.model.SMENA_A
import com.zi.raspored.model.SkolskiPodaci
import com.zi.raspored.model.TipIzmene
import java.time.LocalDate

@Composable
fun PodesavanjaScreen(
    danas: LocalDate,
    podaci: Podaci,
    onSmene: () -> Unit,
    onRaspored: () -> Unit,
    onSatnica: () -> Unit,
    onIzvoz: () -> Unit,
    onUvoz: () -> Unit,
    onObrisiSve: () -> Unit,
    onObrisi: (Izmena) -> Unit,
    onObrisiStare: () -> Unit,
) {
    val post = podaci.postavke ?: return
    val kraj = SkolskiPodaci.polugodista.last().doDana
    var potvrdaBrisanja by remember { mutableStateOf(false) }

    Column(
        Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        Zaglavlje("Školska ${SkolskiPodaci.polugodista.first().od.year}/${kraj.year}.", "Podešavanja")

        BelaKartica {
            if (post.b == null) {
                InfoRed("Smena", dobaTekst(Motor.posle(danas, SMENA_A, post)))
                Motor.promenaSmene(danas, post)?.let { (d, posle) -> InfoRed("Sledeća promena", "${dobaTekst(posle)} od ${d.kratko()}") }
            } else {
                post.aktivne.forEach { s ->
                    InfoRed("Smena ${smenaSlovo(s)}", dobaTekst(Motor.posle(danas, s, post)))
                }
            }
            InfoRed("Kraj nastave", "${kraj.dayOfMonth}. ${meseci[kraj.monthValue - 1]} ${kraj.year}.")
            InfoRed("Preostalo", brojNastavnihDana(Motor.preostaloNastavnihDana(danas)))
        }

        Naslov("Moj raspored")
        BelaKartica {
            Stavka("Odeljenja i smene", "${post.a.odeljenja.size + (post.b?.odeljenja?.size ?: 0)} odeljenja", onSmene)
            HorizontalDivider(color = Linija)
            Stavka("Osnovni raspored", brojCasova(podaci.raspored.size), onRaspored)
            HorizontalDivider(color = Linija)
            Stavka("Satnica zvona", "${podaci.satnica.pre.first().pocetak.hm()} / ${podaci.satnica.posle.first().pocetak.hm()}", onSatnica)
        }

        Naslov("Rezervna kopija")
        BelaKartica {
            Stavka("Sačuvaj kopiju", "fajl za Viber, mejl, novi telefon", onIzvoz)
            HorizontalDivider(color = Linija)
            Stavka("Učitaj kopiju", "zamenjuje trenutne podatke", onUvoz)
        }

        Naslov("Izmene")
        if (podaci.izmene.isEmpty()) {
            Text(
                "Nema unetih izmena. Izmenu dodaješ dodirom na čas u prikazu Dan ili Nedelja.",
                Modifier.padding(horizontal = 4.dp),
                fontSize = 14.sp,
                color = Siva,
            )
        } else {
            podaci.izmene.sortedWith(compareBy<Izmena>({ it.datum }, { it.broj })).forEach { iz ->
                BelaKartica {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Column(Modifier.weight(1f)) {
                            Text(
                                "${daniKratko[iz.datum.dayOfWeek.value - 1]} ${iz.datum.kratko()} · ${iz.broj}. čas" +
                                    if (post.b != null) " · smena ${smenaSlovo(iz.smena)}" else "",
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
            if (podaci.izmene.any { it.datum.isBefore(danas.minusDays(30)) }) {
                TextButton(onClick = onObrisiStare) {
                    Text("Obriši izmene starije od 30 dana", color = Plava, fontWeight = FontWeight.Bold)
                }
            }
        }

        TextButton(onClick = { potvrdaBrisanja = true }, modifier = Modifier.padding(top = 12.dp)) {
            Text("Obriši sve i počni ispočetka", color = NarandzastaTekst, fontWeight = FontWeight.Bold)
        }
        Spacer(Modifier.height(12.dp))
    }

    if (potvrdaBrisanja) {
        AlertDialog(
            onDismissRequest = { potvrdaBrisanja = false },
            title = { Text("Obrisati sve?") },
            text = { Text("Brišu se odeljenja, raspored i izmene. Ako želiš da ih sačuvaš, prvo napravi rezervnu kopiju.") },
            confirmButton = {
                TextButton(onClick = { potvrdaBrisanja = false; onObrisiSve() }) {
                    Text("Obriši", color = NarandzastaTekst, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { potvrdaBrisanja = false }) { Text("Odustani") }
            },
        )
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
private fun Naslov(tekst: String) {
    Text(
        tekst,
        Modifier.padding(start = 4.dp, top = 8.dp),
        fontSize = 17.sp,
        fontWeight = FontWeight.Bold,
        color = Tekst,
    )
}

@Composable
private fun Stavka(naziv: String, opis: String, onClick: () -> Unit) {
    Row(
        Modifier.fillMaxWidth().clickable(onClick = onClick).padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(Modifier.weight(1f)) {
            Text(naziv, fontSize = 16.sp, fontWeight = FontWeight.SemiBold, color = Tekst)
            Text(opis, fontSize = 13.sp, color = Siva)
        }
        Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, contentDescription = null, tint = Siva)
    }
}

@Composable
private fun InfoRed(naziv: String, vrednost: String) {
    Row(Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
        Text(naziv, Modifier.weight(1f), fontSize = 15.sp, color = Siva)
        Text(vrednost, fontSize = 15.sp, fontWeight = FontWeight.SemiBold, color = Tekst)
    }
}
