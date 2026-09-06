# Hoja de ruta

Mejoras planificadas para JavaCalcFX, ordenadas de menor a mayor riesgo. Se van
marcando conforme se completan.

## Decisiones tomadas

- **Formateador:** `palantir-java-format` (vía Spotless).
- **`jpackage`:** solo Windows por ahora (`.msi`/`.exe`); es donde se puede probar.
  El workflow de release se podrá ampliar a Linux/macOS más adelante.
- **Versionado:** SemVer, empezando en `0.1.0`.

---

## Fase 0 · Tooling base

Se hace primero para que el código nuevo nazca ya con el estilo correcto.

- [x] **Spotless** con `palantir-java-format`: `mvn spotless:apply` sobre el
      código actual y `spotless:check` en la CI.
- [x] **`.github/dependabot.yml`** — PRs de actualización para dependencias de
      Maven y GitHub Actions.
- [x] **README**: badges de CI y licencia; capturas reales en vez del diagrama ASCII.
- [x] **Icono de la app** (`stage.getIcons(...)`, PNGs en `resources/.../icons/`).

## Fase 1 · Pulido de UX

- [x] **Persistir tamaño y posición de la ventana** entre sesiones
      (`ui/EstadoVentana`, con `java.util.prefs`).
- [x] **Recordar la última calculadora abierta** (`ui/UltimaCalculadora`): al
      arrancar se reabre esa pantalla; volver al menú lo olvida.
- [x] El **primer campo recibe el foco** al abrir un formulario.

## Fase 2 · Más calculadoras

Patrón por cada una: método puro en `Calculadora` + test + textos `es`/`en`
(incluido `menu.boton.<clave>` y `.tooltip`) + `pantallaX()` + `botonMenu`.
Un mini-commit por calculadora.

- [x] Ecuación de 2.º grado
- [x] Potencia (`xⁿ`) y raíz n-ésima (pantallas separadas; la raíz admite
      índices impares de negativos)
- [x] MCD y MCM
- [x] ¿Es primo? (da un divisor y la factorización si es compuesto)
- [x] Conversor de bases (bin/oct/hex/dec; la base de entrada se infiere del
      prefijo 0b/0o/0x)
- [x] Porcentaje (X % de una cantidad) y regla de tres directa (pantallas
      separadas, categoría «Proporciones y porcentajes»)
- [x] IMC (peso/altura², con categoría según la OMS)
- [x] **Menú agrupado por categorías** (Geometría · Aritmética · Potencias y
      ecuaciones · Otros). El catálogo es una lista de `Categoria` en
      `SelectorDeOpciones`.

## Fase 3 · Calidad y modernización

- [x] **JaCoCo**: informe de cobertura (HTML como artefacto, comentario en las
      PR y badge que se regenera al hacer push a `main`). Pendiente opcional: un
      umbral que rompa el build.
- [x] **Migrar a JavaFX 21 LTS**: `javafx.version` a `21.0.10` (el JDK sigue en
      17; JavaFX 21 lo admite). `openjfx-monocle` se queda en `17.0.10`: es
      compatible con JavaFX 21 y su bytecode corre en JDK 17, mientras que la
      21.x exigiría un runtime 21+ y rompería el job de JDK 17 de la CI.
- [x] **Matriz de CI**: JDK 17 y 21.
- [x] **SpotBugs** como check en la CI (esfuerzo Max, umbral Medium, solo código
      de producción; falsos positivos en `spotbugs-exclude.xml`).

## Fase 4 · Distribución (Windows)

- [x] **`javafx:jlink`** — runtime autocontenido en `target/JavaCalcFX`.
- [x] **`jpackage`** (perfil `dist`): *app-image* portable por defecto,
      instalador `.msi` con `-Pdist,installer`. Vía `panteleyev/jpackage-maven-plugin`.
- [x] **Workflow de release** (`release.yml`): al empujar una etiqueta `vX.Y.Z`
      en `windows-latest`, construye el `.msi` y el zip portable y los adjunta al
      GitHub Release.
- [x] **Versionado**: `pom.xml` a `0.1.0`. Pendiente opcional: convención
      `0.2.0-SNAPSHOT` entre releases (ahora la versión del build es siempre la
      del último tag).

## Fase 5 · Paso a paso

Explicación desarrollada del cálculo, en notación lineal, tras un botón
«Mostrar pasos». Plantillas deterministas y testeadas, no álgebra simbólica.

- [x] **Prototipo: ecuación de 2.º grado** (`ui/PasoAPasoCuadratica` + toggle en
      `ConstructorDeFormularios`).
- [x] Extendido a Pitágoras, cilindro, porcentaje y regla de tres (un
      `ui/PasoAPasoX` por calculadora). En «primo», «bisiesto», etc. no aporta.

## Fase 6 · Opcionales

- [x] **Modo oscuro** con toggle persistido (`ui/Tema` + clase `tema-oscuro` en
      `styles.css` + botón en la barra superior del menú).
- [x] Desacoplar `calc` de `Textos`: `ErrorDeCalculo` lleva la clave del mensaje
      y sus argumentos; `ui/MensajesDeError` traduce. El paquete `calc` ya no
      importa nada del resto del proyecto.
- [x] `CHANGELOG.md` / `CONTRIBUTING.md`.
- [x] Copiar el resultado al portapapeles (botón «Copiar» tras un cálculo).
- [x] Historial de cálculos (`ui/Historial`, persistido; pantalla accesible
      desde la barra del menú, con botón «Vaciar»).
