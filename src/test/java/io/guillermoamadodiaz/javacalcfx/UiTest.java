package io.guillermoamadodiaz.javacalcfx;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.testfx.api.FxAssert.verifyThat;
import static org.testfx.matcher.control.LabeledMatchers.hasText;

import io.guillermoamadodiaz.javacalcfx.i18n.Messages;
import io.guillermoamadodiaz.javacalcfx.ui.History;
import io.guillermoamadodiaz.javacalcfx.ui.LastCalculator;
import io.guillermoamadodiaz.javacalcfx.ui.Theme;
import io.guillermoamadodiaz.javacalcfx.ui.WindowState;
import java.util.Locale;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.prefs.Preferences;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.input.Clipboard;
import javafx.scene.input.KeyCode;
import javafx.stage.Stage;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.testfx.framework.junit5.ApplicationTest;
import org.testfx.util.WaitForAsyncUtils;

/**
 * Interface tests with TestFX (headless, via Monocle). They cover menu ↔ form
 * navigation and that results and errors appear in the label.
 */
class UiTest extends ApplicationTest {

    @BeforeAll
    static void inEnglish() {
        Messages.useLocale(Locale.ENGLISH);
    }

    @BeforeEach
    void cleanHistory() {
        History.clear();
    }

    @AfterAll
    static void cleanPreferences() {
        History.clear();
        LastCalculator.forget();
        try {
            Preferences.userNodeForPackage(WindowState.class).clear();
            Preferences.userNodeForPackage(Theme.class).node("theme").clear();
            Preferences.userNodeForPackage(History.class).node("history").clear();
        } catch (Exception ignored) {
            // no persistence available in the test environment: nothing to clean
        }
    }

    @Override
    public void start(Stage stage) {
        LastCalculator.forget(); // every test starts on the menu
        new CalculatorApp().start(stage);
        // The categorized menu is tall; give it a large window so TestFX can see
        // and click any button without depending on the scroll.
        stage.setWidth(900);
        stage.setHeight(1000);
    }

    private String resultText() {
        return lookup(".result").queryAs(Label.class).getText();
    }

    @Test
    void the_menu_shows_the_title_and_the_calculators() {
        verifyThat(".title", hasText("Math Calculator"));
        assertTrue(lookup("Pythagorean Theorem").tryQuery().isPresent());
        assertTrue(lookup("Pass / Fail").tryQuery().isPresent());
    }

    @Test
    void the_menu_groups_the_calculators_by_category() {
        assertTrue(lookup(".category").queryAll().size() >= 3);
        assertTrue(lookup("Geometry").tryQuery().isPresent());
        assertTrue(lookup("Arithmetic").tryQuery().isPresent());
    }

    @Test
    void the_focus_starts_in_the_first_field() {
        clickOn("Cylinder Surface Area");
        WaitForAsyncUtils.waitForFxEvents();
        TextField first = lookup(".text-field").nth(0).queryAs(TextField.class);
        assertTrue(first.isFocused(), "the first field must have the focus when the form opens");
    }

    @Test
    void open_a_calculator_and_go_back_with_the_button() {
        clickOn("Cylinder Surface Area");
        verifyThat(".header", hasText("Cylinder Surface Area"));

        clickOn("Back");
        verifyThat(".title", hasText("Math Calculator"));
    }

    @Test
    void the_esc_key_goes_back_to_the_menu_from_a_field() {
        clickOn("Factorial");
        clickOn(".text-field").write("5");
        press(KeyCode.ESCAPE).release(KeyCode.ESCAPE);

        verifyThat(".title", hasText("Math Calculator"));
    }

    @Test
    void a_valid_calculation_shows_the_result() throws TimeoutException {
        clickOn("Pythagorean Theorem");
        clickOn(lookup(".text-field").nth(0).queryAs(TextField.class)).write("3");
        clickOn(lookup(".text-field").nth(1).queryAs(TextField.class)).write("4");
        clickOn("Calculate");

        WaitForAsyncUtils.waitFor(5, TimeUnit.SECONDS, () -> resultText().contains("Hypotenuse"));
        assertTrue(resultText().contains("5"), "hypotenuse 3-4-5, was: " + resultText());
    }

    @Test
    void the_show_steps_button_unfolds_the_explanation() throws TimeoutException {
        clickOn("Pythagorean Theorem");
        clickOn(lookup(".text-field").nth(0).queryAs(TextField.class)).write("3");
        clickOn(lookup(".text-field").nth(1).queryAs(TextField.class)).write("4");
        clickOn("Calculate");
        WaitForAsyncUtils.waitFor(5, TimeUnit.SECONDS, () -> resultText().contains("Hypotenuse"));

        clickOn("Show steps");
        String steps = lookup(".steps").queryAs(Label.class).getText();
        assertTrue(steps.contains("h = √(9 + 16)"), "expected the Pythagoras explanation, was: " + steps);
    }

    @Test
    void the_copy_button_puts_the_result_on_the_clipboard() throws TimeoutException {
        clickOn("Pythagorean Theorem");
        clickOn(lookup(".text-field").nth(0).queryAs(TextField.class)).write("3");
        clickOn(lookup(".text-field").nth(1).queryAs(TextField.class)).write("4");
        clickOn("Calculate");
        WaitForAsyncUtils.waitFor(5, TimeUnit.SECONDS, () -> resultText().contains("Hypotenuse"));

        String expected = resultText();
        clickOn("Copy");
        WaitForAsyncUtils.waitForFxEvents();

        final String[] onClipboard = new String[1];
        interact(() -> onClipboard[0] = Clipboard.getSystemClipboard().getString());
        assertEquals(expected, onClipboard[0]);
    }

    @Test
    void the_history_collects_the_calculations() throws TimeoutException {
        clickOn("Pythagorean Theorem");
        clickOn(lookup(".text-field").nth(0).queryAs(TextField.class)).write("3");
        clickOn(lookup(".text-field").nth(1).queryAs(TextField.class)).write("4");
        clickOn("Calculate");
        WaitForAsyncUtils.waitFor(5, TimeUnit.SECONDS, () -> resultText().contains("Hypotenuse"));
        clickOn("Calculate"); // repeating the same calculation must not duplicate the entry
        WaitForAsyncUtils.waitForFxEvents();

        clickOn("Back");
        clickOn("History");

        verifyThat(".header", hasText("Calculation history"));
        assertEquals(1, lookup(".history-title").queryAll().size(), "the repeated calculation must not duplicate");
        assertTrue(lookup(".history-date").tryQuery().isPresent(), "each entry carries its date");
        String result = lookup(".result").nth(0).queryAs(Label.class).getText();
        assertTrue(result.contains("Hypotenuse: 5"), "was: " + result);

        clickOn("Clear history");
        WaitForAsyncUtils.waitForFxEvents();
        assertTrue(History.recent().isEmpty(), "«Clear history» must delete the calculations");
        assertTrue(lookup(".history-title").tryQuery().isEmpty(), "the screen must be left with no entries");
    }

    @Test
    void the_bmi_swaps_the_fields_by_measurement_system() throws TimeoutException {
        clickOn("Body Mass Index");
        WaitForAsyncUtils.waitForFxEvents();
        assertEquals(2, lookup(".text-field").queryAll().size(), "metric: weight and height");

        clickOn(lookup(".text-field").nth(0).queryAs(TextField.class)).write("70");
        clickOn(lookup(".text-field").nth(1).queryAs(TextField.class)).write("175");
        clickOn("Calculate");
        WaitForAsyncUtils.waitFor(5, TimeUnit.SECONDS, () -> resultText().contains("BMI"));
        assertTrue(resultText().contains("22.86"), "70 kg / 175 cm ≈ 22.86, was: " + resultText());

        ComboBox<?> selector = lookup(".combo-box").queryAs(ComboBox.class);
        interact(() -> selector.getSelectionModel().select(1)); // imperial
        WaitForAsyncUtils.waitForFxEvents();
        assertEquals(3, lookup(".text-field").queryAll().size(), "imperial: weight, feet and inches");

        // when switching system, the data is carried across converted
        assertTrue(
                lookup(".text-field").nth(0).queryAs(TextField.class).getText().startsWith("154"), "70 kg ≈ 154 lb");
        assertEquals("5", lookup(".text-field").nth(1).queryAs(TextField.class).getText(), "175 cm ≈ 5 ft");
        assertTrue(
                lookup(".text-field").nth(2).queryAs(TextField.class).getText().startsWith("8.9"),
                "175 cm ≈ 5 ft 8.9 in");

        // back to metric: the round-trip conversion lands on the same values
        interact(() -> selector.getSelectionModel().select(0));
        WaitForAsyncUtils.waitForFxEvents();
        assertEquals("70", lookup(".text-field").nth(0).queryAs(TextField.class).getText());
        assertEquals(
                "175", lookup(".text-field").nth(1).queryAs(TextField.class).getText());
    }

    @Test
    void it_remembers_the_last_calculator_and_forgets_it_on_going_back() {
        clickOn("Factorial");
        WaitForAsyncUtils.waitForFxEvents();
        assertEquals("factorial", LastCalculator.remembered().orElse(null));

        clickOn("Back");
        WaitForAsyncUtils.waitForFxEvents();
        assertTrue(LastCalculator.remembered().isEmpty(), "going back to the menu must forget the calculator");
    }

    @Test
    void the_theme_button_toggles_dark_mode() {
        boolean darkBefore = darkMode();
        clickOn(darkBefore ? "Light mode" : "Dark mode");
        assertTrue(darkMode() != darkBefore, "the button must toggle dark mode");

        clickOn(darkBefore ? "Dark mode" : "Light mode"); // leave it as it was
        assertTrue(darkMode() == darkBefore, "the second click must return to the previous theme");
    }

    private boolean darkMode() {
        return lookup(".title").query().getScene().getRoot().getStyleClass().contains(Theme.DARK_CLASS);
    }

    @Test
    void the_quadratic_explains_the_discriminant_and_gives_the_complex_roots() throws TimeoutException {
        clickOn("Solve Quadratic Equation");
        clickOn(lookup(".text-field").nth(0).queryAs(TextField.class)).write("7");
        clickOn(lookup(".text-field").nth(1).queryAs(TextField.class)).write("-3");
        clickOn(lookup(".text-field").nth(2).queryAs(TextField.class)).write("1");
        clickOn("Calculate");

        WaitForAsyncUtils.waitFor(5, TimeUnit.SECONDS, () -> resultText().contains("Δ"));
        String r = resultText();
        assertTrue(r.contains("-19"), "the discriminant is -19, was: " + r);
        assertTrue(r.contains("no real solutions"), r);
        assertTrue(r.contains("i"), "complex roots, was: " + r);
    }

    @Test
    void a_non_numeric_value_shows_an_error_message() throws TimeoutException {
        clickOn("Cylinder Surface Area");
        clickOn(".text-field").write("abc");
        clickOn("Calculate");

        WaitForAsyncUtils.waitFor(5, TimeUnit.SECONDS, () -> !resultText().isBlank());
        assertTrue(
                resultText().contains("valid numbers"), "expected the invalid-numbers warning, was: " + resultText());
    }
}
