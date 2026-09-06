package io.guillermoamadodiaz.javacalcfx.i18n;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.text.MessageFormat;
import java.util.Locale;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

class TextosTest {

    private static final String RUTA = "/io/guillermoamadodiaz/javacalcfx/i18n/";

    @AfterEach
    void restaurarIdioma() {
        Textos.usarIdioma(Locale.getDefault());
    }

    private static Properties cargar(String fichero) {
        Properties p = new Properties();
        try (InputStream in = TextosTest.class.getResourceAsStream(RUTA + fichero)) {
            if (in == null) {
                fail("No se encuentra " + fichero);
            }
            try (Reader r = new InputStreamReader(in, StandardCharsets.UTF_8)) {
                p.load(r);
            }
        } catch (IOException e) {
            fail(e);
        }
        return p;
    }

    @Test
    void espanol_y_ingles_tienen_exactamente_las_mismas_claves() {
        Properties es = cargar("messages.properties");
        Properties en = cargar("messages_en.properties");
        assertEquals(es.stringPropertyNames(), en.stringPropertyNames(),
                "los dos ficheros de idioma deben declarar las mismas claves");
    }

    @Test
    void resuelve_textos_en_espanol() {
        Textos.usarIdioma(Locale.forLanguageTag("es"));
        assertEquals("Calcular", Textos.get("form.calcular"));
    }

    @Test
    void resuelve_textos_en_ingles() {
        Textos.usarIdioma(Locale.ENGLISH);
        assertEquals("Calculate", Textos.get("form.calcular"));
    }

    @Test
    void idioma_desconocido_cae_en_el_base_espanol() {
        Textos.usarIdioma(Locale.forLanguageTag("de"));
        assertEquals("Volver", Textos.get("form.volver"));
    }

    /** Regresión: pedir español debe dar español aunque el locale por defecto sea otro. */
    @Test
    void el_idioma_no_depende_del_locale_por_defecto() {
        Locale previo = Locale.getDefault();
        try {
            Locale.setDefault(Locale.ENGLISH);
            Textos.usarIdioma(Locale.forLanguageTag("es"));
            assertEquals("Calcular", Textos.get("form.calcular"));

            Locale.setDefault(Locale.forLanguageTag("es"));
            Textos.usarIdioma(Locale.ENGLISH);
            assertEquals("Calculate", Textos.get("form.calcular"));
        } finally {
            Locale.setDefault(previo);
        }
    }

    @Test
    void sustituye_los_parametros() {
        Textos.usarIdioma(Locale.forLanguageTag("es"));
        assertEquals("El año 2024 es bisiesto.", Textos.get("bisiesto.resultado.si", "2024"));
    }

    @Test
    void todas_las_claves_del_base_se_pueden_resolver() {
        Textos.usarIdioma(Locale.forLanguageTag("es"));
        Properties es = cargar("messages.properties");
        for (String clave : es.stringPropertyNames()) {
            assertTrue(Textos.get(clave).length() >= 0, clave);
        }
    }

    private static final Pattern PLACEHOLDER = Pattern.compile("\\{(\\d+)\\}");

    /**
     * Cada valor con parámetros debe ser un patrón MessageFormat válido: tras
     * formatearlo con argumentos de prueba no puede quedar ningún {@code {n}}
     * literal (fallo típico: una apóstrofe sin duplicar deja de sustituir).
     */
    @ParameterizedTest
    @ValueSource(strings = {"messages.properties", "messages_en.properties"})
    void los_valores_con_parametros_son_patrones_validos(String fichero) {
        Properties p = cargar(fichero);
        for (String clave : p.stringPropertyNames()) {
            String valor = p.getProperty(clave);
            Matcher m = PLACEHOLDER.matcher(valor);
            int maximo = -1;
            while (m.find()) {
                maximo = Math.max(maximo, Integer.parseInt(m.group(1)));
            }
            if (maximo < 0) {
                continue; // sin parámetros
            }
            Object[] args = new Object[maximo + 1];
            for (int i = 0; i <= maximo; i++) {
                args[i] = "X" + i;
            }
            String formateado = MessageFormat.format(valor, args);
            for (int i = 0; i <= maximo; i++) {
                assertTrue(formateado.contains("X" + i),
                        fichero + " / " + clave + ": no sustituye {" + i + "} -> " + formateado);
            }
        }
    }
}
