package main.java.main;

import main.java.lp.LPStatisticsFormatter;
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

    public static void main(String[] args) throws IOException {
        InputParser.parseArguments(args);
        Configuration.setDefaultAlgorithms();
        List<String> filePaths = getFilePathsForComputation();
        StringBuilder sb = computeStructuralParametersForFiles(filePaths);
        writeStatisticsToOutputFile(sb);
    }

    private static void writeStatisticsToOutputFile(StringBuilder sb) {
        try{
            PrintWriter writer = new PrintWriter(Configuration.OUTPUT_FILE, "UTF-8");
            writer.print(sb.toString());
            writer.close();
        } catch (IOException e) {
            LOGGER.error("", e);
        }
    }

    private static StringBuilder computeStructuralParametersForFiles(List<String> filePaths) {
        StringBuilder sb = appendHeader();
        ExecutorService executor = null;
        for (String fileName : filePaths) {
            executor = createSingleThreadExecutor();
            String resultString = invokeTask(executor, fileName);
            if (resultString != null) {
                sb.append(resultString);
            }
        }
        LOGGER.debug("Finished with structural parameters computation");
        shutdownExecutor(executor);
        return sb;
    }

    private static void shutdownExecutor(ExecutorService executor) {
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
    }

    private static String invokeTask(ExecutorService executor, String fileName) {
        List<Future<String>> result;
        String resultString = null;
        LOGGER.debug("Structural Parameters: " + fileName);
        // ThreadLocal
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
        return resultString;
    }

    private static ExecutorService createSingleThreadExecutor() {
        return Executors.newSingleThreadExecutor();
    }

    private static StringBuilder appendHeader() {
        StringBuilder sb = new StringBuilder();
        sb.append(LPStatisticsFormatter.csvFormatHeader());
        return sb;
    }

    private static List<String> getFilePathsForComputation() throws IOException {
        boolean isTxt = Configuration.INPUT_FILE.substring(Configuration.INPUT_FILE.length() - 3, Configuration.INPUT_FILE.length()).equals("txt");

        List<String> files = new ArrayList<>();
        if (isTxt) {
            readFilePathsInTxt(files);
        } else {
            files.add(Configuration.INPUT_FILE);
        }
        return files;
    }

    private static void readFilePathsInTxt(List<String> files) throws IOException {
        BufferedReader br = new BufferedReader(new FileReader(Configuration.INPUT_FILE));

        String line;
        while ((line = br.readLine()) != null) {
            files.add(line);
        }
    }
}
