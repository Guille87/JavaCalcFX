package io.guillermoamadodiaz.javacalcfx.calc;

import io.guillermoamadodiaz.javacalcfx.i18n.Textos;
import java.math.BigInteger;
import java.util.concurrent.CancellationException;

/**
 * Lógica matemática pura de la aplicación.
 *
 * <p>Esta clase no depende de JavaFX ni de ninguna capa de interfaz: recibe
 * valores primitivos, valida sus precondiciones y devuelve resultados o lanza
 * {@link IllegalArgumentException} con un mensaje ya traducido ({@link Textos}).
 * De este modo toda la aritmética es verificable con tests unitarios.
 */
public final class Calculadora {

    /** Límite de {@link #factorial(int)} para evitar bloqueos por cómputo desbordado. */
    public static final int MAX_FACTORIAL = 100_000;

    private Calculadora() {}

    /**
     * Resuelve un triángulo rectángulo a partir de sus dos catetos.
     *
     * @param catetoA longitud del cateto A (&gt; 0)
     * @param catetoB longitud del cateto B (&gt; 0)
     * @throws IllegalArgumentException si algún cateto no es un número finito y positivo
     */
    public static Triangulo resolverTrianguloRectangulo(double catetoA, double catetoB) {
        exigirPositivoFinito(catetoA, Textos.get("calc.nombre.catetoA"));
        exigirPositivoFinito(catetoB, Textos.get("calc.nombre.catetoB"));

        double hipotenusa = Math.hypot(catetoA, catetoB); // estable frente a overflow
        double area = (catetoA * catetoB) / 2.0;
        double perimetro = catetoA + catetoB + hipotenusa;
        double alfa = Math.toDegrees(Math.atan2(catetoB, catetoA));
        double beta = Math.toDegrees(Math.atan2(catetoA, catetoB));
        return new Triangulo(catetoA, catetoB, hipotenusa, area, perimetro, alfa, beta);
    }

    /**
     * Área total (superficie lateral + dos bases) de un cilindro recto.
     *
     * @param radio  radio de la base (&ge; 0)
     * @param altura altura del cilindro (&ge; 0)
     */
    public static double areaCilindro(double radio, double altura) {
        exigirNoNegativoFinito(radio, Textos.get("calc.nombre.radio"));
        exigirNoNegativoFinito(altura, Textos.get("calc.nombre.altura"));
        return 2 * Math.PI * radio * (radio + altura);
    }

    /**
     * Indica si un año del calendario gregoriano proléptico es bisiesto.
     *
     * @throws IllegalArgumentException si {@code anio <= 0}
     */
    public static boolean esBisiesto(int anio) {
        if (anio <= 0) {
            throw new IllegalArgumentException(Textos.get("calc.anio.no.positivo"));
        }
        return (anio % 4 == 0 && anio % 100 != 0) || anio % 400 == 0;
    }

    /**
     * Factorial de un entero no negativo.
     *
     * @throws IllegalArgumentException si {@code n < 0} o {@code n > }{@link #MAX_FACTORIAL}
     */
    public static BigInteger factorial(int n) {
        if (n < 0) {
            throw new IllegalArgumentException(Textos.get("calc.factorial.negativo"));
        }
        if (n > MAX_FACTORIAL) {
            throw new IllegalArgumentException(Textos.get("calc.factorial.grande", String.valueOf(MAX_FACTORIAL)));
        }
        BigInteger resultado = BigInteger.ONE;
        for (int i = 2; i <= n; i++) {
            // Permite que quien ejecute este cálculo en un hilo aparte (Task) lo aborte.
            if (Thread.currentThread().isInterrupted()) {
                throw new CancellationException(Textos.get("calc.factorial.cancelado"));
            }
            resultado = resultado.multiply(BigInteger.valueOf(i));
        }
        return resultado;
    }

    /**
     * Indica si {@code a} es múltiplo de {@code b}. Por convención matemática todo
     * entero es múltiplo de 0 solo si él mismo es 0, y 0 es múltiplo de cualquier entero.
     */
    public static boolean esMultiplo(long a, long b) {
        if (b == 0) {
            return a == 0;
        }
        return a % b == 0;
    }

    /** Nota mínima y máxima admitidas por {@link #media(double...)}. */
    public static final double NOTA_MINIMA = 0.0;

    public static final double NOTA_MAXIMA = 10.0;

    /**
     * Media aritmética de un conjunto no vacío de notas, cada una en el rango
     * {@code [NOTA_MINIMA, NOTA_MAXIMA]}.
     *
     * @throws IllegalArgumentException si no se pasa ninguna nota o si alguna
     *     queda fuera del rango permitido
     */
    public static double media(double... notas) {
        if (notas == null || notas.length == 0) {
            throw new IllegalArgumentException(Textos.get("calc.notas.vacio"));
        }
        double suma = 0;
        for (double nota : notas) {
            if (!Double.isFinite(nota) || nota < NOTA_MINIMA || nota > NOTA_MAXIMA) {
                throw new IllegalArgumentException(Textos.get(
                        "calc.nota.rango", String.valueOf((int) NOTA_MINIMA), String.valueOf((int) NOTA_MAXIMA)));
            }
            suma += nota;
        }
        return suma / notas.length;
    }

    /** Un alumno aprueba con media mayor o igual a 5. */
    public static boolean estaAprobado(double media) {
        return media >= 5.0;
    }

    /**
     * Resuelve la ecuación de segundo grado {@code a·x² + b·x + c = 0}.
     *
     * <p>Usa una fórmula numéricamente estable (evita la cancelación catastrófica
     * de {@code (-b ± √Δ) / 2a} cuando {@code b²} domina a {@code 4ac}).
     *
     * @throws IllegalArgumentException si {@code a} es 0 (no sería de segundo
     *     grado) o si algún coeficiente no es finito
     */
    public static EcuacionCuadratica resolverEcuacionCuadratica(double a, double b, double c) {
        exigirFinito(a, Textos.get("calc.nombre.coefA"));
        exigirFinito(b, Textos.get("calc.nombre.coefB"));
        exigirFinito(c, Textos.get("calc.nombre.coefC"));
        if (a == 0.0) {
            throw new IllegalArgumentException(Textos.get("calc.cuadratica.no.cuadratica"));
        }

        double discriminante = b * b - 4 * a * c;
        if (discriminante == 0.0) {
            Raiz doble = new Raiz(-b / (2 * a), 0);
            return new EcuacionCuadratica(discriminante, doble, doble);
        }
        if (discriminante > 0) {
            double raizDelta = Math.sqrt(discriminante);
            double q = -0.5 * (b + Math.copySign(raizDelta, b));
            return new EcuacionCuadratica(discriminante, new Raiz(q / a, 0), new Raiz(c / q, 0));
        }
        double parteReal = -b / (2 * a);
        double parteImaginaria = Math.sqrt(-discriminante) / (2 * a);
        return new EcuacionCuadratica(
                discriminante, new Raiz(parteReal, parteImaginaria), new Raiz(parteReal, -parteImaginaria));
    }

    private static void exigirFinito(double valor, String nombre) {
        if (!Double.isFinite(valor)) {
            throw new IllegalArgumentException(Textos.get("calc.valor.no.finito", nombre));
        }
    }

    private static void exigirPositivoFinito(double valor, String nombre) {
        if (!Double.isFinite(valor) || valor <= 0) {
            throw new IllegalArgumentException(Textos.get("calc.valor.no.positivo", nombre));
        }
    }

    private static void exigirNoNegativoFinito(double valor, String nombre) {
        if (!Double.isFinite(valor) || valor < 0) {
            throw new IllegalArgumentException(Textos.get("calc.valor.negativo", nombre));
        }
    }

    /**
     * Resultado inmutable de {@link #resolverTrianguloRectangulo(double, double)}.
     * Los ángulos se expresan en grados.
     */
    public record Triangulo(
            double catetoA,
            double catetoB,
            double hipotenusa,
            double area,
            double perimetro,
            double anguloAlfa,
            double anguloBeta) {}

    /** Una raíz: {@code real + imaginaria·i}. Si {@link #esReal()}, {@code imaginaria == 0}. */
    public record Raiz(double real, double imaginaria) {
        public boolean esReal() {
            return imaginaria == 0.0;
        }
    }

    /** Solución de {@link #resolverEcuacionCuadratica(double, double, double)}. */
    public record EcuacionCuadratica(double discriminante, Raiz x1, Raiz x2) {
        public boolean tieneRaicesReales() {
            return discriminante >= 0.0;
        }

        public boolean tieneRaizDoble() {
            return discriminante == 0.0;
        }
    }
}
