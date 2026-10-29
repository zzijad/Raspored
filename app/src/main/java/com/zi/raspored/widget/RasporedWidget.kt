package com.zi.raspored.widget

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.action.actionStartActivity
import androidx.glance.action.clickable
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.GlanceAppWidgetReceiver
import androidx.glance.appwidget.SizeMode
import androidx.glance.appwidget.cornerRadius
import androidx.glance.appwidget.provideContent
import androidx.glance.background
import androidx.glance.layout.Alignment
import androidx.glance.layout.Box
import androidx.glance.layout.Column
import androidx.glance.layout.Row
import androidx.glance.layout.Spacer
import androidx.glance.layout.fillMaxHeight
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.fillMaxWidth
import androidx.glance.layout.padding
import androidx.glance.layout.width
import androidx.glance.text.FontWeight
import androidx.glance.text.Text
import androidx.glance.text.TextAlign
import androidx.glance.text.TextDecoration
import androidx.glance.text.TextStyle
import androidx.glance.unit.ColorProvider
import com.zi.raspored.MainActivity
import com.zi.raspored.data.Skladiste
import com.zi.raspored.logic.Motor
import com.zi.raspored.logic.Stanje
import com.zi.raspored.model.CasUDanu
import com.zi.raspored.model.Podaci
import com.zi.raspored.model.SMENA_A
import com.zi.raspored.model.SkolskiPodaci
import com.zi.raspored.ui.NarandzastaPozadina
import com.zi.raspored.ui.NarandzastaTekst
import com.zi.raspored.ui.Plava
import com.zi.raspored.ui.Siva
import com.zi.raspored.ui.Tekst
import com.zi.raspored.ui.bojaRazreda
import com.zi.raspored.ui.daniKratko
import com.zi.raspored.ui.dobaTekst
import com.zi.raspored.ui.kratko
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.temporal.TemporalAdjusters

class RasporedWidgetReceiver : GlanceAppWidgetReceiver() {
    override val glanceAppWidget: GlanceAppWidget = RasporedWidget()
}

/** Widget sa nedeljnim rasporedom (isti prikaz kao ekran Nedelja). */
class RasporedWidget : GlanceAppWidget() {

    override val sizeMode: SizeMode = SizeMode.Single

    override suspend fun provideGlance(context: Context, id: GlanceId) {
        val podaci = Skladiste(context).ucitaj()
        val sada = LocalDateTime.now()
        Osvezavanje.zakazi(context, podaci, sada)
        provideContent {
            Sadrzaj(podaci, sada)
        }
    }
}

private val Pozadina = Color(0xFFF3F5F7)

private fun stil(boja: Color, velicina: Int = 11, debljina: FontWeight = FontWeight.Normal, precrtano: Boolean = false) =
    TextStyle(
        color = ColorProvider(boja),
        fontSize = velicina.sp,
        fontWeight = debljina,
        textAlign = TextAlign.Center,
        textDecoration = if (precrtano) TextDecoration.LineThrough else TextDecoration.None,
    )

@Composable
private fun Sadrzaj(podaci: Podaci, sada: LocalDateTime) {
    val koren = GlanceModifier.fillMaxSize().background(Color.White).cornerRadius(20.dp).padding(10.dp)
        .clickable(actionStartActivity<MainActivity>())
    val post = podaci.postavke
    if (post == null || podaci.raspored.isEmpty()) {
        Box(koren, contentAlignment = Alignment.Center) {
            Text("Otvori Raspored i unesi svoj raspored", style = stil(Siva, 13, FontWeight.Medium))
        }
        return
    }

    val danas = sada.toLocalDate()
    // vikendom se prikazuje sledeća nedelja
    val ponedeljak = if (danas.dayOfWeek.value > 5) {
        danas.with(TemporalAdjusters.next(DayOfWeek.MONDAY))
    } else {
        danas.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY))
    }

    // smena koja se prikazuje: ona u kojoj je trenutni ili sledeći čas danas (za one koji rade u obe smene)
    val smena = when (val s = Motor.stanje(sada, podaci)) {
        is Stanje.NaCasu -> s.cas.smena
        is Stanje.Ceka -> s.cas.smena
        is Stanje.Slobodno -> s.cas?.smena ?: SMENA_A
    }.let { if (post.smena(it) != null) it else SMENA_A }

    val posle = Motor.posle(ponedeljak, smena, post)

    Column(koren) {
        Row(GlanceModifier.fillMaxWidth().padding(start = 2.dp, end = 2.dp, bottom = 6.dp), verticalAlignment = Alignment.CenterVertically) {
            Text(
                "Nedelja ${ponedeljak.dayOfMonth}.–${ponedeljak.plusDays(4).kratko()}",
                GlanceModifier.defaultWeight(),
                style = TextStyle(color = ColorProvider(Tekst), fontSize = 13.sp, fontWeight = FontWeight.Bold),
            )
            Text(
                (if (post.b != null) "Smena ${if (smena == SMENA_A) "A" else "B"} · " else "") + dobaTekst(posle),
                GlanceModifier.background(Color(0xFFDBE4FB)).cornerRadius(10.dp).padding(horizontal = 8.dp, vertical = 3.dp),
                style = stil(Plava, 11, FontWeight.Bold),
            )
        }

        Row(GlanceModifier.fillMaxWidth().padding(bottom = 2.dp)) {
            Spacer(GlanceModifier.width(30.dp))
            for (b in 1..SkolskiPodaci.BROJ_CASOVA) {
                Text("$b.", GlanceModifier.defaultWeight(), style = stil(Siva, 10, FontWeight.Bold))
            }
        }

        for (i in 0..4) {
            val d = ponedeljak.plusDays(i.toLong())
            DanRed(d, d == danas, smena, podaci, sada, GlanceModifier.fillMaxWidth().defaultWeight().padding(vertical = 1.5.dp))
        }
    }
}

@Composable
private fun DanRed(d: LocalDate, jeDanas: Boolean, smena: Int, podaci: Podaci, sada: LocalDateTime, modifier: GlanceModifier) {
    Row(modifier, verticalAlignment = Alignment.CenterVertically) {
        Text(
            daniKratko[d.dayOfWeek.value - 1],
            GlanceModifier.width(30.dp),
            style = TextStyle(
                color = ColorProvider(if (jeDanas) Plava else Tekst),
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
            ),
        )
        if (!Motor.jeNastavniDan(d)) {
            Box(
                GlanceModifier.defaultWeight().fillMaxHeight().background(Color(0xFFE9ECEF)).cornerRadius(6.dp),
                contentAlignment = Alignment.Center,
            ) {
                Text(Motor.razlogNeradnog(d) ?: "", style = stil(Siva, 11, FontWeight.Medium))
            }
        } else {
            val casovi = Motor.casoviSmene(d, smena, podaci)
            val termini = Motor.termini(d, smena, podaci)
            val vreme = sada.toLocalTime()
            for (b in 1..SkolskiPodaci.BROJ_CASOVA) {
                val c = casovi.firstOrNull { it.broj == b }
                val t = termini.getOrNull(b - 1)
                val trenutni = jeDanas && t != null && !vreme.isBefore(t.pocetak) && vreme.isBefore(t.kraj)
                Celija(c, trenutni, GlanceModifier.defaultWeight().fillMaxHeight().padding(horizontal = 1.dp))
            }
        }
    }
}

@Composable
private fun Celija(c: CasUDanu?, trenutni: Boolean, modifier: GlanceModifier) {
    if (c == null) {
        Box(modifier) {
            Box(GlanceModifier.fillMaxSize().background(Pozadina).cornerRadius(6.dp)) {}
        }
        return
    }
    Box(modifier) {
        val (razredPozadina, razredBoja) = bojaRazreda(c.odeljenje)
        val pozadina = when {
            trenutni && !c.otkazan -> Plava
            c.izmenjen -> NarandzastaPozadina
            else -> razredPozadina
        }
        val boja = when {
            trenutni && !c.otkazan -> Color.White
            c.izmenjen -> NarandzastaTekst
            else -> razredBoja
        }
        Box(
            GlanceModifier.fillMaxSize().background(pozadina).cornerRadius(6.dp),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                c.odeljenje + "\n" + (if (c.grupa == null) "" else "[${c.grupa}]"),
                style = stil(boja, 10, FontWeight.Bold, precrtano = c.otkazan),
                maxLines = 2,
            )
        }
    }
}
