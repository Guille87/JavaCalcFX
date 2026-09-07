# Roadmap

<p align="center"><a href="ROADMAP.md">English</a> · <a href="docs/ROADMAP_es.md">Español</a></p>

Living document — it has no end date. Ideas are added freely and move
**Ideas → Next up → Done** as they progress. The details of each shipped change
live in the [CHANGELOG](CHANGELOG.md).

## Decisions made

- **Formatter:** `palantir-java-format` (via Spotless).
- **`jpackage`:** Windows only for now (`.msi`/`.exe`); it is where it can be
  tested. The release workflow can be extended to Linux/macOS later.
- **Versioning:** SemVer, starting at `0.1.0`.

---

## Next up

Short, committed shortlist.

### Settings screen (v1)

A **⚙** button in the menu's top bar opens a settings screen that gathers the
options currently scattered around the UI, plus a few new ones. A new
`ui/Settings` centralizes the `java.util.prefs` reads/writes, the way `ui/Theme`
and `ui/LastCalculator` already do. New keys in both `messages*.properties`, unit
tests for `ui/Settings` and a `UiTest` case for the navigation.

- [x] **⚙ button + `settingsScreen()`**, with «Back» / Esc like any other screen.
- [x] **Language** — moved here from the top bar (the `ComboBox` leaves the menu).
- [x] **Theme** — light / dark, moved here (a quick toggle may still stay in the
      bar).
- [x] **Remember the last calculator on exit** — on/off. When off,
      `LastCalculator` records nothing and the app always starts on the menu.
- [x] **Remember the window size and position** — on/off, plus a **«Reset
      window»** button.
- [x] **History**
  - [x] enable / disable — when off, nothing is recorded.
  - [x] max entries — 10 / 25 / 50 / 100.
  - [x] clear the history on exit — on/off.
- [x] **«Restore defaults»** — clears every settings subnode (light theme,
      default language, history on, …).
- [x] **About** — version (from `pom.xml`), MIT license, link to the repository.

---

## Ideas / backlog

No commitment; anything worth remembering.

- [ ] **Interface scale** — small / medium / large (`-fx-font-size` on `.root`).
- [ ] **Angle unit** — degrees or radians for the Pythagoras angles.
- [ ] **Default BMI system** — metric or imperial as the initial mode.
- [ ] **Decimals shown** — 2–6, and/or a thousands-separator toggle (`Format`
      already centralizes this).
- [ ] **«Follow the OS theme»** — detection in JavaFX is limited; investigate.
- [ ] **Copy format** — copy just the number vs. the full labeled result.
- [ ] **Release notes from the CHANGELOG** — `release.yml` should fill the GitHub
      Release body with that version's CHANGELOG section instead of leaving it to
      the annotated-tag message (which is why `v0.2.0`'s body is thin).
- [ ] **Coverage threshold** that breaks the build (JaCoCo `check` goal).
- [ ] **`0.2.0-SNAPSHOT` version convention** between releases (today the build
      version is always the last tag).
- [ ] **Linux / macOS packaging** — extend `release.yml` beyond `windows-latest`.
- [ ] **More calculators** — combinatorics, unit conversions, simple statistics…

---

## Done

Since `0.1.0`, outside the original plan. See the [CHANGELOG](CHANGELOG.md) for
the details of each.

- **English conventions** — class, method and test names, comments and i18n keys
  migrated to English; English as the default language.
- **BMI in cm + metric/imperial selector**, with the data carried across
  converted on switching systems (`calc/Conversions` +
  `FormBuilder.showWithModes`).
- **History**: a date on each entry and no consecutive duplicates.

---

<details>
<summary>Historical plan — phases 0–6 (how the app was built up to <code>0.1.0</code>)</summary>

Ordered from lowest to highest risk; all completed.

### Phase 0 · Base tooling

- [x] **Spotless** with `palantir-java-format`: `mvn spotless:apply` over the
      current code and `spotless:check` in CI.
- [x] **`.github/dependabot.yml`** — update PRs for Maven and GitHub Actions
      dependencies.
- [x] **README**: CI and license badges; real screenshots instead of the ASCII
      diagram.
- [x] **App icon** (`stage.getIcons(...)`, PNGs in `resources/.../icons/`).

### Phase 1 · UX polish

- [x] **Persist window size and position** between sessions (`ui/WindowState`,
      with `java.util.prefs`).
- [x] **Remember the last calculator opened** (`ui/LastCalculator`): on start it
      reopens that screen; going back to the menu forgets it.
- [x] The **first field gets the focus** when a form opens.

### Phase 2 · More calculators

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

### Phase 3 · Quality and modernization

- [x] **JaCoCo**: coverage report (HTML as an artifact, a PR comment and a badge
      regenerated on push to `main`).
- [x] **Migrate to JavaFX 21 LTS**: `javafx.version` to `21.0.10` (the JDK stays
      at 17; JavaFX 21 supports it). `openjfx-monocle` stays at `17.0.10`: it is
      compatible with JavaFX 21 and its bytecode runs on JDK 17, whereas 21.x
      would require a 21+ runtime and break the JDK 17 CI job.
- [x] **CI matrix**: JDK 17 and 21.
- [x] **SpotBugs** as a CI check (effort Max, threshold Medium, production code
      only; false positives in `spotbugs-exclude.xml`).

### Phase 4 · Distribution (Windows)

- [x] **`javafx:jlink`** — self-contained runtime in `target/JavaCalcFX`.
- [x] **`jpackage`** (`dist` profile): portable app-image by default, `.msi`
      installer with `-Pdist,installer`. Via `panteleyev/jpackage-maven-plugin`.
- [x] **Release workflow** (`release.yml`): on pushing a `vX.Y.Z` tag on
      `windows-latest`, it builds the `.msi` and the portable zip and attaches
      them to the GitHub Release.
- [x] **Versioning**: `pom.xml` at `0.1.0`.

### Phase 5 · Step by step

A worked explanation of the calculation, in linear notation, behind a «Show
steps» button. Deterministic, tested templates, not symbolic algebra.

- [x] **Prototype: quadratic equation** (`ui/QuadraticSteps` + toggle in
      `FormBuilder`).
- [x] Extended to Pythagoras, cylinder, percentage and rule of three (one
      `ui/XSteps` per calculator). For «prime», «leap year», etc. it adds
      nothing.

### Phase 6 · Optional

- [x] **Dark mode** with a persisted toggle (`ui/Theme` + `dark-theme` class in
      `styles.css` + button in the menu's top bar).
- [x] Decouple `calc` from `Messages`: `CalculationError` carries the message key
      and its arguments; `ui/ErrorMessages` translates. The `calc` package no
      longer imports anything from the rest of the project.
- [x] `CHANGELOG.md` / `CONTRIBUTING.md`.
- [x] Copy the result to the clipboard («Copy» button after a calculation).
- [x] Calculation history (`ui/History`, persisted; screen reachable from the
      menu bar, with a «Clear» button).

</details>
