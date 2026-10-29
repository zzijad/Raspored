package com.zi.raspored

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import com.zi.raspored.data.Kodek
import com.zi.raspored.data.Skladiste
import com.zi.raspored.logic.Motor
import com.zi.raspored.model.Cas
import com.zi.raspored.model.Podaci
import com.zi.raspored.model.SMENA_A
import com.zi.raspored.ui.DanScreen
import com.zi.raspored.ui.IzborCasaScreen
import com.zi.raspored.ui.IzmenaScreen
import com.zi.raspored.ui.NedeljaScreen
import com.zi.raspored.ui.PocetakScreen
import com.zi.raspored.ui.PodesavanjaScreen
import com.zi.raspored.ui.Plava
import com.zi.raspored.ui.PlavaSvetla
import com.zi.raspored.ui.Pozadina
import com.zi.raspored.ui.RasporedTema
import com.zi.raspored.ui.SadaScreen
import com.zi.raspored.ui.SatnicaScreen
import com.zi.raspored.ui.Siva
import com.zi.raspored.ui.SmeneScreen
import com.zi.raspored.ui.UnosScreen
import com.zi.raspored.ui.smenaSlovo
import com.zi.raspored.widget.Osvezavanje
import kotlinx.coroutines.launch
import kotlinx.coroutines.delay
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.temporal.TemporalAdjusters

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        val skladiste = Skladiste(this)
        setContent {
            RasporedTema {
                App(skladiste)
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

/** Ekrani preko kojih se navigira (stek). */
sealed interface Nav {
    data object Glavno : Nav
    data object Pocetak : Nav
    data class Smene(val onboarding: Boolean) : Nav
    data class Unos(val onboarding: Boolean) : Nav
    data class IzborCasa(val dan: Int, val broj: Int, val smena: Int) : Nav
    data class Izmena(val datum: LocalDate, val broj: Int, val smena: Int) : Nav
    data object Satnica : Nav
}

private fun radniDan(d: LocalDate): LocalDate =
    if (d.dayOfWeek.value > 5) d.with(TemporalAdjusters.next(DayOfWeek.MONDAY)) else d

@Composable
fun App(skladiste: Skladiste) {
    val context = LocalContext.current
    var podaci by remember { mutableStateOf(skladiste.ucitaj()) }
    val stek = remember { mutableStateListOf<Nav>(if (podaci.postavke == null) Nav.Pocetak else Nav.Glavno) }
    var ekran by rememberSaveable { mutableStateOf(Ekran.SADA) }
    var sada by remember { mutableStateOf(LocalDateTime.now()) }
    var izabraniDan by remember { mutableStateOf(radniDan(LocalDate.now())) }
    var unosTab by remember { mutableStateOf(SMENA_A) }
    var nedavno by remember { mutableStateOf(listOf<Pair<String, Int?>>()) }

    // osvežava prikaz svakih 10 sekundi
    LaunchedEffect(Unit) {
        while (true) {
            delay(10_000)
            sada = LocalDateTime.now()
        }
    }

    val scope = rememberCoroutineScope()

    fun postavi(novi: Podaci) {
        podaci = novi
        skladiste.sacuvaj(novi)
        // widget odmah prikazuje nove podatke
        scope.launch { runCatching { Osvezavanje.osveziOdmah(context) } }
    }

    fun idi(n: Nav) {
        stek.add(n)
    }

    fun nazad() {
        if (stek.size > 1) stek.removeAt(stek.lastIndex)
    }

    fun resetuj(n: Nav) {
        stek.clear()
        stek.add(n)
    }

    fun poruka(tekst: String) {
        Toast.makeText(context, tekst, Toast.LENGTH_SHORT).show()
    }

    val izvoz = rememberLauncherForActivityResult(ActivityResultContracts.CreateDocument("application/json")) { uri ->
        if (uri != null) {
            val uspeh = runCatching {
                context.contentResolver.openOutputStream(uri)?.use { it.write(Kodek.uJson(podaci).toByteArray()) }
            }.isSuccess
            poruka(if (uspeh) "Kopija je sačuvana" else "Kopija nije sačuvana")
        }
    }

    val uvoz = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
        if (uri != null) {
            val ucitano = runCatching {
                context.contentResolver.openInputStream(uri)?.use { Kodek.izJsona(it.readBytes().decodeToString()) }
            }.getOrNull()
            if (ucitano?.postavke != null) {
                postavi(ucitano)
                ekran = Ekran.SADA
                resetuj(Nav.Glavno)
                poruka("Kopija je učitana")
            } else {
                poruka("Ovaj fajl nije rezervna kopija rasporeda")
            }
        }
    }

    BackHandler(enabled = stek.size > 1) { nazad() }

    when (val n = stek.last()) {
        Nav.Pocetak -> PocetakScreen(
            onUnos = { idi(Nav.Smene(onboarding = true)) },
            onUcitaj = { uvoz.launch(arrayOf("*/*")) },
        )

        is Nav.Smene -> SmeneScreen(
            pocetne = podaci.postavke,
            onboarding = n.onboarding,
            onNazad = { nazad() },
            onSacuvaj = { nove ->
                postavi(Motor.primeniPostavke(podaci, nove))
                if (n.onboarding) idi(Nav.Unos(onboarding = true)) else nazad()
            },
        )

        is Nav.Unos -> UnosScreen(
            podaci = podaci,
            onboarding = n.onboarding,
            tab = unosTab,
            onTab = { unosTab = it },
            onCelija = { dan, broj, smena -> idi(Nav.IzborCasa(dan, broj, smena)) },
            onNazad = { nazad() },
            onGotovo = {
                if (n.onboarding) {
                    ekran = Ekran.SADA
                    resetuj(Nav.Glavno)
                } else {
                    nazad()
                }
            },
        )

        is Nav.IzborCasa -> {
            val smena = podaci.postavke?.smena(n.smena)
            if (smena == null) {
                LaunchedEffect(n) { nazad() }
            } else {
                val postojeci = podaci.raspored.firstOrNull { it.dan == n.dan && it.broj == n.broj && it.smena == n.smena }
                fun bezOvog() = podaci.raspored.filterNot { it.dan == n.dan && it.broj == n.broj && it.smena == n.smena }
                IzborCasaScreen(
                    dan = n.dan,
                    broj = n.broj,
                    oznakaSmene = if (podaci.postavke?.b != null) "smena ${smenaSlovo(n.smena)}" else null,
                    smena = smena,
                    postojeci = postojeci,
                    nedavno = nedavno,
                    onSacuvaj = { od, gr ->
                        postavi(podaci.copy(raspored = bezOvog() + Cas(n.dan, n.broj, n.smena, od, gr)))
                        nedavno = (listOf(od to gr) + nedavno).distinct().take(6)
                        nazad()
                    },
                    onIsprazni = {
                        postavi(podaci.copy(raspored = bezOvog()))
                        nazad()
                    },
                    onNazad = { nazad() },
                )
            }
        }

        is Nav.Izmena -> {
            val post = podaci.postavke
            val smena = post?.smena(n.smena)
            if (post == null || smena == null) {
                LaunchedEffect(n) { nazad() }
            } else {
                val original = Motor.casoviSmene(n.datum, n.smena, podaci.copy(izmene = emptyList()))
                    .firstOrNull { it.broj == n.broj }
                val postojeca = podaci.izmene.firstOrNull { it.datum == n.datum && it.broj == n.broj && it.smena == n.smena }
                fun bezOve() = podaci.izmene.filterNot { it.datum == n.datum && it.broj == n.broj && it.smena == n.smena }
                IzmenaScreen(
                    datum = n.datum,
                    broj = n.broj,
                    smenaIndeks = n.smena,
                    oznakaSmene = if (post.b != null) "smena ${smenaSlovo(n.smena)}" else null,
                    smena = smena,
                    termin = Motor.termini(n.datum, n.smena, podaci)[n.broj - 1],
                    original = original,
                    postojeca = postojeca,
                    onSacuvaj = { nova ->
                        postavi(podaci.copy(izmene = bezOve() + nova))
                        nazad()
                    },
                    onVrati = {
                        postavi(podaci.copy(izmene = bezOve()))
                        nazad()
                    },
                    onNazad = { nazad() },
                )
            }
        }

        Nav.Satnica -> SatnicaScreen(
            satnica = podaci.satnica,
            onSacuvaj = { nova ->
                postavi(podaci.copy(satnica = nova))
                nazad()
            },
            onNazad = { nazad() },
        )

        Nav.Glavno -> GlavniEkran(
            ekran = ekran,
            onEkran = { ekran = it },
            sada = sada,
            podaci = podaci,
            izabraniDan = izabraniDan,
            onDan = { izabraniDan = it },
            onIzmena = { d, b, s -> idi(Nav.Izmena(d, b, s)) },
            onSmene = { idi(Nav.Smene(onboarding = false)) },
            onRaspored = { idi(Nav.Unos(onboarding = false)) },
            onSatnica = { idi(Nav.Satnica) },
            onIzvoz = { izvoz.launch("raspored-kopija.json") },
            onUvoz = { uvoz.launch(arrayOf("*/*")) },
            onObrisiSve = {
                postavi(Skladiste.prazno())
                resetuj(Nav.Pocetak)
            },
            onObrisiIzmenu = { iz -> postavi(podaci.copy(izmene = podaci.izmene - iz)) },
            onObrisiStare = {
                val granica = sada.toLocalDate().minusDays(30)
                postavi(podaci.copy(izmene = podaci.izmene.filterNot { it.datum.isBefore(granica) }))
            },
        )
    }
}

@Composable
private fun GlavniEkran(
    ekran: Ekran,
    onEkran: (Ekran) -> Unit,
    sada: LocalDateTime,
    podaci: Podaci,
    izabraniDan: LocalDate,
    onDan: (LocalDate) -> Unit,
    onIzmena: (LocalDate, Int, Int) -> Unit,
    onSmene: () -> Unit,
    onRaspored: () -> Unit,
    onSatnica: () -> Unit,
    onIzvoz: () -> Unit,
    onUvoz: () -> Unit,
    onObrisiSve: () -> Unit,
    onObrisiIzmenu: (com.zi.raspored.model.Izmena) -> Unit,
    onObrisiStare: () -> Unit,
) {
    Scaffold(
        containerColor = Pozadina,
        bottomBar = {
            NavigationBar(containerColor = Color.White) {
                Ekran.entries.forEach { e ->
                    NavigationBarItem(
                        selected = ekran == e,
                        onClick = { onEkran(e) },
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
            when (ekran) {
                Ekran.SADA -> SadaScreen(sada, podaci)
                Ekran.DAN -> DanScreen(izabraniDan, onDan, sada, podaci, onIzmena)
                Ekran.NEDELJA -> NedeljaScreen(izabraniDan, onDan, sada, podaci, onIzmena)
                Ekran.PODESAVANJA -> PodesavanjaScreen(
                    danas = sada.toLocalDate(),
                    podaci = podaci,
                    onSmene = onSmene,
                    onRaspored = onRaspored,
                    onSatnica = onSatnica,
                    onIzvoz = onIzvoz,
                    onUvoz = onUvoz,
                    onObrisiSve = onObrisiSve,
                    onObrisi = onObrisiIzmenu,
                    onObrisiStare = onObrisiStare,
                )
            }
        }
    }
}
