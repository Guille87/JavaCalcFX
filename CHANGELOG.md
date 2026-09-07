# Registro de cambios

Todos los cambios notables del proyecto se documentan en este archivo.

El formato se basa en [Keep a Changelog](https://keepachangelog.com/es-ES/1.1.0/)
y el proyecto sigue [versionado semántico](https://semver.org/lang/es/).

## [Sin publicar]

### Añadido

- **Historial de cálculos**: pantalla accesible desde la barra del menú con los
  últimos 25 resultados; se guarda entre sesiones y tiene botón «Vaciar».
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
  entrada.
- `Formato` usa siempre `Locale.ROOT`: los números se muestran con punto decimal
  y coma para los miles, con independencia del idioma del sistema. Antes, en un
  equipo con configuración regional española, el resultado salía con coma
  decimal aunque el campo de entrada solo admite el punto.
- El texto de ayuda de los campos se lee mejor en los dos temas.
- El paquete `calc` deja de depender de la capa de textos: lanza `ErrorDeCalculo`
  con la clave del mensaje y sus argumentos, y es la interfaz
  (`ui/MensajesDeError`) quien lo traduce.
- La CI añade **análisis estático con SpotBugs** al *check* de formato.

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

[Sin publicar]: https://github.com/Guille87/JavaCalcFX/compare/v0.1.0...HEAD
[0.1.0]: https://github.com/Guille87/JavaCalcFX/releases/tag/v0.1.0
