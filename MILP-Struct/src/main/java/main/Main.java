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
 * This program is used for analyzing structural parameters of graphical representations of (M)ILP instances.
 * These parameters may provide a measure on the difficulty, in terms of the runtime, of an (M)ILP instance.
 *
 * It allows the parsing of (M)ILP instances, construction of primal, incidence and dual graph representations
 * of (M)ILP instances and the computation of the structural parameters itself.
 *
 * A configurable timeout value can be set after which the program cancels the current computation of a (M)ILP instance.
 * See also --help for more information.
 *
 *
 *
 * Created by Verena on 28.02.2017.
 * Copyright 2017, Verena Dittmer
 *
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU Lesser General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU Lesser General Public License for more details.
 *
 *  You should have received a copy of the GNU Lesser General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
public class Main {

    private static final Logger LOGGER = LoggerFactory.getLogger(Main.class);
    private static final String LINE_SEPARATOR = System.lineSeparator();

    static {
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
            LOGGER.error("Error writing statistics to output file {}", Configuration.OUTPUT_FILE, e);
        }
    }

    private static StringBuilder computeStructuralParametersForFiles(List<String> filePaths) {
        StringBuilder sb = appendHeader();
        ThreadExecutor threadExecutor = new ThreadExecutor();

        for (String fileName : filePaths) {
            String resultString = invokeTask(threadExecutor, fileName);
            if (resultString != null) {
                sb.append(resultString);
            }
        }
        LOGGER.info("Finished with structural parameters computation");
        threadExecutor.shutdown();
        return sb;
    }

    private static String invokeTask(ThreadExecutor executor, String fileName) {
        String resultString = null;
        LOGGER.info("Start structural parameters computation for {}", fileName);
        try {
            // invoke all waits until all tasks are finished (= terminated or had an error)
            List<Future<String>> result = executor.startStructuralParameterComputation(fileName);

            if (result.get(0).isCancelled()) {
                // task finished by cancellation (seconds exceeded)
                LOGGER.warn("{} was cancelled", fileName);
                resultString = fileName + ";no result;" + LINE_SEPARATOR;
            } else {
                resultString = result.get(0).get();
            }
        } catch (InterruptedException | ExecutionException e) {
            LOGGER.error("Error for {} occured " , fileName, e);
            resultString = fileName + ";no result;" + LINE_SEPARATOR;
        }
        return resultString;
    }

    private static StringBuilder appendHeader() {
        StringBuilder sb = new StringBuilder();
        sb.append("Timeout for one MILP instance: ").append(Configuration.TIMEOUT).append("s").append(LINE_SEPARATOR);
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
        br.close();
    }
}
