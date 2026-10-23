package com.zi.raspored.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material3.Icon
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

@Composable
fun PocetakScreen(onUnos: () -> Unit, onUcitaj: () -> Unit) {
    Column(
        Modifier.fillMaxSize().background(Color.White).systemBarsPadding().padding(horizontal = 20.dp, vertical = 24.dp),
    ) {
        Column(
            Modifier.weight(1f).fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(22.dp, Alignment.CenterVertically),
        ) {
            Box(
                Modifier.size(88.dp).clip(RoundedCornerShape(26.dp)).background(Plava),
                contentAlignment = Alignment.Center,
            ) {
                Icon(Icons.Filled.DateRange, contentDescription = null, tint = Color.White, modifier = Modifier.size(48.dp))
            }
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(
                    "Dobro došli u Raspored",
                    fontSize = 30.sp,
                    lineHeight = 35.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = Tekst,
                )
                Text(
                    "Prepiši svoj raspored časova jednom, a aplikacija će ti uvek pokazati koji je čas sada, koji je sledeći i koliko ima do zvona.",
                    fontSize = 17.sp,
                    lineHeight = 25.sp,
                    color = Siva,
                )
            }
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Korak(1, "Štikliraj odeljenja kojima predaješ")
                Korak(2, "Prepiši raspored sa papira")
                Korak(3, "Gotovo, za par minuta")
            }
        }
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            DugmeGlavno("Unesi svoj raspored", Modifier.fillMaxWidth(), onClick = onUnos)
            DugmeSporedno("Učitaj rezervnu kopiju", Modifier.fillMaxWidth(), onClick = onUcitaj)
            Text(
                "Kopiju praviš u Podešavanjima, korisno kad menjaš telefon.",
                Modifier.fillMaxWidth().padding(top = 4.dp),
                fontSize = 13.sp,
                color = Siva,
                textAlign = TextAlign.Center,
            )
        }
    }
}

@Composable
private fun Korak(broj: Int, tekst: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            Modifier.size(32.dp).clip(CircleShape).background(PlavaSvetla),
            contentAlignment = Alignment.Center,
        ) {
            Text("$broj", color = Plava, fontWeight = FontWeight.ExtraBold, fontSize = 15.sp)
        }
        Spacer(Modifier.width(12.dp))
        Text(tekst, fontSize = 16.sp, color = Tekst)
    }
}
