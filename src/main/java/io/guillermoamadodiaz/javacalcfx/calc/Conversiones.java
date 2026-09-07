package io.guillermoamadodiaz.javacalcfx.calc;

/**
 * Conversiones entre unidades a las del SI (kilogramos y metros), para poder
 * reutilizar la misma lógica de cálculo con datos en distintos sistemas.
 *
 * <p>Funciones puras, sin validación: quien las use pasa el resultado a la
 * calculadora, que ya comprueba sus precondiciones.
 */
public final class Conversiones {

    private static final double LIBRA_EN_KILOS = 0.453_592_37;
    private static final double PULGADA_EN_METROS = 0.025_4;
    private static final int PULGADAS_POR_PIE = 12;

    private Conversiones() {}

    /** Libras a kilogramos. */
    public static double librasAKilos(double libras) {
        return libras * LIBRA_EN_KILOS;
    }

    /** Kilogramos a libras. */
    public static double kilosALibras(double kilos) {
        return kilos / LIBRA_EN_KILOS;
    }

    /** Centímetros a metros. */
    public static double centimetrosAMetros(double centimetros) {
        return centimetros / 100.0;
    }

    /** Metros a centímetros. */
    public static double metrosACentimetros(double metros) {
        return metros * 100.0;
    }

    /** Pies y pulgadas (p. ej. {@code 5}, {@code 9}) a metros. */
    public static double piesYPulgadasAMetros(double pies, double pulgadas) {
        return (pies * PULGADAS_POR_PIE + pulgadas) * PULGADA_EN_METROS;
    }

    /**
     * Metros a {@code [pies, pulgadas]}, con las pulgadas en {@code [0, 12)} y
     * pudiendo llevar decimales (p. ej. {@code 1.75 m → [5, 8.9…]}).
     */
    public static double[] metrosAPiesYPulgadas(double metros) {
        double totalPulgadas = metros / PULGADA_EN_METROS;
        double pies = Math.floor(totalPulgadas / PULGADAS_POR_PIE);
        return new double[] {pies, totalPulgadas - pies * PULGADAS_POR_PIE};
    }
}
