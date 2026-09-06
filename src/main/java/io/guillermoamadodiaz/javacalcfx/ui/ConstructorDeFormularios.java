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
 * en la misma etiqueta. Si la pantalla aporta una función {@code pasos}, tras un
 * cálculo correcto aparece un botón «Mostrar pasos» que despliega su desarrollo.
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
        mostrar(titulo, instrucciones, prompts, tipoCampo, calculo, null);
    }

    public void mostrar(
            String titulo,
            String instrucciones,
            List<String> prompts,
            FiltroNumerico.Tipo tipoCampo,
            Function<List<String>, String> calculo,
            Function<List<String>, String> pasos) {
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

        SeccionPasos seccion = pasos == null ? null : new SeccionPasos();

        Button calcular = new Button(Textos.get("form.calcular"));
        calcular.setDefaultButton(true); // permite pulsar Enter
        calcular.setTooltip(new Tooltip(Textos.get("form.calcular.tooltip")));
        calcular.setOnAction(e -> {
            List<String> valores = campos.stream()
                    .map(c -> c.getText() == null ? "" : c.getText().trim())
                    .toList();
            if (seccion != null) {
                seccion.ocultar();
            }
            calculos.ejecutar(
                    () -> calculo.apply(valores),
                    () -> {
                        calcular.setDisable(true);
                        resultado.setText(Textos.get("form.calculando"));
                    },
                    texto -> {
                        calcular.setDisable(false);
                        resultado.setText(texto);
                        if (seccion != null && !texto.isBlank()) {
                            seccion.preparar(() -> pasos.apply(valores));
                        }
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
        pantalla.getChildren().addAll(calcular, resultado);
        if (seccion != null) {
            pantalla.getChildren().addAll(seccion.boton, seccion.detalle);
        }
        pantalla.getChildren().add(volver);

        StackPane centrador = new StackPane(pantalla); // mantiene el formulario centrado
        ScrollPane scroll = new ScrollPane(centrador);
        scroll.setFitToWidth(true);
        scroll.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        scroll.getStyleClass().add("formulario");
        navegador.mostrar(scroll);

        campos.get(0).requestFocus(); // el cursor ya está en el primer campo
    }

    /** Botón «Mostrar / Ocultar pasos» y la etiqueta con el desarrollo. */
    private static final class SeccionPasos {

        private final Button boton = new Button();
        private final Label detalle = new Label();

        SeccionPasos() {
            detalle.setWrapText(true);
            detalle.setMaxWidth(Double.MAX_VALUE);
            detalle.getStyleClass().add("pasos");
            boton.setOnAction(e -> mostrarDetalle(!detalle.isVisible()));
            ocultar();
        }

        void ocultar() {
            boton.setVisible(false);
            boton.setManaged(false);
            mostrarDetalle(false);
        }

        /** Calcula el texto y muestra el botón (aún plegado). */
        void preparar(java.util.function.Supplier<String> desarrollo) {
            try {
                detalle.setText(desarrollo.get());
            } catch (RuntimeException ignorado) {
                return; // si el desarrollo falla, simplemente no ofrecemos los pasos
            }
            boton.setVisible(true);
            boton.setManaged(true);
            mostrarDetalle(false);
        }

        private void mostrarDetalle(boolean visible) {
            detalle.setVisible(visible);
            detalle.setManaged(visible);
            boton.setText(Textos.get(visible ? "form.pasos.ocultar" : "form.pasos.mostrar"));
        }
    }
}
