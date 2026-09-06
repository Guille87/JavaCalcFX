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
  Other test classes: `i18n/TextosTest` and, in `ui`, `FormatoTest`, `MensajesDeErrorTest`,
  `EntradaTest`, `FiltroNumericoTest`.

No linter is configured. `.github/workflows/ci.yml` runs `mvn -B clean test` on JDK 17 for
every push and pull request.

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
- **`i18n/Textos.java`** — loads `ResourceBundle` `messages` for `Locale.getDefault()`
  (`messages.properties` is Spanish and the fallback; `messages_en.properties` is English).
  `Textos.get(key)` / `Textos.get(key, args...)` (the latter via `MessageFormat` — a literal
  `'` in a parametrized value must be doubled). `usarIdioma(Locale)` switches at runtime
  (tests use it). All user-visible strings go through here.
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
    filter on the screen root).
  - `MensajesDeError` — pure `Throwable → String` mapping (`NumberFormatException` /
    `IllegalArgumentException` / `ArithmeticException` / cancellation). Unit-tested.
  - `Formato` — pure number-to-text formatting (thread-safe: a fresh `DecimalFormat` per
    call). Unit-tested.
  - `Entrada` — the single text→number parsing seam. Unit-tested.
  - `FiltroNumerico` — installs a `TextFormatter` that keeps fields to numeric text while
    typing, in two variants (`Tipo.ENTERO` / `Tipo.DECIMAL`); `esValido` is a pure prefix
    check. Unit-tested. Each `pantallaX()` passes the `Tipo` for its fields.
  - `Botones` — button factory.
- **`SelectorDeOpciones.java`** — thin `Application`: wires `Navegador` + `CalculosAsync` +
  `ConstructorDeFormularios`, loads `styles.css`, builds the titled 6-button menu, and
  defines one `pantallaX()` per calculator (each declares its title, prompts, field `Tipo`
  and display function). `stop()` delegates to `calculos.cerrar()`.
- `Calculadora.factorial` polls `Thread.isInterrupted()` so a cancelled long computation
  aborts promptly.

To add a calculator: add a pure method to `Calculadora` (with a test); add its strings to
both `messages*.properties`; add a `pantallaX()` that calls `formularios.mostrar(titulo,
instrucciones, prompts, FiltroNumerico.Tipo, calculo)` with everything resolved via
`Textos.get(...)`; and a `Botones.crear(...)` entry in `mostrarMenu()`.

Identifiers and comments are in Spanish (keep that convention); user-visible strings live in
`messages*.properties`. `TextosTest` guards that the two bundles have identical keys and
that every parametrized value is a valid `MessageFormat` pattern.

Open improvements (not yet applied): TestFX UI tests.
