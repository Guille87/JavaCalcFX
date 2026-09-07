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

class MessagesTest {

    private static final String PATH = "/io/guillermoamadodiaz/javacalcfx/i18n/";

    @AfterEach
    void restoreLanguage() {
        Messages.useLocale(Locale.getDefault());
    }

    private static Properties read(String file) {
        Properties p = new Properties();
        try (InputStream in = MessagesTest.class.getResourceAsStream(PATH + file)) {
            if (in == null) {
                fail("Cannot find " + file);
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
    void spanish_and_english_declare_exactly_the_same_keys() {
        Properties es = read("messages.properties");
        Properties en = read("messages_en.properties");
        assertEquals(
                es.stringPropertyNames(), en.stringPropertyNames(), "both language files must declare the same keys");
    }

    @Test
    void resolves_text_in_spanish() {
        Messages.useLocale(Locale.forLanguageTag("es"));
        assertEquals("Calcular", Messages.get("form.calcular"));
    }

    @Test
    void resolves_text_in_english() {
        Messages.useLocale(Locale.ENGLISH);
        assertEquals("Calculate", Messages.get("form.calcular"));
    }

    @Test
    void an_unknown_language_falls_back_to_the_spanish_base() {
        Messages.useLocale(Locale.forLanguageTag("de"));
        assertEquals("Volver", Messages.get("form.volver"));
    }

    /** Regression: asking for Spanish must yield Spanish even if the default locale is another. */
    @Test
    void the_language_does_not_depend_on_the_default_locale() {
        Locale previous = Locale.getDefault();
        try {
            Locale.setDefault(Locale.ENGLISH);
            Messages.useLocale(Locale.forLanguageTag("es"));
            assertEquals("Calcular", Messages.get("form.calcular"));

            Locale.setDefault(Locale.forLanguageTag("es"));
            Messages.useLocale(Locale.ENGLISH);
            assertEquals("Calculate", Messages.get("form.calcular"));
        } finally {
            Locale.setDefault(previous);
        }
    }

    @Test
    void substitutes_the_parameters() {
        Messages.useLocale(Locale.forLanguageTag("es"));
        assertEquals("El año 2024 es bisiesto.", Messages.get("bisiesto.resultado.si", "2024"));
    }

    @Test
    void every_key_in_the_base_can_be_resolved() {
        Messages.useLocale(Locale.forLanguageTag("es"));
        Properties es = read("messages.properties");
        for (String key : es.stringPropertyNames()) {
            assertTrue(Messages.get(key).length() >= 0, key);
        }
    }

    private static final Pattern PLACEHOLDER = Pattern.compile("\\{(\\d+)\\}");

    /**
     * Every parametrized value must be a valid MessageFormat pattern: after
     * formatting it with test arguments no literal {@code {n}} may remain
     * (typical failure: an un-doubled apostrophe stops substitution).
     */
    @ParameterizedTest
    @ValueSource(strings = {"messages.properties", "messages_en.properties"})
    void parametrized_values_are_valid_patterns(String file) {
        Properties p = read(file);
        for (String key : p.stringPropertyNames()) {
            String value = p.getProperty(key);
            Matcher m = PLACEHOLDER.matcher(value);
            int max = -1;
            while (m.find()) {
                max = Math.max(max, Integer.parseInt(m.group(1)));
            }
            if (max < 0) {
                continue; // no parameters
            }
            Object[] args = new Object[max + 1];
            for (int i = 0; i <= max; i++) {
                args[i] = "X" + i;
            }
            String formatted = MessageFormat.format(value, args);
            for (int i = 0; i <= max; i++) {
                assertTrue(
                        formatted.contains("X" + i),
                        file + " / " + key + ": does not substitute {" + i + "} -> " + formatted);
            }
        }
    }
}
