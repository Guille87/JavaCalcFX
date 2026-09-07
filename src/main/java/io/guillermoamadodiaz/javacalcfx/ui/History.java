package io.guillermoamadodiaz.javacalcfx.ui;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.prefs.Preferences;
import java.util.stream.Collectors;

/**
 * History of the latest calculations: the screen title, the result that was
 * shown and when. The most recent goes first and at most
 * {@link Settings#historyMax()} are kept. Repeating the same calculation (same
 * title and result) does not add a new entry: it just refreshes the timestamp of
 * the one already at the top. Nothing is recorded while
 * {@link Settings#historyEnabled()} is off.
 *
 * <p>It is saved between sessions with {@link Preferences} in its own subnode, as
 * a single string (entries separated by {@code RS}, fields by {@code US}). The
 * result is trimmed to {@link #MAX_RESULT} characters so a huge factorial, say,
 * cannot overflow the preferences size limit.
 */
public final class History {

    /** Maximum length of the result stored per entry. */
    public static final int MAX_RESULT = 300;

    private static final Preferences PREFS =
            Preferences.userNodeForPackage(History.class).node("history");
    private static final String KEY = "entries";
    private static final String ENTRY_SEPARATOR = "\u001e"; // RS: between entries
    private static final String FIELD_SEPARATOR = "\u001f"; // US: between fields

    private static final List<Entry> entries = new ArrayList<>(load());

    private History() {}

    /**
     * A calculation from the history. {@code timestamp} may be {@code null} in
     * entries saved by earlier versions.
     */
    public record Entry(String title, String result, Instant timestamp) {}

    /** The saved calculations, most recent to oldest. */
    public static synchronized List<Entry> recent() {
        return List.copyOf(entries);
    }

    /** Records a calculation (puts it first) and persists it, unless the history is off. */
    public static synchronized void record(String title, String result) {
        if (!Settings.historyEnabled()) {
            return;
        }
        Entry fresh = new Entry(title, trim(result), Instant.now());
        if (!entries.isEmpty()
                && entries.get(0).title().equals(fresh.title())
                && entries.get(0).result().equals(fresh.result())) {
            entries.set(0, fresh); // same calculation repeated: only the timestamp changes
        } else {
            entries.add(0, fresh);
        }
        trimToMax();
        save();
    }

    /** Clears the history. */
    public static synchronized void clear() {
        entries.clear();
        save();
    }

    private static String trim(String text) {
        return text.length() <= MAX_RESULT ? text : text.substring(0, MAX_RESULT) + "…";
    }

    private static void trimToMax() {
        while (entries.size() > Settings.historyMax()) {
            entries.remove(entries.size() - 1);
        }
    }

    private static List<Entry> load() {
        List<Entry> list = new ArrayList<>();
        try {
            String raw = PREFS.get(KEY, "");
            if (!raw.isEmpty()) {
                for (String chunk : raw.split(ENTRY_SEPARATOR, -1)) {
                    String[] fields = chunk.split(FIELD_SEPARATOR, -1);
                    if (fields.length >= 2) {
                        list.add(new Entry(fields[0], fields[1], timestampOf(fields)));
                    }
                }
            }
            while (list.size() > Settings.historyMax()) {
                list.remove(list.size() - 1); // the saved max may have been lowered
            }
        } catch (RuntimeException ignored) {
            // preferences unavailable or value corrupt: start with no history
        }
        return list;
    }

    private static Instant timestampOf(String[] fields) {
        if (fields.length < 3 || fields[2].isEmpty()) {
            return null; // entry from an earlier version, without a date
        }
        try {
            return Instant.ofEpochMilli(Long.parseLong(fields[2]));
        } catch (NumberFormatException corrupt) {
            return null;
        }
    }

    private static void save() {
        try {
            PREFS.put(KEY, entries.stream().map(History::serialize).collect(Collectors.joining(ENTRY_SEPARATOR)));
        } catch (RuntimeException ignored) {
            // if it cannot be persisted (or does not fit), the history still holds for this session
        }
    }

    private static String serialize(Entry entry) {
        String millis =
                entry.timestamp() == null ? "" : Long.toString(entry.timestamp().toEpochMilli());
        return entry.title() + FIELD_SEPARATOR + entry.result() + FIELD_SEPARATOR + millis;
    }
}
