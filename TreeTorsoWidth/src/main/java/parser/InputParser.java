package main.java.parser;

import main.java.main.Configuration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by Verena on 19.08.2017.
 */
public class InputParser {
    private static final Logger LOGGER = LoggerFactory.getLogger(InputParser.class);

    public static void parseArguments(String[] args) {
        boolean error = args.length <= 1 ||  args[0].equals("--help");
        handleError(error);

        setInputFilePath(args);
        error = setConfigurationsForArguments(args, error);
        error = checkForConfigurationErrors(error);

        handleError(error);

        setOutputFilePathIfNotSet();
        Configuration.print();
    }

    private static void handleError(boolean error) {
        if (error) {
            printHelpMessage();
            exitProgram();
        }
    }

    private static void setInputFilePath(String[] args) {
        Configuration.INPUT_FILE = args[0];
    }

    private static boolean checkForConfigurationErrors(boolean error) {
        if (!Configuration.LOWER_BOUND & !Configuration.UPPER_BOUND & !Configuration.TORSO_WIDTH && !Configuration.TREE_DEPTH) {
            LOGGER.error("Either --ub --lb --to or --td must be set!");
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
        return error;
    }

    private static void exitProgram() {
        System.exit(1);
    }

    private static boolean setConfigurationsForArguments(String[] args, boolean error) {
        boolean expectOutputFile = false , expectGraphType = false;
        for (int i = 1; i < args.length; i++) {
            switch (args[i]) {
                case "-o":
                case "-O":
                case "--output": expectOutputFile = true; break;

                case "-g":
                case "-G":
                case "--graph": expectGraphType = true; break;

                case "--obj": Configuration.OBJ_FUNCTION = true; break;

                case "--ub":
                case "--UB":
                case "--upperbound": Configuration.UPPER_BOUND = true; break;

                case "--lb":
                case "--LB":
                case "--lowerbound": Configuration.LOWER_BOUND = true; break;

                case "--to":
                case "--TO":
                case "--torsowidth": Configuration.TORSO_WIDTH = true; break;

                case "--td":
                case "--TD":
                case "--treedepth": Configuration.TREE_DEPTH = true; break;

                default:
                    if (expectOutputFile) {
                        parseOutputFile(args[i]);
                        expectOutputFile = false;
                        break;
                    }
                    if (expectGraphType) {
                        error = parseGraphType(args[i], error);
                        break;
                    }
                    error = true;
            }
        }
        return error;
    }

    private static void parseOutputFile(String outputFile) {
        Configuration.OUTPUT_FILE = outputFile;
    }

    private static boolean parseGraphType(String arg, boolean error) {
        String graphType = arg;
        if (graphType.equalsIgnoreCase("p") || graphType.equalsIgnoreCase("primal")) {
            Configuration.PRIMAL = true;
        } else if (graphType.equalsIgnoreCase("i") || graphType.equalsIgnoreCase("incidence")){
            Configuration.INCIDENCE = true;
        } else if (graphType.equalsIgnoreCase("d") || graphType.equalsIgnoreCase("dual")) {
            Configuration.DUAL = true;
        } else {
            LOGGER.error("Error: Graph type that should be computed is not recognized!");
            error = true;
        }
        return error;
    }

    private static void setOutputFilePathIfNotSet() {
        if (Configuration.OUTPUT_FILE == null) {
            int endIndex = Configuration.INPUT_FILE.lastIndexOf("/");
            String path = "./output";

            String inputFile = Configuration.INPUT_FILE.substring(endIndex+1, Configuration.INPUT_FILE.length())
                    .replace(".txt", "")
                    .replace(".mps", "");

            String outputFile = path + "/" + inputFile;

            if (Configuration.LOWER_BOUND) {
                outputFile += "_LB";
            }
            if (Configuration.LOWER_BOUND) {
                outputFile += "_UB";
            }
            if (Configuration.TORSO_WIDTH) {
                outputFile += "_TO";
            }
            if (Configuration.TREE_DEPTH) {
                outputFile += "_TD";
            }
            if (Configuration.OBJ_FUNCTION) {
                outputFile += "_OBJ";
            }
            if (Configuration.PRIMAL) {
                outputFile += "_P";
            }
            if (Configuration.INCIDENCE) {
                outputFile += "_I";
            }
            outputFile += ".csv";
            Configuration.OUTPUT_FILE = outputFile;
        }
    }

    private static void printHelpMessage() {
        String usageMessage = "Usage: TreeTorsoWidth [--help] <inputFile(.mps|.txt)> [-o <outputFile(.txt|.csv)>] " +
                "(--lb|--ub|--to|--td) -g (<primal>|<incidence>|<dual>) [--obj]" + System.getProperty("line.separator" +
                "See TreeTorsoWidth --help for more information");
        LOGGER.error(usageMessage);
    }
}
