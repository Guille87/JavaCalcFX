package io.guillermoamadodiaz.javacalcfx.ui;

import javafx.scene.Node;
import javafx.scene.layout.StackPane;

/**
 * Swaps the active screen inside a root container that always holds exactly one
 * child. Before each navigation it runs {@code onNavigate} (normally, cancelling
 * the in-flight calculation).
 */
public final class Navigator {

    private final StackPane root;
    private final Runnable onNavigate;

    public Navigator(StackPane root, Runnable onNavigate) {
        this.root = root;
        this.onNavigate = onNavigate;
    }

    public void show(Node screen) {
        onNavigate.run();
        root.getChildren().setAll(screen);
    }
}
