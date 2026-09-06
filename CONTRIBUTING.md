# Guía de contribución

Gracias por tu interés. El proyecto es pequeño; estas son las convenciones.

## Antes de empezar

- **JDK 17 o superior.** Las dependencias (JavaFX 21, JUnit 5, TestFX…) las
  descarga Maven; no hace falta instalar nada más.
- Comandos habituales en el [README](README.md#cómo-ejecutar); la arquitectura,
  en [CLAUDE.md](CLAUDE.md).

## Flujo de trabajo

1. Crea una rama a partir de `main`.
2. Un commit por unidad lógica de cambio.
3. Ejecuta `mvn spotless:apply` (formatea con `palantir-java-format`, 120
   columnas) y luego `mvn clean test`: deben pasar todos los tests en JDK 17.
4. Abre una *pull request*. La CI ejecuta `spotless:check` y los tests en **JDK
   17 y 21**, y comenta la cobertura; tiene que quedar en verde.

## Convenciones

- **Identificadores y comentarios en español.** Los textos visibles para la
  persona usuaria NO van en el código: viven en
  `src/main/resources/io/guillermoamadodiaz/javacalcfx/i18n/messages*.properties`
  y se resuelven con `Textos.get(...)`. Toda clave nueva va en **los dos**
  ficheros (`TextosTest` comprueba que tengan las mismas claves y que los
  patrones con `{0}` sean válidos).
- **Lógica sin interfaz.** La aritmética vive en `calc/Calculadora` como método
  puro —sin JavaFX ni textos—, lanza `ErrorDeCalculo` con una *clave* ante una
  entrada inválida, y siempre acompañada de su test.
- **Mensajes de commit**: prefijo `feat:`, `fix:`, `docs:`, `refactor:`,
  `build:`, `ci:` o `test:`, y el resto en imperativo.

## Añadir una calculadora

El patrón completo está en [CLAUDE.md](CLAUDE.md) («To add a calculator»):
método puro en `Calculadora` + test + textos `es`/`en` (incluidos
`menu.boton.<clave>` y `.tooltip`) + `pantallaX()` + `EntradaMenu` en la
`Categoria` correspondiente de `catalogo()`. Si aporta, añade un `ui/PasoAPasoX`
puro y pásalo como argumento `pasos`.

## Publicar una versión

Sube `<version>` en `pom.xml`, mueve lo que corresponda de «Sin publicar» a la
nueva versión en [CHANGELOG.md](CHANGELOG.md), y:

```bash
git tag vX.Y.Z && git push origin vX.Y.Z
```

El workflow de release construye el `.msi` y la versión portable para Windows y
los adjunta al GitHub Release.
