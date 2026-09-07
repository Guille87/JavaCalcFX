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
import io.guillermoamadodiaz.javacalcfx.ui.ConstructorDeFormularios;
import io.guillermoamadodiaz.javacalcfx.ui.Format;
import io.guillermoamadodiaz.javacalcfx.ui.Historial;
import io.guillermoamadodiaz.javacalcfx.ui.Input;
import io.guillermoamadodiaz.javacalcfx.ui.Navigator;
import io.guillermoamadodiaz.javacalcfx.ui.NumericFilter;
import io.guillermoamadodiaz.javacalcfx.ui.PasoAPasoCilindro;
import io.guillermoamadodiaz.javacalcfx.ui.PasoAPasoCuadratica;
import io.guillermoamadodiaz.javacalcfx.ui.PasoAPasoPitagoras;
import io.guillermoamadodiaz.javacalcfx.ui.PasoAPasoPorcentaje;
import io.guillermoamadodiaz.javacalcfx.ui.PasoAPasoReglaDeTres;
import io.guillermoamadodiaz.javacalcfx.ui.Tema;
import io.guillermoamadodiaz.javacalcfx.ui.UltimaCalculadora;
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
 * Ventana principal. Solo hace tres cosas: montar la infraestructura de interfaz
 * ({@link Navigator}, {@link AsyncCalculations}, {@link ConstructorDeFormularios}),
 * declarar el catálogo de calculadoras y cerrar el ejecutor al salir. Cada
 * {@code pantallaX()} describe únicamente <em>qué</em> se pide y <em>qué</em> se
 * muestra; la aritmética vive en {@link Calculator} y los textos en {@link Messages}.
 */
public class SelectorDeOpciones extends Application {

    private static final double ANCHO_BOTON_MENU = 210;
    private static final double ANCHO_MENU = 690;
    private static final int NOTAS_APROBADO = 5;

    private final StackPane raiz = new StackPane();
    private final AsyncCalculations calculos = new AsyncCalculations();
    private final Navigator navegador = new Navigator(raiz, calculos::cancel);
    private final ConstructorDeFormularios formularios =
            new ConstructorDeFormularios(navegador, calculos, this::mostrarMenu);

    private Stage escenario;

    private static final int[] TAMANOS_ICONO = {16, 32, 48, 64, 128, 256};

    @Override
    public void start(Stage escenario) {
        this.escenario = escenario;
        escenario.setTitle(Messages.get("app.titulo"));
        for (int px : TAMANOS_ICONO) {
            var url = getClass().getResource("icons/icon-" + px + ".png");
            if (url != null) {
                escenario.getIcons().add(new Image(url.toExternalForm()));
            }
        }
        raiz.setPadding(new Insets(20));

        Scene escena = new Scene(raiz, 695, 500);
        var hojaEstilos = getClass().getResource("styles.css");
        if (hojaEstilos != null) {
            escena.getStylesheets().add(hojaEstilos.toExternalForm());
        }
        Tema.aplicarA(escena); // restaura el modo claro/oscuro guardado

        Optional<String> ultimaCalculadora = UltimaCalculadora.recordada();
        mostrarMenu(); // esto la olvida; por eso se lee antes
        escenario.setScene(escena);
        escenario.setMinWidth(710);
        escenario.setMinHeight(600);
        WindowState.restore(escenario);
        escenario.show();
        WindowState.watch(escenario);
        ultimaCalculadora.ifPresent(this::abrirCalculadora); // reabre la última pantalla
    }

    @Override
    public void stop() {
        calculos.close();
    }

    // ------------------------------------------------------------------
    // Menú
    // ------------------------------------------------------------------

    /** Una entrada del menú: su clave de textos y qué pantalla abre. */
    private record EntradaMenu(String clave, Runnable abrir) {}

    /** Una categoría del menú: su clave de textos y sus calculadoras. */
    private record Categoria(String clave, List<EntradaMenu> entradas) {}

    private List<Categoria> catalogo() {
        return List.of(
                new Categoria(
                        "geometria",
                        List.of(
                                entrada("pitagoras", this::pantallaPitagoras),
                                entrada("cilindro", this::pantallaCilindro))),
                new Categoria(
                        "aritmetica",
                        List.of(
                                entrada("factorial", this::pantallaFactorial),
                                entrada("multiplo", this::pantallaMultiplo),
                                entrada("mcd", this::pantallaMcd),
                                entrada("primo", this::pantallaPrimo),
                                entrada("base", this::pantallaBase))),
                new Categoria(
                        "potencias",
                        List.of(
                                entrada("potencia", this::pantallaPotencia),
                                entrada("raiz", this::pantallaRaiz),
                                entrada("cuadratica", this::pantallaCuadratica))),
                new Categoria(
                        "proporciones",
                        List.of(
                                entrada("porcentaje", this::pantallaPorcentaje),
                                entrada("regladetres", this::pantallaReglaDeTres))),
                new Categoria(
                        "otros",
                        List.of(
                                entrada("bisiesto", this::pantallaBisiesto),
                                entrada("aprobado", this::pantallaAprobado),
                                entrada("imc", this::pantallaImc))));
    }

    /** Entrada de menú que, al abrirse, recuerda la calculadora para el próximo arranque. */
    private EntradaMenu entrada(String clave, Runnable pantalla) {
        return new EntradaMenu(clave, () -> {
            UltimaCalculadora.recordar(clave);
            pantalla.run();
        });
    }

    private void abrirCalculadora(String clave) {
        catalogo().stream()
                .flatMap(categoria -> categoria.entradas().stream())
                .filter(menu -> menu.clave().equals(clave))
                .findFirst()
                .ifPresent(menu -> menu.abrir().run());
    }

    private void mostrarMenu() {
        UltimaCalculadora.olvidar(); // se está en el menú: no hay «última calculadora» que reabrir

        Label titulo = new Label(Messages.get("menu.titulo"));
        titulo.getStyleClass().add("titulo");

        VBox categorias = new VBox(16);
        categorias.setAlignment(Pos.TOP_LEFT);
        categorias.setMaxWidth(ANCHO_MENU);
        for (Categoria categoria : catalogo()) {
            Label nombre = new Label(Messages.get("menu.categoria." + categoria.clave()));
            nombre.getStyleClass().add("categoria");

            FlowPane botones = new FlowPane(10, 10);
            for (EntradaMenu entrada : categoria.entradas()) {
                Button boton = botonMenu(entrada.clave(), entrada.abrir());
                boton.setPrefWidth(ANCHO_BOTON_MENU);
                boton.setWrapText(true);
                botones.getChildren().add(boton);
            }
            categorias.getChildren().add(new VBox(6, nombre, botones));
        }

        VBox centro = new VBox(20, titulo, categorias);
        centro.setAlignment(Pos.CENTER);

        ScrollPane scroll = new ScrollPane(new StackPane(centro));
        scroll.setFitToWidth(true);
        scroll.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        scroll.getStyleClass().add("formulario"); // fondo transparente, sin borde

        BorderPane menu = new BorderPane(scroll);
        menu.setTop(barraDeIdioma());
        navegador.show(menu);
    }

    /** Botón del menú a partir de su clave: usa {@code menu.boton.<clave>[.tooltip]}. */
    private Button botonMenu(String clave, Runnable accion) {
        return Buttons.create(
                Messages.get("menu.boton." + clave), Messages.get("menu.boton." + clave + ".tooltip"), accion);
    }

    /** Barra superior del menú: historial, tema e idioma, arriba a la derecha. */
    private HBox barraDeIdioma() {
        ComboBox<Language> selector = new ComboBox<>();
        selector.getItems().setAll(Language.values());
        selector.setValue(Messages.language());
        selector.setTooltip(new Tooltip(Messages.get("menu.idioma.tooltip")));
        selector.setOnAction(e -> {
            Language elegido = selector.getValue();
            if (elegido != null && elegido != Messages.language()) {
                Messages.select(elegido);
                escenario.setTitle(Messages.get("app.titulo"));
                mostrarMenu(); // reconstruye el menú ya traducido
            }
        });

        String clave = Tema.esOscuro() ? "menu.tema.claro" : "menu.tema.oscuro";
        Button tema = Buttons.create(Messages.get(clave), Messages.get("menu.tema.tooltip"), () -> {
            Tema.alternar();
            Tema.aplicarA(escenario.getScene());
            mostrarMenu(); // reconstruye la barra con la etiqueta correcta
        });

        Button historial = Buttons.create(
                Messages.get("menu.historial"), Messages.get("menu.historial.tooltip"), this::pantallaHistorial);

        HBox barra = new HBox(8, historial, tema, selector);
        barra.setAlignment(Pos.CENTER_RIGHT);
        return barra;
    }

    /** Pantalla con los últimos cálculos ({@link Historial}). */
    private void pantallaHistorial() {
        Label encabezado = new Label(Messages.get("historial.titulo"));
        encabezado.getStyleClass().add("encabezado");

        List<Historial.Entrada> entradas = Historial.reciente();

        Button vaciar = Buttons.create(Messages.get("historial.vaciar"), () -> {
            Historial.limpiar();
            pantallaHistorial();
        });
        vaciar.setDisable(entradas.isEmpty());
        Button volver =
                Buttons.create(Messages.get("form.volver"), Messages.get("form.volver.tooltip"), this::mostrarMenu);
        volver.setCancelButton(true); // Esc

        VBox contenido = new VBox(16, encabezado, new HBox(8, vaciar, volver));
        contenido.setAlignment(Pos.TOP_CENTER);
        contenido.setMaxWidth(ANCHO_MENU);
        contenido.setPadding(new Insets(20));

        if (entradas.isEmpty()) {
            Label vacio = new Label(Messages.get("historial.vacio"));
            vacio.getStyleClass().add("categoria");
            contenido.getChildren().add(vacio);
        } else {
            VBox lista = new VBox(14);
            for (Historial.Entrada entrada : entradas) {
                Label titulo = new Label(entrada.titulo());
                titulo.getStyleClass().add("historial-titulo");
                Label resultado = new Label(entrada.resultado());
                resultado.setWrapText(true);
                resultado.getStyleClass().add("resultado");

                VBox bloque = new VBox(2, titulo);
                if (entrada.momento() != null) {
                    Label fecha = new Label(fechaLegible(entrada.momento()));
                    fecha.getStyleClass().add("historial-fecha");
                    bloque.getChildren().add(fecha);
                }
                bloque.getChildren().add(resultado);
                lista.getChildren().add(bloque);
            }
            contenido.getChildren().add(lista);
        }

        ScrollPane scroll = new ScrollPane(new StackPane(contenido));
        scroll.setFitToWidth(true);
        scroll.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        scroll.getStyleClass().add("formulario");
        navegador.show(scroll);
    }

    /** Fecha y hora de un cálculo del historial, con los meses en el idioma actual. */
    private static String fechaLegible(Instant momento) {
        return DateTimeFormatter.ofPattern(
                        "d MMM yyyy, HH:mm", Messages.language().locale())
                .withZone(ZoneId.systemDefault())
                .format(momento);
    }

    // ------------------------------------------------------------------
    // Catálogo de calculadoras
    // ------------------------------------------------------------------

    private void pantallaPitagoras() {
        formularios.mostrar(
                Messages.get("pitagoras.titulo"),
                Messages.get("pitagoras.instrucciones"),
                List.of(Messages.get("pitagoras.campo.catetoA"), Messages.get("pitagoras.campo.catetoB")),
                NumericFilter.Type.DECIMAL,
                valores -> {
                    Triangle t = Calculator.solveRightTriangle(
                            Input.asDouble(valores.get(0)), Input.asDouble(valores.get(1)));
                    return Messages.get(
                            "pitagoras.resultado",
                            Format.number(t.hypotenuse()),
                            Format.number(t.area()),
                            Format.number(t.perimeter()),
                            Format.number(t.angleAlpha()),
                            Format.number(t.angleBeta()));
                },
                valores ->
                        PasoAPasoPitagoras.desarrollo(Input.asDouble(valores.get(0)), Input.asDouble(valores.get(1))));
    }

    private void pantallaCilindro() {
        formularios.mostrar(
                Messages.get("cilindro.titulo"),
                Messages.get("cilindro.instrucciones"),
                List.of(Messages.get("cilindro.campo.radio"), Messages.get("cilindro.campo.altura")),
                NumericFilter.Type.DECIMAL,
                valores -> Messages.get(
                        "cilindro.resultado",
                        Format.number(Calculator.cylinderArea(
                                Input.asDouble(valores.get(0)), Input.asDouble(valores.get(1))))),
                valores ->
                        PasoAPasoCilindro.desarrollo(Input.asDouble(valores.get(0)), Input.asDouble(valores.get(1))));
    }

    private void pantallaBisiesto() {
        formularios.mostrar(
                Messages.get("bisiesto.titulo"),
                Messages.get("bisiesto.instrucciones"),
                List.of(Messages.get("bisiesto.campo.anio")),
                NumericFilter.Type.INTEGER,
                valores -> {
                    int anio = Input.asInt(valores.get(0));
                    String clave = Calculator.isLeapYear(anio) ? "bisiesto.resultado.si" : "bisiesto.resultado.no";
                    return Messages.get(clave, String.valueOf(anio));
                });
    }

    private void pantallaFactorial() {
        formularios.mostrar(
                Messages.get("factorial.titulo"),
                Messages.get("factorial.instrucciones"),
                List.of(Messages.get("factorial.campo.numero")),
                NumericFilter.Type.INTEGER,
                valores -> {
                    int n = Input.asInt(valores.get(0));
                    BigInteger factorial = Calculator.factorial(n);
                    return Messages.get("factorial.resultado", String.valueOf(n), Format.bigInteger(factorial));
                });
    }

    private void pantallaMultiplo() {
        formularios.mostrar(
                Messages.get("multiplo.titulo"),
                Messages.get("multiplo.instrucciones"),
                List.of(Messages.get("multiplo.campo.primero"), Messages.get("multiplo.campo.segundo")),
                NumericFilter.Type.INTEGER,
                valores -> {
                    long a = Input.asLong(valores.get(0));
                    long b = Input.asLong(valores.get(1));
                    String clave = Calculator.isMultiple(a, b) ? "multiplo.resultado.si" : "multiplo.resultado.no";
                    return Messages.get(clave, String.valueOf(a), String.valueOf(b));
                });
    }

    private void pantallaAprobado() {
        List<String> prompts = new ArrayList<>(NOTAS_APROBADO);
        for (int i = 1; i <= NOTAS_APROBADO; i++) {
            prompts.add(Messages.get("aprobado.campo.nota", i));
        }
        formularios.mostrar(
                Messages.get("aprobado.titulo"),
                Messages.get("aprobado.instrucciones", String.valueOf((int) Calculator.MIN_GRADE), String.valueOf((int)
                        Calculator.MAX_GRADE)),
                prompts,
                NumericFilter.Type.DECIMAL,
                valores -> {
                    double[] notas =
                            valores.stream().mapToDouble(Input::asDouble).toArray();
                    double media = Calculator.mean(notas);
                    String clave = Calculator.isPassing(media)
                            ? "aprobado.resultado.aprobado"
                            : "aprobado.resultado.suspendido";
                    return Messages.get(clave, Format.twoDecimals(media));
                });
    }

    private void pantallaCuadratica() {
        formularios.mostrar(
                Messages.get("cuadratica.titulo"),
                Messages.get("cuadratica.instrucciones"),
                List.of(
                        Messages.get("cuadratica.campo.a"),
                        Messages.get("cuadratica.campo.b"),
                        Messages.get("cuadratica.campo.c")),
                NumericFilter.Type.DECIMAL,
                valores -> {
                    QuadraticEquation e = Calculator.solveQuadratic(
                            Input.asDouble(valores.get(0)),
                            Input.asDouble(valores.get(1)),
                            Input.asDouble(valores.get(2)));
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
                            "cuadratica.resultado.complejas", delta, formatoRaiz(e.x1()), formatoRaiz(e.x2()));
                },
                valores -> PasoAPasoCuadratica.desarrollo(
                        Input.asDouble(valores.get(0)),
                        Input.asDouble(valores.get(1)),
                        Input.asDouble(valores.get(2))));
    }

    /** Formatea una raíz compleja como {@code a + b i} (o {@code a - b i}). */
    private static String formatoRaiz(Root raiz) {
        String signo = raiz.imaginary() < 0 ? " - " : " + ";
        return Format.number(raiz.real()) + signo + Format.number(Math.abs(raiz.imaginary())) + " i";
    }

    private void pantallaPotencia() {
        formularios.mostrar(
                Messages.get("potencia.titulo"),
                Messages.get("potencia.instrucciones"),
                List.of(Messages.get("potencia.campo.base"), Messages.get("potencia.campo.exponente")),
                NumericFilter.Type.DECIMAL,
                valores -> {
                    double base = Input.asDouble(valores.get(0));
                    double exponente = Input.asDouble(valores.get(1));
                    return Messages.get(
                            "potencia.resultado",
                            Format.number(base),
                            Format.number(exponente),
                            Format.number(Calculator.power(base, exponente)));
                });
    }

    private void pantallaRaiz() {
        formularios.mostrar(
                Messages.get("raiz.titulo"),
                Messages.get("raiz.instrucciones"),
                List.of(Messages.get("raiz.campo.radicando"), Messages.get("raiz.campo.indice")),
                NumericFilter.Type.DECIMAL,
                valores -> {
                    double radicando = Input.asDouble(valores.get(0));
                    double indice = Input.asDouble(valores.get(1));
                    return Messages.get(
                            "raiz.resultado",
                            Format.number(indice),
                            Format.number(radicando),
                            Format.number(Calculator.nthRoot(radicando, indice)));
                });
    }

    private void pantallaMcd() {
        formularios.mostrar(
                Messages.get("mcd.titulo"),
                Messages.get("mcd.instrucciones"),
                List.of(Messages.get("mcd.campo.a"), Messages.get("mcd.campo.b")),
                NumericFilter.Type.INTEGER,
                valores -> {
                    long a = Input.asLong(valores.get(0));
                    long b = Input.asLong(valores.get(1));
                    return Messages.get(
                            "mcd.resultado",
                            Format.integer(a),
                            Format.integer(b),
                            Format.integer(Calculator.gcd(a, b)),
                            Format.integer(Calculator.lcm(a, b)));
                });
    }

    private void pantallaPrimo() {
        formularios.mostrar(
                Messages.get("primo.titulo"),
                Messages.get("primo.instrucciones"),
                List.of(Messages.get("primo.campo.numero")),
                NumericFilter.Type.INTEGER,
                valores -> {
                    long n = Input.asLong(valores.get(0));
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

    private void pantallaBase() {
        formularios.mostrar(
                Messages.get("base.titulo"),
                Messages.get("base.instrucciones"),
                List.of(Messages.get("base.campo.numero")),
                null, // sin filtro: el número puede llevar prefijo y dígitos hexadecimales
                valores -> {
                    Calculator.BaseConversion c = Calculator.convertBase(valores.get(0));
                    return Messages.get("base.resultado", c.binary(), c.octal(), c.decimal(), c.hex());
                });
    }

    private void pantallaPorcentaje() {
        formularios.mostrar(
                Messages.get("porcentaje.titulo"),
                Messages.get("porcentaje.instrucciones"),
                List.of(Messages.get("porcentaje.campo.porcentaje"), Messages.get("porcentaje.campo.cantidad")),
                NumericFilter.Type.DECIMAL,
                valores -> {
                    double porcentaje = Input.asDouble(valores.get(0));
                    double cantidad = Input.asDouble(valores.get(1));
                    return Messages.get(
                            "porcentaje.resultado",
                            Format.number(porcentaje),
                            Format.number(cantidad),
                            Format.number(Calculator.percentageOf(porcentaje, cantidad)));
                },
                valores ->
                        PasoAPasoPorcentaje.desarrollo(Input.asDouble(valores.get(0)), Input.asDouble(valores.get(1))));
    }

    private void pantallaReglaDeTres() {
        formularios.mostrar(
                Messages.get("regladetres.titulo"),
                Messages.get("regladetres.instrucciones"),
                List.of(
                        Messages.get("regladetres.campo.a"),
                        Messages.get("regladetres.campo.b"),
                        Messages.get("regladetres.campo.c")),
                NumericFilter.Type.DECIMAL,
                valores -> {
                    double a = Input.asDouble(valores.get(0));
                    double b = Input.asDouble(valores.get(1));
                    double c = Input.asDouble(valores.get(2));
                    return Messages.get(
                            "regladetres.resultado",
                            Format.number(a),
                            Format.number(b),
                            Format.number(c),
                            Format.number(Calculator.ruleOfThree(a, b, c)));
                },
                valores -> PasoAPasoReglaDeTres.desarrollo(
                        Input.asDouble(valores.get(0)),
                        Input.asDouble(valores.get(1)),
                        Input.asDouble(valores.get(2))));
    }

    private void pantallaImc() {
        // Representación común entre los dos modos: {peso en kg, altura en m}.
        ConstructorDeFormularios.Modo metrico = new ConstructorDeFormularios.Modo(
                Messages.get("imc.sistema.metrico"),
                List.of(Messages.get("imc.campo.peso.kg"), Messages.get("imc.campo.altura.cm")),
                valores -> resultadoImc(
                        Input.asDouble(valores.get(0)), Conversions.cmToMeters(Input.asDouble(valores.get(1)))),
                valores -> comun(valores, v -> new double[] {v[0], Conversions.cmToMeters(v[1])}),
                comun -> List.of(redondeado(comun[0], 1), redondeado(Conversions.metersToCm(comun[1]), 0)));

        ConstructorDeFormularios.Modo imperial = new ConstructorDeFormularios.Modo(
                Messages.get("imc.sistema.imperial"),
                List.of(
                        Messages.get("imc.campo.peso.lb"),
                        Messages.get("imc.campo.altura.pies"),
                        Messages.get("imc.campo.altura.pulgadas")),
                valores -> resultadoImc(
                        Conversions.poundsToKilos(Input.asDouble(valores.get(0))),
                        Conversions.feetInchesToMeters(Input.asDouble(valores.get(1)), Input.asDouble(valores.get(2)))),
                valores -> comun(valores, v ->
                        new double[] {Conversions.poundsToKilos(v[0]), Conversions.feetInchesToMeters(v[1], v[2])}),
                comun -> {
                    double[] piesYPulgadas = Conversions.metersToFeetInches(comun[1]);
                    return List.of(
                            redondeado(Conversions.kilosToPounds(comun[0]), 1),
                            Format.number(piesYPulgadas[0]),
                            redondeado(piesYPulgadas[1], 1));
                });

        formularios.mostrarConModos(
                Messages.get("imc.titulo"),
                Messages.get("imc.instrucciones"),
                Messages.get("imc.sistema"),
                List.of(metrico, imperial),
                NumericFilter.Type.DECIMAL);
    }

    private String resultadoImc(double pesoKg, double alturaM) {
        Calculator.BodyMassIndex r = Calculator.bmi(pesoKg, alturaM);
        return Messages.get(
                "imc.resultado",
                Format.twoDecimals(r.value()),
                Messages.get("imc.categoria." + r.category().name().toLowerCase()));
    }

    /** Parsea los campos y aplica {@code aComun}; vacío si algún campo falta o no es válido. */
    private static Optional<double[]> comun(List<String> valores, UnaryOperator<double[]> aComun) {
        try {
            double[] crudos = new double[valores.size()];
            for (int i = 0; i < valores.size(); i++) {
                crudos[i] = Input.asDouble(valores.get(i));
            }
            return Optional.of(aComun.apply(crudos));
        } catch (RuntimeException noValido) {
            return Optional.empty();
        }
    }

    /**
     * Número redondeado a {@code decimales} decimales, para rellenar campos al
     * cambiar de sistema de medida. Los cm van sin decimales y las libras,
     * pulgadas y kg a uno: así la conversión de ida y vuelta (p. ej.
     * cm → pulgadas → cm) vuelve al mismo valor.
     */
    private static String redondeado(double valor, int decimales) {
        double factor = Math.pow(10, decimales);
        return Format.number(Math.round(valor * factor) / factor);
    }

    public static void main(String[] args) {
        launch(args);
    }
}
