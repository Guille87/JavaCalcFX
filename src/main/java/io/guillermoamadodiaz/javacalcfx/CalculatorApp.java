package io.guillermoamadodiaz.javacalcfx;

import io.guillermoamadodiaz.javacalcfx.calc.Calculator;
import io.guillermoamadodiaz.javacalcfx.calc.Calculator.QuadraticEquation;
import io.guillermoamadodiaz.javacalcfx.calc.Calculator.Root;
import io.guillermoamadodiaz.javacalcfx.calc.Calculator.Triangle;
import io.guillermoamadodiaz.javacalcfx.calc.Conversions;
import io.guillermoamadodiaz.javacalcfx.i18n.Language;
import io.guillermoamadodiaz.javacalcfx.i18n.Messages;
import io.guillermoamadodiaz.javacalcfx.ui.AsyncCalculations;
import io.guillermoamadodiaz.javacalcfx.ui.Buttons;
import io.guillermoamadodiaz.javacalcfx.ui.CylinderSteps;
import io.guillermoamadodiaz.javacalcfx.ui.FormBuilder;
import io.guillermoamadodiaz.javacalcfx.ui.Format;
import io.guillermoamadodiaz.javacalcfx.ui.History;
import io.guillermoamadodiaz.javacalcfx.ui.Input;
import io.guillermoamadodiaz.javacalcfx.ui.LastCalculator;
import io.guillermoamadodiaz.javacalcfx.ui.Navigator;
import io.guillermoamadodiaz.javacalcfx.ui.NumericFilter;
import io.guillermoamadodiaz.javacalcfx.ui.PercentageSteps;
import io.guillermoamadodiaz.javacalcfx.ui.PythagorasSteps;
import io.guillermoamadodiaz.javacalcfx.ui.QuadraticSteps;
import io.guillermoamadodiaz.javacalcfx.ui.RuleOfThreeSteps;
import io.guillermoamadodiaz.javacalcfx.ui.Settings;
import io.guillermoamadodiaz.javacalcfx.ui.Theme;
import io.guillermoamadodiaz.javacalcfx.ui.WindowState;
import java.math.BigInteger;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.UnaryOperator;
import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.image.Image;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

/**
 * The main window. It does only three things: wire up the interface
 * infrastructure ({@link Navigator}, {@link AsyncCalculations},
 * {@link FormBuilder}), declare the catalog of calculators and shut down the
 * executor on exit. Each {@code xScreen()} describes only <em>what</em> is asked
 * and <em>what</em> is shown; the arithmetic lives in {@link Calculator} and the
 * text in {@link Messages}.
 */
public class CalculatorApp extends Application {

    private static final double MENU_BUTTON_WIDTH = 210;
    private static final double MENU_WIDTH = 690;
    private static final int PASSING_GRADES = 5;

    private final StackPane root = new StackPane();
    private final AsyncCalculations calculations = new AsyncCalculations();
    private final Navigator navigator = new Navigator(root, calculations::cancel);
    private final FormBuilder forms = new FormBuilder(navigator, calculations, this::showMenu);

    private Stage stage;

    private static final int[] ICON_SIZES = {16, 32, 48, 64, 128, 256};

    @Override
    public void start(Stage stage) {
        this.stage = stage;
        stage.setTitle(Messages.get("app.title"));
        for (int px : ICON_SIZES) {
            var url = getClass().getResource("icons/icon-" + px + ".png");
            if (url != null) {
                stage.getIcons().add(new Image(url.toExternalForm()));
            }
        }
        root.setPadding(new Insets(20));

        Scene scene = new Scene(root, 695, 500);
        var stylesheet = getClass().getResource("styles.css");
        if (stylesheet != null) {
            scene.getStylesheets().add(stylesheet.toExternalForm());
        }
        Theme.applyTo(scene); // restores the saved light/dark mode

        Optional<String> lastCalculator = LastCalculator.remembered();
        showMenu(); // this forgets it; that is why it is read first
        stage.setScene(scene);
        stage.setMinWidth(710);
        stage.setMinHeight(600);
        WindowState.restore(stage);
        stage.show();
        WindowState.watch(stage);
        lastCalculator.ifPresent(this::openCalculator); // reopens the last screen
    }

    @Override
    public void stop() {
        calculations.close();
    }

    // ------------------------------------------------------------------
    // Menu
    // ------------------------------------------------------------------

    /** A menu entry: its text key and which screen it opens. */
    private record MenuEntry(String key, Runnable open) {}

    /** A menu category: its text key and its calculators. */
    private record Category(String key, List<MenuEntry> entries) {}

    private List<Category> catalog() {
        return List.of(
                new Category(
                        "geometry",
                        List.of(
                                menuEntry("pythagoras", this::pythagorasScreen),
                                menuEntry("cylinder", this::cylinderScreen))),
                new Category(
                        "arithmetic",
                        List.of(
                                menuEntry("factorial", this::factorialScreen),
                                menuEntry("multiple", this::multipleScreen),
                                menuEntry("gcd", this::gcdScreen),
                                menuEntry("prime", this::primeScreen),
                                menuEntry("base", this::baseScreen))),
                new Category(
                        "powers",
                        List.of(
                                menuEntry("power", this::powerScreen),
                                menuEntry("root", this::rootScreen),
                                menuEntry("quadratic", this::quadraticScreen))),
                new Category(
                        "proportions",
                        List.of(
                                menuEntry("percentage", this::percentageScreen),
                                menuEntry("ruleofthree", this::ruleOfThreeScreen))),
                new Category(
                        "other",
                        List.of(
                                menuEntry("leapyear", this::leapYearScreen),
                                menuEntry("grades", this::gradesScreen),
                                menuEntry("bmi", this::bmiScreen))));
    }

    /** Menu entry that, when opened, remembers the calculator for the next start. */
    private MenuEntry menuEntry(String key, Runnable screen) {
        return new MenuEntry(key, () -> {
            LastCalculator.remember(key);
            screen.run();
        });
    }

    private void openCalculator(String key) {
        catalog().stream()
                .flatMap(category -> category.entries().stream())
                .filter(menu -> menu.key().equals(key))
                .findFirst()
                .ifPresent(menu -> menu.open().run());
    }

    private void showMenu() {
        LastCalculator.forget(); // we are on the menu: no «last calculator» to reopen

        Label title = new Label(Messages.get("menu.title"));
        title.getStyleClass().add("title");

        VBox categories = new VBox(16);
        categories.setAlignment(Pos.TOP_LEFT);
        categories.setMaxWidth(MENU_WIDTH);
        for (Category category : catalog()) {
            Label name = new Label(Messages.get("menu.category." + category.key()));
            name.getStyleClass().add("category");

            FlowPane buttons = new FlowPane(10, 10);
            for (MenuEntry entry : category.entries()) {
                Button button = menuButton(entry.key(), entry.open());
                button.setPrefWidth(MENU_BUTTON_WIDTH);
                button.setWrapText(true);
                buttons.getChildren().add(button);
            }
            categories.getChildren().add(new VBox(6, name, buttons));
        }

        VBox center = new VBox(20, title, categories);
        center.setAlignment(Pos.CENTER);

        ScrollPane scroll = new ScrollPane(new StackPane(center));
        scroll.setFitToWidth(true);
        scroll.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        scroll.getStyleClass().add("form"); // transparent background, no border

        BorderPane menu = new BorderPane(scroll);
        menu.setTop(topBar());
        navigator.show(menu);
    }

    /** Menu button from its key: uses {@code menu.button.<key>[.tooltip]}. */
    private Button menuButton(String key, Runnable action) {
        return Buttons.create(
                Messages.get("menu.button." + key), Messages.get("menu.button." + key + ".tooltip"), action);
    }

    /** The menu's top bar: history, theme toggle and settings, top right. */
    private HBox topBar() {
        String themeKey = Theme.isDark() ? "menu.theme.light" : "menu.theme.dark";
        Button theme = Buttons.create(Messages.get(themeKey), Messages.get("menu.theme.tooltip"), () -> {
            Theme.toggle();
            Theme.applyTo(stage.getScene());
            showMenu(); // rebuilds the bar with the correct label
        });

        Button history =
                Buttons.create(Messages.get("menu.history"), Messages.get("menu.history.tooltip"), this::historyScreen);

        Button settings = Buttons.create(
                Messages.get("menu.settings"), Messages.get("menu.settings.tooltip"), this::settingsScreen);

        HBox bar = new HBox(8, history, theme, settings);
        bar.setAlignment(Pos.CENTER_RIGHT);
        return bar;
    }

    /** Settings screen: the options that were scattered around the UI, plus new ones. */
    private void settingsScreen() {
        Label header = new Label(Messages.get("settings.title"));
        header.getStyleClass().add("header");

        ComboBox<Language> language = new ComboBox<>();
        language.getItems().setAll(Language.values());
        language.setValue(Messages.language());
        language.setOnAction(e -> {
            Language chosen = language.getValue();
            if (chosen != null && chosen != Messages.language()) {
                Messages.select(chosen);
                stage.setTitle(Messages.get("app.title"));
                settingsScreen(); // re-render with the new language
            }
        });

        String light = Messages.get("settings.theme.light");
        String dark = Messages.get("settings.theme.dark");
        ComboBox<String> theme = new ComboBox<>();
        theme.getItems().setAll(light, dark);
        theme.setValue(Theme.isDark() ? dark : light);
        theme.setOnAction(e -> {
            boolean wantDark = dark.equals(theme.getValue());
            if (wantDark != Theme.isDark()) {
                Theme.setDark(wantDark);
                Theme.applyTo(stage.getScene());
            }
        });

        CheckBox rememberLast = new CheckBox(Messages.get("settings.remember.last.calculator"));
        rememberLast.setSelected(Settings.rememberLastCalculator());
        rememberLast.setOnAction(e -> Settings.setRememberLastCalculator(rememberLast.isSelected()));

        CheckBox rememberWindow = new CheckBox(Messages.get("settings.remember.window"));
        rememberWindow.setSelected(Settings.rememberWindow());
        rememberWindow.setOnAction(e -> Settings.setRememberWindow(rememberWindow.isSelected()));

        Button resetWindow = Buttons.create(Messages.get("settings.reset.window"), () -> WindowState.reset(stage));

        GridPane rows = new GridPane();
        rows.setHgap(12);
        rows.setVgap(12);
        rows.addRow(0, new Label(Messages.get("settings.language")), language);
        rows.addRow(1, new Label(Messages.get("settings.theme")), theme);
        rows.add(rememberLast, 0, 2, 2, 1);
        rows.add(rememberWindow, 0, 3, 2, 1);
        rows.add(resetWindow, 0, 4, 2, 1);

        Button back = Buttons.create(Messages.get("form.back"), Messages.get("form.back.tooltip"), this::showMenu);
        back.setCancelButton(true); // Esc

        VBox content = new VBox(16, header, rows, back);
        content.setAlignment(Pos.TOP_CENTER);
        content.setMaxWidth(MENU_WIDTH);
        content.setPadding(new Insets(20));

        ScrollPane scroll = new ScrollPane(new StackPane(content));
        scroll.setFitToWidth(true);
        scroll.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        scroll.getStyleClass().add("form");
        navigator.show(scroll);
    }

    /** Screen with the latest calculations ({@link History}). */
    private void historyScreen() {
        Label header = new Label(Messages.get("history.title"));
        header.getStyleClass().add("header");

        List<History.Entry> entries = History.recent();

        Button clear = Buttons.create(Messages.get("history.clear"), () -> {
            History.clear();
            historyScreen();
        });
        clear.setDisable(entries.isEmpty());
        Button back = Buttons.create(Messages.get("form.back"), Messages.get("form.back.tooltip"), this::showMenu);
        back.setCancelButton(true); // Esc

        VBox content = new VBox(16, header, new HBox(8, clear, back));
        content.setAlignment(Pos.TOP_CENTER);
        content.setMaxWidth(MENU_WIDTH);
        content.setPadding(new Insets(20));

        if (entries.isEmpty()) {
            Label empty = new Label(Messages.get("history.empty"));
            empty.getStyleClass().add("category");
            content.getChildren().add(empty);
        } else {
            VBox list = new VBox(14);
            for (History.Entry entry : entries) {
                Label title = new Label(entry.title());
                title.getStyleClass().add("history-title");
                Label result = new Label(entry.result());
                result.setWrapText(true);
                result.getStyleClass().add("result");

                VBox block = new VBox(2, title);
                if (entry.timestamp() != null) {
                    Label date = new Label(readableDate(entry.timestamp()));
                    date.getStyleClass().add("history-date");
                    block.getChildren().add(date);
                }
                block.getChildren().add(result);
                list.getChildren().add(block);
            }
            content.getChildren().add(list);
        }

        ScrollPane scroll = new ScrollPane(new StackPane(content));
        scroll.setFitToWidth(true);
        scroll.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        scroll.getStyleClass().add("form");
        navigator.show(scroll);
    }

    /** Date and time of a history calculation, with the months in the current language. */
    private static String readableDate(Instant timestamp) {
        return DateTimeFormatter.ofPattern(
                        "d MMM yyyy, HH:mm", Messages.language().locale())
                .withZone(ZoneId.systemDefault())
                .format(timestamp);
    }

    // ------------------------------------------------------------------
    // Calculator catalog
    // ------------------------------------------------------------------

    private void pythagorasScreen() {
        forms.show(
                Messages.get("pythagoras.title"),
                Messages.get("pythagoras.instructions"),
                List.of(Messages.get("pythagoras.field.legA"), Messages.get("pythagoras.field.legB")),
                NumericFilter.Type.DECIMAL,
                values -> {
                    Triangle t =
                            Calculator.solveRightTriangle(Input.asDouble(values.get(0)), Input.asDouble(values.get(1)));
                    return Messages.get(
                            "pythagoras.result",
                            Format.number(t.hypotenuse()),
                            Format.number(t.area()),
                            Format.number(t.perimeter()),
                            Format.number(t.angleAlpha()),
                            Format.number(t.angleBeta()));
                },
                values -> PythagorasSteps.explain(Input.asDouble(values.get(0)), Input.asDouble(values.get(1))));
    }

    private void cylinderScreen() {
        forms.show(
                Messages.get("cylinder.title"),
                Messages.get("cylinder.instructions"),
                List.of(Messages.get("cylinder.field.radius"), Messages.get("cylinder.field.height")),
                NumericFilter.Type.DECIMAL,
                values -> Messages.get(
                        "cylinder.result",
                        Format.number(
                                Calculator.cylinderArea(Input.asDouble(values.get(0)), Input.asDouble(values.get(1))))),
                values -> CylinderSteps.explain(Input.asDouble(values.get(0)), Input.asDouble(values.get(1))));
    }

    private void leapYearScreen() {
        forms.show(
                Messages.get("leapyear.title"),
                Messages.get("leapyear.instructions"),
                List.of(Messages.get("leapyear.field.year")),
                NumericFilter.Type.INTEGER,
                values -> {
                    int year = Input.asInt(values.get(0));
                    String key = Calculator.isLeapYear(year) ? "leapyear.result.yes" : "leapyear.result.no";
                    return Messages.get(key, String.valueOf(year));
                });
    }

    private void factorialScreen() {
        forms.show(
                Messages.get("factorial.title"),
                Messages.get("factorial.instructions"),
                List.of(Messages.get("factorial.field.number")),
                NumericFilter.Type.INTEGER,
                values -> {
                    int n = Input.asInt(values.get(0));
                    BigInteger factorial = Calculator.factorial(n);
                    return Messages.get("factorial.result", String.valueOf(n), Format.bigInteger(factorial));
                });
    }

    private void multipleScreen() {
        forms.show(
                Messages.get("multiple.title"),
                Messages.get("multiple.instructions"),
                List.of(Messages.get("multiple.field.first"), Messages.get("multiple.field.second")),
                NumericFilter.Type.INTEGER,
                values -> {
                    long a = Input.asLong(values.get(0));
                    long b = Input.asLong(values.get(1));
                    String key = Calculator.isMultiple(a, b) ? "multiple.result.yes" : "multiple.result.no";
                    return Messages.get(key, String.valueOf(a), String.valueOf(b));
                });
    }

    private void gradesScreen() {
        List<String> prompts = new ArrayList<>(PASSING_GRADES);
        for (int i = 1; i <= PASSING_GRADES; i++) {
            prompts.add(Messages.get("grades.field.grade", i));
        }
        forms.show(
                Messages.get("grades.title"),
                Messages.get("grades.instructions", String.valueOf((int) Calculator.MIN_GRADE), String.valueOf((int)
                        Calculator.MAX_GRADE)),
                prompts,
                NumericFilter.Type.DECIMAL,
                values -> {
                    double[] grades =
                            values.stream().mapToDouble(Input::asDouble).toArray();
                    double mean = Calculator.mean(grades);
                    String key = Calculator.isPassing(mean) ? "grades.result.pass" : "grades.result.fail";
                    return Messages.get(key, Format.twoDecimals(mean));
                });
    }

    private void quadraticScreen() {
        forms.show(
                Messages.get("quadratic.title"),
                Messages.get("quadratic.instructions"),
                List.of(
                        Messages.get("quadratic.field.a"),
                        Messages.get("quadratic.field.b"),
                        Messages.get("quadratic.field.c")),
                NumericFilter.Type.DECIMAL,
                values -> {
                    QuadraticEquation e = Calculator.solveQuadratic(
                            Input.asDouble(values.get(0)),
                            Input.asDouble(values.get(1)),
                            Input.asDouble(values.get(2)));
                    String delta = Format.number(e.discriminant());
                    if (e.hasDoubleRoot()) {
                        return Messages.get("quadratic.result.double", Format.number(e.x1().real()));
                    }
                    if (e.hasRealRoots()) {
                        return Messages.get(
                                "quadratic.result.real",
                                delta,
                                Format.number(e.x1().real()),
                                Format.number(e.x2().real()));
                    }
                    return Messages.get("quadratic.result.complex", delta, formatRoot(e.x1()), formatRoot(e.x2()));
                },
                values -> QuadraticSteps.explain(
                        Input.asDouble(values.get(0)), Input.asDouble(values.get(1)), Input.asDouble(values.get(2))));
    }

    /** Formats a complex root as {@code a + b i} (or {@code a - b i}). */
    private static String formatRoot(Root root) {
        String sign = root.imaginary() < 0 ? " - " : " + ";
        return Format.number(root.real()) + sign + Format.number(Math.abs(root.imaginary())) + " i";
    }

    private void powerScreen() {
        forms.show(
                Messages.get("power.title"),
                Messages.get("power.instructions"),
                List.of(Messages.get("power.field.base"), Messages.get("power.field.exponent")),
                NumericFilter.Type.DECIMAL,
                values -> {
                    double base = Input.asDouble(values.get(0));
                    double exponent = Input.asDouble(values.get(1));
                    return Messages.get(
                            "power.result",
                            Format.number(base),
                            Format.number(exponent),
                            Format.number(Calculator.power(base, exponent)));
                });
    }

    private void rootScreen() {
        forms.show(
                Messages.get("root.title"),
                Messages.get("root.instructions"),
                List.of(Messages.get("root.field.radicand"), Messages.get("root.field.index")),
                NumericFilter.Type.DECIMAL,
                values -> {
                    double radicand = Input.asDouble(values.get(0));
                    double index = Input.asDouble(values.get(1));
                    return Messages.get(
                            "root.result",
                            Format.number(index),
                            Format.number(radicand),
                            Format.number(Calculator.nthRoot(radicand, index)));
                });
    }

    private void gcdScreen() {
        forms.show(
                Messages.get("gcd.title"),
                Messages.get("gcd.instructions"),
                List.of(Messages.get("gcd.field.a"), Messages.get("gcd.field.b")),
                NumericFilter.Type.INTEGER,
                values -> {
                    long a = Input.asLong(values.get(0));
                    long b = Input.asLong(values.get(1));
                    return Messages.get(
                            "gcd.result",
                            Format.integer(a),
                            Format.integer(b),
                            Format.integer(Calculator.gcd(a, b)),
                            Format.integer(Calculator.lcm(a, b)));
                });
    }

    private void primeScreen() {
        forms.show(
                Messages.get("prime.title"),
                Messages.get("prime.instructions"),
                List.of(Messages.get("prime.field.number")),
                NumericFilter.Type.INTEGER,
                values -> {
                    long n = Input.asLong(values.get(0));
                    Calculator.Primality p = Calculator.analyzePrimality(n);
                    if (p.prime()) {
                        return Messages.get("prime.result.yes", Format.integer(n));
                    }
                    if (p.isComposite()) {
                        long divisor = p.smallestProperDivisor();
                        return Messages.get(
                                "prime.result.composite",
                                Format.integer(n),
                                Format.integer(divisor),
                                Format.integer(n / divisor));
                    }
                    return Messages.get("prime.result.no", Format.integer(n));
                });
    }

    private void baseScreen() {
        forms.show(
                Messages.get("base.title"),
                Messages.get("base.instructions"),
                List.of(Messages.get("base.field.number")),
                null, // no filter: the number may carry a prefix and hexadecimal digits
                values -> {
                    Calculator.BaseConversion c = Calculator.convertBase(values.get(0));
                    return Messages.get("base.result", c.binary(), c.octal(), c.decimal(), c.hex());
                });
    }

    private void percentageScreen() {
        forms.show(
                Messages.get("percentage.title"),
                Messages.get("percentage.instructions"),
                List.of(Messages.get("percentage.field.percentage"), Messages.get("percentage.field.amount")),
                NumericFilter.Type.DECIMAL,
                values -> {
                    double percentage = Input.asDouble(values.get(0));
                    double amount = Input.asDouble(values.get(1));
                    return Messages.get(
                            "percentage.result",
                            Format.number(percentage),
                            Format.number(amount),
                            Format.number(Calculator.percentageOf(percentage, amount)));
                },
                values -> PercentageSteps.explain(Input.asDouble(values.get(0)), Input.asDouble(values.get(1))));
    }

    private void ruleOfThreeScreen() {
        forms.show(
                Messages.get("ruleofthree.title"),
                Messages.get("ruleofthree.instructions"),
                List.of(
                        Messages.get("ruleofthree.field.a"),
                        Messages.get("ruleofthree.field.b"),
                        Messages.get("ruleofthree.field.c")),
                NumericFilter.Type.DECIMAL,
                values -> {
                    double a = Input.asDouble(values.get(0));
                    double b = Input.asDouble(values.get(1));
                    double c = Input.asDouble(values.get(2));
                    return Messages.get(
                            "ruleofthree.result",
                            Format.number(a),
                            Format.number(b),
                            Format.number(c),
                            Format.number(Calculator.ruleOfThree(a, b, c)));
                },
                values -> RuleOfThreeSteps.explain(
                        Input.asDouble(values.get(0)), Input.asDouble(values.get(1)), Input.asDouble(values.get(2))));
    }

    private void bmiScreen() {
        // Common representation between the two modes: {weight in kg, height in m}.
        FormBuilder.Mode metric = new FormBuilder.Mode(
                Messages.get("bmi.system.metric"),
                List.of(Messages.get("bmi.field.weight.kg"), Messages.get("bmi.field.height.cm")),
                values ->
                        bmiResult(Input.asDouble(values.get(0)), Conversions.cmToMeters(Input.asDouble(values.get(1)))),
                values -> toCommon(values, v -> new double[] {v[0], Conversions.cmToMeters(v[1])}),
                common -> List.of(rounded(common[0], 1), rounded(Conversions.metersToCm(common[1]), 0)));

        FormBuilder.Mode imperial = new FormBuilder.Mode(
                Messages.get("bmi.system.imperial"),
                List.of(
                        Messages.get("bmi.field.weight.lb"),
                        Messages.get("bmi.field.height.feet"),
                        Messages.get("bmi.field.height.inches")),
                values -> bmiResult(
                        Conversions.poundsToKilos(Input.asDouble(values.get(0))),
                        Conversions.feetInchesToMeters(Input.asDouble(values.get(1)), Input.asDouble(values.get(2)))),
                values -> toCommon(values, v ->
                        new double[] {Conversions.poundsToKilos(v[0]), Conversions.feetInchesToMeters(v[1], v[2])}),
                common -> {
                    double[] feetAndInches = Conversions.metersToFeetInches(common[1]);
                    return List.of(
                            rounded(Conversions.kilosToPounds(common[0]), 1),
                            Format.number(feetAndInches[0]),
                            rounded(feetAndInches[1], 1));
                });

        forms.showWithModes(
                Messages.get("bmi.title"),
                Messages.get("bmi.instructions"),
                Messages.get("bmi.system"),
                List.of(metric, imperial),
                NumericFilter.Type.DECIMAL);
    }

    private String bmiResult(double weightKg, double heightM) {
        Calculator.BodyMassIndex r = Calculator.bmi(weightKg, heightM);
        return Messages.get(
                "bmi.result",
                Format.twoDecimals(r.value()),
                Messages.get("bmi.category." + r.category().name().toLowerCase()));
    }

    /** Parses the fields and applies {@code toCommon}; empty if a field is missing or invalid. */
    private static Optional<double[]> toCommon(List<String> values, UnaryOperator<double[]> toCommon) {
        try {
            double[] raw = new double[values.size()];
            for (int i = 0; i < values.size(); i++) {
                raw[i] = Input.asDouble(values.get(i));
            }
            return Optional.of(toCommon.apply(raw));
        } catch (RuntimeException notValid) {
            return Optional.empty();
        }
    }

    /**
     * Number rounded to {@code decimals} decimals, to fill fields when switching
     * measurement system. Centimeters go without decimals and pounds, inches and
     * kilos to one: that way the round-trip conversion (e.g. cm → inches → cm)
     * returns the same value.
     */
    private static String rounded(double value, int decimals) {
        double factor = Math.pow(10, decimals);
        return Format.number(Math.round(value * factor) / factor);
    }

    public static void main(String[] args) {
        launch(args);
    }
}
