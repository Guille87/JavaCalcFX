package io.guillermoamadodiaz.javacalcfx.ui;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Consumer;
import java.util.function.Supplier;
import javafx.concurrent.Task;

/**
 * Ejecuta cálculos fuera del hilo de JavaFX y gestiona su ciclo de vida.
 *
 * <p>Un único hilo demonio serializa las operaciones y no impide cerrar la JVM.
 * Solo puede haber un cálculo en curso: lanzar otro o llamar a {@link #cancelar()}
 * (al navegar) interrumpe el anterior. Los <em>callbacks</em> se disparan siempre
 * en el hilo de JavaFX, porque provienen de los eventos de {@link Task}.
 */
public final class CalculosAsync {

    private final ExecutorService ejecutor = Executors.newSingleThreadExecutor(tarea -> {
        Thread hilo = new Thread(tarea, "calculo");
        hilo.setDaemon(true);
        return hilo;
    });

    private Task<String> enCurso;

    /**
     * @param calculo    trabajo a ejecutar en segundo plano (puede lanzar excepción)
     * @param alEmpezar  se invoca cuando el cálculo arranca
     * @param alTerminar recibe el resultado; también se invoca con cadena vacía si se cancela
     * @param alFallar   recibe el mensaje ya traducido por {@link MensajesDeError}
     */
    public void ejecutar(
            Supplier<String> calculo, Runnable alEmpezar, Consumer<String> alTerminar, Consumer<String> alFallar) {
        cancelar();

        Task<String> tarea = new Task<>() {
            @Override
            protected String call() {
                return calculo.get();
            }
        };
        tarea.setOnRunning(e -> alEmpezar.run());
        tarea.setOnSucceeded(e -> alTerminar.accept(tarea.getValue()));
        tarea.setOnCancelled(e -> alTerminar.accept("")); // limpia el estado "Calculando…"
        tarea.setOnFailed(e -> alFallar.accept(MensajesDeError.describir(tarea.getException())));

        enCurso = tarea;
        ejecutor.execute(tarea);
    }

    /** Cancela (con interrupción) el cálculo en curso, si lo hay. */
    public void cancelar() {
        if (enCurso != null && enCurso.isRunning()) {
            enCurso.cancel(true);
        }
        enCurso = null;
    }

    /** Detiene el ejecutor. Debe llamarse desde {@code Application.stop()}. */
    public void cerrar() {
        ejecutor.shutdownNow();
    }
}
