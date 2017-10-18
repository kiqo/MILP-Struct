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
 * Created by Verena on 28.02.2017. *
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
            LOGGER.error("", e);
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
        LOGGER.debug("Finished with structural parameters computation");
        threadExecutor.shutdown();
        return sb;
    }

    private static String invokeTask(ThreadExecutor executor, String fileName) {
        List<Future<String>> result;
        String resultString = null;
        LOGGER.debug("Structural Parameters: " + fileName);
        try {
            // invoke all waits until all tasks are finished (= terminated or had an error)
            result = executor.startStructuralParameterComputation(fileName);

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
        br.close();
    }
}
