package com.zi.raspored.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun Zaglavlje(nadnaslov: String, naslov: String, desno: @Composable () -> Unit = {}) {
    Row(
        Modifier.fillMaxWidth().padding(start = 4.dp, end = 4.dp, top = 16.dp, bottom = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(Modifier.weight(1f)) {
            Text(nadnaslov, fontSize = 13.sp, color = Siva, fontWeight = FontWeight.Medium)
            Text(naslov, fontSize = 24.sp, fontWeight = FontWeight.ExtraBold, color = Tekst)
        }
        desno()
    }
}

@Composable
fun Znacka(tekst: String, pozadina: Color = PlavaSvetla, boja: Color = Plava) {
    Text(
        tekst,
        Modifier.clip(RoundedCornerShape(50)).background(pozadina).padding(horizontal = 12.dp, vertical = 7.dp),
        color = boja,
        fontSize = 13.sp,
        fontWeight = FontWeight.Bold,
    )
}

@Composable
fun Traka(udeo: Float, boja: Color, pozadina: Color, visina: Dp = 8.dp) {
    Box(Modifier.fillMaxWidth().height(visina).clip(RoundedCornerShape(50)).background(pozadina)) {
        Box(Modifier.fillMaxWidth(udeo.coerceIn(0f, 1f)).fillMaxHeight().clip(RoundedCornerShape(50)).background(boja))
    }
}

@Composable
fun BelaKartica(modifier: Modifier = Modifier, sadrzaj: @Composable ColumnScope.() -> Unit) {
    Column(
        modifier.fillMaxWidth().clip(RoundedCornerShape(20.dp)).background(Color.White)
            .padding(horizontal = 18.dp, vertical = 14.dp),
        verticalArrangement = Arrangement.spacedBy(6.dp),
        content = sadrzaj,
    )
}

@Composable
fun DugmeIkona(ikona: ImageVector, opis: String, onClick: () -> Unit) {
    Box(
        Modifier.size(44.dp).clip(RoundedCornerShape(14.dp)).background(Color.White).clickable(onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        Icon(ikona, contentDescription = opis, tint = Tekst)
    }
}

@Composable
fun Cip(tekst: String, izabran: Boolean, modifier: Modifier = Modifier, onClick: () -> Unit) {
    val oblik = RoundedCornerShape(12.dp)
    Box(
        modifier.height(44.dp).clip(oblik)
            .background(if (izabran) Plava else Color.White)
            .border(1.dp, if (izabran) Plava else Granica, oblik)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            tekst,
            color = if (izabran) Color.White else Tekst,
            fontSize = 13.sp,
            fontWeight = FontWeight.Bold,
            maxLines = 1,
            overflow = TextOverflow.Clip,
        )
    }
}
