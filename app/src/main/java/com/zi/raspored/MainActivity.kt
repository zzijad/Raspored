package com.zi.raspored

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import com.zi.raspored.data.IzmeneStore
import com.zi.raspored.logic.Motor
import com.zi.raspored.model.Izmena
import com.zi.raspored.ui.DanScreen
import com.zi.raspored.ui.IzmenaScreen
import com.zi.raspored.ui.NedeljaScreen
import com.zi.raspored.ui.Plava
import com.zi.raspored.ui.PlavaSvetla
import com.zi.raspored.ui.PodesavanjaScreen
import com.zi.raspored.ui.Pozadina
import com.zi.raspored.ui.RasporedTema
import com.zi.raspored.ui.SadaScreen
import com.zi.raspored.ui.Siva
import kotlinx.coroutines.delay
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.temporal.TemporalAdjusters

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        val store = IzmeneStore(this)
        setContent {
            RasporedTema {
                App(store)
            }
        }
    }
}

enum class Ekran(val naslov: String, val ikona: ImageVector) {
    SADA("Sada", Icons.Filled.Home),
    DAN("Dan", Icons.AutoMirrored.Filled.List),
    NEDELJA("Nedelja", Icons.Filled.DateRange),
    PODESAVANJA("Podešavanja", Icons.Filled.Settings),
}

private data class Cilj(val datum: LocalDate, val broj: Int)

private fun radniDan(d: LocalDate): LocalDate =
    if (d.dayOfWeek.value > 5) d.with(TemporalAdjusters.next(DayOfWeek.MONDAY)) else d

@Composable
fun App(store: IzmeneStore) {
    var ekran by rememberSaveable { mutableStateOf(Ekran.SADA) }
    var izmene by remember { mutableStateOf(store.ucitaj()) }
    var sada by remember { mutableStateOf(LocalDateTime.now()) }
    var izabraniDan by remember { mutableStateOf(radniDan(LocalDate.now())) }
    var cilj by remember { mutableStateOf<Cilj?>(null) }

    // osvežava prikaz svakih 10 sekundi
    LaunchedEffect(Unit) {
        while (true) {
            delay(10_000)
            sada = LocalDateTime.now()
        }
    }

    fun postavi(nove: List<Izmena>) {
        izmene = nove
        store.sacuvaj(nove)
    }

    val c = cilj
    if (c != null) {
        BackHandler { cilj = null }
        val original = Motor.casoviZaDan(c.datum, emptyList()).firstOrNull { it.broj == c.broj }
        val postojeca = izmene.firstOrNull { it.datum == c.datum && it.broj == c.broj }
        IzmenaScreen(
            datum = c.datum,
            broj = c.broj,
            original = original,
            postojeca = postojeca,
            onSacuvaj = { nova ->
                postavi(izmene.filterNot { it.datum == nova.datum && it.broj == nova.broj } + nova)
                cilj = null
            },
            onVrati = {
                postavi(izmene.filterNot { it.datum == c.datum && it.broj == c.broj })
                cilj = null
            },
            onNazad = { cilj = null },
        )
    } else {
        Scaffold(
            containerColor = Pozadina,
            bottomBar = {
                NavigationBar(containerColor = Color.White) {
                    Ekran.entries.forEach { e ->
                        NavigationBarItem(
                            selected = ekran == e,
                            onClick = { ekran = e },
                            icon = { Icon(e.ikona, contentDescription = null) },
                            label = { Text(e.naslov) },
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = Plava,
                                selectedTextColor = Plava,
                                indicatorColor = PlavaSvetla,
                                unselectedIconColor = Siva,
                                unselectedTextColor = Siva,
                            ),
                        )
                    }
                }
            },
        ) { padding ->
            Box(Modifier.padding(padding)) {
                val otvoriIzmenu: (LocalDate, Int) -> Unit = { d, b -> cilj = Cilj(d, b) }
                when (ekran) {
                    Ekran.SADA -> SadaScreen(sada, izmene)
                    Ekran.DAN -> DanScreen(izabraniDan, { izabraniDan = it }, sada, izmene, otvoriIzmenu)
                    Ekran.NEDELJA -> NedeljaScreen(izabraniDan, { izabraniDan = it }, sada, izmene, otvoriIzmenu)
                    Ekran.PODESAVANJA -> PodesavanjaScreen(
                        danas = sada.toLocalDate(),
                        izmene = izmene,
                        onObrisi = { iz -> postavi(izmene - iz) },
                        onObrisiStare = {
                            val granica = sada.toLocalDate().minusDays(30)
                            postavi(izmene.filterNot { it.datum.isBefore(granica) })
                        },
                    )
                }
            }
        }
    }
}
