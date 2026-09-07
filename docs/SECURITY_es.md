# Política de seguridad

<p align="center"><a href="../SECURITY.md">English</a> · <a href="SECURITY_es.md">Español</a></p>

## Versiones con soporte

JavaCalcFX es una pequeña aplicación de escritorio en la línea de versiones
`0.x`. Solo la **última versión publicada** recibe correcciones; las versiones
anteriores no se mantienen.

| Versión | Con soporte |
|---|---|
| última versión `0.x` | ✅ |
| cualquier versión anterior | ❌ |

## Cómo informar de una vulnerabilidad

Por favor, **no abras un *issue* público** para un problema de seguridad.

- Preferido: en la pestaña **Security** del repositorio, usa **«Report a
  vulnerability»** (avisos privados de GitHub).
- O escribe a **guillermo_amado@hotmail.es**.

Incluye la versión, tu sistema operativo y los pasos para reproducirlo. Es un
proyecto personal que se mantiene en el tiempo libre, así que ten paciencia con
el plazo de respuesta; recibirás un acuse de recibo y, cuando se publique la
corrección, crédito en las notas de la versión si lo deseas.

## Alcance

La aplicación funciona completamente sin conexión, incluye su propio *runtime*
de Java y no tiene funciones de red, autenticación ni escritura de ficheros más
allá de `java.util.prefs`, por lo que la superficie de ataque real es pequeña.
También se agradecen los informes sobre la cadena de compilación y publicación
(GitHub Actions, el instalador `.msi`).
