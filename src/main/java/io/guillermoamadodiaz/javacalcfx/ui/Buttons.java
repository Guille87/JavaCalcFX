package io.guillermoamadodiaz.javacalcfx.ui;

import javafx.scene.control.Button;
import javafx.scene.control.Tooltip;

/** Factory for buttons that run a no-argument action. */
public final class Buttons {

    private Buttons() {}

    public static Button create(String text, Runnable action) {
        return create(text, null, action);
    }

    public static Button create(String text, String tooltip, Runnable action) {
        Button button = new Button(text);
        button.setOnAction(e -> action.run());
        if (tooltip != null) {
            button.setTooltip(new Tooltip(tooltip));
        }
        return button;
    }
}
