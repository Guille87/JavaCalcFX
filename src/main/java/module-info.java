module io.guillermoamadodiaz.javacalcfx {
    requires javafx.controls;
    requires javafx.fxml;

    opens io.guillermoamadodiaz.javacalcfx to javafx.fxml;
    exports io.guillermoamadodiaz.javacalcfx;
}
