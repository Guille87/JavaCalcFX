# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Commands

- Run the app: `mvn clean javafx:run`
- Debug (attach on `localhost:8000`, JVM suspends until attached): `mvn clean javafx:run@debug`
- Run tests: `mvn clean test`
- Single test class / method: `mvn test -Dtest=CalculadoraTest` or `-Dtest=CalculadoraTest#terna_3_4_5`.
  `CalculadoraTest` groups cases in `@Nested` classes (`TrianguloRectangulo`, `AreaCilindro`,
  `AnioBisiesto`, `Factorial`, `Multiplos`, `Notas`), so a nested method needs the enclosing
  class: `-Dtest='CalculadoraTest$Factorial#rechaza_negativos'`.
  Other test classes: `i18n/TextosTest`, `i18n/IdiomaTest`, in `ui` `FormatoTest`,
  `MensajesDeErrorTest`, `EntradaTest`, `FiltroNumericoTest`, and `InterfazTest` (TestFX).
- `InterfazTest` drives the real UI. It runs headless via Monocle (surefire `argLine` in the
  POM, plus `useModulePath=false` so TestFX isn't on the module path); `mvn test -Pheaded`
  shows a window. No display or xvfb needed in CI.

No linter is configured. `.github/workflows/ci.yml` runs `mvn -B clean test` on JDK 17 for
every push and pull request (the UI tests run there headless too).

## Requirements

- JDK 17+ (`maven.compiler.release` = 17). JavaFX 17 modules come from Maven (`org.openjfx`); no separate SDK install needed.

## Architecture

Single-module JavaFX desktop app in four packages: `calc` (pure domain), `i18n` (translatable
text), `ui` (reusable interface infrastructure), and the root package (the `Application` and
its screen catalog).

- **`calc/Calculadora.java`** — all mathematics as static, JavaFX-free, precondition-checked
  functions (`resolverTrianguloRectangulo`, `areaCilindro`, `esBisiesto`, `factorial`,
  `esMultiplo`, `media` (grades in `[NOTA_MINIMA, NOTA_MAXIMA]` = 0..10), `estaAprobado`).
  Invalid input throws `IllegalArgumentException` whose message comes from `Textos`.
  Unit-tested by `CalculadoraTest`.
- **`i18n/`** — `Textos` reads the `messages*.properties` files directly (not via
  `ResourceBundle`, whose lookup mixes in `Locale.getDefault()` and would return the wrong
  language on a machine whose default locale differs): `messages.properties` is Spanish and
  the base; a non-Spanish `Idioma` loads its `messages_<lang>.properties` on top, falling
  back to the base for missing keys. `Textos.get(key)` / `Textos.get(key, args...)` (the
  latter via `MessageFormat` — a literal `'` in a parametrized value must be doubled).
  `seleccionar(Idioma)` switches the language and persists it via `java.util.prefs`;
  `idioma()` reads the current one; `usarIdioma(Locale)` switches without persisting
  (tests). `Idioma` is the two-value enum (`ESPANOL` / `INGLES`) behind the menu's language
  `ComboBox`. All user-visible strings go through `Textos`.
- **`ui/` infrastructure** — small single-responsibility pieces:
  - `Navegador` — owns the root `StackPane` (always one child); `mostrar(Node)` swaps the
    screen and first runs an `alNavegar` hook (wired to cancel the in-flight calculation).
  - `CalculosAsync` — owns the single daemon-thread `ExecutorService` and the current
    `Task<String>`. `ejecutar(calculo, alEmpezar, alTerminar, alFallar)` runs work off the
    FX thread; `cancelar()` interrupts it; `cerrar()` (called from `Application.stop()`)
    shuts the executor down. Only one calculation runs at a time.
  - `ConstructorDeFormularios` — builds the generic form screen (bold header, wrapped
    instructions, one `TextField` per prompt with its `FiltroNumerico`, Enter-default
    «Calcular», wrapped result label, «Volver» that also fires on Esc via a `KEY_PRESSED`
    filter on the screen root). Wrapped in a transparent `ScrollPane` so a small window
    scrolls instead of clipping.
  - `MensajesDeError` — pure `Throwable → String` mapping (`NumberFormatException` /
    `IllegalArgumentException` / `ArithmeticException` / cancellation). Unit-tested.
  - `Formato` — pure number-to-text formatting (thread-safe: a fresh `DecimalFormat` per
    call). Unit-tested.
  - `Entrada` — the single text→number parsing seam. Unit-tested.
  - `FiltroNumerico` — installs a `TextFormatter` that keeps fields to numeric text while
    typing, in two variants (`Tipo.ENTERO` / `Tipo.DECIMAL`); `esValido` is a pure prefix
    check. Unit-tested. Each `pantallaX()` passes the `Tipo` for its fields.
  - `Botones` — button factory (`crear(texto, accion)` / `crear(texto, tooltip, accion)`).
- **`SelectorDeOpciones.java`** — thin `Application`: wires `Navegador` + `CalculosAsync` +
  `ConstructorDeFormularios`, loads `styles.css`, sets a minimum window size, builds the
  menu (title, 6-button grid via `botonMenu(clave, accion)` with per-button tooltips, and a
  top-right language `ComboBox` that calls `Textos.seleccionar(...)` and rebuilds the menu),
  and defines one `pantallaX()` per calculator (each declares its title, prompts, field
  `Tipo` and display function). `stop()` delegates to `calculos.cerrar()`.
- `Calculadora.factorial` polls `Thread.isInterrupted()` so a cancelled long computation
  aborts promptly.

To add a calculator: add a pure method to `Calculadora` (with a test); add its strings to
both `messages*.properties`; add a `pantallaX()` that calls `formularios.mostrar(titulo,
instrucciones, prompts, FiltroNumerico.Tipo, calculo)` with everything resolved via
`Textos.get(...)`; and a `Botones.crear(...)` entry in `mostrarMenu()`.

Identifiers and comments are in Spanish (keep that convention); user-visible strings live in
`messages*.properties`. `TextosTest` guards that the two bundles have identical keys and
that every parametrized value is a valid `MessageFormat` pattern.
