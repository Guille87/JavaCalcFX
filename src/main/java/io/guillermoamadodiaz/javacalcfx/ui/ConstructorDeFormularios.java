package io.guillermoamadodiaz.javacalcfx.ui;

import io.guillermoamadodiaz.javacalcfx.i18n.Textos;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.control.Tooltip;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;

/**
 * Construye y muestra la pantalla de formulario genérica que comparten todas las
 * calculadoras: encabezado, instrucciones, un campo por cada dato pedido, botón
 * «Calcular» (activable con Enter), etiqueta de resultado y botón «Volver»
 * (activable con Esc desde cualquier punto de la pantalla). Va dentro de un
 * {@link ScrollPane} para que, si la ventana se hace muy pequeña, se pueda
 * desplazar en vez de recortar contenido.
 *
 * <p>El cálculo se delega en {@link CalculosAsync}; el parseo y la validación de
 * dominio están en la función que recibe cada pantalla, y sus errores se muestran
 * en la misma etiqueta.
 */
public final class ConstructorDeFormularios {

    private static final Insets RELLENO = new Insets(20);
    private static final double ESPACIADO = 10;
    private static final double ANCHO_MAXIMO = 440;

    private final Navegador navegador;
    private final CalculosAsync calculos;
    private final Runnable volverAlMenu;

    public ConstructorDeFormularios(Navegador navegador, CalculosAsync calculos, Runnable volverAlMenu) {
        this.navegador = navegador;
        this.calculos = calculos;
        this.volverAlMenu = volverAlMenu;
    }

    public void mostrar(
            String titulo,
            String instrucciones,
            List<String> prompts,
            FiltroNumerico.Tipo tipoCampo,
            Function<List<String>, String> calculo) {
        VBox pantalla = new VBox(ESPACIADO);
        pantalla.setPadding(RELLENO);
        pantalla.setAlignment(Pos.TOP_CENTER);
        pantalla.setMaxWidth(ANCHO_MAXIMO);
        // Esc vuelve al menú aunque el foco esté en un TextField (que consumiría la tecla).
        pantalla.addEventFilter(KeyEvent.KEY_PRESSED, evento -> {
            if (evento.getCode() == KeyCode.ESCAPE) {
                volverAlMenu.run();
                evento.consume();
            }
        });

        Label encabezado = new Label(titulo);
        encabezado.getStyleClass().add("encabezado");

        Label instruccion = new Label(instrucciones);
        instruccion.setWrapText(true);
        instruccion.setMaxWidth(Double.MAX_VALUE);

        List<TextField> campos = new ArrayList<>(prompts.size());
        for (String prompt : prompts) {
            TextField campo = new TextField();
            campo.setPromptText(prompt);
            FiltroNumerico.aplicarA(campo, tipoCampo);
            campos.add(campo);
        }

        Label resultado = new Label();
        resultado.setWrapText(true);
        resultado.setMaxWidth(Double.MAX_VALUE);
        resultado.getStyleClass().add("resultado");

        Button calcular = new Button(Textos.get("form.calcular"));
        calcular.setDefaultButton(true); // permite pulsar Enter
        calcular.setTooltip(new Tooltip(Textos.get("form.calcular.tooltip")));
        calcular.setOnAction(e -> {
            List<String> valores = campos.stream()
                    .map(c -> c.getText() == null ? "" : c.getText().trim())
                    .toList();
            calculos.ejecutar(
                    () -> calculo.apply(valores),
                    () -> {
                        calcular.setDisable(true);
                        resultado.setText(Textos.get("form.calculando"));
                    },
                    texto -> {
                        calcular.setDisable(false);
                        resultado.setText(texto);
                    },
                    mensaje -> {
                        calcular.setDisable(false);
                        resultado.setText(mensaje);
                    });
        });

        Button volver = Botones.crear(Textos.get("form.volver"), Textos.get("form.volver.tooltip"), volverAlMenu);
        volver.setCancelButton(true); // permite pulsar Esc

        pantalla.getChildren().addAll(encabezado, instruccion);
        pantalla.getChildren().addAll(campos);
        pantalla.getChildren().addAll(calcular, resultado, volver);

        StackPane centrador = new StackPane(pantalla); // mantiene el formulario centrado
        ScrollPane scroll = new ScrollPane(centrador);
        scroll.setFitToWidth(true);
        scroll.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        scroll.getStyleClass().add("formulario");
        navegador.mostrar(scroll);
    }
}
