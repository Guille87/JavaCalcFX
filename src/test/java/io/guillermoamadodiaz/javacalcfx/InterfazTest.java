package io.guillermoamadodiaz.javacalcfx;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.testfx.api.FxAssert.verifyThat;
import static org.testfx.matcher.control.LabeledMatchers.hasText;

import io.guillermoamadodiaz.javacalcfx.i18n.Textos;
import io.guillermoamadodiaz.javacalcfx.ui.EstadoVentana;
import io.guillermoamadodiaz.javacalcfx.ui.Tema;
import java.util.Locale;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.prefs.Preferences;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.stage.Stage;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
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
        Textos.usarIdioma(Locale.forLanguageTag("es"));
    }

    @AfterAll
    static void limpiarPreferencias() {
        try {
            Preferences.userNodeForPackage(EstadoVentana.class).clear();
            Preferences.userNodeForPackage(Tema.class).node("tema").clear();
        } catch (Exception ignorado) {
            // sin persistencia disponible en el entorno de test: nada que limpiar
        }
    }

    @Override
    public void start(Stage escenario) {
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
