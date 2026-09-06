package io.guillermoamadodiaz.javacalcfx.ui;

import javafx.scene.Node;
import javafx.scene.layout.StackPane;

/**
 * Cambia la pantalla activa dentro de un contenedor raíz que siempre tiene
 * exactamente un hijo. Antes de cada navegación ejecuta {@code alNavegar}
 * (normalmente, cancelar el cálculo en curso).
 */
public final class Navegador {

    private final StackPane raiz;
    private final Runnable alNavegar;

    public Navegador(StackPane raiz, Runnable alNavegar) {
        this.raiz = raiz;
        this.alNavegar = alNavegar;
    }

    public void mostrar(Node pantalla) {
        alNavegar.run();
        raiz.getChildren().setAll(pantalla);
    }
}
