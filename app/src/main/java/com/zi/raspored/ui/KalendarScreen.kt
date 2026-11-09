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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.zi.raspored.data.KalendarGreska
import com.zi.raspored.data.KalendarPreuzimanje
import com.zi.raspored.model.Kalendar
import com.zi.raspored.model.Kalendari
import com.zi.raspored.model.SkolskiPodaci
import kotlinx.coroutines.launch

/** Školski kalendar: šta je trenutno u telefonu i preuzimanje sa sajta škole. */
@Composable
fun KalendarScreen(
    preuzet: Kalendar?,
    adresa: String,
    onPreuzet: (Kalendar, String) -> Unit,
    onVratiUgradjeni: () -> Unit,
    onNazad: () -> Unit,
) {
    val scope = rememberCoroutineScope()
    var url by remember { mutableStateOf(adresa) }
    var radi by remember { mutableStateOf(false) }
    var poruka by remember { mutableStateOf<Pair<Boolean, String>?>(null) }
    val k = preuzet ?: Kalendari.ugradjen

    Column(Modifier.fillMaxSize().background(Pozadina).systemBarsPadding().padding(horizontal = 16.dp, vertical = 12.dp)) {
        Column(
            Modifier.weight(1f).verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            KorakZaglavlje(
                null,
                0,
                "Školski kalendar",
                "Kalendar se preuzima sa sajta škole, gde ga ministarstvo ažurira za sve škole. Posle preuzimanja radi i bez interneta.",
            )

            BelaKartica {
                Info("Školska godina", k.godina)
                Info("Nastavnih dana", "${k.nastavni.size}")
                Info("Prvi dan", "${k.prvi.dayOfMonth}. ${meseci[k.prvi.monthValue - 1]} ${k.prvi.year}.")
                Info("Poslednji dan", "${k.poslednji.dayOfMonth}. ${meseci[k.poslednji.monthValue - 1]} ${k.poslednji.year}.")
                k.krajOsmi?.let { Info("Kraj za VIII razred", "${it.dayOfMonth}. ${meseci[it.monthValue - 1]}") }
                Info(
                    "Izvor",
                    if (preuzet == null) "ugrađen u aplikaciju" else "sajt škole" + (preuzet.preuzet?.let { ", ${it.kratko()}" } ?: ""),
                )
            }

            BelaKartica {
                Text("Adresa stranice sa kalendarom", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Siva)
                OutlinedTextField(
                    value = url,
                    onValueChange = { url = it },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    textStyle = androidx.compose.ui.text.TextStyle(fontSize = 14.sp),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Uri),
                )
                Text(
                    "Na sajtu svake škole: Školski kalendar. Iz druge škole? Upiši adresu svoje škole.",
                    fontSize = 12.sp,
                    color = Siva,
                )
                if (url.trim() != SkolskiPodaci.KALENDAR_URL) {
                    TextButton(onClick = { url = SkolskiPodaci.KALENDAR_URL }) {
                        Text("Vrati podrazumevanu adresu", color = Plava, fontWeight = FontWeight.Bold)
                    }
                }
            }

            poruka?.let { (uspeh, tekst) ->
                Text(
                    tekst,
                    Modifier.fillMaxWidth().clip(RoundedCornerShape(14.dp))
                        .background(if (uspeh) Color(0xFFDCFCE7) else NarandzastaPozadina)
                        .padding(horizontal = 14.dp, vertical = 12.dp),
                    fontSize = 14.sp,
                    color = if (uspeh) Color(0xFF166534) else NarandzastaTekst,
                    fontWeight = FontWeight.SemiBold,
                )
            }

            if (preuzet != null) {
                TextButton(onClick = onVratiUgradjeni) {
                    Text("Vrati ugrađeni kalendar 2026/2027", color = NarandzastaTekst, fontWeight = FontWeight.Bold)
                }
            }
        }

        Spacer(Modifier.height(12.dp))
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            DugmeSporedno("Nazad", Modifier.weight(1f), onClick = onNazad)
            DugmeGlavno(if (radi) "Preuzimam…" else "Preuzmi", Modifier.weight(1f), omoguceno = !radi && url.isNotBlank()) {
                radi = true
                poruka = null
                scope.launch {
                    try {
                        val novi = KalendarPreuzimanje.preuzmi(url)
                        onPreuzet(novi, url.trim())
                        poruka = true to "Preuzet kalendar ${novi.godina}: ${novi.nastavni.size} nastavnih dana, svi meseci se slažu sa sajtom."
                    } catch (e: KalendarGreska) {
                        poruka = false to (e.message ?: "Kalendar nije preuzet.")
                    } catch (e: Exception) {
                        poruka = false to "Kalendar nije preuzet."
                    } finally {
                        radi = false
                    }
                }
            }
        }
        if (radi) {
            Row(Modifier.fillMaxWidth().padding(top = 8.dp), horizontalArrangement = Arrangement.Center, verticalAlignment = Alignment.CenterVertically) {
                CircularProgressIndicator(Modifier.size(18.dp), strokeWidth = 2.dp, color = Plava)
                Spacer(Modifier.width(8.dp))
                Text("Učitavam sajt škole…", fontSize = 13.sp, color = Siva)
            }
        }
    }
}

@Composable
private fun Info(naziv: String, vrednost: String) {
    Row(Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
        Text(naziv, Modifier.weight(1f), fontSize = 15.sp, color = Siva)
        Text(vrednost, fontSize = 15.sp, fontWeight = FontWeight.SemiBold, color = Tekst)
    }
}
