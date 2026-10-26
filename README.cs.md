# Morning Stretch

Fabric mod pro **Minecraft 26.3**, který přidá animaci protahování postavy.

Postava široce rozpaží a zvedne ruce nad ramena, zakloní se a zívne — jako když se člověk protáhne po probuzení.

## Co mod umí

| Situace | Co se stane |
|---|---|
| Probuzení z postele | Postava se automaticky protáhne |
| Stisk klávesy **R** | Protáhne se kdykoliv během hry |
| Multiplayer | Animaci uvidí i ostatní hráči v okolí |

Během protahování:

- postava jde jen rychlostí plížení (jako se Shiftem) a nemůže sprintovat,
- ostatním hráčům zmizí její jmenovka a tečka na locator baru,
- příkazy ji nenajdou (`@a`, `@p`, `@r`, `@e` ani jméno) a její jméno se nenabízí přes Tab.

Animace trvá zhruba **4 sekundy** (80 herních ticků).

Autoři: Matyáš Záruba a Lukáš Záruba.

*English version: [README.md](README.md)*

---

## Pro koho je tento návod

Tento README je psané pro někoho, kdo s Minecraft mody ještě nepracoval. Projdeš vše od instalace až po testování ve hře i vývoj ze zdrojového kódu.

---

## Část 1: Hraní s modem (bez programování)

### Co budeš potřebovat

1. **Minecraft Java Edition 26.3** (ne Bedrock, ne novější verze)
2. **Fabric Loader** — „motor“, který umožní spouštět mody
3. **Fabric API** — knihovna, kterou tento mod vyžaduje
4. **Samotný mod** — soubor `morning-stretch-1.0.0.jar`

### Krok 1: Nainstaluj Fabric

1. Stáhni instalátor z [fabricmc.net/use](https://fabricmc.net/use/)
2. Spusť ho, vyber verzi **Minecraft 26.3**
3. Klikni na **Install**
4. V Minecraft launcheru se ti objeví nový profil **Fabric 26.3**

### Krok 2: Nainstaluj Fabric API

1. Stáhni **Fabric API pro 26.3** z [Modrinth](https://modrinth.com/mod/fabric-api) nebo [CurseForge](https://www.curseforge.com/minecraft/mc-mods/fabric-api)
2. Ujisti se, že verze odpovídá Minecraftu **26.3**

### Krok 3: Nainstaluj tento mod

Máš dvě možnosti:

**A) Stáhnout hotový JAR** (pokud ho někdo sestavil za tebe)

**B) Sestavit ho sám** (viz [Část 2](#část-2-vývoj-a-testování-ze-zdrojového-kódu))

Soubor `morning-stretch-1.0.0.jar` zkopíruj do složky s mody:

| Operační systém | Cesta ke složce `mods` |
|---|---|
| Windows | `%appdata%\.minecraft\mods` |
| macOS | `~/Library/Application Support/minecraft/mods` |
| Linux | `~/.minecraft/mods` |

Složku `mods` vytvoř, pokud neexistuje.

### Krok 4: Spusť hru

1. Otevři Minecraft Launcher
2. Vyber profil **Fabric 26.3**
3. Klikni **Hrát**
4. V hlavním menu jdi do **Mods** (nebo **Modifikace**) — měl bys vidět **Morning Stretch**

Pokud mod v seznamu není, zkontroluj [řešení problémů](#řešení-problémů).

### Krok 5: Jak mod používat ve hře

#### Automatické protahování po spánku

1. Vytvoř nový svět (nebo použij existující)
2. Polož postel a počkej, až bude noc (nebo `/time set night`)
3. Lehni si do postele a přeskoč noc
4. Po probuzení by se postava měla **automaticky protáhnout**

#### Ruční protahování na klávesu

1. Kdykoliv během hry stiskni klávesu **R**
2. Postava se protáhne

Klávesu lze změnit v nastavení:

1. **Options** → **Controls** (Nastavení → Ovládání)
2. Najdi položku **Protáhnout se** (nebo **Stretch** v anglické verzi)
3. Přiřaď jinou klávesu

---

## Část 2: Vývoj a testování ze zdrojového kódu

Tato část je pro tebe, pokud chceš mod upravovat nebo testovat přímo z repozitáře.

### Co budeš potřebovat

| Nástroj | Verze | K čemu slouží |
|---|---|---|
| **Java (JDK)** | 17 nebo novější (doporučeno 25) | Spuštění Gradlu |
| **Git** | libovolná | Stažení zdrojového kódu |
| **IDE** (volitelné) | IntelliJ IDEA doporučeno | Pohodlnější úpravy kódu |

#### Instalace Javy

- Stáhni **JDK 25** z [Adoptium](https://adoptium.net/)
- Ověř instalaci v terminálu:

```bash
java -version
```

Mělo by se zobrazit něco jako `openjdk version "25.x.x"`.

Mod se kompiluje pro **Javu 25**. Pokud ji nemáš nainstalovanou, Gradle si ji při prvním buildu stáhne sám
(do `~/.gradle/jdks`, viz `gradle/gradle-daemon-jvm.properties`). Ke spuštění Gradlu stačí jakákoli Java 17+.

### Stažení projektu

```bash
git clone <url-repozitáře>
cd morning-stretch
```

### Rychlé příkazy přes Make

Projekt obsahuje `Makefile`, který zjednodušuje build, deploy a spuštění:

```bash
make          # zobrazí nápovědu
make build    # sestaví JAR
make deploy   # zkopíruje JAR do složky mods v Minecraftu
make run      # spustí testovací klienta s modem
make all      # build + deploy + run
make play     # deploy + otevře Minecraft Launcher
```

| Příkaz | Co dělá |
|---|---|
| `make build` | Sestaví `build/libs/morning-stretch-1.0.0.jar` |
| `make deploy` | Zkopíruje JAR do `~/Library/Application Support/minecraft/mods` (macOS) |
| `make run` | Spustí `./gradlew runClient` — mod se načte automaticky, deploy není nutný |
| `make all` | Nejdřív deployne do `.minecraft/mods`, pak spustí testovacího klienta |
| `make play` | Deployne mod a otevře běžný Minecraft Launcher (profil Fabric 26.3) |

Vlastní cesta k Minecraftu:

```bash
make deploy MINECRAFT_DIR="/cesta/k/minecraft"
```

**Tip pro vývoj:** Stačí `make run`. Gradle spustí izolovanou testovací instanci — nemusíš nic kopírovat do `.minecraft/mods`. Deploy (`make deploy`) použij, až chceš mod vyzkoušet v normální hře přes launcher.

### Spuštění testovacího klienta

Nejjednodušší způsob, jak mod vyzkoušet bez ruční instalace do `.minecraft`:

```bash
./gradlew runClient
# nebo
make run
```

**Co se stane:**
- Gradle stáhne Minecraft, Fabric a závislosti (první spuštění trvá několik minut)
- Automaticky se spustí Minecraft s modem
- Vytvoří se izolovaná testovací instance — nic se nepřepisuje v tvé běžné hře

Na Windows použij místo `./gradlew` příkaz:

```bash
gradlew.bat runClient
```

### Sestavení JAR souboru

```bash
./gradlew build
```

Hotový mod najdeš v:

```
build/libs/morning-stretch-1.0.0.jar
```

Tento soubor pak zkopíruj do složky `mods` (viz [Krok 3](#krok-3-nainstaluj-tento-mod)).

### Otevření v IntelliJ IDEA (volitelné)

1. Spusť IntelliJ IDEA
2. **Open** → vyber složku `morning-stretch`
3. Otevři soubor `build.gradle` a potvrď import Gradle projektu
4. Počkej, až Gradle stáhne závislosti
5. Vpravo nahoře by se měla objevit run konfigurace **Minecraft Client**

---

## Část 3: Jak mod otestovat (kontrolní seznam)

Projdi tyto scénáře a ověř, že vše funguje.

### Test 1: Mod se načte

- [ ] Spusť hru s Fabric profilem
- [ ] V menu **Mods** vidíš **Morning Stretch**
- [ ] Verze je **1.0.0**

### Test 2: Ruční protahování

- [ ] Vytvoř svět (Creative nebo Survival)
- [ ] Stiskni **R**
- [ ] Postava rozpaží a zvedne ruce nad ramena
- [ ] Animace trvá cca 4 sekundy a pak skončí
- [ ] Slyšíš tichý zvuk dechu

### Test 3: Protahování po spánku

- [ ] Polož postel
- [ ] Nastav noc: `/time set night`
- [ ] Lehni a přeskoč noc
- [ ] Po vstání z postele se postava **sama** protáhne

### Test 4: Opakované použití

- [ ] Stiskni **R** několikrát za sebou
- [ ] Animace se spustí znovu až po dokončení předchozí
- [ ] Mod nespadne a nezamrzne hra

### Test 5: Změna klávesy

- [ ] Jdi do **Nastavení → Ovládání**
- [ ] Najdi **Protáhnout se** a přiřaď jinou klávesu (např. **Y**)
- [ ] Nová klávesa funguje, stará **R** už ne

### Test 6: Multiplayer (volitelné)

- [ ] Spusť lokální svět a otevři ho pro LAN
- [ ] Připoj se druhým klientem
- [ ] Když jeden hráč stiskne protahovací klávesu, druhý hráč vidí animaci

---

### Test bez druhého hráče

Ve vývojovém klientovi (`make run`) je k dispozici příkaz `/stretchtest`:

- `/stretchtest spawn` postaví před tebe mannequina, který se každých 10 sekund protáhne,
- `/stretchtest remove` všechny mannequiny odstraní.

Na něm ověříš skrytí jmenovky, locator baru i selektorů v příkazech. Ve vydaném modu tento příkaz není.

---

## Vydání nové verze

1. Změny zapiš do [CHANGELOG.md](CHANGELOG.md) pod **Unreleased** a dostaň je do `main`.
2. Na GitHubu spusť workflow **release** (*Actions → release → Run workflow*) a vyber, kterou část verze zvýšit (`patch`, `minor`, `major`), nebo zadej přesnou verzi.

Workflow vezme `main`, zvýší `mod_version` v `gradle.properties`, z Unreleased udělá sekci nové verze, vytvoří release commit s tagem `v<verze>` a publikuje mod na Modrinth a do GitHub Releases. Potřebuje secret `MODRINTH_TOKEN` (viz anglický README).

---

## Struktura projektu

```
morning-stretch/
├── src/main/java/cz/lzaruba/minecraft/morningstretch/
│   ├── MorningStretchMod.java       # hlavní vstupní bod (server + společná logika)
│   ├── network/                     # síťové pakety mezi klientem a serverem
│   └── stretch/                     # logika protahování na serveru
├── src/client/java/cz/lzaruba/minecraft/morningstretch/client/
│   ├── MorningStretchClient.java    # klávesová zkratka, client síť
│   ├── stretch/                     # stav animace na klientovi
│   └── mixin/                       # úprava vzhledu postavy (pozice rukou)
├── src/main/resources/
│   ├── fabric.mod.json              # metadata modu
│   └── assets/morning-stretch/lang/ # překlady (čeština, angličtina)
├── build.gradle                     # build konfigurace
└── gradle.properties                # verze Minecraftu, Fabric API atd.
```

### Jak to funguje uvnitř (zjednodušeně)

1. **Server** rozhodne, že hráč se má protáhnout (probudil se / stiskl klávesu)
2. **Server** pošle zprávu všem hráčům, kteří ho vidí
3. **Klient** spustí časovač animace
4. **Mixin** při vykreslování postavy upraví úhel rukou a hlavy

---

## Řešení problémů

### Mod se nezobrazuje v seznamu modů

- Ověř, že hraješ na profilu **Fabric 26.3** (ne vanilla)
- Zkontroluj, že máš nainstalované **Fabric API**
- Ujisti se, že JAR je ve složce `mods`, ne v podsložce
- Verze modu musí odpovídat Minecraftu **26.3**

### Hra spadne při startu

- Chybí **Fabric API** — nejčastější příčina
- Špatná verze Javy — mod vyžaduje **Java 25+** (oficiální launcher ji pro 26.3 dodává sám)
- Konflikt s jiným modem — zkus spustit jen Fabric API + Morning Stretch

### `./gradlew runClient` nefunguje

- Nemáš nainstalovanou Javu 17+ (`java -version`), nebo se nepodařilo stáhnout JDK 25 — zkontroluj připojení k internetu
- První build trvá dlouho — počkej, až Gradle stáhne vše
- Na macOS/Linux dej skriptu práva: `chmod +x gradlew`

### Klávesa R nic nedělá

- Nejsi ve hře (musíš být ve světě, ne v menu)
- Klávesa může být přeobsazená jiným modem — změň ji v ovládání
- V multiplayeru musíš být připojený k serveru (ne jen v offline menu)

### Po probuzení se postava neprotáhne

- Musíš se opravdu **probudit z postele** (ne jen vstát bez spaní)
- Zkus `/time set night`, pak spát a přeskočit noc

---

## Technické informace

| Položka | Hodnota |
|---|---|
| Minecraft | 26.3 |
| Mod loader | Fabric |
| Fabric Loader | 0.20.1+ |
| Fabric API | 0.165.0+26.3 |
| Java | 25+ |
| Mod ID | `morning-stretch` |
| Výchozí klávesa | `R` |
| Délka animace | 80 ticků (~4 s) |
| Licence | LGPL-3.0-or-later |

---

## Licence

GNU Lesser General Public License v3.0 (**LGPL-3.0-or-later**).

Mod můžeš volně používat, upravovat i šířit. Pokud ale rozšíříš nebo upravíš
samotný kód modu a takovou verzi budeš distribuovat, musíš zdrojový kód těchto
úprav zpřístupnit pod stejnou licencí.

Viz [LICENSE](LICENSE) (GPL-3.0) a [LICENSE.LESSER](LICENSE.LESSER) (dodatek LGPL-3.0).
