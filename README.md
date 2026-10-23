# Raspored

Android aplikacija za lični školski raspored: trenutni i sledeći čas, smene, školski kalendar 2026/2027. i jednokratne izmene.

## Ekrani
- **Sada** – čas koji traje sa odbrojavanjem do zvona, sledeći čas, današnji dan, preostali nastavni dani i sledeća promena smene.
- **Dan** – izbor dana u nedelji, svih 7 termina; dodir na čas otvara izmenu.
- **Nedelja** – tabela rasporeda za izabranu nedelju (sa izmenama i neradnim danima).
- **Podešavanja** – smena, kraj nastave, lista izmena (brisanje).

## Gde se menjaju podaci
`app/src/main/java/com/zi/raspored/model/SkolskiPodaci.kt` – raspored, satnica smena, polugodišta i neradni dani.
Pravilo smena je u `logic/Motor.kt` (`smena()`).

## Build
Svaki `git push` na `main` pokreće GitHub Actions: testovi + `Raspored.apk` (Actions → poslednji build → Artifacts).
Tag `v*` (npr. `git tag v0.1 && git push --tags`) pravi i GitHub Release sa APK-om.
