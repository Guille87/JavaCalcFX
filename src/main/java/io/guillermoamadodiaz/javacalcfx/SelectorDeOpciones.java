package io.guillermoamadodiaz.javacalcfx;

import io.guillermoamadodiaz.javacalcfx.calc.Calculadora;
import io.guillermoamadodiaz.javacalcfx.calc.Calculadora.EcuacionCuadratica;
import io.guillermoamadodiaz.javacalcfx.calc.Calculadora.Raiz;
import io.guillermoamadodiaz.javacalcfx.calc.Calculadora.Triangulo;
import io.guillermoamadodiaz.javacalcfx.calc.Conversiones;
import io.guillermoamadodiaz.javacalcfx.i18n.Idioma;
import io.guillermoamadodiaz.javacalcfx.i18n.Textos;
import io.guillermoamadodiaz.javacalcfx.ui.Botones;
import io.guillermoamadodiaz.javacalcfx.ui.CalculosAsync;
import io.guillermoamadodiaz.javacalcfx.ui.ConstructorDeFormularios;
import io.guillermoamadodiaz.javacalcfx.ui.Entrada;
import io.guillermoamadodiaz.javacalcfx.ui.EstadoVentana;
import io.guillermoamadodiaz.javacalcfx.ui.FiltroNumerico;
import io.guillermoamadodiaz.javacalcfx.ui.Formato;
import io.guillermoamadodiaz.javacalcfx.ui.Historial;
import io.guillermoamadodiaz.javacalcfx.ui.Navegador;
import io.guillermoamadodiaz.javacalcfx.ui.PasoAPasoCilindro;
import io.guillermoamadodiaz.javacalcfx.ui.PasoAPasoCuadratica;
import io.guillermoamadodiaz.javacalcfx.ui.PasoAPasoPitagoras;
import io.guillermoamadodiaz.javacalcfx.ui.PasoAPasoPorcentaje;
import io.guillermoamadodiaz.javacalcfx.ui.PasoAPasoReglaDeTres;
import io.guillermoamadodiaz.javacalcfx.ui.Tema;
import io.guillermoamadodiaz.javacalcfx.ui.UltimaCalculadora;
import java.math.BigInteger;
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
 * ({@link Navegador}, {@link CalculosAsync}, {@link ConstructorDeFormularios}),
 * declarar el catálogo de calculadoras y cerrar el ejecutor al salir. Cada
 * {@code pantallaX()} describe únicamente <em>qué</em> se pide y <em>qué</em> se
 * muestra; la aritmética vive en {@link Calculadora} y los textos en {@link Textos}.
 */
public class SelectorDeOpciones extends Application {

    private static final double ANCHO_BOTON_MENU = 210;
    private static final double ANCHO_MENU = 690;
    private static final int NOTAS_APROBADO = 5;

    private final StackPane raiz = new StackPane();
    private final CalculosAsync calculos = new CalculosAsync();
    private final Navegador navegador = new Navegador(raiz, calculos::cancelar);
    private final ConstructorDeFormularios formularios =
            new ConstructorDeFormularios(navegador, calculos, this::mostrarMenu);

    private Stage escenario;

    private static final int[] TAMANOS_ICONO = {16, 32, 48, 64, 128, 256};

    @Override
    public void start(Stage escenario) {
        this.escenario = escenario;
        escenario.setTitle(Textos.get("app.titulo"));
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
        EstadoVentana.restaurar(escenario);
        escenario.show();
        EstadoVentana.vigilar(escenario);
        ultimaCalculadora.ifPresent(this::abrirCalculadora); // reabre la última pantalla
    }

    @Override
    public void stop() {
        calculos.cerrar();
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

        Label titulo = new Label(Textos.get("menu.titulo"));
        titulo.getStyleClass().add("titulo");

        VBox categorias = new VBox(16);
        categorias.setAlignment(Pos.TOP_LEFT);
        categorias.setMaxWidth(ANCHO_MENU);
        for (Categoria categoria : catalogo()) {
            Label nombre = new Label(Textos.get("menu.categoria." + categoria.clave()));
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
        navegador.mostrar(menu);
    }

    /** Botón del menú a partir de su clave: usa {@code menu.boton.<clave>[.tooltip]}. */
    private Button botonMenu(String clave, Runnable accion) {
        return Botones.crear(Textos.get("menu.boton." + clave), Textos.get("menu.boton." + clave + ".tooltip"), accion);
    }

    /** Barra superior del menú: historial, tema e idioma, arriba a la derecha. */
    private HBox barraDeIdioma() {
        ComboBox<Idioma> selector = new ComboBox<>();
        selector.getItems().setAll(Idioma.values());
        selector.setValue(Textos.idioma());
        selector.setTooltip(new Tooltip(Textos.get("menu.idioma.tooltip")));
        selector.setOnAction(e -> {
            Idioma elegido = selector.getValue();
            if (elegido != null && elegido != Textos.idioma()) {
                Textos.seleccionar(elegido);
                escenario.setTitle(Textos.get("app.titulo"));
                mostrarMenu(); // reconstruye el menú ya traducido
            }
        });

        String clave = Tema.esOscuro() ? "menu.tema.claro" : "menu.tema.oscuro";
        Button tema = Botones.crear(Textos.get(clave), Textos.get("menu.tema.tooltip"), () -> {
            Tema.alternar();
            Tema.aplicarA(escenario.getScene());
            mostrarMenu(); // reconstruye la barra con la etiqueta correcta
        });

        Button historial = Botones.crear(
                Textos.get("menu.historial"), Textos.get("menu.historial.tooltip"), this::pantallaHistorial);

        HBox barra = new HBox(8, historial, tema, selector);
        barra.setAlignment(Pos.CENTER_RIGHT);
        return barra;
    }

    /** Pantalla con los últimos cálculos ({@link Historial}). */
    private void pantallaHistorial() {
        Label encabezado = new Label(Textos.get("historial.titulo"));
        encabezado.getStyleClass().add("encabezado");

        List<Historial.Entrada> entradas = Historial.reciente();

        Button vaciar = Botones.crear(Textos.get("historial.vaciar"), () -> {
            Historial.limpiar();
            pantallaHistorial();
        });
        vaciar.setDisable(entradas.isEmpty());
        Button volver = Botones.crear(Textos.get("form.volver"), Textos.get("form.volver.tooltip"), this::mostrarMenu);
        volver.setCancelButton(true); // Esc

        VBox contenido = new VBox(16, encabezado, new HBox(8, vaciar, volver));
        contenido.setAlignment(Pos.TOP_CENTER);
        contenido.setMaxWidth(ANCHO_MENU);
        contenido.setPadding(new Insets(20));

        if (entradas.isEmpty()) {
            Label vacio = new Label(Textos.get("historial.vacio"));
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
                lista.getChildren().add(new VBox(2, titulo, resultado));
            }
            contenido.getChildren().add(lista);
        }

        ScrollPane scroll = new ScrollPane(new StackPane(contenido));
        scroll.setFitToWidth(true);
        scroll.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        scroll.getStyleClass().add("formulario");
        navegador.mostrar(scroll);
    }

    // ------------------------------------------------------------------
    // Catálogo de calculadoras
    // ------------------------------------------------------------------

    private void pantallaPitagoras() {
        formularios.mostrar(
                Textos.get("pitagoras.titulo"),
                Textos.get("pitagoras.instrucciones"),
                List.of(Textos.get("pitagoras.campo.catetoA"), Textos.get("pitagoras.campo.catetoB")),
                FiltroNumerico.Tipo.DECIMAL,
                valores -> {
                    Triangulo t = Calculadora.resolverTrianguloRectangulo(
                            Entrada.doble(valores.get(0)), Entrada.doble(valores.get(1)));
                    return Textos.get(
                            "pitagoras.resultado",
                            Formato.numero(t.hipotenusa()),
                            Formato.numero(t.area()),
                            Formato.numero(t.perimetro()),
                            Formato.numero(t.anguloAlfa()),
                            Formato.numero(t.anguloBeta()));
                },
                valores -> PasoAPasoPitagoras.desarrollo(Entrada.doble(valores.get(0)), Entrada.doble(valores.get(1))));
    }

    private void pantallaCilindro() {
        formularios.mostrar(
                Textos.get("cilindro.titulo"),
                Textos.get("cilindro.instrucciones"),
                List.of(Textos.get("cilindro.campo.radio"), Textos.get("cilindro.campo.altura")),
                FiltroNumerico.Tipo.DECIMAL,
                valores -> Textos.get(
                        "cilindro.resultado",
                        Formato.numero(Calculadora.areaCilindro(
                                Entrada.doble(valores.get(0)), Entrada.doble(valores.get(1))))),
                valores -> PasoAPasoCilindro.desarrollo(Entrada.doble(valores.get(0)), Entrada.doble(valores.get(1))));
    }

    private void pantallaBisiesto() {
        formularios.mostrar(
                Textos.get("bisiesto.titulo"),
                Textos.get("bisiesto.instrucciones"),
                List.of(Textos.get("bisiesto.campo.anio")),
                FiltroNumerico.Tipo.ENTERO,
                valores -> {
                    int anio = Entrada.entero(valores.get(0));
                    String clave = Calculadora.esBisiesto(anio) ? "bisiesto.resultado.si" : "bisiesto.resultado.no";
                    return Textos.get(clave, String.valueOf(anio));
                });
    }

    private void pantallaFactorial() {
        formularios.mostrar(
                Textos.get("factorial.titulo"),
                Textos.get("factorial.instrucciones"),
                List.of(Textos.get("factorial.campo.numero")),
                FiltroNumerico.Tipo.ENTERO,
                valores -> {
                    int n = Entrada.entero(valores.get(0));
                    BigInteger factorial = Calculadora.factorial(n);
                    return Textos.get("factorial.resultado", String.valueOf(n), Formato.enteroGrande(factorial));
                });
    }

    private void pantallaMultiplo() {
        formularios.mostrar(
                Textos.get("multiplo.titulo"),
                Textos.get("multiplo.instrucciones"),
                List.of(Textos.get("multiplo.campo.primero"), Textos.get("multiplo.campo.segundo")),
                FiltroNumerico.Tipo.ENTERO,
                valores -> {
                    long a = Entrada.largo(valores.get(0));
                    long b = Entrada.largo(valores.get(1));
                    String clave = Calculadora.esMultiplo(a, b) ? "multiplo.resultado.si" : "multiplo.resultado.no";
                    return Textos.get(clave, String.valueOf(a), String.valueOf(b));
                });
    }

    private void pantallaAprobado() {
        List<String> prompts = new ArrayList<>(NOTAS_APROBADO);
        for (int i = 1; i <= NOTAS_APROBADO; i++) {
            prompts.add(Textos.get("aprobado.campo.nota", i));
        }
        formularios.mostrar(
                Textos.get("aprobado.titulo"),
                Textos.get("aprobado.instrucciones", String.valueOf((int) Calculadora.NOTA_MINIMA), String.valueOf((int)
                        Calculadora.NOTA_MAXIMA)),
                prompts,
                FiltroNumerico.Tipo.DECIMAL,
                valores -> {
                    double[] notas =
                            valores.stream().mapToDouble(Entrada::doble).toArray();
                    double media = Calculadora.media(notas);
                    String clave = Calculadora.estaAprobado(media)
                            ? "aprobado.resultado.aprobado"
                            : "aprobado.resultado.suspendido";
                    return Textos.get(clave, Formato.dosDecimales(media));
                });
    }

    private void pantallaCuadratica() {
        formularios.mostrar(
                Textos.get("cuadratica.titulo"),
                Textos.get("cuadratica.instrucciones"),
                List.of(
                        Textos.get("cuadratica.campo.a"),
                        Textos.get("cuadratica.campo.b"),
                        Textos.get("cuadratica.campo.c")),
                FiltroNumerico.Tipo.DECIMAL,
                valores -> {
                    EcuacionCuadratica e = Calculadora.resolverEcuacionCuadratica(
                            Entrada.doble(valores.get(0)),
                            Entrada.doble(valores.get(1)),
                            Entrada.doble(valores.get(2)));
                    String delta = Formato.numero(e.discriminante());
                    if (e.tieneRaizDoble()) {
                        return Textos.get("cuadratica.resultado.doble", Formato.numero(e.x1().real()));
                    }
                    if (e.tieneRaicesReales()) {
                        return Textos.get(
                                "cuadratica.resultado.reales",
                                delta,
                                Formato.numero(e.x1().real()),
                                Formato.numero(e.x2().real()));
                    }
                    return Textos.get(
                            "cuadratica.resultado.complejas", delta, formatoRaiz(e.x1()), formatoRaiz(e.x2()));
                },
                valores -> PasoAPasoCuadratica.desarrollo(
                        Entrada.doble(valores.get(0)), Entrada.doble(valores.get(1)), Entrada.doble(valores.get(2))));
    }

    /** Formatea una raíz compleja como {@code a + b i} (o {@code a - b i}). */
    private static String formatoRaiz(Raiz raiz) {
        String signo = raiz.imaginaria() < 0 ? " - " : " + ";
        return Formato.numero(raiz.real()) + signo + Formato.numero(Math.abs(raiz.imaginaria())) + " i";
    }

    private void pantallaPotencia() {
        formularios.mostrar(
                Textos.get("potencia.titulo"),
                Textos.get("potencia.instrucciones"),
                List.of(Textos.get("potencia.campo.base"), Textos.get("potencia.campo.exponente")),
                FiltroNumerico.Tipo.DECIMAL,
                valores -> {
                    double base = Entrada.doble(valores.get(0));
                    double exponente = Entrada.doble(valores.get(1));
                    return Textos.get(
                            "potencia.resultado",
                            Formato.numero(base),
                            Formato.numero(exponente),
                            Formato.numero(Calculadora.potencia(base, exponente)));
                });
    }

    private void pantallaRaiz() {
        formularios.mostrar(
                Textos.get("raiz.titulo"),
                Textos.get("raiz.instrucciones"),
                List.of(Textos.get("raiz.campo.radicando"), Textos.get("raiz.campo.indice")),
                FiltroNumerico.Tipo.DECIMAL,
                valores -> {
                    double radicando = Entrada.doble(valores.get(0));
                    double indice = Entrada.doble(valores.get(1));
                    return Textos.get(
                            "raiz.resultado",
                            Formato.numero(indice),
                            Formato.numero(radicando),
                            Formato.numero(Calculadora.raiz(radicando, indice)));
                });
    }

    private void pantallaMcd() {
        formularios.mostrar(
                Textos.get("mcd.titulo"),
                Textos.get("mcd.instrucciones"),
                List.of(Textos.get("mcd.campo.a"), Textos.get("mcd.campo.b")),
                FiltroNumerico.Tipo.ENTERO,
                valores -> {
                    long a = Entrada.largo(valores.get(0));
                    long b = Entrada.largo(valores.get(1));
                    return Textos.get(
                            "mcd.resultado",
                            Formato.entero(a),
                            Formato.entero(b),
                            Formato.entero(Calculadora.mcd(a, b)),
                            Formato.entero(Calculadora.mcm(a, b)));
                });
    }

    private void pantallaPrimo() {
        formularios.mostrar(
                Textos.get("primo.titulo"),
                Textos.get("primo.instrucciones"),
                List.of(Textos.get("primo.campo.numero")),
                FiltroNumerico.Tipo.ENTERO,
                valores -> {
                    long n = Entrada.largo(valores.get(0));
                    Calculadora.Primalidad p = Calculadora.analizarPrimalidad(n);
                    if (p.primo()) {
                        return Textos.get("primo.resultado.si", Formato.entero(n));
                    }
                    if (p.compuesto()) {
                        long divisor = p.menorDivisorPropio();
                        return Textos.get(
                                "primo.resultado.compuesto",
                                Formato.entero(n),
                                Formato.entero(divisor),
                                Formato.entero(n / divisor));
                    }
                    return Textos.get("primo.resultado.no", Formato.entero(n));
                });
    }

    private void pantallaBase() {
        formularios.mostrar(
                Textos.get("base.titulo"),
                Textos.get("base.instrucciones"),
                List.of(Textos.get("base.campo.numero")),
                null, // sin filtro: el número puede llevar prefijo y dígitos hexadecimales
                valores -> {
                    Calculadora.ConversionBase c = Calculadora.convertirBase(valores.get(0));
                    return Textos.get("base.resultado", c.binario(), c.octal(), c.decimal(), c.hexadecimal());
                });
    }

    private void pantallaPorcentaje() {
        formularios.mostrar(
                Textos.get("porcentaje.titulo"),
                Textos.get("porcentaje.instrucciones"),
                List.of(Textos.get("porcentaje.campo.porcentaje"), Textos.get("porcentaje.campo.cantidad")),
                FiltroNumerico.Tipo.DECIMAL,
                valores -> {
                    double porcentaje = Entrada.doble(valores.get(0));
                    double cantidad = Entrada.doble(valores.get(1));
                    return Textos.get(
                            "porcentaje.resultado",
                            Formato.numero(porcentaje),
                            Formato.numero(cantidad),
                            Formato.numero(Calculadora.porcentajeDe(porcentaje, cantidad)));
                },
                valores ->
                        PasoAPasoPorcentaje.desarrollo(Entrada.doble(valores.get(0)), Entrada.doble(valores.get(1))));
    }

    private void pantallaReglaDeTres() {
        formularios.mostrar(
                Textos.get("regladetres.titulo"),
                Textos.get("regladetres.instrucciones"),
                List.of(
                        Textos.get("regladetres.campo.a"),
                        Textos.get("regladetres.campo.b"),
                        Textos.get("regladetres.campo.c")),
                FiltroNumerico.Tipo.DECIMAL,
                valores -> {
                    double a = Entrada.doble(valores.get(0));
                    double b = Entrada.doble(valores.get(1));
                    double c = Entrada.doble(valores.get(2));
                    return Textos.get(
                            "regladetres.resultado",
                            Formato.numero(a),
                            Formato.numero(b),
                            Formato.numero(c),
                            Formato.numero(Calculadora.reglaDeTres(a, b, c)));
                },
                valores -> PasoAPasoReglaDeTres.desarrollo(
                        Entrada.doble(valores.get(0)), Entrada.doble(valores.get(1)), Entrada.doble(valores.get(2))));
    }

    private void pantallaImc() {
        // Representación común entre los dos modos: {peso en kg, altura en m}.
        ConstructorDeFormularios.Modo metrico = new ConstructorDeFormularios.Modo(
                Textos.get("imc.sistema.metrico"),
                List.of(Textos.get("imc.campo.peso.kg"), Textos.get("imc.campo.altura.cm")),
                valores -> resultadoImc(
                        Entrada.doble(valores.get(0)), Conversiones.centimetrosAMetros(Entrada.doble(valores.get(1)))),
                valores -> comun(valores, v -> new double[] {v[0], Conversiones.centimetrosAMetros(v[1])}),
                comun -> List.of(redondeado(comun[0], 1), redondeado(Conversiones.metrosACentimetros(comun[1]), 0)));

        ConstructorDeFormularios.Modo imperial = new ConstructorDeFormularios.Modo(
                Textos.get("imc.sistema.imperial"),
                List.of(
                        Textos.get("imc.campo.peso.lb"),
                        Textos.get("imc.campo.altura.pies"),
                        Textos.get("imc.campo.altura.pulgadas")),
                valores -> resultadoImc(
                        Conversiones.librasAKilos(Entrada.doble(valores.get(0))),
                        Conversiones.piesYPulgadasAMetros(
                                Entrada.doble(valores.get(1)), Entrada.doble(valores.get(2)))),
                valores -> comun(valores, v ->
                        new double[] {Conversiones.librasAKilos(v[0]), Conversiones.piesYPulgadasAMetros(v[1], v[2])}),
                comun -> {
                    double[] piesYPulgadas = Conversiones.metrosAPiesYPulgadas(comun[1]);
                    return List.of(
                            redondeado(Conversiones.kilosALibras(comun[0]), 1),
                            Formato.numero(piesYPulgadas[0]),
                            redondeado(piesYPulgadas[1], 1));
                });

        formularios.mostrarConModos(
                Textos.get("imc.titulo"),
                Textos.get("imc.instrucciones"),
                Textos.get("imc.sistema"),
                List.of(metrico, imperial),
                FiltroNumerico.Tipo.DECIMAL);
    }

    private String resultadoImc(double pesoKg, double alturaM) {
        Calculadora.IndiceMasaCorporal r = Calculadora.imc(pesoKg, alturaM);
        return Textos.get(
                "imc.resultado",
                Formato.dosDecimales(r.valor()),
                Textos.get("imc.categoria." + r.categoria().name().toLowerCase()));
    }

    /** Parsea los campos y aplica {@code aComun}; vacío si algún campo falta o no es válido. */
    private static Optional<double[]> comun(List<String> valores, UnaryOperator<double[]> aComun) {
        try {
            double[] crudos = new double[valores.size()];
            for (int i = 0; i < valores.size(); i++) {
                crudos[i] = Entrada.doble(valores.get(i));
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
        return Formato.numero(Math.round(valor * factor) / factor);
    }

    public static void main(String[] args) {
        launch(args);
    }
}
