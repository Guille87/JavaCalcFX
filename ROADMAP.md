# Roadmap

<p align="center"><a href="ROADMAP.md">English</a> · <a href="docs/ROADMAP_es.md">Español</a></p>

Planned improvements for JavaCalcFX, ordered from lowest to highest risk. They
are ticked off as they are completed.

## Decisions made

- **Formatter:** `palantir-java-format` (via Spotless).
- **`jpackage`:** Windows only for now (`.msi`/`.exe`); it is where it can be
  tested. The release workflow can be extended to Linux/macOS later.
- **Versioning:** SemVer, starting at `0.1.0`.

---

## Phase 0 · Base tooling

- [x] **Spotless** with `palantir-java-format`: `mvn spotless:apply` over the
      current code and `spotless:check` in CI.
- [x] **`.github/dependabot.yml`** — update PRs for Maven and GitHub Actions
      dependencies.
- [x] **README**: CI and license badges; real screenshots instead of the ASCII
      diagram.
- [x] **App icon** (`stage.getIcons(...)`, PNGs in `resources/.../icons/`).

## Phase 1 · UX polish

- [x] **Persist window size and position** between sessions (`ui/WindowState`,
      with `java.util.prefs`).
- [x] **Remember the last calculator opened** (`ui/LastCalculator`): on start it
      reopens that screen; going back to the menu forgets it.
- [x] The **first field gets the focus** when a form opens.

## Phase 2 · More calculators

Pattern for each: pure method in `Calculator` + test + text in `en`/`es`
(including `menu.button.<key>` and `.tooltip`) + `xScreen()` + `menuButton`. One
mini-commit per calculator.

- [x] Quadratic equation
- [x] Power (`xⁿ`) and nth root (separate screens; the root allows odd indices of
      negatives)
- [x] GCD and LCM
- [x] Is it prime? (gives a divisor and the factorization if composite)
- [x] Base converter (bin/oct/hex/dec; the input base is inferred from the
      0b/0o/0x prefix)
- [x] Percentage (X% of an amount) and direct rule of three (separate screens,
      «Proportions and percentages» category)
- [x] BMI (weight/height², with a WHO category)
- [x] **Category-grouped menu** (Geometry · Arithmetic · Powers and equations ·
      Other). The catalog is a list of `Category` in `CalculatorApp`.

## Phase 3 · Quality and modernization

- [x] **JaCoCo**: coverage report (HTML as an artifact, a PR comment and a badge
      regenerated on push to `main`). Optional pending: a threshold that breaks
      the build.
- [x] **Migrate to JavaFX 21 LTS**: `javafx.version` to `21.0.10` (the JDK stays
      at 17; JavaFX 21 supports it). `openjfx-monocle` stays at `17.0.10`: it is
      compatible with JavaFX 21 and its bytecode runs on JDK 17, whereas 21.x
      would require a 21+ runtime and break the JDK 17 CI job.
- [x] **CI matrix**: JDK 17 and 21.
- [x] **SpotBugs** as a CI check (effort Max, threshold Medium, production code
      only; false positives in `spotbugs-exclude.xml`).

## Phase 4 · Distribution (Windows)

- [x] **`javafx:jlink`** — self-contained runtime in `target/JavaCalcFX`.
- [x] **`jpackage`** (`dist` profile): portable app-image by default, `.msi`
      installer with `-Pdist,installer`. Via `panteleyev/jpackage-maven-plugin`.
- [x] **Release workflow** (`release.yml`): on pushing a `vX.Y.Z` tag on
      `windows-latest`, it builds the `.msi` and the portable zip and attaches
      them to the GitHub Release.
- [x] **Versioning**: `pom.xml` at `0.1.0`. Optional pending: a
      `0.2.0-SNAPSHOT` convention between releases.

## Phase 5 · Step by step

A worked explanation of the calculation, in linear notation, behind a «Show
steps» button. Deterministic, tested templates, not symbolic algebra.

- [x] **Prototype: quadratic equation** (`ui/QuadraticSteps` + toggle in
      `FormBuilder`).
- [x] Extended to Pythagoras, cylinder, percentage and rule of three (one
      `ui/XSteps` per calculator). For «prime», «leap year», etc. it adds
      nothing.

## Phase 6 · Optional

- [x] **Dark mode** with a persisted toggle (`ui/Theme` + `dark-theme` class in
      `styles.css` + button in the menu's top bar).
- [x] Decouple `calc` from `Messages`: `CalculationError` carries the message key
      and its arguments; `ui/ErrorMessages` translates. The `calc` package no
      longer imports anything from the rest of the project.
- [x] `CHANGELOG.md` / `CONTRIBUTING.md`.
- [x] Copy the result to the clipboard («Copy» button after a calculation).
- [x] Calculation history (`ui/History`, persisted; screen reachable from the
      menu bar, with a «Clear» button).

---

## Later improvements

Outside the original plan, as they come up.

- [x] **BMI**: height in centimeters and a measurement-system selector (metric /
      imperial), with the data carried across converted on switching systems.
      `calc/Conversions` + `FormBuilder.showWithModes`.
- [x] **English conventions**: class, method and test names, comments and i18n
      keys migrated to English; English as the default language.
