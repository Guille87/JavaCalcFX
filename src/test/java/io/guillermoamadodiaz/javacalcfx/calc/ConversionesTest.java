package io.guillermoamadodiaz.javacalcfx.calc;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

class ConversionesTest {

    private static final double EPS = 1e-9;

    @Test
    void libras_a_kilos() {
        assertEquals(0.45359237, Conversiones.librasAKilos(1), EPS);
        assertEquals(70.0, Conversiones.librasAKilos(154.323583529), 1e-6);
    }

    @Test
    void centimetros_a_metros() {
        assertEquals(1.75, Conversiones.centimetrosAMetros(175), EPS);
        assertEquals(0.0, Conversiones.centimetrosAMetros(0), EPS);
    }

    @Test
    void pies_y_pulgadas_a_metros() {
        assertEquals(1.8288, Conversiones.piesYPulgadasAMetros(6, 0), EPS); // 72 in
        assertEquals(1.7526, Conversiones.piesYPulgadasAMetros(5, 9), EPS); // 69 in
        assertEquals(0.0254, Conversiones.piesYPulgadasAMetros(0, 1), EPS);
    }

    @Test
    void las_conversiones_inversas_cuadran() {
        assertEquals(154.32358, Conversiones.kilosALibras(70), 1e-5);
        assertEquals(180.0, Conversiones.metrosACentimetros(1.8), EPS);

        double[] ftIn = Conversiones.metrosAPiesYPulgadas(1.8288); // exactamente 6'0"
        assertEquals(6.0, ftIn[0], EPS);
        assertEquals(0.0, ftIn[1], 1e-9);

        double[] otro = Conversiones.metrosAPiesYPulgadas(1.7526); // 5'9"
        assertEquals(5.0, otro[0], EPS);
        assertEquals(9.0, otro[1], 1e-6);
    }
}
