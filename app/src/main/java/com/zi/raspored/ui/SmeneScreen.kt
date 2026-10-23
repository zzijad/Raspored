package com.zi.raspored.ui

import androidx.compose.foundation.BorderStroke
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
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
import com.zi.raspored.model.Postavke
import com.zi.raspored.model.SkolskiPodaci
import com.zi.raspored.model.Smena

/** Korak 1: smene i odeljenja. onboarding = deo prvog pokretanja (prikazuje "Korak 1 od 2"). */
@Composable
fun SmeneScreen(
    pocetne: Postavke?,
    onboarding: Boolean,
    onNazad: () -> Unit,
    onSacuvaj: (Postavke) -> Unit,
) {
    var aOd by remember { mutableStateOf(pocetne?.a?.odeljenja?.toSet() ?: emptySet()) }
    var aGr by remember { mutableStateOf(pocetne?.a?.grupe ?: emptySet()) }
    var aPosle by remember { mutableStateOf(pocetne?.aPosleUSeptembru ?: false) }
    var imaB by remember { mutableStateOf(pocetne?.b != null) }
    var bOd by remember { mutableStateOf(pocetne?.b?.odeljenja?.toSet() ?: emptySet()) }
    var bGr by remember { mutableStateOf(pocetne?.b?.grupe ?: emptySet()) }

    Column(Modifier.fillMaxSize().background(Pozadina).systemBarsPadding().padding(horizontal = 16.dp, vertical = 12.dp)) {
        Column(
            Modifier.weight(1f).verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            KorakZaglavlje(
                korak = if (onboarding) 1 else null,
                ukupno = 2,
                naslov = "Kojim odeljenjima predaješ?",
                opis = "Dodirni svoja odeljenja. Ako predaješ u obe smene, dodaj i drugu smenu ispod.",
            )

            SmenaKartica(
                naslov = "Smena A",
                izabrana = aOd,
                grupe = aGr,
                zauzeta = if (imaB) bOd else emptySet(),
                onIzabrana = { novo -> aOd = novo; aGr = aGr.intersect(novo) },
                onGrupe = { aGr = it },
                dodatak = {
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text("U septembru radi", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Siva)
                        Segment(listOf("Pre podne", "Posle podne"), if (aPosle) 1 else 0) { aPosle = it == 1 }
                    }
                },
                desno = {},
            )

            if (!imaB) {
                OutlinedButton(
                    onClick = { imaB = true },
                    modifier = Modifier.fillMaxWidth().height(56.dp),
                    shape = RoundedCornerShape(16.dp),
                    border = BorderStroke(2.dp, Color(0xFF9CA3AF)),
                ) {
                    Icon(Icons.Filled.Add, contentDescription = null, tint = Tekst)
                    Spacer(Modifier.width(8.dp))
                    Text("Dodaj drugu smenu", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = Tekst)
                }
            } else {
                SmenaKartica(
                    naslov = "Smena B",
                    izabrana = bOd,
                    grupe = bGr,
                    zauzeta = aOd,
                    onIzabrana = { novo -> bOd = novo; bGr = bGr.intersect(novo) },
                    onGrupe = { bGr = it },
                    dodatak = {
                        Text(
                            "U septembru radi ${if (aPosle) "pre podne" else "posle podne"}, uvek suprotno od smene A.",
                            Modifier.fillMaxWidth().clip(RoundedCornerShape(12.dp)).background(Pozadina)
                                .padding(horizontal = 12.dp, vertical = 10.dp),
                            fontSize = 14.sp,
                            color = Siva,
                        )
                    },
                    desno = {
                        TextButton(onClick = { imaB = false; bOd = emptySet(); bGr = emptySet() }) {
                            Text("Ukloni", color = NarandzastaTekst, fontWeight = FontWeight.Bold)
                        }
                    },
                )
            }
            Spacer(Modifier.height(4.dp))
        }

        Spacer(Modifier.height(12.dp))
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            DugmeSporedno(if (onboarding) "Nazad" else "Odustani", Modifier.weight(1f), onClick = onNazad)
            DugmeGlavno(
                if (onboarding) "Dalje: unos rasporeda" else "Sačuvaj",
                Modifier.weight(2f),
                omoguceno = aOd.isNotEmpty(),
            ) {
                val b = if (imaB && bOd.isNotEmpty()) Smena(SkolskiPodaci.sortiraj(bOd), bGr.intersect(bOd)) else null
                onSacuvaj(Postavke(Smena(SkolskiPodaci.sortiraj(aOd), aGr.intersect(aOd)), aPosle, b))
            }
        }
    }
}

@Composable
private fun SmenaKartica(
    naslov: String,
    izabrana: Set<String>,
    grupe: Set<String>,
    zauzeta: Set<String>,
    onIzabrana: (Set<String>) -> Unit,
    onGrupe: (Set<String>) -> Unit,
    dodatak: @Composable () -> Unit,
    desno: @Composable () -> Unit,
) {
    Column(
        Modifier.fillMaxWidth().clip(RoundedCornerShape(20.dp)).background(Color.White).padding(horizontal = 14.dp, vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Text(naslov, Modifier.weight(1f), fontSize = 17.sp, fontWeight = FontWeight.ExtraBold, color = Tekst)
            Text(
                if (izabrana.size == 1) "1 odeljenje" else "${izabrana.size} odeljenja",
                fontSize = 13.sp,
                color = Siva,
                fontWeight = FontWeight.SemiBold,
            )
            desno()
        }
        dodatak()

        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                Spacer(Modifier.width(34.dp))
                for (n in 1..SkolskiPodaci.MAKS_ODELJENJA) {
                    Text(
                        "$n",
                        Modifier.weight(1f),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = Siva,
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                    )
                }
            }
            SkolskiPodaci.razredi.forEach { razred ->
                Row(
                    Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(razred, Modifier.width(34.dp), fontSize = 14.sp, fontWeight = FontWeight.ExtraBold, color = Tekst)
                    for (n in 1..SkolskiPodaci.MAKS_ODELJENJA) {
                        val id = "$razred$n"
                        val zauzet = id in zauzeta
                        Cip(
                            if (zauzet) "–" else "$n",
                            izabran = id in izabrana,
                            modifier = Modifier.weight(1f),
                            omogucen = !zauzet,
                        ) {
                            onIzabrana(if (id in izabrana) izabrana - id else izabrana + id)
                        }
                    }
                }
            }
        }

        if (izabrana.isNotEmpty()) {
            HorizontalDivider(color = Linija)
            Text("Koja od njih imaju grupe [1] i [2]?", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Siva)
            SkolskiPodaci.sortiraj(izabrana).chunked(4).forEach { red ->
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    red.forEach { id ->
                        val ima = id in grupe
                        Cip(
                            if (ima) "$id ✓" else id,
                            izabran = ima,
                            modifier = Modifier.weight(1f),
                            visina = 40.dp,
                        ) {
                            onGrupe(if (ima) grupe - id else grupe + id)
                        }
                    }
                    repeat(4 - red.size) { Spacer(Modifier.weight(1f)) }
                }
            }
            Text("Plavo = odeljenje ima grupe.", fontSize = 12.sp, color = Siva)
        }
    }
}
