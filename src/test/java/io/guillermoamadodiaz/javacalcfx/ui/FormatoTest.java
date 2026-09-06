package io.guillermoamadodiaz.javacalcfx.ui;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigInteger;
import org.junit.jupiter.api.Test;

class FormatoTest {

    /** Quita todo lo que no sea dígito, para comparar sin depender del locale. */
    private static String soloDigitos(String s) {
        return s.replaceAll("\\D", "");
    }

    @Test
    void numero_entero_sin_decimales() {
        assertEquals("5", Formato.numero(5));
    }

    @Test
    void numero_recorta_a_cuatro_decimales_sin_ceros_finales() {
        assertEquals("20711", soloDigitos(Formato.numero(2.07106781)));
    }

    @Test
    void numero_agrupa_los_miles() {
        String s = Formato.numero(12345);
        assertEquals("12345", soloDigitos(s));
        assertTrue(s.length() > 5, "debe incluir un separador de miles: " + s);
    }

    @Test
    void dos_decimales_siempre_dos_posiciones() {
        assertEquals("500", soloDigitos(Formato.dosDecimales(5)));
    }

    @Test
    void entero_grande_con_separador_de_miles() {
        String s = Formato.enteroGrande(new BigInteger("1000000"));
        assertEquals("1000000", soloDigitos(s));
        assertTrue(s.length() > 7, "debe incluir separadores de miles: " + s);
    }
}
