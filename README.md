# Raspored

Android aplikacija za lični školski raspored: trenutni i sledeći čas, smene, školski kalendar 2026/2027. i jednokratne izmene.
Svaki nastavnik pri prvom pokretanju sam unosi svoja odeljenja i prepisuje raspored.

## Prvo pokretanje
1. **Smene i odeljenja** – štikliraju se odeljenja (V–VIII × 1–8), označe ona sa grupama i izabere da li smena radi pre ili posle podne u septembru. Ko radi u obe smene doda smenu B (uvek suprotna od A).
2. **Unos rasporeda** – dodir na polje → izbor odeljenja (i grupe, samo gde postoje). Vreme časova se računa samo.

Ili: **Učitaj rezervnu kopiju** (JSON fajl).

## Ekrani
- **Sada** – čas koji traje sa odbrojavanjem, sledeći čas, današnji dan, preostali nastavni dani.
- **Dan** – svi termini izabranog dana; dodir na čas = jednokratna izmena (zameni / otkaži / dodaj).
- **Nedelja** – tabela izabrane nedelje.
- **Podešavanja** – odeljenja i smene, osnovni raspored, satnica zvona, rezervna kopija, lista izmena.

## Pravilo smena
Smene se menjaju svakog meseca; odlučuje mesec u kome je ponedeljak te nedelje (`logic/Motor.kt`, `kaoUSeptembru()`).

## Školski kalendar
`model/SkolskiPodaci.kt` – polugodišta, neradni dani i podrazumevana satnica. Za novu školsku godinu menjaju se ovde.

## Build
Svaki `git push` na `main` pokreće GitHub Actions: testovi + `Raspored.apk` (Actions → poslednji build → Artifacts).
Tag `v*` (npr. `git tag v0.2 && git push --tags`) pravi i GitHub Release sa APK-om.
