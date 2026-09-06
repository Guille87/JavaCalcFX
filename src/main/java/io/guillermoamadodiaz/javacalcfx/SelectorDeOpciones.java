package io.guillermoamadodiaz.javacalcfx;

import io.guillermoamadodiaz.javacalcfx.calc.Calculadora;
import io.guillermoamadodiaz.javacalcfx.calc.Calculadora.EcuacionCuadratica;
import io.guillermoamadodiaz.javacalcfx.calc.Calculadora.Raiz;
import io.guillermoamadodiaz.javacalcfx.calc.Calculadora.Triangulo;
import io.guillermoamadodiaz.javacalcfx.i18n.Idioma;
import io.guillermoamadodiaz.javacalcfx.i18n.Textos;
import io.guillermoamadodiaz.javacalcfx.ui.Botones;
import io.guillermoamadodiaz.javacalcfx.ui.CalculosAsync;
import io.guillermoamadodiaz.javacalcfx.ui.ConstructorDeFormularios;
import io.guillermoamadodiaz.javacalcfx.ui.Entrada;
import io.guillermoamadodiaz.javacalcfx.ui.EstadoVentana;
import io.guillermoamadodiaz.javacalcfx.ui.FiltroNumerico;
import io.guillermoamadodiaz.javacalcfx.ui.Formato;
import io.guillermoamadodiaz.javacalcfx.ui.Navegador;
import io.guillermoamadodiaz.javacalcfx.ui.PasoAPasoCuadratica;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;
import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.Tooltip;
import javafx.scene.image.Image;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
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

    private static final int COLUMNAS_MENU = 3;
    private static final double ANCHO_BOTON_MENU = 210;
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

        Scene escena = new Scene(raiz, 680, 480);
        var hojaEstilos = getClass().getResource("styles.css");
        if (hojaEstilos != null) {
            escena.getStylesheets().add(hojaEstilos.toExternalForm());
        }

        mostrarMenu();
        escenario.setScene(escena);
        escenario.setMinWidth(655);
        escenario.setMinHeight(490);
        EstadoVentana.restaurar(escenario);
        escenario.show();
        EstadoVentana.vigilar(escenario);
    }

    @Override
    public void stop() {
        calculos.cerrar();
    }

    // ------------------------------------------------------------------
    // Menú
    // ------------------------------------------------------------------

    private void mostrarMenu() {
        Label titulo = new Label(Textos.get("menu.titulo"));
        titulo.getStyleClass().add("titulo");

        GridPane botonera = new GridPane();
        botonera.setHgap(10);
        botonera.setVgap(10);
        botonera.setAlignment(Pos.CENTER);

        Button[] botones = {
            botonMenu("pitagoras", this::pantallaPitagoras),
            botonMenu("cilindro", this::pantallaCilindro),
            botonMenu("bisiesto", this::pantallaBisiesto),
            botonMenu("factorial", this::pantallaFactorial),
            botonMenu("multiplo", this::pantallaMultiplo),
            botonMenu("aprobado", this::pantallaAprobado),
            botonMenu("cuadratica", this::pantallaCuadratica),
            botonMenu("potencia", this::pantallaPotencia),
            botonMenu("raiz", this::pantallaRaiz),
        };
        for (int i = 0; i < botones.length; i++) {
            botones[i].setPrefWidth(ANCHO_BOTON_MENU);
            botones[i].setWrapText(true);
            botonera.add(botones[i], i % COLUMNAS_MENU, i / COLUMNAS_MENU);
        }

        VBox centro = new VBox(20, titulo, botonera);
        centro.setAlignment(Pos.CENTER);

        BorderPane menu = new BorderPane(centro);
        menu.setTop(barraDeIdioma());
        navegador.mostrar(menu);
    }

    /** Botón del menú a partir de su clave: usa {@code menu.boton.<clave>[.tooltip]}. */
    private Button botonMenu(String clave, Runnable accion) {
        return Botones.crear(Textos.get("menu.boton." + clave), Textos.get("menu.boton." + clave + ".tooltip"), accion);
    }

    /** Selector de idioma alineado arriba a la derecha del menú. */
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

        HBox barra = new HBox(selector);
        barra.setAlignment(Pos.CENTER_RIGHT);
        return barra;
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
                });
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
                                Entrada.doble(valores.get(0)), Entrada.doble(valores.get(1))))));
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

    public static void main(String[] args) {
        launch(args);
    }
}
