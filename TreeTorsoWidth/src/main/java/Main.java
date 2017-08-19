package main.java;

import main.java.lp.LPStatistics;

import main.java.parser.InputParser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.*;

/**
 * Created by Verena on 28.02.2017.
 */
public class Main {

    private static final Logger LOGGER = LoggerFactory.getLogger(Main.class);

    static{
        // system property current.date used for the name of the log file
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy-hh-mm-ss");
        System.setProperty("current.date", dateFormat.format(new Date()));
    }

    private static void init() {
        try {
            Configuration.UPPER_BOUND_ALG = Class.forName(Configuration.DEFAULT_UPPER_BOUND_ALG);
            Configuration.LOWER_BOUND_ALG = Class.forName(Configuration.DEFAULT_LOWER_BOUND_ALG);
        } catch (ClassNotFoundException e) {
            LOGGER.error("", e);
        }
    }

    public static void main(String[] args) throws IOException {

        init();
        InputParser.parseArguments(args);
        boolean isTxt = Configuration.INPUT_FILE.substring(Configuration.INPUT_FILE.length() - 3, Configuration.INPUT_FILE.length()).equals("txt");

        List<String> files = new ArrayList<>();
        if (isTxt) {
            // read all the files that need to be processed
            BufferedReader br = new BufferedReader(new FileReader(Configuration.INPUT_FILE));

            String line;
            while ((line = br.readLine()) != null) {
                files.add(line);
            }
        } else {
            files.add(Configuration.INPUT_FILE);
        }

        StringBuilder sb = new StringBuilder();

        // append header
        if (Configuration.OUTPUT_FILE.endsWith(".csv")) {
            sb.append(LPStatistics.csvFormatHeader(Configuration.PRIMAL, Configuration.INCIDENCE));
        } else {
            sb.append(LPStatistics.shortDescriptionHeader());
        }

        // single threaded
        ExecutorService executor = Executors.newFixedThreadPool(1);
        List<Future<String>> result = null;
        for (String fileName : files) {
            LOGGER.debug("Structural Parameters: " + fileName);
            executor = Executors.newSingleThreadExecutor(); // -> keeps EXACTLY one thread, change to using a thread pool?
            String resultString = null;
            try {
                // invoke all waits until all tasks are finished (= terminated or had an error)
                result = executor.invokeAll(Arrays.asList(new StructuralParametersComputation(fileName)), Configuration.TIMEOUT, TimeUnit.SECONDS);

                if (result.get(0).isCancelled()) {
                    // task finished by cancellation (seconds exceeded)
                    LOGGER.warn(fileName + " was cancelled");
                    resultString = fileName + ";no result;" + System.lineSeparator();
                } else {
                    resultString = result.get(0).get();
                }

            } catch (InterruptedException | ExecutionException e) {
                LOGGER.error("", e);
            }
            if (resultString != null) {
                sb.append(resultString);
            }
        }
        LOGGER.debug("Finished with structural parameter computation");

        // disable new tasks from being submitted
        executor.shutdown();
        try {
            // Wait a while for existing tasks to terminate
            if (!executor.awaitTermination(Configuration.TERMINATION_TIMEOUT, TimeUnit.SECONDS)) {
                // Cancel currently executing tasks
                executor.shutdownNow();
                // Wait a while for tasks to respond to being cancelled
                if (!executor.awaitTermination(Configuration.TERMINATION_TIMEOUT, TimeUnit.SECONDS)) {
                    LOGGER.error("Error: Executor did not terminate");
                }
            }
        } catch (InterruptedException e) {
            LOGGER.error("Interrupted Exception", e);
        }

        // print to output file statistics about lp and primal graph
        try{
            PrintWriter writer = new PrintWriter(Configuration.OUTPUT_FILE, "UTF-8");
            writer.print(sb.toString());
            writer.close();
        } catch (IOException e) {
            LOGGER.error("", e);
        }
    }
}
