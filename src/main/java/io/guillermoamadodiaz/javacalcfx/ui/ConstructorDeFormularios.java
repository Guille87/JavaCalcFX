package io.guillermoamadodiaz.javacalcfx.ui;

import io.guillermoamadodiaz.javacalcfx.i18n.Textos;
import java.util.ArrayList;
import java.util.List;
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
 * {@link #mostrarConModos} es una variante con un selector que cambia los campos
 * y el cálculo (p. ej. sistema de medida). Tras un cálculo correcto siempre
 * aparece «Copiar» y la operación queda registrada en el {@link Historial}.
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
            FiltroNumerico.Tipo tipoCampo, // {@code null} = sin filtro (p. ej. permitir dígitos hex)
            Function<List<String>, String> calculo,
            Function<List<String>, String> pasos) {
        VBox pantalla = new VBox(ESPACIADO);
        pantalla.setPadding(RELLENO);
        pantalla.setAlignment(Pos.TOP_CENTER);
        pantalla.setMaxWidth(ANCHO_MAXIMO);
        escVuelveAlMenu(pantalla);

        Label encabezado = new Label(titulo);
        encabezado.getStyleClass().add("encabezado");

        Label instruccion = new Label(instrucciones);
        instruccion.setWrapText(true);
        instruccion.setMaxWidth(Double.MAX_VALUE);

        List<TextField> campos = new ArrayList<>(prompts.size());
        for (String prompt : prompts) {
            TextField campo = new TextField();
            campo.setPromptText(prompt);
            if (tipoCampo != null) {
                FiltroNumerico.aplicarA(campo, tipoCampo);
            }
            campos.add(campo);
        }

        Label resultado = new Label();
        resultado.setWrapText(true);
        resultado.setMaxWidth(Double.MAX_VALUE);
        resultado.getStyleClass().add("resultado");

        SeccionPasos seccion = pasos == null ? null : new SeccionPasos();
        BotonCopiar copiar = new BotonCopiar(resultado);

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
            copiar.ocultar();
            calculos.ejecutar(
                    () -> calculo.apply(valores),
                    () -> {
                        calcular.setDisable(true);
                        resultado.setText(Textos.get("form.calculando"));
                    },
                    texto -> {
                        calcular.setDisable(false);
                        resultado.setText(texto);
                        if (!texto.isBlank()) {
                            Historial.registrar(titulo, texto);
                            copiar.mostrar();
                            if (seccion != null) {
                                seccion.preparar(() -> pasos.apply(valores));
                            }
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
        pantalla.getChildren().addAll(calcular, resultado, copiar.boton);
        if (seccion != null) {
            pantalla.getChildren().addAll(seccion.boton, seccion.detalle);
        }
        pantalla.getChildren().add(volver);

        mostrarEnScroll(pantalla);
        campos.get(0).requestFocus(); // el cursor ya está en el primer campo
    }

    /**
     * Registro de un «modo» de un formulario con selector: su nombre visible, los
     * campos que pide y el cálculo que hace con esos campos.
     */
    public record Modo(String nombre, List<String> prompts, Function<List<String>, String> calculo) {}

    /**
     * Variante del formulario con un {@link ComboBox} de modos (p. ej. sistema de
     * medida): al cambiar de modo se reconstruyen los campos y se usa su cálculo.
     * No admite «paso a paso». El primer modo de la lista es el inicial.
     */
    public void mostrarConModos(
            String titulo,
            String instrucciones,
            String etiquetaModos,
            List<Modo> modos,
            FiltroNumerico.Tipo tipoCampo) {
        VBox pantalla = new VBox(ESPACIADO);
        pantalla.setPadding(RELLENO);
        pantalla.setAlignment(Pos.TOP_CENTER);
        pantalla.setMaxWidth(ANCHO_MAXIMO);
        escVuelveAlMenu(pantalla);

        Label encabezado = new Label(titulo);
        encabezado.getStyleClass().add("encabezado");

        Label instruccion = new Label(instrucciones);
        instruccion.setWrapText(true);
        instruccion.setMaxWidth(Double.MAX_VALUE);

        ComboBox<Modo> selector = new ComboBox<>();
        selector.getItems().setAll(modos);
        selector.setMaxWidth(Double.MAX_VALUE);
        selector.setConverter(new StringConverter<>() {
            @Override
            public String toString(Modo modo) {
                return modo == null ? "" : modo.nombre();
            }

            @Override
            public Modo fromString(String texto) {
                return null;
            }
        });

        Label resultado = new Label();
        resultado.setWrapText(true);
        resultado.setMaxWidth(Double.MAX_VALUE);
        resultado.getStyleClass().add("resultado");
        BotonCopiar copiar = new BotonCopiar(resultado);

        List<TextField> campos = new ArrayList<>();
        VBox camposBox = new VBox(ESPACIADO);
        AtomicReference<Function<List<String>, String>> calculoActual = new AtomicReference<>();

        Runnable aplicarModo = () -> {
            Modo modo = selector.getValue();
            campos.clear();
            camposBox.getChildren().clear();
            for (String prompt : modo.prompts()) {
                TextField campo = new TextField();
                campo.setPromptText(prompt);
                if (tipoCampo != null) {
                    FiltroNumerico.aplicarA(campo, tipoCampo);
                }
                campos.add(campo);
                camposBox.getChildren().add(campo);
            }
            calculoActual.set(modo.calculo());
            resultado.setText("");
            copiar.ocultar();
            campos.get(0).requestFocus();
        };
        selector.getSelectionModel().selectFirst();
        aplicarModo.run();
        selector.setOnAction(e -> aplicarModo.run());

        Button calcular = new Button(Textos.get("form.calcular"));
        calcular.setDefaultButton(true);
        calcular.setTooltip(new Tooltip(Textos.get("form.calcular.tooltip")));
        calcular.setOnAction(e -> {
            List<String> valores = campos.stream()
                    .map(c -> c.getText() == null ? "" : c.getText().trim())
                    .toList();
            Function<List<String>, String> calculo = calculoActual.get();
            copiar.ocultar();
            calculos.ejecutar(
                    () -> calculo.apply(valores),
                    () -> {
                        calcular.setDisable(true);
                        resultado.setText(Textos.get("form.calculando"));
                    },
                    texto -> {
                        calcular.setDisable(false);
                        resultado.setText(texto);
                        if (!texto.isBlank()) {
                            Historial.registrar(titulo, texto);
                            copiar.mostrar();
                        }
                    },
                    mensaje -> {
                        calcular.setDisable(false);
                        resultado.setText(mensaje);
                    });
        });

        Button volver = Botones.crear(Textos.get("form.volver"), Textos.get("form.volver.tooltip"), volverAlMenu);
        volver.setCancelButton(true);

        pantalla.getChildren()
                .addAll(
                        encabezado,
                        instruccion,
                        new VBox(4, new Label(etiquetaModos), selector),
                        camposBox,
                        calcular,
                        resultado,
                        copiar.boton,
                        volver);

        mostrarEnScroll(pantalla);
        campos.get(0).requestFocus();
    }

    /** Esc vuelve al menú aunque el foco esté en un {@link TextField} (que consumiría la tecla). */
    private void escVuelveAlMenu(VBox pantalla) {
        pantalla.addEventFilter(KeyEvent.KEY_PRESSED, evento -> {
            if (evento.getCode() == KeyCode.ESCAPE) {
                volverAlMenu.run();
                evento.consume();
            }
        });
    }

    /** Envuelve el formulario en el {@link ScrollPane} transparente y lo muestra. */
    private void mostrarEnScroll(VBox pantalla) {
        StackPane centrador = new StackPane(pantalla); // mantiene el formulario centrado
        ScrollPane scroll = new ScrollPane(centrador);
        scroll.setFitToWidth(true);
        scroll.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        scroll.getStyleClass().add("formulario");
        navegador.mostrar(scroll);
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

    /** Botón «Copiar»: copia el texto del resultado al portapapeles del sistema. */
    private static final class BotonCopiar {

        private final Button boton;

        BotonCopiar(Label resultado) {
            boton = Botones.crear(
                    Textos.get("form.copiar"), Textos.get("form.copiar.tooltip"), () -> copiar(resultado));
            ocultar();
        }

        void ocultar() {
            boton.setVisible(false);
            boton.setManaged(false);
        }

        void mostrar() {
            boton.setVisible(true);
            boton.setManaged(true);
            boton.setText(Textos.get("form.copiar"));
        }

        private void copiar(Label resultado) {
            ClipboardContent contenido = new ClipboardContent();
            contenido.putString(resultado.getText() == null ? "" : resultado.getText());
            Clipboard.getSystemClipboard().setContent(contenido);

            boton.setText(Textos.get("form.copiar.hecho"));
            PauseTransition volver = new PauseTransition(Duration.seconds(1.5));
            volver.setOnFinished(e -> boton.setText(Textos.get("form.copiar")));
            volver.play();
        }
    }
}
