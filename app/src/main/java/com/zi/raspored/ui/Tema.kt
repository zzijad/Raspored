package com.zi.raspored.ui

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

val Plava = Color(0xFF1D4ED8)
val PlavaSvetla = Color(0xFFDBE4FB)
val PlavaTamna = Color(0xFF1E3A8A)
val Tirkizna = Color(0xFFCCEFE9)
val TirkiznaTamna = Color(0xFF0B4F47)
val Pozadina = Color(0xFFF3F5F7)
val Tekst = Color(0xFF111827)
val Siva = Color(0xFF4B5563)
val SivaSvetla = Color(0xFF6B7280)
val Linija = Color(0xFFE5E7EB)
val Granica = Color(0xFFD1D5DB)
val NarandzastaPozadina = Color(0xFFFFF7ED)
val NarandzastaIvica = Color(0xFFFDBA74)
val NarandzastaTekst = Color(0xFF9A3412)

@Composable
fun RasporedTema(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = lightColorScheme(
            primary = Plava,
            onPrimary = Color.White,
            secondaryContainer = PlavaSvetla,
            onSecondaryContainer = Plava,
            background = Pozadina,
            onBackground = Tekst,
            surface = Color.White,
            onSurface = Tekst,
            onSurfaceVariant = Siva,
        ),
        content = content,
    )
}
