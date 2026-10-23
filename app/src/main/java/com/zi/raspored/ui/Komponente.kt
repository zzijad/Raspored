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
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
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

/** Zaglavlje za korake podešavanja: traka napretka, "Korak X od Y", naslov i objašnjenje. */
@Composable
fun KorakZaglavlje(korak: Int?, ukupno: Int, naslov: String, opis: String) {
    Column(
        Modifier.fillMaxWidth().padding(start = 4.dp, end = 4.dp, top = 12.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        if (korak != null) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                for (i in 1..ukupno) {
                    Box(
                        Modifier.weight(1f).height(5.dp).clip(RoundedCornerShape(50))
                            .background(if (i <= korak) Plava else Granica),
                    )
                }
            }
            Text("Korak $korak od $ukupno", fontSize = 13.sp, color = Siva, fontWeight = FontWeight.SemiBold)
        }
        Text(naslov, fontSize = 24.sp, fontWeight = FontWeight.ExtraBold, color = Tekst)
        Text(opis, fontSize = 15.sp, color = Siva, lineHeight = 21.sp)
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
fun Cip(
    tekst: String,
    izabran: Boolean,
    modifier: Modifier = Modifier,
    omogucen: Boolean = true,
    visina: Dp = 44.dp,
    onClick: () -> Unit,
) {
    val oblik = RoundedCornerShape(12.dp)
    val pozadina = when {
        izabran -> Plava
        !omogucen -> Pozadina
        else -> Color.White
    }
    val ivica = when {
        izabran -> Plava
        !omogucen -> Pozadina
        else -> Granica
    }
    Box(
        modifier.height(visina).clip(oblik).background(pozadina).border(1.dp, ivica, oblik)
            .clickable(enabled = omogucen, onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            tekst,
            color = when {
                izabran -> Color.White
                !omogucen -> Color(0xFF9CA3AF)
                else -> Tekst
            },
            fontSize = 13.sp,
            fontWeight = FontWeight.Bold,
            maxLines = 1,
            overflow = TextOverflow.Clip,
        )
    }
}

/** Izbor između nekoliko opcija (kao segmentirano dugme). */
@Composable
fun Segment(opcije: List<String>, izabrana: Int, onIzbor: (Int) -> Unit) {
    Row(
        Modifier.fillMaxWidth().clip(RoundedCornerShape(14.dp)).background(Pozadina).padding(4.dp),
        horizontalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        opcije.forEachIndexed { i, naziv ->
            Box(
                Modifier.weight(1f).height(44.dp).clip(RoundedCornerShape(11.dp))
                    .background(if (i == izabrana) Color.White else Color.Transparent)
                    .clickable { onIzbor(i) },
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    naziv,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (i == izabrana) Tekst else Siva,
                )
            }
        }
    }
}

@Composable
fun DugmeGlavno(tekst: String, modifier: Modifier = Modifier, omoguceno: Boolean = true, onClick: () -> Unit) {
    Button(
        onClick = onClick,
        enabled = omoguceno,
        modifier = modifier.height(56.dp),
        shape = RoundedCornerShape(16.dp),
        colors = ButtonDefaults.buttonColors(containerColor = Plava),
    ) {
        Text(tekst, fontSize = 15.sp, fontWeight = FontWeight.Bold)
    }
}

@Composable
fun DugmeSporedno(tekst: String, modifier: Modifier = Modifier, onClick: () -> Unit) {
    OutlinedButton(
        onClick = onClick,
        modifier = modifier.height(56.dp),
        shape = RoundedCornerShape(16.dp),
    ) {
        Text(tekst, fontSize = 15.sp, fontWeight = FontWeight.Bold, color = Tekst)
    }
}
