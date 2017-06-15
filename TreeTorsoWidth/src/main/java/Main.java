package main.java;

import main.java.lp.LPStatistics;

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
            Configuration.UPPER_BOUND_ALG = Class.forName("nl.uu.cs.treewidth.algorithm.GreedyDegree");
            Configuration.LOWER_BOUND_ALG = Class.forName("nl.uu.cs.treewidth.algorithm.MaximumMinimumDegreePlusLeastC");
        } catch (ClassNotFoundException e) {
            LOGGER.error("", e);
        }
    }

    public static void main(String[] args) throws IOException {

        init();
        parseArguments(args);
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
            sb.append(resultString);
            LOGGER.debug("-------------------");
        }

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
            LOGGER.error("", e);
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


    private static void parseArguments(String[] args) {
        int argc = args.length;
        boolean error = false;
        String helpMessage = "Usage: TreeTorsoWidth -i (inputFile.mps|inputFile.txt) (-u|-l|-t|-td) [-o outputFile] [-g (p[rimal]|i[ncidence]|pi)]";

        if (argc <= 1) {
            error = true;
        }

        boolean expectInputFile = false, expectOutputFile = false , expectGraphType = false;
        for (int i = 0; i < argc; i++) {
            switch (args[i]) {
                case "-i":
                case "-I":
                case "--input": expectInputFile = true; break;

                case "-o":
                case "-O":
                case "--output": expectOutputFile = true; break;

                case "-g":
                case "-G":
                case "--graph": expectGraphType = true; break;

                case "-obj": Configuration.OBJ_FUNCTION = true; break;

                case "-u":
                case "-U":
                case "--upperbound": Configuration.UPPER_BOUND = true; break;

                case "-l":
                case "-L":
                case "--lowerbound": Configuration.LOWER_BOUND = true; break;

                case "-t":
                case "-T":
                case "--torsowidth": Configuration.TORSO_WIDTH = true; break;

                case "-td":
                case "-TD":
                case "--treedepth": Configuration.TREE_DEPTH = true; break;

                default:
                    if (expectInputFile) {
                        expectInputFile = false;
                        Configuration.INPUT_FILE = args[i];
                        break;
                    }
                    if (expectOutputFile) {
                        expectOutputFile = false;
                        Configuration.OUTPUT_FILE = args[i];
                        break;

                    }
                    if (expectGraphType) {
                        Configuration.GRAPH_TYPE = args[i];
                        break;
                    }

                    error = true;
            }
        }

        // check that either treewidth upper- or lowerbound, torsowidth or treedepth is computed
        if (!Configuration.LOWER_BOUND & !Configuration.UPPER_BOUND & !Configuration.TORSO_WIDTH && !Configuration.TREE_DEPTH) {
            LOGGER.error("Either -u -l -t -td must be set!");
            LOGGER.error(helpMessage);
            System.exit(1);
            return;
        }

        if (Configuration.GRAPH_TYPE.equals("p") || Configuration.GRAPH_TYPE.equals("primal")) {
            Configuration.PRIMAL = true;
        } else if (Configuration.GRAPH_TYPE.equals("i") || Configuration.GRAPH_TYPE.equals("incidence")){
            Configuration.INCIDENCE = true;
        } else if (Configuration.GRAPH_TYPE.equals("pi") || Configuration.GRAPH_TYPE.equals("ip")) {
            Configuration.PRIMAL = true;
            Configuration.INCIDENCE = true;
        } else {
            LOGGER.error("Error: Graph type that should be computed is not recognized!");
            error = true;
        }

        if (Configuration.TORSO_WIDTH && !Configuration.PRIMAL) {
            LOGGER.error("Error: Option to compute torso width is only possible if graph type Configuration.PRIMAL is specified!");
            error = true;
        }

        if (Configuration.TREE_DEPTH && !Configuration.PRIMAL) {
            LOGGER.error("Error: Option to compute treedepth is only possible if graph type Configuration.PRIMAL is specified!");
            error = true;
        }

        if (error) {
            LOGGER.error(helpMessage);
            System.exit(1);
            return;
        }

        LOGGER.debug("Input: " + Configuration.INPUT_FILE);
    }
}
