package io.guillermoamadodiaz.javacalcfx.ui;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class EstadoVentanaTest {

    @Test
    void acepta_un_tamano_no_menor_que_el_minimo() {
        assertTrue(EstadoVentana.tamanoValido(800, 600, 655, 490));
        assertTrue(EstadoVentana.tamanoValido(655, 490, 655, 490));
    }

    @Test
    void rechaza_tamanos_por_debajo_del_minimo_o_no_finitos() {
        assertFalse(EstadoVentana.tamanoValido(400, 600, 655, 490));
        assertFalse(EstadoVentana.tamanoValido(800, 300, 655, 490));
        assertFalse(EstadoVentana.tamanoValido(Double.NaN, 600, 655, 490));
        assertFalse(EstadoVentana.tamanoValido(800, Double.POSITIVE_INFINITY, 655, 490));
    }

    @Test
    void un_punto_no_finito_nunca_es_visible() {
        assertFalse(EstadoVentana.puntoVisible(Double.NaN, 0));
        assertFalse(EstadoVentana.puntoVisible(0, Double.NEGATIVE_INFINITY));
    }
}
