package io.guillermoamadodiaz.javacalcfx;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.testfx.api.FxAssert.verifyThat;
import static org.testfx.matcher.control.LabeledMatchers.hasText;

import io.guillermoamadodiaz.javacalcfx.i18n.Messages;
import io.guillermoamadodiaz.javacalcfx.ui.EstadoVentana;
import io.guillermoamadodiaz.javacalcfx.ui.Historial;
import io.guillermoamadodiaz.javacalcfx.ui.Tema;
import io.guillermoamadodiaz.javacalcfx.ui.UltimaCalculadora;
import java.util.Locale;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.prefs.Preferences;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.input.Clipboard;
import javafx.scene.input.KeyCode;
import javafx.stage.Stage;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.testfx.framework.junit5.ApplicationTest;
import org.testfx.util.WaitForAsyncUtils;

/**
 * Tests de interfaz con TestFX (sin pantalla, vía Monocle). Cubren la navegación
 * menú ↔ formulario y que los resultados y errores aparecen en la etiqueta.
 */
class InterfazTest extends ApplicationTest {

    @BeforeAll
    static void enEspanol() {
        Messages.useLocale(Locale.forLanguageTag("es"));
    }

    @BeforeEach
    void historialLimpio() {
        Historial.limpiar();
    }

    @AfterAll
    static void limpiarPreferencias() {
        Historial.limpiar();
        UltimaCalculadora.olvidar();
        try {
            Preferences.userNodeForPackage(EstadoVentana.class).clear();
            Preferences.userNodeForPackage(Tema.class).node("tema").clear();
            Preferences.userNodeForPackage(Historial.class).node("historial").clear();
        } catch (Exception ignorado) {
            // sin persistencia disponible en el entorno de test: nada que limpiar
        }
    }

    @Override
    public void start(Stage escenario) {
        UltimaCalculadora.olvidar(); // cada test arranca en el menú
        new SelectorDeOpciones().start(escenario);
        // El menú por categorías es alto; damos una ventana grande para que TestFX
        // pueda ver y pulsar cualquier botón sin depender del scroll.
        escenario.setWidth(900);
        escenario.setHeight(1000);
    }

    private String textoResultado() {
        return lookup(".resultado").queryAs(Label.class).getText();
    }

    @Test
    void el_menu_muestra_el_titulo_y_las_calculadoras() {
        verifyThat(".titulo", hasText("Calculadora Matemática"));
        assertTrue(lookup("Calcular Teorema de Pitágoras").tryQuery().isPresent());
        assertTrue(lookup("Determinar Aprobado").tryQuery().isPresent());
    }

    @Test
    void el_menu_agrupa_las_calculadoras_por_categorias() {
        assertTrue(lookup(".categoria").queryAll().size() >= 3);
        assertTrue(lookup("Geometría").tryQuery().isPresent());
        assertTrue(lookup("Aritmética").tryQuery().isPresent());
    }

    @Test
    void el_foco_empieza_en_el_primer_campo() {
        clickOn("Calcular Área de Cilindro");
        WaitForAsyncUtils.waitForFxEvents();
        TextField primero = lookup(".text-field").nth(0).queryAs(TextField.class);
        assertTrue(primero.isFocused(), "el primer campo debe tener el foco al abrir el formulario");
    }

    @Test
    void abrir_una_calculadora_y_volver_con_el_boton() {
        clickOn("Calcular Área de Cilindro");
        verifyThat(".encabezado", hasText("Área de Cilindro"));

        clickOn("Volver");
        verifyThat(".titulo", hasText("Calculadora Matemática"));
    }

    @Test
    void la_tecla_esc_vuelve_al_menu_desde_un_campo() {
        clickOn("Calcular Factorial");
        clickOn(".text-field").write("5");
        press(KeyCode.ESCAPE).release(KeyCode.ESCAPE);

        verifyThat(".titulo", hasText("Calculadora Matemática"));
    }

    @Test
    void un_calculo_valido_muestra_el_resultado() throws TimeoutException {
        clickOn("Calcular Teorema de Pitágoras");
        clickOn(lookup(".text-field").nth(0).queryAs(TextField.class)).write("3");
        clickOn(lookup(".text-field").nth(1).queryAs(TextField.class)).write("4");
        clickOn("Calcular");

        WaitForAsyncUtils.waitFor(5, TimeUnit.SECONDS, () -> textoResultado().contains("Hipotenusa"));
        assertTrue(textoResultado().contains("5"), "hipotenusa 3-4-5, era: " + textoResultado());
    }

    @Test
    void el_boton_mostrar_pasos_despliega_el_desarrollo() throws TimeoutException {
        clickOn("Calcular Teorema de Pitágoras");
        clickOn(lookup(".text-field").nth(0).queryAs(TextField.class)).write("3");
        clickOn(lookup(".text-field").nth(1).queryAs(TextField.class)).write("4");
        clickOn("Calcular");
        WaitForAsyncUtils.waitFor(5, TimeUnit.SECONDS, () -> textoResultado().contains("Hipotenusa"));

        clickOn("Mostrar pasos");
        String pasos = lookup(".pasos").queryAs(Label.class).getText();
        assertTrue(pasos.contains("h = √(9 + 16)"), "esperaba el desarrollo de Pitágoras, era: " + pasos);
    }

    @Test
    void el_boton_copiar_pone_el_resultado_en_el_portapapeles() throws TimeoutException {
        clickOn("Calcular Teorema de Pitágoras");
        clickOn(lookup(".text-field").nth(0).queryAs(TextField.class)).write("3");
        clickOn(lookup(".text-field").nth(1).queryAs(TextField.class)).write("4");
        clickOn("Calcular");
        WaitForAsyncUtils.waitFor(5, TimeUnit.SECONDS, () -> textoResultado().contains("Hipotenusa"));

        String esperado = textoResultado();
        clickOn("Copiar");
        WaitForAsyncUtils.waitForFxEvents();

        final String[] enPortapapeles = new String[1];
        interact(() -> enPortapapeles[0] = Clipboard.getSystemClipboard().getString());
        assertEquals(esperado, enPortapapeles[0]);
    }

    @Test
    void el_historial_recoge_los_calculos() throws TimeoutException {
        clickOn("Calcular Teorema de Pitágoras");
        clickOn(lookup(".text-field").nth(0).queryAs(TextField.class)).write("3");
        clickOn(lookup(".text-field").nth(1).queryAs(TextField.class)).write("4");
        clickOn("Calcular");
        WaitForAsyncUtils.waitFor(5, TimeUnit.SECONDS, () -> textoResultado().contains("Hipotenusa"));
        clickOn("Calcular"); // repetir el mismo cálculo no debe duplicar la entrada
        WaitForAsyncUtils.waitForFxEvents();

        clickOn("Volver");
        clickOn("Historial");

        verifyThat(".encabezado", hasText("Historial de cálculos"));
        assertEquals(1, lookup(".historial-titulo").queryAll().size(), "el cálculo repetido no debe duplicarse");
        assertTrue(lookup(".historial-fecha").tryQuery().isPresent(), "cada entrada lleva su fecha");
        String resultado = lookup(".resultado").nth(0).queryAs(Label.class).getText();
        assertTrue(resultado.contains("Hipotenusa: 5"), "era: " + resultado);

        clickOn("Vaciar historial");
        WaitForAsyncUtils.waitForFxEvents();
        assertTrue(Historial.reciente().isEmpty(), "«Vaciar historial» debe borrar los cálculos");
        assertTrue(lookup(".historial-titulo").tryQuery().isEmpty(), "la pantalla debe quedar sin entradas");
    }

    @Test
    void el_imc_cambia_los_campos_segun_el_sistema_de_medida() throws TimeoutException {
        clickOn("Calcular IMC");
        WaitForAsyncUtils.waitForFxEvents();
        assertEquals(2, lookup(".text-field").queryAll().size(), "métrico: peso y altura");

        clickOn(lookup(".text-field").nth(0).queryAs(TextField.class)).write("70");
        clickOn(lookup(".text-field").nth(1).queryAs(TextField.class)).write("175");
        clickOn("Calcular");
        WaitForAsyncUtils.waitFor(5, TimeUnit.SECONDS, () -> textoResultado().contains("IMC"));
        assertTrue(textoResultado().contains("22.86"), "70 kg / 175 cm ≈ 22.86, era: " + textoResultado());

        ComboBox<?> selector = lookup(".combo-box").queryAs(ComboBox.class);
        interact(() -> selector.getSelectionModel().select(1)); // imperial
        WaitForAsyncUtils.waitForFxEvents();
        assertEquals(3, lookup(".text-field").queryAll().size(), "imperial: peso, pies y pulgadas");

        // al cambiar de sistema, los datos se traspasan convertidos
        assertTrue(
                lookup(".text-field").nth(0).queryAs(TextField.class).getText().startsWith("154"), "70 kg ≈ 154 lb");
        assertEquals("5", lookup(".text-field").nth(1).queryAs(TextField.class).getText(), "175 cm ≈ 5 ft");
        assertTrue(
                lookup(".text-field").nth(2).queryAs(TextField.class).getText().startsWith("8.9"),
                "175 cm ≈ 5 ft 8.9 in");

        // volver a métrico: la conversión de ida y vuelta cae en los mismos valores
        interact(() -> selector.getSelectionModel().select(0));
        WaitForAsyncUtils.waitForFxEvents();
        assertEquals("70", lookup(".text-field").nth(0).queryAs(TextField.class).getText());
        assertEquals(
                "175", lookup(".text-field").nth(1).queryAs(TextField.class).getText());
    }

    @Test
    void recuerda_la_ultima_calculadora_y_la_olvida_al_volver() {
        clickOn("Calcular Factorial");
        WaitForAsyncUtils.waitForFxEvents();
        assertEquals("factorial", UltimaCalculadora.recordada().orElse(null));

        clickOn("Volver");
        WaitForAsyncUtils.waitForFxEvents();
        assertTrue(UltimaCalculadora.recordada().isEmpty(), "volver al menú debe olvidar la calculadora");
    }

    @Test
    void el_boton_de_tema_alterna_el_modo_oscuro() {
        boolean oscuroAntes = modoOscuro();
        clickOn(oscuroAntes ? "Modo claro" : "Modo oscuro");
        assertTrue(modoOscuro() != oscuroAntes, "el botón debe alternar el modo oscuro");

        clickOn(oscuroAntes ? "Modo oscuro" : "Modo claro"); // lo dejamos como estaba
        assertTrue(modoOscuro() == oscuroAntes, "el segundo clic debe volver al tema anterior");
    }

    private boolean modoOscuro() {
        return lookup(".titulo").query().getScene().getRoot().getStyleClass().contains(Tema.CLASE_OSCURO);
    }

    @Test
    void la_cuadratica_explica_el_discriminante_y_da_las_raices_complejas() throws TimeoutException {
        clickOn("Resolver Ecuación de 2.º Grado");
        clickOn(lookup(".text-field").nth(0).queryAs(TextField.class)).write("7");
        clickOn(lookup(".text-field").nth(1).queryAs(TextField.class)).write("-3");
        clickOn(lookup(".text-field").nth(2).queryAs(TextField.class)).write("1");
        clickOn("Calcular");

        WaitForAsyncUtils.waitFor(5, TimeUnit.SECONDS, () -> textoResultado().contains("Δ"));
        String r = textoResultado();
        assertTrue(r.contains("-19"), "el discriminante es -19, era: " + r);
        assertTrue(r.contains("no hay soluciones reales"), r);
        assertTrue(r.contains("i"), "raíces complejas, era: " + r);
    }

    @Test
    void un_dato_no_numerico_muestra_un_mensaje_de_error() throws TimeoutException {
        clickOn("Calcular Área de Cilindro");
        clickOn(".text-field").write("abc");
        clickOn("Calcular");

        WaitForAsyncUtils.waitFor(5, TimeUnit.SECONDS, () -> !textoResultado().isBlank());
        assertTrue(
                textoResultado().contains("números válidos"),
                "esperaba el aviso de números inválidos, era: " + textoResultado());
    }
}
