# Guía de contribución

<p align="center"><a href="../CONTRIBUTING.md">English</a> · <a href="CONTRIBUTING_es.md">Español</a></p>

Gracias por tu interés. El proyecto es pequeño; estas son las convenciones.

Al participar aceptas el [Código de Conducta](CODE_OF_CONDUCT_es.md). Para
problemas de seguridad, consulta [SECURITY_es.md](SECURITY_es.md) — no abras un
*issue* público.

## Antes de empezar

- **JDK 17 o superior.** Las dependencias (JavaFX 21, JUnit 5, TestFX…) las
  descarga Maven; no hace falta instalar nada más.
- Comandos habituales en el [README](README_es.md#cómo-ejecutar); la
  arquitectura, en [CLAUDE.md](../CLAUDE.md).

## Flujo de trabajo

1. Crea una rama a partir de `main`.
2. Un commit por unidad lógica de cambio.
3. Ejecuta `mvn spotless:apply` (formatea con `palantir-java-format`, 120
   columnas) y luego `mvn clean test`: deben pasar todos los tests en JDK 17.
4. Abre una *pull request*. La CI ejecuta `spotless:check` y los tests en **JDK
   17 y 21**, y comenta la cobertura; tiene que quedar en verde.

## Convenciones

- **Identificadores y comentarios en inglés.** Los textos visibles para la
  persona usuaria NO van en el código: viven en
  `src/main/resources/io/guillermoamadodiaz/javacalcfx/i18n/messages*.properties`
  y se resuelven con `Messages.get(...)`. Toda clave nueva va en **los dos**
  ficheros (`MessagesTest` comprueba que tengan las mismas claves y que los
  patrones con `{0}` sean válidos).
- **Lógica sin interfaz.** La aritmética vive en `calc/Calculator` como método
  puro —sin JavaFX ni textos—, lanza `CalculationError` con una *clave* ante una
  entrada inválida, y siempre acompañada de su test.
- **Mensajes de commit**: prefijo `feat:`, `fix:`, `docs:`, `refactor:`,
  `build:`, `ci:` o `test:`, y el resto en imperativo.

## Añadir una calculadora

El patrón completo está en [CLAUDE.md](../CLAUDE.md) («To add a calculator»):
método puro en `Calculator` + test + textos `en`/`es` (incluidos
`menu.button.<clave>` y `.tooltip`) + `xScreen()` + `MenuEntry` en la `Category`
correspondiente de `catalog()`. Si aporta, añade un `ui/XSteps` puro y pásalo
como argumento `steps`.

## Publicar una versión

Sube `<version>` en `pom.xml`, mueve lo que corresponda de «Unreleased» a la
nueva versión en [`CHANGELOG_es.md`](CHANGELOG_es.md), y:

```bash
git tag vX.Y.Z && git push origin vX.Y.Z
```

El workflow de release construye el `.msi` y la versión portable para Windows y
los adjunta al GitHub Release.
