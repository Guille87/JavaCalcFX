# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Commands

- Run the app: `mvn clean javafx:run`
- Debug (attach on `localhost:8000`, JVM suspends until attached): `mvn clean javafx:run@debug`
- Run tests: `mvn clean test` — also writes the JaCoCo coverage report to
  `target/site/jacoco/index.html`.
- Formatting: `mvn spotless:apply` reformats (palantir-java-format, 120 cols); `mvn
  spotless:check` verifies. Run `apply` before committing — CI fails on unformatted code.
- Static analysis: `mvn compile spotbugs:check` (CI runs it). Effort Max, threshold
  Medium, production code only. False positives go in `spotbugs-exclude.xml` (currently:
  `VA_FORMAT_STRING_USES_NEWLINE` — UI labels need literal `\n`, not `%n`; and
  `EI_EXPOSE_REP*` — GUI classes legitimately hold their `Stage`/`StackPane`). Any *new*
  finding should be fixed, not excluded.
- Single test class / method: `mvn test -Dtest=FormatoTest` or
  `-Dtest=FormatoTest#numero_entero_sin_decimales`. `CalculadoraTest`'s methods live in
  `@Nested` classes (`TrianguloRectangulo`, `AreaCilindro`, `AnioBisiesto`, `Factorial`,
  `Multiplos`, `Notas`, `EcuacionSegundoGrado`, `Potencia`, `RaizNesima`, `McdYMcm`,
  `Primos`, `ConversorDeBases`, `ProporcionesYPorcentajes`, `IndiceMasaCorporal`), so
  select one with the enclosing class:
  `-Dtest='CalculadoraTest$Factorial#rechaza_negativos'`. Other test classes:
  `calc/ConversionesTest`; `i18n/{Textos,Idioma}Test`; in `ui` `Formato`, `MensajesDeError`, `Entrada`,
  `FiltroNumerico`, `EstadoVentana`, `Tema`, `Historial`, `UltimaCalculadora`, and
  `PasoAPaso{Cuadratica,Pitagoras,Cilindro,
  Proporciones}Test`; and root `InterfazTest` (TestFX).
- `InterfazTest` drives the real UI headless via Monocle (surefire `argLine` in the POM,
  plus `useModulePath=false` so TestFX isn't on the module path). `mvn test -Pheaded` shows
  a window. No display or xvfb needed. It opens the stage large (the categorized menu is
  tall — small windows would scroll buttons out of TestFX's reach); a `@BeforeEach` clears
  `Historial` and `@AfterAll` clears the `EstadoVentana`/`Tema`/`Historial` prefs it touched.
- Package for Windows (profile `dist`): `mvn -Pdist -DskipTests clean javafx:jlink package`
  → portable app-image in `target/dist/JavaCalcFX/`. Add `,installer` for the `.msi`
  (needs WiX 3.x on PATH). `javafx:jlink` must run before `package`.

## CI / release

- `.github/workflows/ci.yml`: an `analisis-estatico` job (`spotless:check` +
  `spotbugs:check`) then a `test` job on a **JDK 17 + 21 matrix** (`mvn -B clean test`). On JDK 17 it uploads the JaCoCo HTML,
  comments coverage on PRs, and — on push to `main` — regenerates `.github/badges/jacoco.svg`
  and commits it back with `[skip ci]`.
- `.github/workflows/release.yml`: pushing a tag `vX.Y.Z` builds the `.msi` + portable zip
  on `windows-latest` and attaches them to a GitHub Release. Bump `pom.xml` `<version>`
  then `git tag vX.Y.Z && git push origin vX.Y.Z`.
- Dependabot (`.github/dependabot.yml`) tracks Maven + Actions; it ignores semver-major
  bumps of `org.openjfx:*` and `org.testfx:openjfx-monocle`.

## Requirements

- JDK 17+ (`maven.compiler.release` = 17). JavaFX 21 LTS and all other deps come from Maven
  (`org.openjfx`, `org.testfx`); no separate SDK install. `openjfx-monocle` deliberately
  stays on `17.0.10` — the `21.x` artifact is Java-21 bytecode and breaks the JDK 17 CI job,
  while `17.0.10` runs fine against JavaFX 21.

## Architecture

Single-module JavaFX desktop app in four packages: `calc` (pure domain), `i18n`
(translatable text), `ui` (reusable interface infrastructure), and the root package (the
`Application` and its screen catalog). Dependency direction: `ui` → `calc` and `ui` →
`i18n`; **`calc` imports nothing from the rest of the project** (nor JavaFX).

- **`calc/`** — all mathematics as static, dependency-free, precondition-checked functions:
  `resolverTrianguloRectangulo`, `areaCilindro`, `esBisiesto`, `factorial`, `esMultiplo`,
  `media` (grades in `[NOTA_MINIMA, NOTA_MAXIMA]` = 0..10), `estaAprobado`,
  `resolverEcuacionCuadratica`, `potencia`, `raiz` (n-th root; odd roots of negatives;
  Newton-refined so exact roots come out exact), `mcd`, `mcm` (`absExact`/`multiplyExact`
  guarded), `analizarPrimalidad` (trial division to √n, interruptible; smallest proper
  divisor for composites), `convertirBase` (base inferred from a `0b`/`0o`/`0x` prefix),
  `porcentajeDe`, `reglaDeTres`, `imc` (kg + m; `CategoriaImc` per WHO ranges). Returns
  immutable records (`Triangulo`, `EcuacionCuadratica`/`Raiz`, `Primalidad`,
  `ConversionBase`, `IndiceMasaCorporal`). `Conversiones` turns lb / ft+in / cm into
  SI units (kg, m) so `imc` serves both metric and imperial input. Interruptible loops (`factorial`,
  `analizarPrimalidad`) throw a bare `CancellationException` when `Thread.isInterrupted()`.
  Invalid input throws **`ErrorDeCalculo`** (a subclass of `IllegalArgumentException`)
  carrying the *message key* (`clave()`) and its `argumentos()`, never translated text; an
  arg of type `ErrorDeCalculo.Nombre` marks a value that is itself a key (a field name).
  Unit-tested by `CalculadoraTest`.
- **`i18n/`** — `Textos` reads the `messages*.properties` files directly (not via
  `ResourceBundle`, whose lookup mixes in `Locale.getDefault()` and returns the wrong
  language on a machine with a different default locale): `messages.properties` is Spanish
  and the base; a non-Spanish `Idioma` loads `messages_<lang>.properties` on top, falling
  back to the base. `Textos.get(key)` / `get(key, args...)` (latter via `MessageFormat` — a
  literal `'` in a parametrized value must be doubled `''`). `seleccionar(Idioma)` switches
  and persists via `java.util.prefs`; `usarIdioma(Locale)` switches without persisting
  (tests). `Idioma` is the `ESPANOL`/`INGLES` enum behind the menu's language `ComboBox`.
- **`ui/` infrastructure** — small single-responsibility pieces:
  - `Navegador` — owns the root `StackPane` (always one child); `mostrar(Node)` swaps the
    screen and first runs an `alNavegar` hook (wired to cancel the in-flight calculation).
  - `CalculosAsync` — the single daemon-thread `ExecutorService` + current `Task<String>`.
    `ejecutar(calculo, alEmpezar, alTerminar, alFallar)` runs work off the FX thread;
    `cancelar()` interrupts; `cerrar()` (from `Application.stop()`) shuts it down. One
    calculation at a time.
  - `ConstructorDeFormularios` — builds the generic form screen (bold header, wrapped
    instructions, one `TextField` per prompt with its `FiltroNumerico` — pass `null` `Tipo`
    to skip filtering, e.g. hex digits; Enter-default «Calcular»; wrapped result label;
    «Volver» that also fires on Esc via a capture-phase `KEY_PRESSED` filter). In a
    transparent `ScrollPane`; focuses the first field. After a successful calc it shows a
    **«Copiar»** button (`BotonCopiar` → system clipboard) and records the calc in
    `Historial`; the `mostrar(...)` overload that also takes a `pasos` function shows a
    **«Mostrar pasos»** toggle. `mostrarConModos(...)` is the variant with a `ComboBox<Modo>`
    that swaps the fields + calc function (IMC uses it for metric/imperial); no pasos there.
    A `Modo` may carry `aComun`/`desdeComun` so switching modes carries the entered data
    across, converted (métrico↔imperial in IMC via `calc/Conversiones`).
  - `MensajesDeError` — pure `Throwable → String`. This is where `ErrorDeCalculo.clave()`
    (and any `Nombre` args) get translated via `Textos`; `NumberFormatException` →
    generic message; cancellation → `""`. Unit-tested.
  - `Formato` — pure number-to-text formatting, **always `Locale.ROOT`** (dot decimal,
    comma thousands) so output matches the dot-only input and doesn't vary by machine
    locale or between local/CI. Fresh `DecimalFormat` per call (thread-safety). Unit-tested.
  - `Entrada` — the single text→number parsing seam. Unit-tested.
  - `FiltroNumerico` — installs a `TextFormatter` keeping fields numeric while typing,
    `Tipo.ENTERO` / `Tipo.DECIMAL`; `esValido` is a pure prefix check. Unit-tested.
  - `Botones` — button factory (`crear(texto[, tooltip], accion)`).
  - `EstadoVentana` — persists window size/position via `java.util.prefs`; `restaurar(stage)`
    before `show()`, `vigilar(stage)` after. Discards sub-minimum sizes / off-screen
    positions. Pure `tamanoValido` / `puntoVisible` are unit-tested.
  - `Tema` — light/dark. `esOscuro()` / `alternar()` persist to a `java.util.prefs` subnode;
    `aplicarA(Scene)` toggles the `tema-oscuro` style class on the scene root. `styles.css`
    redefines `-fx-base`/`-fx-background`/`-fx-control-inner-background` (+ prompt-text
    fill) for `.root.tema-oscuro`; Modena derives the rest.
  - `Historial` — the last `MAXIMO` (25) calculations (`Entrada` = título + resultado +
    `Instant`, most recent first), persisted to a `java.util.prefs` subnode as one string
    (results trimmed to `MAX_RESULTADO`=300 so a huge factorial can't overflow the prefs
    limit; legacy 2-field entries load with a null `momento`). `registrar` collapses a
    repeat of the current head (same title+result) into a timestamp refresh.
    `ConstructorDeFormularios` calls `Historial.registrar(titulo, texto)` after every
    successful calc; `SelectorDeOpciones.pantallaHistorial()` renders it with the date.
  - `UltimaCalculadora` — `recordar`/`recordada`/`olvidar` a calculator key in a
    `java.util.prefs` subnode. `catalogo()`'s `entrada(...)` factory records on open;
    `mostrarMenu()` calls `olvidar()`; `start()` reads it before `mostrarMenu()` and
    reopens that screen via `abrirCalculadora(clave)`. `InterfazTest.start()` clears it so
    each test begins on the menu.
  - `PasoAPaso{Cuadratica,Pitagoras,Cilindro,Porcentaje,ReglaDeTres}` — pure, deterministic
    templates rendering a calculation step by step in linear notation. Each calls its
    `Calculadora` method (for validation + values) then fills fixed templates. Unit-tested.
- **`SelectorDeOpciones.java`** — thin `Application`: wires `Navegador` + `CalculosAsync` +
  `ConstructorDeFormularios`, loads `styles.css` + window icons, calls `Tema.aplicarA(scene)`,
  sets a minimum window size. `catalogo()` is a `List<Categoria>` (`geometria`,
  `aritmetica`, `potencias`, `proporciones`, `otros`), each a `menu.categoria.<clave>`
  heading over a `FlowPane` of `botonMenu(clave, accion)`. The menu's top bar has a theme
  toggle + a language `ComboBox` (both rebuild the menu). Each calculator is one
  `pantallaX()` + one `EntradaMenu` in `catalogo()`. `stop()` → `calculos.cerrar()`.

To add a calculator: add a pure method to `calc/Calculadora` that throws `ErrorDeCalculo`
on bad input (with a test); add its strings to both `messages*.properties` (including
`menu.boton.<clave>` and `.tooltip`, plus any `calc.*` error keys); add a `pantallaX()`
calling `formularios.mostrar(titulo, instrucciones, prompts, FiltroNumerico.Tipo, calculo
[, pasos])` with text via `Textos.get(...)`; and an `EntradaMenu("<clave>",
this::pantallaX)` in the right `Categoria` (via the `entrada(...)` factory). If it warrants
a step-by-step, add a pure `ui/PasoAPasoX` and pass it as the `pasos` argument; if it needs
an input-mode selector, use `formularios.mostrarConModos(...)` instead.

Identifiers and comments are in Spanish (keep that convention); user-visible strings live
in `messages*.properties`. `TextosTest` guards that the two bundles have identical keys and
that every parametrized value is a valid `MessageFormat` pattern. `ROADMAP.md` tracks
planned work (Fases 0–5 done; remaining items are optional).
