# Registro de cambios

<p align="center"><a href="../CHANGELOG.md">English</a> · <a href="CHANGELOG_es.md">Español</a></p>

Todos los cambios notables del proyecto se documentan en este archivo.

El formato se basa en [Keep a Changelog](https://keepachangelog.com/es-ES/1.1.0/)
y el proyecto sigue [versionado semántico](https://semver.org/lang/es/).

## [Sin publicar]

### Arreglado

- El historial de cálculos se guarda ahora codificado en Base64, para que sus
  separadores internos no rompan la serialización a XML de `java.util.prefs` en
  Linux/macOS.

## [0.3.0] - 2026-09-07

### Añadido

- **Pantalla de ajustes**: un botón ⚙ en la barra superior del menú abre una
  pantalla que reúne las opciones: idioma (sale de la barra superior), tema, un
  interruptor «Reabrir la última calculadora al arrancar», un interruptor
  «Recordar el tamaño y la posición de la ventana» con un botón «Restablecer
  ventana», opciones de historial (guardar historial sí/no, tamaño
  10 / 25 / 50 / 100, vaciar al cerrar sí/no), un botón «Restablecer ajustes» y
  una pantalla «Acerca de» (versión de la app, licencia MIT, enlace al
  repositorio).

### Cambiado

- La app ya no **reabre la última calculadora al arrancar** por defecto; ahora
  arranca en el menú. Se puede volver a activar en la pantalla de ajustes.
- La **pantalla de historial** muestra cada cálculo como una tarjeta, para que
  las entradas ya no se confundan entre sí.

## [0.2.0] - 2026-09-07

### Añadido

- **Historial de cálculos**: pantalla accesible desde la barra del menú con los
  últimos 25 resultados; se guarda entre sesiones y tiene botón «Vaciar». Cada
  entrada muestra su **fecha y hora**, y repetir el mismo cálculo no añade una
  entrada nueva (solo actualiza la hora de la que ya está arriba).
- Al arrancar, la app **reabre la última calculadora** que estuviera abierta al
  cerrarla (si se cerró en el menú, abre el menú).
- Botón **«Copiar»** que pone el resultado en el portapapeles tras un cálculo.
- **Modo oscuro** con interruptor en la barra superior del menú; la preferencia
  se recuerda entre sesiones.
- Desarrollo **«paso a paso»** también para el teorema de Pitágoras, el área del
  cilindro, el porcentaje y la regla de tres (antes solo la ecuación de 2.º grado).

### Cambiado

- **IMC**: la altura se introduce en **centímetros** (antes en metros) y hay un
  selector de sistema de medida: **métrico** (kg, cm) o **imperial** (lb, pies y
  pulgadas). El IMC y su categoría son los mismos; solo cambian los datos de
  entrada. Al cambiar de sistema, si los campos estaban rellenos, se **traspasan
  convertidos** (p. ej. 70 kg / 175 cm → 154.3 lb / 5 ft 8.9 in): los cm sin
  decimales y kg, libras y pulgadas a uno, para que la conversión de ida y vuelta
  vuelva al mismo valor.
- `Formato` usa siempre `Locale.ROOT`: los números se muestran con punto decimal
  y coma para los miles, con independencia del idioma del sistema. Antes, en un
  equipo con configuración regional española, el resultado salía con coma
  decimal aunque el campo de entrada solo admite el punto.
- El texto de ayuda de los campos se lee mejor en los dos temas.
- El paquete `calc` deja de depender de la capa de textos: lanza `CalculationError`
  con la clave del mensaje y sus argumentos, y es la interfaz
  (`ui/ErrorMessages`) quien lo traduce.
- La CI añade **análisis estático con SpotBugs** al *check* de formato.
- **Inglés como idioma por defecto**: el código, los comentarios y las claves de
  i18n pasan al inglés, y `messages.properties` (la base) ahora está en inglés,
  con el español en `messages_es.properties`. La app arranca en inglés si no hay
  un idioma guardado y el selector permite cambiar a español.
- Los nombres de nodo y clave de `java.util.prefs` de los ajustes guardados
  (idioma, tema, historial, última calculadora) pasan al inglés. Como efecto
  secundario, en un equipo que tuviera ajustes guardados de la `0.1.0`, esos
  ajustes se reinician una vez: la app abre en inglés, tema claro, historial
  vacío y en el menú.

## [0.1.0] - 2026-09-06

Primera versión empaquetada.

### Añadido

- 14 calculadoras tras un menú agrupado por categorías (Geometría · Aritmética ·
  Potencias y ecuaciones · Proporciones y porcentajes · Otros): teorema de
  Pitágoras, área de un cilindro, año bisiesto, factorial, múltiplo, aprobado,
  ecuación de 2.º grado, potencia, raíz n-ésima, MCD y MCM, ¿es primo?, conversor
  de bases, porcentaje, regla de tres e IMC.
- Interfaz en español e inglés, con la elección recordada entre sesiones.
- Cálculo asíncrono (la ventana nunca se congela) y cancelable al cambiar de pantalla.
- Desarrollo «paso a paso» opcional para la ecuación de 2.º grado.
- La ventana recuerda su tamaño y posición; el foco empieza en el primer campo.
- Empaquetado para Windows con `jlink` + `jpackage`: instalador `.msi` y versión
  portable, publicados en el GitHub Release al empujar una etiqueta `vX.Y.Z`.
- Integración continua con Spotless, tests (JUnit 5 + TestFX headless) en JDK 17
  y 21, e informe de cobertura con JaCoCo.

[Sin publicar]: https://github.com/Guille87/JavaCalcFX/compare/v0.3.0...HEAD
[0.3.0]: https://github.com/Guille87/JavaCalcFX/compare/v0.2.0...v0.3.0
[0.2.0]: https://github.com/Guille87/JavaCalcFX/compare/v0.1.0...v0.2.0
[0.1.0]: https://github.com/Guille87/JavaCalcFX/releases/tag/v0.1.0
