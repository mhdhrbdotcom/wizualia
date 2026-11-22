# Wizualia – mod wizualny Fabric 1.21.4 (po polsku)

Oryginalny mod napisany od zera. Nie zawiera kodu ani zasobów z żadnych cudzych modów.

## Sterowanie
- **B** – otwiera / zamyka menu
- **C** – zoom (przytrzymaj, gdy moduł Zoom jest włączony)
- **LPM** na module – włącz/wyłącz, **PPM** – rozwiń ustawienia
- **Ctrl+F** – wyszukiwarka modułów
- Panele przeciągasz za nagłówek. Ustawienia zapisują się w `config/wizualia.properties`.

## Moduły
**Widok:** Rozjaśnienie, Zoom, Własne FOV, Model ręki, Własny celownik (3 style)
**HUD:** Znak wodny, Lista modułów, Panel informacji (FPS, ping, XYZ, kierunek, prędkość, CPS, zegar),
Klawisze WASD, Pancerz, Efekty mikstur, Wskaźnik celu, Licznik totemów
**Klient:** Motyw kolorystyczny (7 kolorów + tęcza), Wygląd menu (przyciemnienie, zaokrąglenie, animacje)

## Budowanie .jar

### Sposób 1 – GitHub Actions (bez instalowania czegokolwiek)
1. Utwórz repozytorium na GitHubie i wrzuć do niego zawartość tego folderu.
2. Wejdź w zakładkę **Actions** → zakończony build → pobierz artefakt `wizualia-jar`.
3. Wrzuć `wizualia-1.0.0.jar` do `.minecraft/mods` razem z Fabric API 1.21.4.

### Sposób 2 – lokalnie
1. Zainstaluj Javę 21 i Gradle 8.12+ (lub skopiuj `gradlew`, `gradlew.bat` i folder `gradle/`
   z https://github.com/FabricMC/fabric-example-mod).
2. W folderze projektu: `gradle build` (albo `./gradlew build`).
3. Jar znajdziesz w `build/libs/wizualia-1.0.0.jar`.
