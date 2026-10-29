package com.zi.raspored.widget

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.glance.appwidget.updateAll
import com.zi.raspored.logic.Motor
import com.zi.raspored.model.Podaci
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.ZoneId

/**
 * Osvežava widget na svakom početku i kraju časa (da se pomeri oznaka trenutnog časa)
 * i posle ponoći (novi dan). Ne traži dozvolu za tačne alarme: kašnjenje je do minut.
 */
object Osvezavanje {

    fun zakazi(context: Context, podaci: Podaci, sada: LocalDateTime) {
        val sledece = sledeceVreme(podaci, sada)
        val millis = sledece.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()
        val alarm = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        alarm.setWindow(AlarmManager.RTC, millis, 60_000L, pendingIntent(context))
    }

    /** Prvi sledeći početak ili kraj časa danas, inače malo posle ponoći. */
    fun sledeceVreme(podaci: Podaci, sada: LocalDateTime): LocalDateTime {
        val danas = sada.toLocalDate()
        val vreme = sada.toLocalTime()
        val granice = Motor.casoviZaDan(danas, podaci)
            .flatMap { listOf(it.termin.pocetak, it.termin.kraj) }
            .filter { it.isAfter(vreme) }
            .sorted()
        val prva = granice.firstOrNull()
        return if (prva != null) danas.atTime(prva) else danas.plusDays(1).atTime(LocalTime.of(0, 5))
    }

    private fun pendingIntent(context: Context): PendingIntent =
        PendingIntent.getBroadcast(
            context,
            0,
            Intent(context, OsveziReceiver::class.java),
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT,
        )

    /** Poziva se iz aplikacije kad se promene podaci. */
    suspend fun osveziOdmah(context: Context) {
        RasporedWidget().updateAll(context)
    }
}

class OsveziReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val rezultat = goAsync()
        CoroutineScope(Dispatchers.Default).launch {
            try {
                RasporedWidget().updateAll(context)
            } finally {
                rezultat.finish()
            }
        }
    }
}
