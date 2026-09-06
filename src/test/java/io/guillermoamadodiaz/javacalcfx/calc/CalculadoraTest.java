package io.guillermoamadodiaz.javacalcfx.calc;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigInteger;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;

import io.guillermoamadodiaz.javacalcfx.calc.Calculadora.Triangulo;

class CalculadoraTest {

    private static final double EPS = 1e-9;

    @Nested
    @DisplayName("Triángulo rectángulo")
    class TrianguloRectangulo {

        @Test
        void terna_3_4_5() {
            Triangulo t = Calculadora.resolverTrianguloRectangulo(3, 4);
            assertEquals(5.0, t.hipotenusa(), EPS);
            assertEquals(6.0, t.area(), EPS);
            assertEquals(12.0, t.perimetro(), EPS);
            assertEquals(90.0, t.anguloAlfa() + t.anguloBeta(), EPS);
        }

        @Test
        void hipotenusa_no_desborda_con_catetos_enormes() {
            Triangulo t = Calculadora.resolverTrianguloRectangulo(3e200, 4e200);
            assertEquals(5e200, t.hipotenusa(), 1e190);
        }

        @ParameterizedTest
        @CsvSource({"0,1", "-1,1", "1,0", "NaN,1"})
        void rechaza_catetos_no_positivos(double a, double b) {
            assertThrows(IllegalArgumentException.class,
                    () -> Calculadora.resolverTrianguloRectangulo(a, b));
        }
    }

    @Nested
    @DisplayName("Área de cilindro")
    class AreaCilindro {

        @Test
        void valor_conocido() {
            // 2*pi*r*(r+h) con r=1, h=1 -> 4*pi
            assertEquals(4 * Math.PI, Calculadora.areaCilindro(1, 1), EPS);
        }

        @Test
        void admite_cero() {
            assertEquals(0.0, Calculadora.areaCilindro(0, 5), EPS);
        }

        @Test
        void rechaza_negativos() {
            assertThrows(IllegalArgumentException.class, () -> Calculadora.areaCilindro(-1, 2));
            assertThrows(IllegalArgumentException.class, () -> Calculadora.areaCilindro(2, -1));
        }
    }

    @Nested
    @DisplayName("Año bisiesto")
    class AnioBisiesto {

        @ParameterizedTest
        @ValueSource(ints = {2000, 2024, 1600, 2400})
        void bisiestos(int anio) {
            assertTrue(Calculadora.esBisiesto(anio));
        }

        @ParameterizedTest
        @ValueSource(ints = {1900, 2023, 2100, 1})
        void no_bisiestos(int anio) {
            assertFalse(Calculadora.esBisiesto(anio));
        }

        @ParameterizedTest
        @ValueSource(ints = {0, -4})
        void rechaza_anios_no_positivos(int anio) {
            assertThrows(IllegalArgumentException.class, () -> Calculadora.esBisiesto(anio));
        }
    }

    @Nested
    @DisplayName("Factorial")
    class Factorial {

        @Test
        void casos_base() {
            assertEquals(BigInteger.ONE, Calculadora.factorial(0));
            assertEquals(BigInteger.ONE, Calculadora.factorial(1));
        }

        @Test
        void valores_conocidos() {
            assertEquals(BigInteger.valueOf(120), Calculadora.factorial(5));
            assertEquals(new BigInteger("2432902008176640000"), Calculadora.factorial(20));
        }

        @Test
        void rechaza_negativos() {
            assertThrows(IllegalArgumentException.class, () -> Calculadora.factorial(-1));
        }

        @Test
        void rechaza_desmesurado() {
            assertThrows(IllegalArgumentException.class,
                    () -> Calculadora.factorial(Calculadora.MAX_FACTORIAL + 1));
        }

        @Test
        void se_puede_cancelar_via_interrupcion() throws InterruptedException {
            var abortado = new java.util.concurrent.atomic.AtomicBoolean(false);
            Thread hilo = new Thread(() -> {
                try {
                    Calculadora.factorial(Calculadora.MAX_FACTORIAL);
                } catch (java.util.concurrent.CancellationException e) {
                    abortado.set(true);
                }
            });
            hilo.start();
            Thread.sleep(20);
            hilo.interrupt();
            hilo.join(5_000);
            assertTrue(abortado.get(), "el factorial debe abortar al interrumpir el hilo");
        }
    }

    @Nested
    @DisplayName("Múltiplos")
    class Multiplos {

        @ParameterizedTest
        @CsvSource({"10,5", "0,7", "-9,3", "5,1"})
        void es_multiplo(long a, long b) {
            assertTrue(Calculadora.esMultiplo(a, b));
        }

        @ParameterizedTest
        @CsvSource({"10,3", "7,0", "1,5"})
        void no_es_multiplo(long a, long b) {
            assertFalse(Calculadora.esMultiplo(a, b));
        }

        @Test
        void cero_entre_cero() {
            assertTrue(Calculadora.esMultiplo(0, 0));
        }

        @Test
        void no_lanza_con_divisor_cero() {
            Calculadora.esMultiplo(5, 0); // antes provocaba ArithmeticException
        }
    }

    @Nested
    @DisplayName("Notas y aprobado")
    class Notas {

        @Test
        void media_y_aprobado() {
            double media = Calculadora.media(5, 5, 5, 5, 5);
            assertEquals(5.0, media, EPS);
            assertTrue(Calculadora.estaAprobado(media));
        }

        @Test
        void suspendido() {
            double media = Calculadora.media(4, 4, 4, 4, 4);
            assertFalse(Calculadora.estaAprobado(media));
        }

        @Test
        void media_exige_datos() {
            assertThrows(IllegalArgumentException.class, Calculadora::media);
        }

        @ParameterizedTest
        @CsvSource({"11", "-1", "10.5", "NaN"})
        void media_rechaza_notas_fuera_de_rango(double nota) {
            assertThrows(IllegalArgumentException.class,
                    () -> Calculadora.media(nota, 5, 5, 5, 5));
        }

        @Test
        void media_admite_los_extremos_del_rango() {
            assertEquals(5.0, Calculadora.media(0, 10, 0, 10, 5), EPS);
        }
    }
}
