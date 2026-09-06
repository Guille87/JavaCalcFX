package io.guillermoamadodiaz.javacalcfx.ui;

import javafx.scene.control.Button;

/** Fábrica de botones que ejecutan una acción sin parámetros. */
public final class Botones {

    private Botones() {
    }

    public static Button crear(String texto, Runnable accion) {
        Button boton = new Button(texto);
        boton.setOnAction(e -> accion.run());
        return boton;
    }
}
