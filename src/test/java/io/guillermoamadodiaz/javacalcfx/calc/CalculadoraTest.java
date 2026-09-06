package io.guillermoamadodiaz.javacalcfx.calc;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import io.guillermoamadodiaz.javacalcfx.calc.Calculadora.Triangulo;
import java.math.BigInteger;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;

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
            assertThrows(IllegalArgumentException.class, () -> Calculadora.resolverTrianguloRectangulo(a, b));
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
            assertThrows(IllegalArgumentException.class, () -> Calculadora.factorial(Calculadora.MAX_FACTORIAL + 1));
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
            assertThrows(IllegalArgumentException.class, () -> Calculadora.media(nota, 5, 5, 5, 5));
        }

        @Test
        void media_admite_los_extremos_del_rango() {
            assertEquals(5.0, Calculadora.media(0, 10, 0, 10, 5), EPS);
        }
    }

    @Nested
    @DisplayName("Ecuación de segundo grado")
    class EcuacionSegundoGrado {

        private double[] raicesReales(double a, double b, double c) {
            var e = Calculadora.resolverEcuacionCuadratica(a, b, c);
            double[] r = {e.x1().real(), e.x2().real()};
            java.util.Arrays.sort(r);
            return r;
        }

        @Test
        void dos_raices_reales_distintas() {
            var e = Calculadora.resolverEcuacionCuadratica(1, -5, 6); // x²-5x+6 -> 2 y 3
            assertTrue(e.tieneRaicesReales());
            assertFalse(e.tieneRaizDoble());
            assertArrayEquals(new double[] {2, 3}, raicesReales(1, -5, 6), EPS);
        }

        @Test
        void raiz_doble() {
            var e = Calculadora.resolverEcuacionCuadratica(1, -4, 4); // (x-2)²
            assertTrue(e.tieneRaizDoble());
            assertEquals(2.0, e.x1().real(), EPS);
            assertEquals(e.x1(), e.x2());
        }

        @Test
        void raices_complejas_conjugadas() {
            var e = Calculadora.resolverEcuacionCuadratica(1, 0, 1); // x²+1 -> ±i
            assertFalse(e.tieneRaicesReales());
            assertEquals(0.0, e.x1().real(), EPS);
            assertEquals(1.0, Math.abs(e.x1().imaginaria()), EPS);
            assertEquals(e.x1().real(), e.x2().real(), EPS);
            assertEquals(-e.x1().imaginaria(), e.x2().imaginaria(), EPS);
        }

        @Test
        void a_cero_no_es_de_segundo_grado() {
            assertThrows(IllegalArgumentException.class, () -> Calculadora.resolverEcuacionCuadratica(0, 2, 1));
        }

        @Test
        void rechaza_coeficientes_no_finitos() {
            assertThrows(
                    IllegalArgumentException.class, () -> Calculadora.resolverEcuacionCuadratica(1, Double.NaN, 1));
        }

        @Test
        void formula_estable_con_b_muy_grande() {
            // x² + 1e8·x + 1 = 0 : raíces ≈ -1e8 y ≈ -1e-8. La fórmula ingenua pierde
            // toda la precisión de la raíz pequeña por cancelación catastrófica.
            double[] r = raicesReales(1, 1e8, 1);
            assertEquals(-1e8, r[0], 1.0);
            assertEquals(-1e-8, r[1], 1e-12);
        }
    }

    @Nested
    @DisplayName("Potencia")
    class Potencia {

        @Test
        void valores_conocidos() {
            assertEquals(1024.0, Calculadora.potencia(2, 10), EPS);
            assertEquals(0.125, Calculadora.potencia(2, -3), EPS);
            assertEquals(1.0, Calculadora.potencia(5, 0), EPS);
        }

        @Test
        void exponente_fraccionario_es_una_raiz() {
            assertEquals(3.0, Calculadora.potencia(9, 0.5), EPS);
        }

        @ParameterizedTest
        @CsvSource({"-8,0.5", "0,-1", "10,400"}) // no real, 0^-1, desbordamiento
        void resultados_no_reales_o_infinitos_se_rechazan(double base, double exponente) {
            assertThrows(IllegalArgumentException.class, () -> Calculadora.potencia(base, exponente));
        }

        @Test
        void rechaza_argumentos_no_finitos() {
            assertThrows(IllegalArgumentException.class, () -> Calculadora.potencia(Double.NaN, 2));
        }
    }

    @Nested
    @DisplayName("Raíz n-ésima")
    class RaizNesima {

        @Test
        void raices_exactas() {
            assertEquals(3.0, Calculadora.raiz(27, 3), 1e-12);
            assertEquals(2.0, Calculadora.raiz(16, 4), 1e-12);
            assertEquals(5.0, Calculadora.raiz(25, 2), 1e-12);
        }

        @Test
        void indice_impar_de_radicando_negativo() {
            assertEquals(-2.0, Calculadora.raiz(-8, 3), 1e-12);
        }

        @Test
        void indice_par_de_radicando_negativo_no_tiene_valor_real() {
            assertThrows(IllegalArgumentException.class, () -> Calculadora.raiz(-4, 2));
        }

        @ParameterizedTest
        @CsvSource({"27,1", "27,0", "27,-3", "27,2.5", "27,NaN"})
        void indice_debe_ser_entero_mayor_o_igual_que_dos(double radicando, double indice) {
            assertThrows(IllegalArgumentException.class, () -> Calculadora.raiz(radicando, indice));
        }

        @Test
        void radicando_cero() {
            assertEquals(0.0, Calculadora.raiz(0, 5), EPS);
        }
    }

    @Nested
    @DisplayName("MCD y MCM")
    class McdYMcm {

        @Test
        void mcd_valores_conocidos() {
            assertEquals(6, Calculadora.mcd(12, 18));
            assertEquals(1, Calculadora.mcd(7, 13));
            assertEquals(5, Calculadora.mcd(-15, 10)); // el signo se ignora
        }

        @Test
        void mcd_con_cero() {
            assertEquals(7, Calculadora.mcd(0, 7));
            assertEquals(7, Calculadora.mcd(7, 0));
            assertEquals(0, Calculadora.mcd(0, 0));
        }

        @Test
        void mcm_valores_conocidos() {
            assertEquals(36, Calculadora.mcm(12, 18));
            assertEquals(91, Calculadora.mcm(7, 13));
            assertEquals(30, Calculadora.mcm(-6, 10));
        }

        @Test
        void mcm_con_cero_es_cero() {
            assertEquals(0, Calculadora.mcm(0, 5));
        }

        @Test
        void mcd_por_mcm_es_el_producto_de_los_valores_absolutos() {
            assertEquals(12L * 18, Calculadora.mcd(12, 18) * Calculadora.mcm(12, 18));
        }

        @Test
        void mcm_que_desborda_un_long_se_rechaza() {
            assertThrows(IllegalArgumentException.class, () -> Calculadora.mcm(1_000_000_000_000L, 999_999_999_999L));
        }
    }

    @Nested
    @DisplayName("Primalidad")
    class Primos {

        @ParameterizedTest
        @ValueSource(longs = {2, 3, 5, 7, 13, 97, 7919, 1_000_000_007L})
        void primos(long n) {
            assertTrue(Calculadora.analizarPrimalidad(n).primo());
        }

        @ParameterizedTest
        @ValueSource(longs = {4, 6, 9, 15, 100, 7917})
        void compuestos(long n) {
            var p = Calculadora.analizarPrimalidad(n);
            assertFalse(p.primo());
            assertTrue(p.compuesto());
            assertEquals(0, n % p.menorDivisorPropio());
        }

        @Test
        void el_divisor_es_el_menor() {
            assertEquals(2, Calculadora.analizarPrimalidad(14).menorDivisorPropio());
            assertEquals(3, Calculadora.analizarPrimalidad(15).menorDivisorPropio()); // 15 = 3·5
        }

        @ParameterizedTest
        @ValueSource(longs = {1, 0, -7})
        void menores_que_dos_no_son_primos_ni_compuestos(long n) {
            var p = Calculadora.analizarPrimalidad(n);
            assertFalse(p.primo());
            assertFalse(p.compuesto());
        }

        @Test
        void se_puede_cancelar_via_interrupcion() throws InterruptedException {
            var abortado = new java.util.concurrent.atomic.AtomicBoolean(false);
            Thread hilo = new Thread(() -> {
                try {
                    Calculadora.analizarPrimalidad(2_305_843_009_213_693_951L); // 2^61 - 1, primo de Mersenne
                } catch (java.util.concurrent.CancellationException e) {
                    abortado.set(true);
                }
            });
            hilo.start();
            Thread.sleep(30);
            hilo.interrupt();
            hilo.join(5_000);
            assertTrue(abortado.get(), "la comprobación debe abortar al interrumpir el hilo");
        }
    }

    @Nested
    @DisplayName("Conversor de bases")
    class ConversorDeBases {

        @Test
        void decimal_a_las_cuatro_bases() {
            var c = Calculadora.convertirBase("255");
            assertEquals("11111111", c.binario());
            assertEquals("377", c.octal());
            assertEquals("255", c.decimal());
            assertEquals("FF", c.hexadecimal());
        }

        @Test
        void reconoce_los_prefijos() {
            assertEquals("255", Calculadora.convertirBase("0xFF").decimal());
            assertEquals("255", Calculadora.convertirBase("0Xff").decimal());
            assertEquals("255", Calculadora.convertirBase("0b11111111").decimal());
            assertEquals("255", Calculadora.convertirBase("0o377").decimal());
        }

        @Test
        void admite_signo() {
            var c = Calculadora.convertirBase("-0xFF");
            assertEquals("-255", c.decimal());
            assertEquals("-11111111", c.binario());
        }

        @Test
        void cero() {
            assertEquals("0", Calculadora.convertirBase("0").hexadecimal());
        }

        @ParameterizedTest
        @ValueSource(strings = {"", "0xGG", "0b102", "abc", "12.5"})
        void texto_no_valido_se_rechaza(String texto) {
            assertThrows(IllegalArgumentException.class, () -> Calculadora.convertirBase(texto));
        }
    }

    @Nested
    @DisplayName("Porcentaje y regla de tres")
    class ProporcionesYPorcentajes {

        @Test
        void porcentaje_de_una_cantidad() {
            assertEquals(30.0, Calculadora.porcentajeDe(15, 200), EPS);
            assertEquals(0.0, Calculadora.porcentajeDe(0, 200), EPS);
            assertEquals(-10.0, Calculadora.porcentajeDe(-5, 200), EPS);
        }

        @Test
        void regla_de_tres_directa() {
            assertEquals(10.0, Calculadora.reglaDeTres(3, 6, 5), EPS); // 3 kg -> 6 €, 5 kg -> 10 €
            assertEquals(30.0, Calculadora.reglaDeTres(100, 15, 200), EPS); // 15 % de 200
        }

        @Test
        void regla_de_tres_con_a_cero_se_rechaza() {
            assertThrows(IllegalArgumentException.class, () -> Calculadora.reglaDeTres(0, 5, 3));
        }

        @Test
        void rechaza_argumentos_no_finitos() {
            assertThrows(IllegalArgumentException.class, () -> Calculadora.porcentajeDe(Double.NaN, 100));
            assertThrows(IllegalArgumentException.class, () -> Calculadora.reglaDeTres(1, Double.POSITIVE_INFINITY, 1));
        }
    }

    @Nested
    @DisplayName("Índice de masa corporal")
    class IndiceMasaCorporal {

        @Test
        void valor_y_formula() {
            assertEquals(22.86, Calculadora.imc(70, 1.75).valor(), 0.01);
        }

        @ParameterizedTest
        @CsvSource({
            "50, 1.75, BAJO_PESO",
            "70, 1.75, NORMAL",
            "80, 1.75, SOBREPESO",
            "100, 1.75, OBESIDAD",
        })
        void clasifica_segun_los_rangos_de_la_oms(double peso, double altura, Calculadora.CategoriaImc esperada) {
            assertEquals(esperada, Calculadora.imc(peso, altura).categoria());
        }

        @Test
        void rechaza_peso_o_altura_no_positivos() {
            assertThrows(IllegalArgumentException.class, () -> Calculadora.imc(0, 1.75));
            assertThrows(IllegalArgumentException.class, () -> Calculadora.imc(70, -1));
            assertThrows(IllegalArgumentException.class, () -> Calculadora.imc(Double.NaN, 1.75));
        }
    }
}
