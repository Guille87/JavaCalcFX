package io.guillermoamadodiaz.javacalcfx;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;

import io.guillermoamadodiaz.javacalcfx.calc.Calculadora;
import io.guillermoamadodiaz.javacalcfx.calc.Calculadora.Triangulo;
import io.guillermoamadodiaz.javacalcfx.i18n.Textos;
import io.guillermoamadodiaz.javacalcfx.ui.Botones;
import io.guillermoamadodiaz.javacalcfx.ui.CalculosAsync;
import io.guillermoamadodiaz.javacalcfx.ui.ConstructorDeFormularios;
import io.guillermoamadodiaz.javacalcfx.ui.Entrada;
import io.guillermoamadodiaz.javacalcfx.ui.FiltroNumerico;
import io.guillermoamadodiaz.javacalcfx.ui.Formato;
import io.guillermoamadodiaz.javacalcfx.ui.Navegador;
import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.GridPane;
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

    @Override
    public void start(Stage escenario) {
        escenario.setTitle(Textos.get("app.titulo"));
        raiz.setPadding(new Insets(20));

        Scene escena = new Scene(raiz, 680, 480);
        var hojaEstilos = getClass().getResource("styles.css");
        if (hojaEstilos != null) {
            escena.getStylesheets().add(hojaEstilos.toExternalForm());
        }

        mostrarMenu();
        escenario.setScene(escena);
        escenario.show();
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
            Botones.crear(Textos.get("menu.boton.pitagoras"), this::pantallaPitagoras),
            Botones.crear(Textos.get("menu.boton.cilindro"), this::pantallaCilindro),
            Botones.crear(Textos.get("menu.boton.bisiesto"), this::pantallaBisiesto),
            Botones.crear(Textos.get("menu.boton.factorial"), this::pantallaFactorial),
            Botones.crear(Textos.get("menu.boton.multiplo"), this::pantallaMultiplo),
            Botones.crear(Textos.get("menu.boton.aprobado"), this::pantallaAprobado),
        };
        for (int i = 0; i < botones.length; i++) {
            botones[i].setPrefWidth(ANCHO_BOTON_MENU);
            botones[i].setWrapText(true);
            botonera.add(botones[i], i % COLUMNAS_MENU, i / COLUMNAS_MENU);
        }

        VBox pantalla = new VBox(20, titulo, botonera);
        pantalla.setAlignment(Pos.CENTER);
        navegador.mostrar(pantalla);
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
                    return Textos.get("pitagoras.resultado",
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
                valores -> Textos.get("cilindro.resultado", Formato.numero(Calculadora.areaCilindro(
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
                    String clave = Calculadora.esBisiesto(anio)
                            ? "bisiesto.resultado.si" : "bisiesto.resultado.no";
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
                    return Textos.get("factorial.resultado",
                            String.valueOf(n), Formato.enteroGrande(factorial));
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
                    String clave = Calculadora.esMultiplo(a, b)
                            ? "multiplo.resultado.si" : "multiplo.resultado.no";
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
                Textos.get("aprobado.instrucciones",
                        String.valueOf((int) Calculadora.NOTA_MINIMA),
                        String.valueOf((int) Calculadora.NOTA_MAXIMA)),
                prompts,
                FiltroNumerico.Tipo.DECIMAL,
                valores -> {
                    double[] notas = valores.stream().mapToDouble(Entrada::doble).toArray();
                    double media = Calculadora.media(notas);
                    String clave = Calculadora.estaAprobado(media)
                            ? "aprobado.resultado.aprobado" : "aprobado.resultado.suspendido";
                    return Textos.get(clave, Formato.dosDecimales(media));
                });
    }

    public static void main(String[] args) {
        launch(args);
    }
}
