package io.guillermoamadodiaz.javacalcfx.ui;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class TemaTest {

    private boolean inicial;

    @BeforeEach
    void recordarEstado() {
        inicial = Tema.esOscuro();
    }

    @AfterEach
    void restaurarEstado() {
        if (Tema.esOscuro() != inicial) {
            Tema.alternar();
        }
    }

    @Test
    void alternar_cambia_el_estado_y_lo_persiste() {
        boolean nuevo = Tema.alternar();
        assertEquals(!inicial, nuevo);
        assertEquals(!inicial, Tema.esOscuro());
    }

    @Test
    void dos_cambios_vuelven_al_punto_de_partida() {
        Tema.alternar();
        Tema.alternar();
        assertEquals(inicial, Tema.esOscuro());
    }
}
