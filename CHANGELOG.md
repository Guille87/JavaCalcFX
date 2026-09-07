# Changelog

<p align="center"><a href="CHANGELOG.md">English</a> · <a href="docs/CHANGELOG_es.md">Español</a></p>

All notable changes to the project are documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.1.0/)
and the project follows [semantic versioning](https://semver.org/).

## [Unreleased]

### Added

- **Settings screen**: a ⚙ button in the menu's top bar opens a screen that
  gathers the options. So far: language (moved off the top bar), theme, a
  «Reopen the last calculator on start» toggle, a «Remember the window size and
  position» toggle and a «Reset window» button.

### Changed

- The app no longer **reopens the last calculator on start** by default; it now
  starts on the menu. Turn it back on in the settings screen.

## [0.2.0] - 2026-09-07

### Added

- **Calculation history**: a screen reachable from the menu bar with the last 25
  results; it is saved between sessions and has a «Clear» button. Each entry
  shows its **date and time**, and repeating the same calculation does not add a
  new entry (it only refreshes the timestamp of the one already at the top).
- On start, the app **reopens the last calculator** that was open when it was
  closed (if it was closed on the menu, it opens the menu).
- A **«Copy»** button that puts the result on the clipboard after a calculation.
- **Dark mode** with a switch in the menu's top bar; the preference is remembered
  between sessions.
- **«Step by step»** explanation also for the Pythagorean theorem, the cylinder
  surface area, the percentage and the rule of three (previously only the
  quadratic equation).

### Changed

- **BMI**: the height is entered in **centimeters** (previously in meters) and
  there is a measurement-system selector: **metric** (kg, cm) or **imperial**
  (lb, feet and inches). The BMI and its category are the same; only the input
  changes. When switching systems, if the fields were filled, the data is
  **carried across converted** (e.g. 70 kg / 175 cm → 154.3 lb / 5 ft 8.9 in):
  centimeters without decimals and kg, pounds and inches to one, so the
  round-trip conversion returns the same value.
- `Format` always uses `Locale.ROOT`: numbers are shown with a dot decimal and a
  comma thousands separator, regardless of the system language. Previously, on a
  machine with a Spanish regional setting, the result came out with a comma
  decimal even though the input field only accepts the dot.
- Field help text reads better in both themes.
- The `calc` package no longer depends on the text layer: it throws
  `CalculationError` with the message key and its arguments, and the interface
  (`ui/ErrorMessages`) translates it.
- CI adds **static analysis with SpotBugs** to the formatting check.
- **English as the default language**: the code, comments and i18n keys move to
  English, and `messages.properties` (the base) is now in English, with Spanish
  in `messages_es.properties`. The app starts in English if no language is saved
  and the selector switches to Spanish.
- The `java.util.prefs` node and key names for the saved settings (language,
  theme, history, last calculator) are now in English. As a side effect, on a
  machine that had saved settings with `0.1.0` those settings reset once: the app
  opens in English, light theme, empty history, on the menu.

## [0.1.0] - 2026-09-06

First packaged version.

### Added

- 14 calculators behind a category-grouped menu (Geometry · Arithmetic · Powers
  and equations · Proportions and percentages · Other): Pythagorean theorem,
  cylinder surface area, leap year, factorial, multiple, pass/fail, quadratic
  equation, power, nth root, GCD and LCM, is it prime?, base converter,
  percentage, rule of three and BMI.
- Interface in Spanish and English, with the choice remembered between sessions.
- Asynchronous calculation (the window never freezes) and cancellable on
  changing screen.
- Optional «step by step» explanation for the quadratic equation.
- The window remembers its size and position; focus starts in the first field.
- Windows packaging with `jlink` + `jpackage`: `.msi` installer and portable
  version, published on the GitHub Release when a `vX.Y.Z` tag is pushed.
- Continuous integration with Spotless, tests (JUnit 5 + headless TestFX) on JDK
  17 and 21, and a coverage report with JaCoCo.

[Unreleased]: https://github.com/Guille87/JavaCalcFX/compare/v0.2.0...HEAD
[0.2.0]: https://github.com/Guille87/JavaCalcFX/compare/v0.1.0...v0.2.0
[0.1.0]: https://github.com/Guille87/JavaCalcFX/releases/tag/v0.1.0
