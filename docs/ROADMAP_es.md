# Hoja de ruta

<p align="center"><a href="../ROADMAP.md">English</a> · <a href="ROADMAP_es.md">Español</a></p>

Documento vivo — no tiene fecha de fin. Las ideas se añaden libremente y van
pasando por **Ideas → Siguiente → Hecho** según avanzan. El detalle de cada
cambio publicado está en el [CHANGELOG](CHANGELOG_es.md).

## Decisiones tomadas

- **Formateador:** `palantir-java-format` (vía Spotless).
- **`jpackage`:** solo Windows por ahora (`.msi`/`.exe`); es donde se puede
  probar. El workflow de release se podrá ampliar a Linux/macOS más adelante.
- **Versionado:** SemVer, empezando en `0.1.0`.

---

## Siguiente

Lista corta y comprometida. _(Nada en cola — elegir del backlog.)_

---

## Ideas / pendientes

Sin compromiso; cualquier cosa que valga la pena apuntar.

- [ ] **Escala de la interfaz** — pequeña / mediana / grande (`-fx-font-size` en
      `.root`).
- [ ] **Unidad de ángulo** — grados o radianes para los ángulos de Pitágoras.
- [ ] **Sistema por defecto del IMC** — métrico o imperial como modo inicial.
- [ ] **Decimales que se muestran** — 2–6, y/o un interruptor para el separador
      de miles (`Format` ya lo centraliza).
- [ ] **«Seguir el tema del sistema»** — la detección en JavaFX es limitada;
      investigar.
- [ ] **Formato al copiar** — copiar solo el número o el resultado completo con
      etiquetas.
- [ ] **Notas de la versión desde el CHANGELOG** — `release.yml` debería rellenar
      el cuerpo del GitHub Release con la sección del CHANGELOG de esa versión, en
      lugar de dejarlo al mensaje de la etiqueta anotada (por eso el cuerpo de la
      `v0.2.0` es escueto).
- [ ] **Umbral de cobertura** que rompa el build (objetivo `check` de JaCoCo).
- [ ] **Convención de versión `0.2.0-SNAPSHOT`** entre releases (hoy la versión
      del build es siempre la del último tag).
- [ ] **Empaquetado para Linux / macOS** — ampliar `release.yml` más allá de
      `windows-latest`.
- [ ] **Más calculadoras** — combinatoria, conversiones de unidades,
      estadística básica…

---

## Hecho

Desde la `0.1.0`, fuera del plan original. Ver el [CHANGELOG](CHANGELOG_es.md)
para el detalle y la versión de cada uno.

- **Pantalla de ajustes** (`ui/Settings` + botón ⚙): idioma, tema, recordar la
  última calculadora (ahora desactivado por defecto), opciones de ventana +
  «Restablecer ventana», opciones de historial (on/off, tamaño, vaciar al
  cerrar), «Restablecer ajustes» y una pantalla «Acerca de».
- **Convenciones en inglés** — nombres de clase, método y test, comentarios y
  claves de i18n migrados al inglés; inglés como idioma por defecto.
- **IMC en cm + selector métrico/imperial**, con traspaso de los datos
  convertidos al cambiar de sistema (`calc/Conversions` +
  `FormBuilder.showWithModes`).
- **Historial**: fecha en cada entrada, sin duplicados consecutivos, y cada
  entrada en una tarjeta.

---

<details>
<summary>Plan histórico — fases 0–6 (cómo se construyó la app hasta la <code>0.1.0</code>)</summary>

Ordenadas de menor a mayor riesgo; todas completadas.

### Fase 0 · Tooling base

- [x] **Spotless** con `palantir-java-format`: `mvn spotless:apply` sobre el
      código actual y `spotless:check` en la CI.
- [x] **`.github/dependabot.yml`** — PRs de actualización para dependencias de
      Maven y GitHub Actions.
- [x] **README**: badges de CI y licencia; capturas reales en vez del diagrama
      ASCII.
- [x] **Icono de la app** (`stage.getIcons(...)`, PNGs en `resources/.../icons/`).

### Fase 1 · Pulido de UX

- [x] **Persistir tamaño y posición de la ventana** entre sesiones
      (`ui/WindowState`, con `java.util.prefs`).
- [x] **Recordar la última calculadora abierta** (`ui/LastCalculator`): al
      arrancar se reabre esa pantalla; volver al menú lo olvida.
- [x] El **primer campo recibe el foco** al abrir un formulario.

### Fase 2 · Más calculadoras

Patrón por cada una: método puro en `Calculator` + test + textos `en`/`es`
(incluido `menu.button.<clave>` y `.tooltip`) + `xScreen()` + `menuButton`. Un
mini-commit por calculadora.

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
      ecuaciones · Otros). El catálogo es una lista de `Category` en
      `CalculatorApp`.

### Fase 3 · Calidad y modernización

- [x] **JaCoCo**: informe de cobertura (HTML como artefacto, comentario en las
      PR y badge que se regenera al hacer push a `main`).
- [x] **Migrar a JavaFX 21 LTS**: `javafx.version` a `21.0.10` (el JDK sigue en
      17; JavaFX 21 lo admite). `openjfx-monocle` se queda en `17.0.10`: es
      compatible con JavaFX 21 y su bytecode corre en JDK 17, mientras que la
      21.x exigiría un runtime 21+ y rompería el job de JDK 17 de la CI.
- [x] **Matriz de CI**: JDK 17 y 21.
- [x] **SpotBugs** como check en la CI (esfuerzo Max, umbral Medium, solo código
      de producción; falsos positivos en `spotbugs-exclude.xml`).

### Fase 4 · Distribución (Windows)

- [x] **`javafx:jlink`** — runtime autocontenido en `target/JavaCalcFX`.
- [x] **`jpackage`** (perfil `dist`): *app-image* portable por defecto,
      instalador `.msi` con `-Pdist,installer`. Vía
      `panteleyev/jpackage-maven-plugin`.
- [x] **Workflow de release** (`release.yml`): al empujar una etiqueta `vX.Y.Z`
      en `windows-latest`, construye el `.msi` y el zip portable y los adjunta al
      GitHub Release.
- [x] **Versionado**: `pom.xml` a `0.1.0`.

### Fase 5 · Paso a paso

Explicación desarrollada del cálculo, en notación lineal, tras un botón «Mostrar
pasos». Plantillas deterministas y testeadas, no álgebra simbólica.

- [x] **Prototipo: ecuación de 2.º grado** (`ui/QuadraticSteps` + toggle en
      `FormBuilder`).
- [x] Extendido a Pitágoras, cilindro, porcentaje y regla de tres (un
      `ui/XSteps` por calculadora). En «primo», «bisiesto», etc. no aporta.

### Fase 6 · Opcionales

- [x] **Modo oscuro** con toggle persistido (`ui/Theme` + clase `dark-theme` en
      `styles.css` + botón en la barra superior del menú).
- [x] Desacoplar `calc` de `Messages`: `CalculationError` lleva la clave del
      mensaje y sus argumentos; `ui/ErrorMessages` traduce. El paquete `calc` ya
      no importa nada del resto del proyecto.
- [x] `CHANGELOG.md` / `CONTRIBUTING.md`.
- [x] Copiar el resultado al portapapeles (botón «Copiar» tras un cálculo).
- [x] Historial de cálculos (`ui/History`, persistido; pantalla accesible desde
      la barra del menú, con botón «Vaciar»).

</details>
