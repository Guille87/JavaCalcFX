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

- [ ] **Persistir tamaño y posición de la ventana** entre sesiones
      (`java.prefs`, junto a `idioma`; p. ej. un `EstadoVentana` en `ui`).
- [ ] Opcional: recordar la última calculadora abierta.
- [ ] Verificar/forzar que el **primer campo recibe el foco** al abrir un formulario.

## Fase 2 · Más calculadoras

Patrón por cada una: método puro en `Calculadora` + test + textos `es`/`en`
(incluido `menu.boton.<clave>` y `.tooltip`) + `pantallaX()` + `botonMenu`.
Un mini-commit por calculadora.

- [ ] Ecuación de 2.º grado
- [ ] Raíz cuadrada / potencia
- [ ] MCD y MCM
- [ ] ¿Es primo?
- [ ] Conversor de bases (bin/oct/hex/dec)
- [ ] Porcentajes / regla de tres
- [ ] IMC
- [ ] Si el menú pasa de ~8 botones → **agrupar por categorías** (cambio
      localizado en `mostrarMenu`).

## Fase 3 · Calidad medible

- [ ] **JaCoCo**: informe de cobertura + badge (opcional: umbral que rompa el build).
- [ ] **Matriz de CI**: JDK 17 y 21 (confirmar Monocle en 21).
- [ ] Opcional: **SpotBugs** o **Error Prone** como check.

## Fase 4 · Distribución (Windows)

- [ ] **`javafx:jlink`** — imagen de runtime autocontenida (paso intermedio).
- [ ] **`jpackage`** — instalador Windows (`.msi`/`.exe`) y/o *app-image* portable.
- [ ] **Workflow de release**: al hacer `git tag vX.Y.Z` en un runner
      `windows-latest`, construir el instalador y adjuntarlo al GitHub Release.
- [ ] **Versionado**: `pom.xml` de `1.0-SNAPSHOT` a `0.1.0` (y `0.1.0-SNAPSHOT`
      entre releases).

## Fase 5 · Opcionales

- [ ] **Modo oscuro** con toggle persistido (variables en `styles.css` + botón en el menú).
- [ ] Desacoplar `Calculadora` de `Textos` (excepciones con clave; traduce la UI).
- [ ] `CHANGELOG.md` / `CONTRIBUTING.md`.
- [ ] Copiar el resultado al portapapeles.
- [ ] Historial de cálculos.
