# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Commands

- Run the app: `mvn clean javafx:run`
- Debug (attach on `localhost:8000`, JVM suspends until attached): `mvn clean javafx:run@debug`
- Run tests: `mvn clean test`
- Single test class / method: `mvn test -Dtest=FormatoTest` or `-Dtest=FormatoTest#numero_entero_sin_decimales`.
  Every method in `CalculadoraTest` lives in a `@Nested` class (`TrianguloRectangulo`,
  `AreaCilindro`, `AnioBisiesto`, `Factorial`, `Multiplos`, `Notas`), so selecting one needs
  the enclosing class: `-Dtest='CalculadoraTest$Factorial#rechaza_negativos'`.
  Other test classes: `i18n/TextosTest`, `i18n/IdiomaTest`, in `ui` `FormatoTest`,
  `MensajesDeErrorTest`, `EntradaTest`, `FiltroNumericoTest`, `EstadoVentanaTest`, and
  `InterfazTest` (TestFX).
- `InterfazTest` drives the real UI. It runs headless via Monocle (surefire `argLine` in the
  POM, plus `useModulePath=false` so TestFX isn't on the module path); `mvn test -Pheaded`
  shows a window. No display or xvfb needed in CI.

- Formatting: `mvn spotless:apply` reformats (palantir-java-format, 120 cols); `mvn
  spotless:check` verifies. Run `apply` before committing.

`.github/workflows/ci.yml` runs `spotless:check` then `mvn -B clean test` on JDK 17 for
every push and pull request (the UI tests run there headless too). No other linter.

## Requirements

- JDK 17+ (`maven.compiler.release` = 17). JavaFX 17 modules come from Maven (`org.openjfx`); no separate SDK install needed.

## Architecture

Single-module JavaFX desktop app in four packages: `calc` (pure domain), `i18n` (translatable
text), `ui` (reusable interface infrastructure), and the root package (the `Application` and
its screen catalog).

- **`calc/Calculadora.java`** — all mathematics as static, JavaFX-free, precondition-checked
  functions (`resolverTrianguloRectangulo`, `areaCilindro`, `esBisiesto`, `factorial`,
  `esMultiplo`, `media` (grades in `[NOTA_MINIMA, NOTA_MAXIMA]` = 0..10), `estaAprobado`,
  `resolverEcuacionCuadratica`, `potencia`, `raiz` (n-th root, handles odd roots of
  negatives, Newton-refined so exact roots come out exact), `mcd`, `mcm` (`Math.absExact`
  / `multiplyExact` guarded), `analizarPrimalidad` (trial division to √n, interruptible;
  returns the smallest proper divisor for composites)). Returns immutable records
  (`Triangulo`, `EcuacionCuadratica` with `Raiz`, `Primalidad`). Invalid input throws
  `IllegalArgumentException` whose message comes from `Textos`. Unit-tested by
  `CalculadoraTest`.
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
    scrolls instead of clipping. Focuses the first field on open. `mostrar(...)` has an
    overload taking an extra `pasos` function: after a successful calc a «Mostrar pasos»
    button reveals its step-by-step development.
  - `MensajesDeError` — pure `Throwable → String` mapping (`NumberFormatException` /
    `IllegalArgumentException` / `ArithmeticException` / cancellation). Unit-tested.
  - `Formato` — pure number-to-text formatting (thread-safe: a fresh `DecimalFormat` per
    call). Unit-tested.
  - `Entrada` — the single text→number parsing seam. Unit-tested.
  - `FiltroNumerico` — installs a `TextFormatter` that keeps fields to numeric text while
    typing, in two variants (`Tipo.ENTERO` / `Tipo.DECIMAL`); `esValido` is a pure prefix
    check. Unit-tested. Each `pantallaX()` passes the `Tipo` for its fields.
  - `Botones` — button factory (`crear(texto, accion)` / `crear(texto, tooltip, accion)`).
  - `EstadoVentana` — persists window size/position via `java.util.prefs`; `restaurar(stage)`
    before `show()`, `vigilar(stage)` after. Discards sizes below the minimum or a position
    off every screen. Pure checks (`tamanoValido`, `puntoVisible`) are unit-tested.
  - `PasoAPasoCuadratica` — pure, deterministic templates that render the classic quadratic
    formula step by step in linear notation (`(4 ± √(16 - 16)) / 8`). Unit-tested. The
    prototype for the "step by step" feature (ROADMAP Fase 5).
- **`SelectorDeOpciones.java`** — thin `Application`: wires `Navegador` + `CalculosAsync` +
  `ConstructorDeFormularios`, loads `styles.css` and the window icons (`resources/.../icons/`),
  sets a minimum window size, builds the menu from `catalogo()` — a `List<Categoria>`, each a
  `menu.categoria.<clave>` heading over a `FlowPane` of `botonMenu(clave, accion)` buttons —
  plus a top-right language `ComboBox` that calls `Textos.seleccionar(...)` and rebuilds the
  menu. Each calculator is one `pantallaX()` (declares title, prompts, field `Tipo`, display
  function) and one `EntradaMenu` in `catalogo()`. `stop()` delegates to `calculos.cerrar()`.
- `Calculadora.factorial` polls `Thread.isInterrupted()` so a cancelled long computation
  aborts promptly.

To add a calculator: add a pure method to `Calculadora` (with a test); add its strings to
both `messages*.properties` (including `menu.boton.<clave>` and `.tooltip`); add a
`pantallaX()` that calls `formularios.mostrar(titulo, instrucciones, prompts,
FiltroNumerico.Tipo, calculo)` with everything resolved via `Textos.get(...)`; and an
`EntradaMenu("<clave>", this::pantallaX)` in the right `Categoria` of `catalogo()`.

Identifiers and comments are in Spanish (keep that convention); user-visible strings live in
`messages*.properties`. `TextosTest` guards that the two bundles have identical keys and
that every parametrized value is a valid `MessageFormat` pattern.
