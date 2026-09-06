package io.guillermoamadodiaz.javacalcfx.ui;

import javafx.scene.control.Button;
import javafx.scene.control.Tooltip;

/** Fábrica de botones que ejecutan una acción sin parámetros. */
public final class Botones {

    private Botones() {
    }

    public static Button crear(String texto, Runnable accion) {
        return crear(texto, null, accion);
    }

    public static Button crear(String texto, String tooltip, Runnable accion) {
        Button boton = new Button(texto);
        boton.setOnAction(e -> accion.run());
        if (tooltip != null) {
            boton.setTooltip(new Tooltip(tooltip));
        }
        return boton;
    }
}
