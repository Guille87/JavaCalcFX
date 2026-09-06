package io.guillermoamadodiaz.javacalcfx.ui;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

class UltimaCalculadoraTest {

    @AfterEach
    void limpiar() {
        UltimaCalculadora.olvidar();
    }

    @Test
    void sin_nada_recordado_devuelve_vacio() {
        UltimaCalculadora.olvidar();
        assertTrue(UltimaCalculadora.recordada().isEmpty());
    }

    @Test
    void recuerda_la_ultima_clave() {
        UltimaCalculadora.recordar("factorial");
        assertEquals("factorial", UltimaCalculadora.recordada().orElseThrow());

        UltimaCalculadora.recordar("cuadratica");
        assertEquals("cuadratica", UltimaCalculadora.recordada().orElseThrow());
    }

    @Test
    void olvidar_borra_lo_recordado() {
        UltimaCalculadora.recordar("imc");
        UltimaCalculadora.olvidar();
        assertTrue(UltimaCalculadora.recordada().isEmpty());
    }
}
