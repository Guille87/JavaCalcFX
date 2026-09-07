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
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Tooltip;
import javafx.scene.image.Image;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.FlowPane;
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
        stage.setTitle(Messages.get("app.titulo"));
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
                        "geometria",
                        List.of(
                                menuEntry("pitagoras", this::pythagorasScreen),
                                menuEntry("cilindro", this::cylinderScreen))),
                new Category(
                        "aritmetica",
                        List.of(
                                menuEntry("factorial", this::factorialScreen),
                                menuEntry("multiplo", this::multipleScreen),
                                menuEntry("mcd", this::gcdScreen),
                                menuEntry("primo", this::primeScreen),
                                menuEntry("base", this::baseScreen))),
                new Category(
                        "potencias",
                        List.of(
                                menuEntry("potencia", this::powerScreen),
                                menuEntry("raiz", this::rootScreen),
                                menuEntry("cuadratica", this::quadraticScreen))),
                new Category(
                        "proporciones",
                        List.of(
                                menuEntry("porcentaje", this::percentageScreen),
                                menuEntry("regladetres", this::ruleOfThreeScreen))),
                new Category(
                        "otros",
                        List.of(
                                menuEntry("bisiesto", this::leapYearScreen),
                                menuEntry("aprobado", this::gradesScreen),
                                menuEntry("imc", this::bmiScreen))));
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

        Label title = new Label(Messages.get("menu.titulo"));
        title.getStyleClass().add("titulo");

        VBox categories = new VBox(16);
        categories.setAlignment(Pos.TOP_LEFT);
        categories.setMaxWidth(MENU_WIDTH);
        for (Category category : catalog()) {
            Label name = new Label(Messages.get("menu.categoria." + category.key()));
            name.getStyleClass().add("categoria");

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
        scroll.getStyleClass().add("formulario"); // transparent background, no border

        BorderPane menu = new BorderPane(scroll);
        menu.setTop(topBar());
        navigator.show(menu);
    }

    /** Menu button from its key: uses {@code menu.boton.<key>[.tooltip]}. */
    private Button menuButton(String key, Runnable action) {
        return Buttons.create(
                Messages.get("menu.boton." + key), Messages.get("menu.boton." + key + ".tooltip"), action);
    }

    /** The menu's top bar: history, theme and language, top right. */
    private HBox topBar() {
        ComboBox<Language> selector = new ComboBox<>();
        selector.getItems().setAll(Language.values());
        selector.setValue(Messages.language());
        selector.setTooltip(new Tooltip(Messages.get("menu.idioma.tooltip")));
        selector.setOnAction(e -> {
            Language chosen = selector.getValue();
            if (chosen != null && chosen != Messages.language()) {
                Messages.select(chosen);
                stage.setTitle(Messages.get("app.titulo"));
                showMenu(); // rebuilds the menu already translated
            }
        });

        String key = Theme.isDark() ? "menu.tema.claro" : "menu.tema.oscuro";
        Button theme = Buttons.create(Messages.get(key), Messages.get("menu.tema.tooltip"), () -> {
            Theme.toggle();
            Theme.applyTo(stage.getScene());
            showMenu(); // rebuilds the bar with the correct label
        });

        Button history = Buttons.create(
                Messages.get("menu.historial"), Messages.get("menu.historial.tooltip"), this::historyScreen);

        HBox bar = new HBox(8, history, theme, selector);
        bar.setAlignment(Pos.CENTER_RIGHT);
        return bar;
    }

    /** Screen with the latest calculations ({@link History}). */
    private void historyScreen() {
        Label header = new Label(Messages.get("historial.titulo"));
        header.getStyleClass().add("encabezado");

        List<History.Entry> entries = History.recent();

        Button clear = Buttons.create(Messages.get("historial.vaciar"), () -> {
            History.clear();
            historyScreen();
        });
        clear.setDisable(entries.isEmpty());
        Button back = Buttons.create(Messages.get("form.volver"), Messages.get("form.volver.tooltip"), this::showMenu);
        back.setCancelButton(true); // Esc

        VBox content = new VBox(16, header, new HBox(8, clear, back));
        content.setAlignment(Pos.TOP_CENTER);
        content.setMaxWidth(MENU_WIDTH);
        content.setPadding(new Insets(20));

        if (entries.isEmpty()) {
            Label empty = new Label(Messages.get("historial.vacio"));
            empty.getStyleClass().add("categoria");
            content.getChildren().add(empty);
        } else {
            VBox list = new VBox(14);
            for (History.Entry entry : entries) {
                Label title = new Label(entry.title());
                title.getStyleClass().add("historial-titulo");
                Label result = new Label(entry.result());
                result.setWrapText(true);
                result.getStyleClass().add("resultado");

                VBox block = new VBox(2, title);
                if (entry.timestamp() != null) {
                    Label date = new Label(readableDate(entry.timestamp()));
                    date.getStyleClass().add("historial-fecha");
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
        scroll.getStyleClass().add("formulario");
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
                Messages.get("pitagoras.titulo"),
                Messages.get("pitagoras.instrucciones"),
                List.of(Messages.get("pitagoras.campo.catetoA"), Messages.get("pitagoras.campo.catetoB")),
                NumericFilter.Type.DECIMAL,
                values -> {
                    Triangle t =
                            Calculator.solveRightTriangle(Input.asDouble(values.get(0)), Input.asDouble(values.get(1)));
                    return Messages.get(
                            "pitagoras.resultado",
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
                Messages.get("cilindro.titulo"),
                Messages.get("cilindro.instrucciones"),
                List.of(Messages.get("cilindro.campo.radio"), Messages.get("cilindro.campo.altura")),
                NumericFilter.Type.DECIMAL,
                values -> Messages.get(
                        "cilindro.resultado",
                        Format.number(
                                Calculator.cylinderArea(Input.asDouble(values.get(0)), Input.asDouble(values.get(1))))),
                values -> CylinderSteps.explain(Input.asDouble(values.get(0)), Input.asDouble(values.get(1))));
    }

    private void leapYearScreen() {
        forms.show(
                Messages.get("bisiesto.titulo"),
                Messages.get("bisiesto.instrucciones"),
                List.of(Messages.get("bisiesto.campo.anio")),
                NumericFilter.Type.INTEGER,
                values -> {
                    int year = Input.asInt(values.get(0));
                    String key = Calculator.isLeapYear(year) ? "bisiesto.resultado.si" : "bisiesto.resultado.no";
                    return Messages.get(key, String.valueOf(year));
                });
    }

    private void factorialScreen() {
        forms.show(
                Messages.get("factorial.titulo"),
                Messages.get("factorial.instrucciones"),
                List.of(Messages.get("factorial.campo.numero")),
                NumericFilter.Type.INTEGER,
                values -> {
                    int n = Input.asInt(values.get(0));
                    BigInteger factorial = Calculator.factorial(n);
                    return Messages.get("factorial.resultado", String.valueOf(n), Format.bigInteger(factorial));
                });
    }

    private void multipleScreen() {
        forms.show(
                Messages.get("multiplo.titulo"),
                Messages.get("multiplo.instrucciones"),
                List.of(Messages.get("multiplo.campo.primero"), Messages.get("multiplo.campo.segundo")),
                NumericFilter.Type.INTEGER,
                values -> {
                    long a = Input.asLong(values.get(0));
                    long b = Input.asLong(values.get(1));
                    String key = Calculator.isMultiple(a, b) ? "multiplo.resultado.si" : "multiplo.resultado.no";
                    return Messages.get(key, String.valueOf(a), String.valueOf(b));
                });
    }

    private void gradesScreen() {
        List<String> prompts = new ArrayList<>(PASSING_GRADES);
        for (int i = 1; i <= PASSING_GRADES; i++) {
            prompts.add(Messages.get("aprobado.campo.nota", i));
        }
        forms.show(
                Messages.get("aprobado.titulo"),
                Messages.get("aprobado.instrucciones", String.valueOf((int) Calculator.MIN_GRADE), String.valueOf((int)
                        Calculator.MAX_GRADE)),
                prompts,
                NumericFilter.Type.DECIMAL,
                values -> {
                    double[] grades =
                            values.stream().mapToDouble(Input::asDouble).toArray();
                    double mean = Calculator.mean(grades);
                    String key = Calculator.isPassing(mean)
                            ? "aprobado.resultado.aprobado"
                            : "aprobado.resultado.suspendido";
                    return Messages.get(key, Format.twoDecimals(mean));
                });
    }

    private void quadraticScreen() {
        forms.show(
                Messages.get("cuadratica.titulo"),
                Messages.get("cuadratica.instrucciones"),
                List.of(
                        Messages.get("cuadratica.campo.a"),
                        Messages.get("cuadratica.campo.b"),
                        Messages.get("cuadratica.campo.c")),
                NumericFilter.Type.DECIMAL,
                values -> {
                    QuadraticEquation e = Calculator.solveQuadratic(
                            Input.asDouble(values.get(0)),
                            Input.asDouble(values.get(1)),
                            Input.asDouble(values.get(2)));
                    String delta = Format.number(e.discriminant());
                    if (e.hasDoubleRoot()) {
                        return Messages.get("cuadratica.resultado.doble", Format.number(e.x1().real()));
                    }
                    if (e.hasRealRoots()) {
                        return Messages.get(
                                "cuadratica.resultado.reales",
                                delta,
                                Format.number(e.x1().real()),
                                Format.number(e.x2().real()));
                    }
                    return Messages.get(
                            "cuadratica.resultado.complejas", delta, formatRoot(e.x1()), formatRoot(e.x2()));
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
                Messages.get("potencia.titulo"),
                Messages.get("potencia.instrucciones"),
                List.of(Messages.get("potencia.campo.base"), Messages.get("potencia.campo.exponente")),
                NumericFilter.Type.DECIMAL,
                values -> {
                    double base = Input.asDouble(values.get(0));
                    double exponent = Input.asDouble(values.get(1));
                    return Messages.get(
                            "potencia.resultado",
                            Format.number(base),
                            Format.number(exponent),
                            Format.number(Calculator.power(base, exponent)));
                });
    }

    private void rootScreen() {
        forms.show(
                Messages.get("raiz.titulo"),
                Messages.get("raiz.instrucciones"),
                List.of(Messages.get("raiz.campo.radicando"), Messages.get("raiz.campo.indice")),
                NumericFilter.Type.DECIMAL,
                values -> {
                    double radicand = Input.asDouble(values.get(0));
                    double index = Input.asDouble(values.get(1));
                    return Messages.get(
                            "raiz.resultado",
                            Format.number(index),
                            Format.number(radicand),
                            Format.number(Calculator.nthRoot(radicand, index)));
                });
    }

    private void gcdScreen() {
        forms.show(
                Messages.get("mcd.titulo"),
                Messages.get("mcd.instrucciones"),
                List.of(Messages.get("mcd.campo.a"), Messages.get("mcd.campo.b")),
                NumericFilter.Type.INTEGER,
                values -> {
                    long a = Input.asLong(values.get(0));
                    long b = Input.asLong(values.get(1));
                    return Messages.get(
                            "mcd.resultado",
                            Format.integer(a),
                            Format.integer(b),
                            Format.integer(Calculator.gcd(a, b)),
                            Format.integer(Calculator.lcm(a, b)));
                });
    }

    private void primeScreen() {
        forms.show(
                Messages.get("primo.titulo"),
                Messages.get("primo.instrucciones"),
                List.of(Messages.get("primo.campo.numero")),
                NumericFilter.Type.INTEGER,
                values -> {
                    long n = Input.asLong(values.get(0));
                    Calculator.Primality p = Calculator.analyzePrimality(n);
                    if (p.prime()) {
                        return Messages.get("primo.resultado.si", Format.integer(n));
                    }
                    if (p.isComposite()) {
                        long divisor = p.smallestProperDivisor();
                        return Messages.get(
                                "primo.resultado.compuesto",
                                Format.integer(n),
                                Format.integer(divisor),
                                Format.integer(n / divisor));
                    }
                    return Messages.get("primo.resultado.no", Format.integer(n));
                });
    }

    private void baseScreen() {
        forms.show(
                Messages.get("base.titulo"),
                Messages.get("base.instrucciones"),
                List.of(Messages.get("base.campo.numero")),
                null, // no filter: the number may carry a prefix and hexadecimal digits
                values -> {
                    Calculator.BaseConversion c = Calculator.convertBase(values.get(0));
                    return Messages.get("base.resultado", c.binary(), c.octal(), c.decimal(), c.hex());
                });
    }

    private void percentageScreen() {
        forms.show(
                Messages.get("porcentaje.titulo"),
                Messages.get("porcentaje.instrucciones"),
                List.of(Messages.get("porcentaje.campo.porcentaje"), Messages.get("porcentaje.campo.cantidad")),
                NumericFilter.Type.DECIMAL,
                values -> {
                    double percentage = Input.asDouble(values.get(0));
                    double amount = Input.asDouble(values.get(1));
                    return Messages.get(
                            "porcentaje.resultado",
                            Format.number(percentage),
                            Format.number(amount),
                            Format.number(Calculator.percentageOf(percentage, amount)));
                },
                values -> PercentageSteps.explain(Input.asDouble(values.get(0)), Input.asDouble(values.get(1))));
    }

    private void ruleOfThreeScreen() {
        forms.show(
                Messages.get("regladetres.titulo"),
                Messages.get("regladetres.instrucciones"),
                List.of(
                        Messages.get("regladetres.campo.a"),
                        Messages.get("regladetres.campo.b"),
                        Messages.get("regladetres.campo.c")),
                NumericFilter.Type.DECIMAL,
                values -> {
                    double a = Input.asDouble(values.get(0));
                    double b = Input.asDouble(values.get(1));
                    double c = Input.asDouble(values.get(2));
                    return Messages.get(
                            "regladetres.resultado",
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
                Messages.get("imc.sistema.metrico"),
                List.of(Messages.get("imc.campo.peso.kg"), Messages.get("imc.campo.altura.cm")),
                values ->
                        bmiResult(Input.asDouble(values.get(0)), Conversions.cmToMeters(Input.asDouble(values.get(1)))),
                values -> toCommon(values, v -> new double[] {v[0], Conversions.cmToMeters(v[1])}),
                common -> List.of(rounded(common[0], 1), rounded(Conversions.metersToCm(common[1]), 0)));

        FormBuilder.Mode imperial = new FormBuilder.Mode(
                Messages.get("imc.sistema.imperial"),
                List.of(
                        Messages.get("imc.campo.peso.lb"),
                        Messages.get("imc.campo.altura.pies"),
                        Messages.get("imc.campo.altura.pulgadas")),
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
                Messages.get("imc.titulo"),
                Messages.get("imc.instrucciones"),
                Messages.get("imc.sistema"),
                List.of(metric, imperial),
                NumericFilter.Type.DECIMAL);
    }

    private String bmiResult(double weightKg, double heightM) {
        Calculator.BodyMassIndex r = Calculator.bmi(weightKg, heightM);
        return Messages.get(
                "imc.resultado",
                Format.twoDecimals(r.value()),
                Messages.get("imc.categoria." + r.category().name().toLowerCase()));
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
