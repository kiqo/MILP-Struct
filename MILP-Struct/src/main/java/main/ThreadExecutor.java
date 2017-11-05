package main.java.main;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

/**
 * Thread creation such that threads can be cancelled after the specified Configuration.TIMEOUT
 */
public class ThreadExecutor {
    private static final Logger LOGGER = LoggerFactory.getLogger(ThreadExecutor.class);

    private ExecutorService executor = null;

    public ThreadExecutor () {
        createSingleThreadExecutor();
    }

    private void createSingleThreadExecutor() {
        executor = Executors.newSingleThreadExecutor();
    }

    public void shutdown() {
        // disable new tasks from being submitted
        executor.shutdown();
        try {
            // Wait a while for existing tasks to terminate
            if (!executor.awaitTermination(Configuration.TERMINATION_TIMEOUT, TimeUnit.SECONDS)) {
                // Cancel currently executing tasks
                executor.shutdownNow();
                // Wait a while for tasks to respond to being cancelled
                if (!executor.awaitTermination(Configuration.TERMINATION_TIMEOUT, TimeUnit.SECONDS)) {
                    LOGGER.debug("Executor did not terminate");
                }
            }
        } catch (InterruptedException e) {
            LOGGER.error("Interrupted Exception", e);
        }
    }

    public List<Future<String>> startStructuralParameterComputation(String fileName) throws InterruptedException {
        return executor.invokeAll(Arrays.asList(new StructuralParametersComputation(fileName)), Configuration.TIMEOUT, TimeUnit.SECONDS);
    }

    public void checkInterrupted() throws InterruptedException {
        if (Thread.currentThread().isInterrupted()) {
            throw new InterruptedException();
        }
    }
}
