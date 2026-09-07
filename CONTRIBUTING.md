# Contributing guide

<p align="center"><a href="CONTRIBUTING.md">English</a> · <a href="docs/CONTRIBUTING_es.md">Español</a></p>

Thanks for your interest. The project is small; these are the conventions.

## Before you start

- **JDK 17 or newer.** The dependencies (JavaFX 21, JUnit 5, TestFX…) are
  downloaded by Maven; nothing else to install.
- Common commands are in the [README](README.md#how-to-run); the architecture, in
  [CLAUDE.md](CLAUDE.md).

## Workflow

1. Branch off `main`.
2. One commit per logical unit of change.
3. Run `mvn spotless:apply` (formats with `palantir-java-format`, 120 columns) and
   then `mvn clean test`: every test must pass on JDK 17.
4. Open a pull request. CI runs `spotless:check` and the tests on **JDK 17 and
   21**, and comments the coverage; it has to be green.

## Conventions

- **Identifiers and comments in English.** User-visible text does NOT go in the
  code: it lives in
  `src/main/resources/io/guillermoamadodiaz/javacalcfx/i18n/messages*.properties`
  and is resolved with `Messages.get(...)`. Every new key goes in **both** files
  (`MessagesTest` checks that they declare the same keys and that the `{0}`
  patterns are valid).
- **Logic without interface.** The arithmetic lives in `calc/Calculator` as a
  pure method —no JavaFX, no text—, throws `CalculationError` with a *key* on
  invalid input, and always comes with its test.
- **Commit messages**: prefix `feat:`, `fix:`, `docs:`, `refactor:`, `build:`,
  `ci:` or `test:`, and the rest in the imperative.

## Adding a calculator

The full pattern is in [CLAUDE.md](CLAUDE.md) («To add a calculator»): a pure
method in `Calculator` + test + text in `en`/`es` (including `menu.button.<key>`
and `.tooltip`) + `xScreen()` + `MenuEntry` in the right `Category` of
`catalog()`. If it helps, add a pure `ui/XSteps` and pass it as the `steps`
argument.

## Releasing a version

Bump `<version>` in `pom.xml`, move what applies from «Unreleased» to the new
version in [`CHANGELOG.md`](CHANGELOG.md), and:

```bash
git tag vX.Y.Z && git push origin vX.Y.Z
```

The release workflow builds the `.msi` and the portable version for Windows and
attaches them to the GitHub Release.
