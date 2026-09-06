package io.guillermoamadodiaz.javacalcfx;

import java.math.BigInteger;
import java.util.List;

import io.guillermoamadodiaz.javacalcfx.calc.Calculadora;
import io.guillermoamadodiaz.javacalcfx.calc.Calculadora.Triangulo;
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
 * muestra; la aritmética vive en {@link Calculadora}.
 */
public class SelectorDeOpciones extends Application {

    private static final int COLUMNAS_MENU = 3;
    private static final double ANCHO_BOTON_MENU = 210;

    /** Recordatorio de que los decimales se escriben con punto. */
    private static final String NOTA_DECIMALES =
            "\nLos decimales se escriben con punto (.), por ejemplo 7.5";

    private final StackPane raiz = new StackPane();
    private final CalculosAsync calculos = new CalculosAsync();
    private final Navegador navegador = new Navegador(raiz, calculos::cancelar);
    private final ConstructorDeFormularios formularios =
            new ConstructorDeFormularios(navegador, calculos, this::mostrarMenu);

    @Override
    public void start(Stage escenario) {
        escenario.setTitle("Calculadora Matemática");
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
        Label titulo = new Label("Calculadora Matemática");
        titulo.getStyleClass().add("titulo");

        GridPane botonera = new GridPane();
        botonera.setHgap(10);
        botonera.setVgap(10);
        botonera.setAlignment(Pos.CENTER);

        Button[] botones = {
            Botones.crear("Calcular Teorema de Pitágoras", this::pantallaPitagoras),
            Botones.crear("Calcular Área de Cilindro", this::pantallaCilindro),
            Botones.crear("Determinar Año Bisiesto", this::pantallaBisiesto),
            Botones.crear("Calcular Factorial", this::pantallaFactorial),
            Botones.crear("Determinar Múltiplo", this::pantallaMultiplo),
            Botones.crear("Determinar Aprobado", this::pantallaAprobado),
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
                "Teorema de Pitágoras",
                "Ingresa las longitudes de los catetos para calcular la hipotenusa:",
                List.of("Longitud del cateto a", "Longitud del cateto b"),
                FiltroNumerico.Tipo.DECIMAL,
                valores -> {
                    Triangulo t = Calculadora.resolverTrianguloRectangulo(
                            Entrada.doble(valores.get(0)), Entrada.doble(valores.get(1)));
                    return "Hipotenusa: " + Formato.numero(t.hipotenusa()) + "\n"
                            + "Área: " + Formato.numero(t.area()) + "\n"
                            + "Perímetro: " + Formato.numero(t.perimetro()) + "\n"
                            + "α ≅ " + Formato.numero(t.anguloAlfa()) + "°\n"
                            + "β ≅ " + Formato.numero(t.anguloBeta()) + "°";
                });
    }

    private void pantallaCilindro() {
        formularios.mostrar(
                "Área de Cilindro",
                "Ingresa el radio y la altura del cilindro:",
                List.of("Radio", "Altura"),
                FiltroNumerico.Tipo.DECIMAL,
                valores -> "Área del cilindro: " + Formato.numero(Calculadora.areaCilindro(
                        Entrada.doble(valores.get(0)), Entrada.doble(valores.get(1)))));
    }

    private void pantallaBisiesto() {
        formularios.mostrar(
                "Año Bisiesto",
                "Ingresa un año para determinar si es bisiesto:",
                List.of("Año"),
                FiltroNumerico.Tipo.ENTERO,
                valores -> {
                    int anio = Entrada.entero(valores.get(0));
                    return "El año " + anio + (Calculadora.esBisiesto(anio)
                            ? " es bisiesto." : " no es bisiesto.");
                });
    }

    private void pantallaFactorial() {
        formularios.mostrar(
                "Factorial",
                "Ingresa un número para calcular su factorial:",
                List.of("Número"),
                FiltroNumerico.Tipo.ENTERO,
                valores -> {
                    int n = Entrada.entero(valores.get(0));
                    BigInteger factorial = Calculadora.factorial(n);
                    return "El factorial de " + n + " es " + Formato.enteroGrande(factorial);
                });
    }

    private void pantallaMultiplo() {
        formularios.mostrar(
                "Múltiplo",
                "Ingresa dos números para determinar si el primero es múltiplo del segundo:",
                List.of("Primer número", "Segundo número"),
                FiltroNumerico.Tipo.ENTERO,
                valores -> {
                    long a = Entrada.largo(valores.get(0));
                    long b = Entrada.largo(valores.get(1));
                    return a + (Calculadora.esMultiplo(a, b) ? " es " : " no es ") + "múltiplo de " + b + ".";
                });
    }

    private void pantallaAprobado() {
        formularios.mostrar(
                "Aprobado",
                "Ingresa las 5 notas del alumno (entre 0 y 10):" + NOTA_DECIMALES,
                List.of("Nota 1", "Nota 2", "Nota 3", "Nota 4", "Nota 5"),
                FiltroNumerico.Tipo.DECIMAL,
                valores -> {
                    double[] notas = valores.stream().mapToDouble(Entrada::doble).toArray();
                    double media = Calculadora.media(notas);
                    return (Calculadora.estaAprobado(media) ? "Aprobado" : "Suspendido")
                            + " con una nota media de " + Formato.dosDecimales(media) + ".";
                });
    }

    public static void main(String[] args) {
        launch(args);
    }
}
