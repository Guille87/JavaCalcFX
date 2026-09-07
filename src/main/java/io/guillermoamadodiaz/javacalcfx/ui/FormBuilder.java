package io.guillermoamadodiaz.javacalcfx.ui;

import io.guillermoamadodiaz.javacalcfx.i18n.Messages;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Function;
import javafx.animation.PauseTransition;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.control.Tooltip;
import javafx.scene.input.Clipboard;
import javafx.scene.input.ClipboardContent;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.util.Duration;
import javafx.util.StringConverter;

/**
 * Builds and shows the generic form screen shared by every calculator: header,
 * instructions, one field per requested value, a «Calculate» button (Enter),
 * result label and a «Back» button (Esc from anywhere on the screen). It sits
 * inside a {@link ScrollPane} so that, if the window gets very small, the content
 * scrolls rather than being clipped.
 *
 * <p>The calculation is delegated to {@link AsyncCalculations}; parsing and domain
 * validation live in the function each screen provides, and their errors are
 * shown in the same label. If a screen supplies a {@code steps} function, after a
 * successful calculation a «Show steps» button appears that unfolds its
 * explanation. {@link #showWithModes} is a variant with a selector that swaps the
 * fields and the calculation (e.g. measurement system). After a successful
 * calculation «Copy» always appears and the operation is recorded in
 * {@link History}.
 */
public final class FormBuilder {

    private static final Insets PADDING = new Insets(20);
    private static final double SPACING = 10;
    private static final double MAX_WIDTH = 440;

    private final Navigator navigator;
    private final AsyncCalculations calculations;
    private final Runnable backToMenu;

    public FormBuilder(Navigator navigator, AsyncCalculations calculations, Runnable backToMenu) {
        this.navigator = navigator;
        this.calculations = calculations;
        this.backToMenu = backToMenu;
    }

    public void show(
            String title,
            String instructions,
            List<String> prompts,
            NumericFilter.Type fieldType,
            Function<List<String>, String> calculation) {
        show(title, instructions, prompts, fieldType, calculation, null);
    }

    public void show(
            String title,
            String instructions,
            List<String> prompts,
            NumericFilter.Type fieldType, // {@code null} = no filter (e.g. allow hex digits)
            Function<List<String>, String> calculation,
            Function<List<String>, String> steps) {
        VBox screen = new VBox(SPACING);
        screen.setPadding(PADDING);
        screen.setAlignment(Pos.TOP_CENTER);
        screen.setMaxWidth(MAX_WIDTH);
        escGoesBackToMenu(screen);

        Label header = new Label(title);
        header.getStyleClass().add("encabezado");

        Label instruction = new Label(instructions);
        instruction.setWrapText(true);
        instruction.setMaxWidth(Double.MAX_VALUE);

        List<TextField> fields = new ArrayList<>(prompts.size());
        for (String prompt : prompts) {
            TextField field = new TextField();
            field.setPromptText(prompt);
            if (fieldType != null) {
                NumericFilter.applyTo(field, fieldType);
            }
            fields.add(field);
        }

        Label result = new Label();
        result.setWrapText(true);
        result.setMaxWidth(Double.MAX_VALUE);
        result.getStyleClass().add("resultado");

        StepsSection section = steps == null ? null : new StepsSection();
        CopyButton copy = new CopyButton(result);

        Button calculate = new Button(Messages.get("form.calculate"));
        calculate.setDefaultButton(true); // allows pressing Enter
        calculate.setTooltip(new Tooltip(Messages.get("form.calculate.tooltip")));
        calculate.setOnAction(e -> {
            List<String> values = fields.stream()
                    .map(c -> c.getText() == null ? "" : c.getText().trim())
                    .toList();
            if (section != null) {
                section.hide();
            }
            copy.hide();
            calculations.run(
                    () -> calculation.apply(values),
                    () -> {
                        calculate.setDisable(true);
                        result.setText(Messages.get("form.calculating"));
                    },
                    text -> {
                        calculate.setDisable(false);
                        result.setText(text);
                        if (!text.isBlank()) {
                            History.record(title, text);
                            copy.show();
                            if (section != null) {
                                section.prepare(() -> steps.apply(values));
                            }
                        }
                    },
                    message -> {
                        calculate.setDisable(false);
                        result.setText(message);
                    });
        });

        Button back = Buttons.create(Messages.get("form.back"), Messages.get("form.back.tooltip"), backToMenu);
        back.setCancelButton(true); // allows pressing Esc

        screen.getChildren().addAll(header, instruction);
        screen.getChildren().addAll(fields);
        screen.getChildren().addAll(calculate, result, copy.button);
        if (section != null) {
            screen.getChildren().addAll(section.button, section.detail);
        }
        screen.getChildren().add(back);

        showInScroll(screen);
        fields.get(0).requestFocus(); // the cursor starts in the first field
    }

    /**
     * A «mode» of a form with a selector: its visible name, the fields it asks for
     * and the calculation it runs with those fields.
     *
     * <p>{@code toCommon} and {@code fromCommon} are optional: if a mode provides
     * them, switching modes carries the data across (the previous mode's fields are
     * converted to a common representation and from there to the new mode's). If a
     * field is empty or invalid, the carry-over is skipped.
     *
     * @param toCommon fields → common values; {@link Optional#empty()} if not applicable
     * @param fromCommon common values → text for the mode's fields
     */
    public record Mode(
            String name,
            List<String> prompts,
            Function<List<String>, String> calculation,
            Function<List<String>, Optional<double[]>> toCommon,
            Function<double[], List<String>> fromCommon) {

        /** Mode without carry-over: switching modes clears the fields. */
        public Mode(String name, List<String> prompts, Function<List<String>, String> calculation) {
            this(name, prompts, calculation, values -> Optional.empty(), common -> List.of());
        }
    }

    /**
     * The form variant with a {@link ComboBox} of modes (e.g. measurement system):
     * switching modes rebuilds the fields and uses that mode's calculation. It does
     * not support «step by step». The first mode in the list is the initial one.
     */
    public void showWithModes(
            String title, String instructions, String modesLabel, List<Mode> modes, NumericFilter.Type fieldType) {
        VBox screen = new VBox(SPACING);
        screen.setPadding(PADDING);
        screen.setAlignment(Pos.TOP_CENTER);
        screen.setMaxWidth(MAX_WIDTH);
        escGoesBackToMenu(screen);

        Label header = new Label(title);
        header.getStyleClass().add("encabezado");

        Label instruction = new Label(instructions);
        instruction.setWrapText(true);
        instruction.setMaxWidth(Double.MAX_VALUE);

        ComboBox<Mode> selector = new ComboBox<>();
        selector.getItems().setAll(modes);
        selector.setMaxWidth(Double.MAX_VALUE);
        selector.setConverter(new StringConverter<>() {
            @Override
            public String toString(Mode mode) {
                return mode == null ? "" : mode.name();
            }

            @Override
            public Mode fromString(String text) {
                return null;
            }
        });

        Label result = new Label();
        result.setWrapText(true);
        result.setMaxWidth(Double.MAX_VALUE);
        result.getStyleClass().add("resultado");
        CopyButton copy = new CopyButton(result);

        List<TextField> fields = new ArrayList<>();
        VBox fieldsBox = new VBox(SPACING);
        AtomicReference<Function<List<String>, String>> currentCalculation = new AtomicReference<>();
        AtomicReference<Mode> previousMode = new AtomicReference<>();

        Runnable applyMode = () -> {
            Mode mode = selector.getValue();
            if (mode == previousMode.get()) {
                return; // the ComboBox notified without a real change
            }
            List<String> inherited = carryOver(previousMode.get(), mode, fields);

            fields.clear();
            fieldsBox.getChildren().clear();
            for (int i = 0; i < mode.prompts().size(); i++) {
                TextField field = new TextField();
                field.setPromptText(mode.prompts().get(i));
                if (fieldType != null) {
                    NumericFilter.applyTo(field, fieldType);
                }
                if (i < inherited.size()) {
                    field.setText(inherited.get(i));
                }
                fields.add(field);
                fieldsBox.getChildren().add(field);
            }
            currentCalculation.set(mode.calculation());
            result.setText("");
            copy.hide();
            fields.get(0).requestFocus();
            previousMode.set(mode);
        };
        selector.getSelectionModel().selectFirst();
        applyMode.run();
        selector.setOnAction(e -> applyMode.run());

        Button calculate = new Button(Messages.get("form.calculate"));
        calculate.setDefaultButton(true);
        calculate.setTooltip(new Tooltip(Messages.get("form.calculate.tooltip")));
        calculate.setOnAction(e -> {
            List<String> values = fields.stream()
                    .map(c -> c.getText() == null ? "" : c.getText().trim())
                    .toList();
            Function<List<String>, String> calculation = currentCalculation.get();
            copy.hide();
            calculations.run(
                    () -> calculation.apply(values),
                    () -> {
                        calculate.setDisable(true);
                        result.setText(Messages.get("form.calculating"));
                    },
                    text -> {
                        calculate.setDisable(false);
                        result.setText(text);
                        if (!text.isBlank()) {
                            History.record(title, text);
                            copy.show();
                        }
                    },
                    message -> {
                        calculate.setDisable(false);
                        result.setText(message);
                    });
        });

        Button back = Buttons.create(Messages.get("form.back"), Messages.get("form.back.tooltip"), backToMenu);
        back.setCancelButton(true);

        screen.getChildren()
                .addAll(
                        header,
                        instruction,
                        new VBox(4, new Label(modesLabel), selector),
                        fieldsBox,
                        calculate,
                        result,
                        copy.button,
                        back);

        showInScroll(screen);
        fields.get(0).requestFocus();
    }

    /** Converts the {@code previous} mode's fields to the text expected by the {@code next} one. */
    private static List<String> carryOver(Mode previous, Mode next, List<TextField> fields) {
        if (previous == null || previous == next) {
            return List.of();
        }
        List<String> current = fields.stream()
                .map(c -> c.getText() == null ? "" : c.getText().trim())
                .toList();
        return previous.toCommon()
                .apply(current)
                .map(common -> next.fromCommon().apply(common))
                .orElse(List.of());
    }

    /** Esc goes back to the menu even if the focus is on a {@link TextField} (which would eat the key). */
    private void escGoesBackToMenu(VBox screen) {
        screen.addEventFilter(KeyEvent.KEY_PRESSED, event -> {
            if (event.getCode() == KeyCode.ESCAPE) {
                backToMenu.run();
                event.consume();
            }
        });
    }

    /** Wraps the form in the transparent {@link ScrollPane} and shows it. */
    private void showInScroll(VBox screen) {
        StackPane centerer = new StackPane(screen); // keeps the form centered
        ScrollPane scroll = new ScrollPane(centerer);
        scroll.setFitToWidth(true);
        scroll.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        scroll.getStyleClass().add("formulario");
        navigator.show(scroll);
    }

    /** «Show / Hide steps» button and the label with the explanation. */
    private static final class StepsSection {

        private final Button button = new Button();
        private final Label detail = new Label();

        StepsSection() {
            detail.setWrapText(true);
            detail.setMaxWidth(Double.MAX_VALUE);
            detail.getStyleClass().add("pasos");
            button.setOnAction(e -> showDetail(!detail.isVisible()));
            hide();
        }

        void hide() {
            button.setVisible(false);
            button.setManaged(false);
            showDetail(false);
        }

        /** Computes the text and shows the button (still folded). */
        void prepare(java.util.function.Supplier<String> explanation) {
            try {
                detail.setText(explanation.get());
            } catch (RuntimeException ignored) {
                return; // if the explanation fails, we just do not offer the steps
            }
            button.setVisible(true);
            button.setManaged(true);
            showDetail(false);
        }

        private void showDetail(boolean visible) {
            detail.setVisible(visible);
            detail.setManaged(visible);
            button.setText(Messages.get(visible ? "form.steps.hide" : "form.steps.show"));
        }
    }

    /** «Copy» button: copies the result text to the system clipboard. */
    private static final class CopyButton {

        private final Button button;

        CopyButton(Label result) {
            button = Buttons.create(Messages.get("form.copy"), Messages.get("form.copy.tooltip"), () -> copy(result));
            hide();
        }

        void hide() {
            button.setVisible(false);
            button.setManaged(false);
        }

        void show() {
            button.setVisible(true);
            button.setManaged(true);
            button.setText(Messages.get("form.copy"));
        }

        private void copy(Label result) {
            ClipboardContent content = new ClipboardContent();
            content.putString(result.getText() == null ? "" : result.getText());
            Clipboard.getSystemClipboard().setContent(content);

            button.setText(Messages.get("form.copy.done"));
            PauseTransition revert = new PauseTransition(Duration.seconds(1.5));
            revert.setOnFinished(e -> button.setText(Messages.get("form.copy")));
            revert.play();
        }
    }
}
