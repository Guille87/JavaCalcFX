package io.guillermoamadodiaz.javacalcfx.i18n;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.text.MessageFormat;
import java.util.Locale;
import java.util.MissingResourceException;
import java.util.Properties;
import java.util.prefs.Preferences;

/**
 * Central access point for the application's translatable text.
 *
 * <p>The initial language is the one saved in the last session or, failing that,
 * the system's ({@link Locale#getDefault()}). {@link #select} changes and
 * remembers it. The {@code messages*.properties} files are read directly (not via
 * {@link java.util.ResourceBundle}, whose lookup depends on the default locale):
 * {@code messages.properties} is English and serves as the base; each other
 * language layers its own file on top. No JavaFX dependencies.
 */
public final class Messages {

    private static final String PATH = "/io/guillermoamadodiaz/javacalcfx/i18n/";
    private static final String LANGUAGE_KEY = "language";

    private static Language language = initialLanguage();
    private static Properties texts = load(language);

    private Messages() {}

    private static Preferences preferences() {
        return Preferences.userNodeForPackage(Messages.class);
    }

    private static Language initialLanguage() {
        try {
            String saved = preferences().get(LANGUAGE_KEY, null);
            if (saved != null) {
                return Language.valueOf(saved);
            }
        } catch (RuntimeException ignored) {
            // preferences unavailable or value corrupt: fall back to the system language
        }
        return Language.from(Locale.getDefault());
    }

    private static Properties read(String file) {
        Properties p = new Properties();
        try (InputStream in = Messages.class.getResourceAsStream(PATH + file)) {
            if (in == null) {
                throw new MissingResourceException("Cannot find " + file, Messages.class.getName(), file);
            }
            try (Reader r = new InputStreamReader(in, StandardCharsets.UTF_8)) {
                p.load(r);
            }
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
        return p;
    }

    private static Properties load(Language language) {
        Properties base = read("messages.properties"); // English, always present
        if (language == Language.ENGLISH) {
            return base;
        }
        Properties p = new Properties(base); // anything missing falls back to English
        p.putAll(read("messages_" + language.locale().getLanguage() + ".properties"));
        return p;
    }

    /** Active language. */
    public static Language language() {
        return language;
    }

    /** Changes the language and remembers it for future starts. */
    public static void select(Language next) {
        language = next;
        texts = load(next);
        try {
            preferences().put(LANGUAGE_KEY, next.name());
        } catch (RuntimeException ignored) {
            // if it cannot be persisted, the change still holds for this session
        }
    }

    /** Changes the language without persisting it. Used by tests. */
    public static void useLocale(Locale locale) {
        language = Language.from(locale);
        texts = load(language);
    }

    /** Text associated with {@code key}. */
    public static String get(String key) {
        String value = texts.getProperty(key);
        if (value == null) {
            throw new MissingResourceException("Missing key " + key, Messages.class.getName(), key);
        }
        return value;
    }

    /** Text of {@code key} with {@code {0}}, {@code {1}}… replaced by {@code args}. */
    public static String get(String key, Object... args) {
        return MessageFormat.format(get(key), args);
    }
}
