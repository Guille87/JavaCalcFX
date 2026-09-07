package io.guillermoamadodiaz.javacalcfx.ui;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Consumer;
import java.util.function.Supplier;
import javafx.concurrent.Task;

/**
 * Runs calculations off the JavaFX thread and manages their life cycle.
 *
 * <p>A single daemon thread serializes the work and does not block JVM shutdown.
 * Only one calculation may run at a time: starting another or calling
 * {@link #cancel()} (on navigation) interrupts the previous one. Callbacks always
 * fire on the JavaFX thread, since they come from {@link Task} events.
 */
public final class AsyncCalculations {

    private final ExecutorService executor = Executors.newSingleThreadExecutor(task -> {
        Thread thread = new Thread(task, "calculation");
        thread.setDaemon(true);
        return thread;
    });

    private Task<String> inProgress;

    /**
     * @param calculation work to run in the background (may throw)
     * @param onStart     invoked when the calculation starts
     * @param onFinish    receives the result; also invoked with an empty string on cancellation
     * @param onFail      receives the message already translated by {@link ErrorMessages}
     */
    public void run(
            Supplier<String> calculation, Runnable onStart, Consumer<String> onFinish, Consumer<String> onFail) {
        cancel();

        Task<String> task = new Task<>() {
            @Override
            protected String call() {
                return calculation.get();
            }
        };
        task.setOnRunning(e -> onStart.run());
        task.setOnSucceeded(e -> onFinish.accept(task.getValue()));
        task.setOnCancelled(e -> onFinish.accept("")); // clears the "Calculating…" state
        task.setOnFailed(e -> onFail.accept(ErrorMessages.describe(task.getException())));

        inProgress = task;
        executor.execute(task);
    }

    /** Cancels (with interruption) the in-flight calculation, if any. */
    public void cancel() {
        if (inProgress != null && inProgress.isRunning()) {
            inProgress.cancel(true);
        }
        inProgress = null;
    }

    /** Stops the executor. Must be called from {@code Application.stop()}. */
    public void close() {
        executor.shutdownNow();
    }
}
