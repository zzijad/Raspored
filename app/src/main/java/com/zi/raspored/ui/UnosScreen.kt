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
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.zi.raspored.model.Podaci
import com.zi.raspored.model.SMENA_A
import com.zi.raspored.model.SkolskiPodaci

/** Korak 2: unos osnovnog rasporeda. Dodir na polje otvara izbor odeljenja. */
@Composable
fun UnosScreen(
    podaci: Podaci,
    onboarding: Boolean,
    tab: Int,
    onTab: (Int) -> Unit,
    onCelija: (dan: Int, broj: Int, smena: Int) -> Unit,
    onNazad: () -> Unit,
    onGotovo: () -> Unit,
) {
    val post = podaci.postavke ?: return
    val smena = if (post.smena(tab) != null) tab else SMENA_A
    val casovi = podaci.raspored.filter { it.smena == smena }

    Column(Modifier.fillMaxSize().background(Pozadina).systemBarsPadding().padding(horizontal = 16.dp, vertical = 12.dp)) {
        Column(
            Modifier.weight(1f).verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            KorakZaglavlje(
                korak = if (onboarding) 2 else null,
                ukupno = 2,
                naslov = if (onboarding) "Prepiši svoj raspored" else "Osnovni raspored",
                opis = "Dodirni polje i izaberi odeljenje. Prazna polja su slobodni časovi.",
            )

            if (post.b != null) {
                Segment(listOf("Smena A", "Smena B"), smena) { onTab(it) }
            }

            Row(
                Modifier.fillMaxWidth().clip(RoundedCornerShape(16.dp)).background(Color.White)
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text("Smena ${smenaSlovo(smena)}", Modifier.weight(1f), fontSize = 15.sp, fontWeight = FontWeight.Bold, color = Tekst)
                Text("uneto: ${brojCasova(casovi.size)}", fontSize = 14.sp, color = Siva, fontWeight = FontWeight.SemiBold)
            }

            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    Spacer(Modifier.width(34.dp))
                    for (b in 1..SkolskiPodaci.BROJ_CASOVA) {
                        Text(
                            "$b.",
                            Modifier.weight(1f).padding(vertical = 4.dp),
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = Tekst,
                            textAlign = TextAlign.Center,
                        )
                    }
                }
                for (dan in 1..5) {
                    Row(
                        Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text(daniKratko[dan - 1], Modifier.width(34.dp), fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Tekst)
                        for (b in 1..SkolskiPodaci.BROJ_CASOVA) {
                            val c = casovi.firstOrNull { it.dan == dan && it.broj == b }
                            val oblik = RoundedCornerShape(10.dp)
                            if (c == null) {
                                Column(
                                    Modifier.weight(1f).height(72.dp).clip(oblik).background(Color.White)
                                        .border(1.dp, Granica, oblik).clickable { onCelija(dan, b, smena) },
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.Center,
                                ) {
                                    Text("+", fontSize = 18.sp, color = Color(0xFF9CA3AF), fontWeight = FontWeight.Bold)
                                }
                            } else {
                                val (pozadina, boja) = bojaRazreda(c.odeljenje)
                                Column(
                                    Modifier.weight(1f).height(72.dp).clip(oblik)
                                        .background(pozadina)
                                        .clickable { onCelija(dan, b, smena) },
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.Center,
                                ) {
                                    Text(
                                        c.odeljenje,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = boja,
                                        maxLines = 1,
                                        softWrap = false,
                                    )
                                    Text(
                                        if (c.grupa == null) "cela" else "[${c.grupa}]",
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = boja,
                                    )
                                }
                            }
                        }
                    }
                }
            }

            Text(
                "Vreme časova aplikacija računa sama, po smeni. Ne moraš da ga unosiš.",
                Modifier.fillMaxWidth().clip(RoundedCornerShape(16.dp)).background(Color.White)
                    .padding(horizontal = 14.dp, vertical = 12.dp),
                fontSize = 13.sp,
                color = Siva,
                lineHeight = 19.sp,
            )
        }

        Spacer(Modifier.height(12.dp))
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            if (onboarding) DugmeSporedno("Nazad", Modifier.weight(1f), onClick = onNazad)
            DugmeGlavno("Gotovo", Modifier.weight(2f), onClick = onGotovo)
        }
    }
}
