package io.guillermoamadodiaz.javacalcfx.ui;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.Instant;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class HistorialTest {

    @BeforeEach
    void empezarLimpio() {
        Historial.limpiar();
    }

    @AfterAll
    static void limpiar() {
        Historial.limpiar();
    }

    @Test
    void el_mas_reciente_va_primero() {
        Historial.registrar("Factorial", "El factorial de 5 es 120");
        Historial.registrar("Año Bisiesto", "El año 2000 es bisiesto.");

        var reciente = Historial.reciente();
        assertEquals("Año Bisiesto", reciente.get(0).titulo());
        assertEquals("Factorial", reciente.get(1).titulo());
    }

    @Test
    void no_pasa_del_maximo_y_descarta_los_mas_antiguos() {
        for (int i = 1; i <= Historial.MAXIMO + 5; i++) {
            Historial.registrar("Cálculo " + i, "resultado " + i);
        }
        var reciente = Historial.reciente();
        assertEquals(Historial.MAXIMO, reciente.size());
        assertEquals("Cálculo " + (Historial.MAXIMO + 5), reciente.get(0).titulo());
        assertFalse(reciente.stream().anyMatch(e -> e.titulo().equals("Cálculo 1")));
    }

    @Test
    void recorta_un_resultado_demasiado_largo() {
        Historial.registrar("Factorial", "x".repeat(5_000));
        String guardado = Historial.reciente().get(0).resultado();
        assertTrue(guardado.length() <= Historial.MAX_RESULTADO + 1, "longitud: " + guardado.length());
        assertTrue(guardado.endsWith("…"));
    }

    @Test
    void limpiar_deja_el_historial_vacio() {
        Historial.registrar("Factorial", "El factorial de 5 es 120");
        Historial.limpiar();
        assertTrue(Historial.reciente().isEmpty());
    }

    @Test
    void repetir_el_mismo_calculo_no_lo_duplica() {
        Historial.registrar("IMC", "IMC: 22.86");
        Historial.registrar("IMC", "IMC: 22.86");
        Historial.registrar("IMC", "IMC: 22.86");

        assertEquals(1, Historial.reciente().size(), "el mismo cálculo repetido no añade entradas");
    }

    @Test
    void un_calculo_distinto_si_se_anade() {
        Historial.registrar("IMC", "IMC: 22.86");
        Historial.registrar("IMC", "IMC: 25.10");
        assertEquals(2, Historial.reciente().size());
    }

    @Test
    void cada_entrada_lleva_la_hora() {
        Instant antes = Instant.now();
        Historial.registrar("Factorial", "El factorial de 5 es 120");
        Instant momento = Historial.reciente().get(0).momento();

        assertNotNull(momento);
        assertFalse(momento.isBefore(antes.minusSeconds(1)));
        assertFalse(momento.isAfter(Instant.now().plusSeconds(1)));
    }
}
