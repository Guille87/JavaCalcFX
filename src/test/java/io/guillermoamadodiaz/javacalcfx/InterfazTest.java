package io.guillermoamadodiaz.javacalcfx;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.testfx.api.FxAssert.verifyThat;
import static org.testfx.matcher.control.LabeledMatchers.hasText;

import io.guillermoamadodiaz.javacalcfx.i18n.Textos;
import java.util.Locale;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.stage.Stage;
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

    @Override
    public void start(Stage escenario) {
        new SelectorDeOpciones().start(escenario);
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
    void el_foco_empieza_en_el_primer_campo() {
        clickOn("Calcular Área de Cilindro");
        WaitForAsyncUtils.waitForFxEvents();
        TextField primero = lookup(".text-field").nth(0).queryAs(TextField.class);
        assertTrue(primero.isFocused(), "el primer campo debe tener el foco al abrir el formulario");
    }

    @Test
    void abrir_una_calculadora_y_volver_con_el_boton() {
        clickOn("Determinar Año Bisiesto");
        verifyThat(".encabezado", hasText("Año Bisiesto"));

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
    void un_dato_no_numerico_muestra_un_mensaje_de_error() throws TimeoutException {
        clickOn("Determinar Año Bisiesto");
        clickOn(".text-field").write("abc");
        clickOn("Calcular");

        WaitForAsyncUtils.waitFor(5, TimeUnit.SECONDS, () -> !textoResultado().isBlank());
        assertTrue(
                textoResultado().contains("números válidos"),
                "esperaba el aviso de números inválidos, era: " + textoResultado());
    }
}
