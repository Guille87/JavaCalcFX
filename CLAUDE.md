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
- Single test class / method: `mvn test -Dtest=FormatTest` or
  `-Dtest=FormatTest#whole_number_without_decimals`. `CalculatorTest`'s methods live in
  `@Nested` classes (`RightTriangle`, `CylinderArea`, `LeapYear`, `Factorial`, `Multiples`,
  `Grades`, `QuadraticEquation`, `Power`, `NthRoot`, `GcdAndLcm`, `Primes`, `BaseConverter`,
  `ProportionsAndPercentages`, `BodyMassIndex`), so select one with the enclosing class:
  `-Dtest='CalculatorTest$Factorial#rejects_negatives'`. Other test classes:
  `calc/ConversionsTest`; `i18n/{Messages,Language}Test`; in `ui` `Format`, `ErrorMessages`,
  `Input`, `NumericFilter`, `WindowState`, `Theme`, `History`, `LastCalculator`, `Settings`, and
  `{Cylinder,Quadratic,Pythagoras,Proportions}StepsTest`; and root `UiTest` (TestFX).
- `UiTest` drives the real UI headless via Monocle (surefire `argLine` in the POM, plus
  `useModulePath=false` so TestFX isn't on the module path). `mvn test -Pheaded` shows a
  window. No display or xvfb needed. It opens the stage large (the categorized menu is
  tall — small windows would scroll buttons out of TestFX's reach); it pins
  `Messages.useLocale(ENGLISH)` and clicks the English labels; a `@BeforeEach` resets
  `History` + every `Settings` flag and `@AfterAll` clears the `WindowState`/`Theme`/
  `History`/`Settings`/language prefs it touched. `every_calculator_computes_a_result` is a
  parametrized case that opens all 15 calculators end to end — the coverage net for
  `CalculatorApp`'s per-screen glue.
- Package for Windows (profile `dist`): `mvn -Pdist -DskipTests clean javafx:jlink package`
  → portable app-image in `target/dist/JavaCalcFX/`. Add `,installer` for the `.msi`
  (needs WiX 3.x on PATH). `javafx:jlink` must run before `package`.

## CI / release

- `.github/workflows/ci.yml`: an `analisis-estatico` job (`spotless:check` +
  `spotbugs:check`), then a `test` job on a **JDK 17 + 21 matrix** (`mvn -B clean test`).
  On JDK 17 it uploads the JaCoCo HTML, comments coverage on PRs, and — on push to `main` —
  regenerates `.github/badges/jacoco.svg` and commits it back with `[skip ci]` (so pulling
  before a push avoids a fast-forward conflict).
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
  `solveRightTriangle`, `cylinderArea`, `isLeapYear`, `factorial`, `isMultiple`, `mean`
  (grades in `[MIN_GRADE, MAX_GRADE]` = 0..10), `isPassing`, `solveQuadratic`, `power`,
  `nthRoot` (n-th root; odd roots of negatives; Newton-refined so exact roots come out
  exact), `gcd`, `lcm` (`absExact`/`multiplyExact` guarded), `analyzePrimality` (trial
  division to √n, interruptible; smallest proper divisor for composites), `convertBase`
  (base inferred from a `0b`/`0o`/`0x` prefix), `percentageOf`, `ruleOfThree`, `bmi`
  (kg + m; `BmiCategory` per WHO ranges). Returns immutable records (`Triangle`,
  `QuadraticEquation`/`Root`, `Primality`, `BaseConversion`, `BodyMassIndex`).
  `Conversions` turns lb / ft+in / cm into SI units (kg, m) so `bmi` serves both metric and
  imperial input. Interruptible loops (`factorial`, `analyzePrimality`) throw a bare
  `CancellationException` when `Thread.isInterrupted()`. Invalid input throws
  **`CalculationError`** (a subclass of `IllegalArgumentException`) carrying the *message
  key* (`key()`) and its `arguments()`, never translated text; an arg of type
  `CalculationError.Name` marks a value that is itself a key (a field name). Unit-tested by
  `CalculatorTest`.
- **`i18n/`** — `Messages` reads the `messages*.properties` files directly (not via
  `ResourceBundle`, whose lookup mixes in `Locale.getDefault()` and returns the wrong
  language on a machine with a different default locale): `messages.properties` is English
  and the base; a non-English `Language` loads `messages_<lang>.properties` on top (so
  `messages_es.properties` for Spanish), falling back to the base. `Messages.get(key)` /
  `get(key, args...)` (latter via `MessageFormat` — a literal `'` in a parametrized value
  must be doubled `''`). `select(Language)` switches and persists via `java.util.prefs`;
  `useLocale(Locale)` switches without persisting (tests). `Language` is the
  `SPANISH`/`ENGLISH` enum behind the menu's language `ComboBox`; `Language.from(Locale)`
  falls back to `ENGLISH`.
- **`ui/` infrastructure** — small single-responsibility pieces:
  - `Navigator` — owns the root `StackPane` (always one child); `show(Node)` swaps the
    screen and first runs an `onNavigate` hook (wired to cancel the in-flight calculation).
  - `AsyncCalculations` — the single daemon-thread `ExecutorService` + current
    `Task<String>`. `run(calculation, onStart, onFinish, onFail)` runs work off the FX
    thread; `cancel()` interrupts; `close()` (from `Application.stop()`) shuts it down. One
    calculation at a time.
  - `FormBuilder` — builds the generic form screen (bold header, wrapped instructions, one
    `TextField` per prompt with its `NumericFilter` — pass `null` `Type` to skip filtering,
    e.g. hex digits; Enter-default «Calculate»; wrapped result label; «Back» that also
    fires on Esc via a capture-phase `KEY_PRESSED` filter). In a transparent `ScrollPane`;
    focuses the first field. After a successful calc it shows a **«Copy»** button
    (`CopyButton` → system clipboard) and records the calc in `History`; the `show(...)`
    overload that also takes a `steps` function shows a **«Show steps»** toggle.
    `showWithModes(...)` is the variant with a `ComboBox<Mode>` that swaps the fields +
    calc function (the BMI screen uses it for metric/imperial); no steps there. A `Mode`
    may carry `toCommon`/`fromCommon` so switching modes carries the entered data across,
    converted (metric↔imperial via `calc/Conversions`).
  - `ErrorMessages` — pure `Throwable → String`. This is where `CalculationError.key()`
    (and any `Name` args) get translated via `Messages`; `NumberFormatException` → generic
    message; cancellation → `""`. Unit-tested.
  - `Format` — pure number-to-text formatting, **always `Locale.ROOT`** (dot decimal,
    comma thousands) so output matches the dot-only input and doesn't vary by machine
    locale or between local/CI. Fresh `DecimalFormat` per call (thread-safety). Unit-tested.
  - `Input` — the single text→number parsing seam. Unit-tested.
  - `NumericFilter` — installs a `TextFormatter` keeping fields numeric while typing,
    `Type.INTEGER` / `Type.DECIMAL`; `isValid` is a pure prefix check. Unit-tested.
  - `Buttons` — button factory (`create(text[, tooltip], action)`).
  - `WindowState` — persists window size/position via `java.util.prefs`; `restore(stage)`
    before `show()`, `watch(stage)` after. Discards sub-minimum sizes / off-screen
    positions. Pure `isValidSize` / `isVisiblePoint` are unit-tested.
  - `Theme` — light/dark. `isDark()` / `toggle()` persist to a `java.util.prefs` subnode;
    `applyTo(Scene)` toggles the `dark-theme` style class on the scene root. `styles.css`
    redefines `-fx-base`/`-fx-background`/`-fx-control-inner-background` (+ prompt-text
    fill) for `.root.dark-theme`; Modena derives the rest.
  - `History` — the last `Settings.historyMax()` (default 25) calculations (`Entry` = title
    + result + `Instant`, most recent first), persisted to a `java.util.prefs` subnode as
    one string that is **Base64-encoded** (the `RS`/`US` separators it uses are not valid
    XML and `java.util.prefs` serializes values to XML on flush — breaks on Linux/macOS
    otherwise; `decode` still reads a legacy unencoded value). Results are trimmed to
    `MAX_RESULT`=300 so a huge factorial can't overflow the prefs limit; legacy 2-field
    entries load with a null `timestamp`. `record` collapses a repeat of the current head
    into a timestamp refresh and is a no-op while `Settings.historyEnabled()` is off. `FormBuilder` calls `History.record(title, text)`
    after every successful calc; `CalculatorApp.historyScreen()` renders it with the date;
    `CalculatorApp.stop()` clears it when `Settings.clearHistoryOnExit()`.
  - `LastCalculator` — `remember`/`remembered`/`forget` a calculator key in a
    `java.util.prefs` subnode; `remember` is a no-op unless `Settings.rememberLastCalculator()`
    (off by default). `catalog()`'s `menuEntry(...)` factory records on open; `showMenu()`
    calls `forget()`; `start()` reads it before `showMenu()` and reopens that screen via
    `openCalculator(key)`. `UiTest.start()` clears it so each test begins on the menu.
  - `Settings` — user options in their own `java.util.prefs` subnode (`node("settings")`),
    each a getter/setter with a default: `rememberLastCalculator` (false), `rememberWindow`
    (true; `WindowState.restore`/save honor it), `historyEnabled` (true), `historyMax` (25),
    `clearHistoryOnExit` (false). `restoreDefaults()` clears the subnode. The settings
    screen is the UI; `Theme`/`Messages` keep their own prefs and their own controls there,
    and a «Restore defaults» button also resets those plus `WindowState.reset(stage)`.
  - `{Quadratic,Pythagoras,Cylinder,Percentage,RuleOfThree}Steps` — pure, deterministic
    templates rendering a calculation step by step in linear notation. Each calls its
    `Calculator` method (for validation + values) then fills fixed templates. Unit-tested.
- **`CalculatorApp.java`** — thin `Application`: wires `Navigator` + `AsyncCalculations` +
  `FormBuilder`, loads `styles.css` + window icons, calls `Theme.applyTo(scene)`, sets a
  minimum window size. `catalog()` is a `List<Category>` (`geometry`, `arithmetic`,
  `powers`, `proportions`, `other`), each a `menu.category.<key>` heading over a `FlowPane`
  of `menuButton(key, action)`. The menu's top bar has three buttons: History, a theme
  quick-toggle and a **⚙ Settings** button. `settingsScreen()` holds language + theme
  selectors and the `Settings` options; `aboutScreen()` (linked from it) shows the version
  read from a Maven-filtered `app.properties` (`pom.xml` `<resources>`). Each calculator is
  one `xScreen()` + one `MenuEntry` in `catalog()`. `stop()` clears the history when
  `Settings.clearHistoryOnExit()`, then `calculations.close()`.

To add a calculator: add a pure method to `calc/Calculator` that throws `CalculationError`
on bad input (with a test); add its strings to both `messages.properties` and
`messages_es.properties` (including `menu.button.<key>` and `.tooltip`, plus any `calc.*`
error keys); add an `xScreen()` calling `forms.show(title, instructions, prompts,
NumericFilter.Type, calculation [, steps])` with text via `Messages.get(...)`; and a
`MenuEntry("<key>", this::xScreen)` in the right `Category` (via the `menuEntry(...)`
factory). If it warrants a step-by-step, add a pure `ui/XSteps` and pass it as the `steps`
argument; if it needs an input-mode selector, use `forms.showWithModes(...)` instead.

Identifiers and comments are in English (keep that convention); user-visible strings live
in `messages*.properties`. `MessagesTest` guards that the two bundles have identical keys
and that every parametrized value is a valid `MessageFormat` pattern.

## Docs

Root `README.md`, `CONTRIBUTING.md`, `CHANGELOG.md`, `ROADMAP.md`, `CODE_OF_CONDUCT.md`
and `SECURITY.md` are English (primary), each with a language-selector line linking to
the Spanish version under `docs/` (`*_es.md`). Keep both languages of a doc in sync
when editing. `CODE_OF_CONDUCT.md` is the Contributor Covenant 2.1 verbatim (contact
`guillermo_amado@hotmail.es`); `SECURITY.md` is tailored and short. README screenshots live in `docs/`
(`menu.png`, `pythagorean-theorem.png`) — from the root README the path is
`docs/screenshots/*.png`, from `docs/README_es.md` it is `screenshots/*.png`. `ROADMAP` is a
living document: `## Next up` (committed shortlist), `## Ideas / backlog`
(uncommitted), `## Done` (post-`0.1.0`), and the original phases 0–6 collapsed in
a `<details>` block — move items between sections as they progress. `CHANGELOG`
has an `## [Unreleased]` / `## [Sin publicar]` section to keep current. Version is `0.3.0`.
`.github/ISSUE_TEMPLATE/` has GitHub issue-form YAML (`bug_report`, `feature_request`) +
`config.yml` (blank issues off, security link).
