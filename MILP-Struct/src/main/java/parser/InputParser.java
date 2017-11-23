package main.java.parser;

import main.java.exception.InputArgumentsException;
import main.java.main.Configuration;
import main.java.main.HelpPage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;

/**
 * Parses the input arguments of the program
 */
public class InputParser {
    private static final Logger LOGGER = LoggerFactory.getLogger(InputParser.class);

    public static void parseArguments(String[] args) {
        try {
            checkAtLeastOneArgument(args);
            if (helpArgumentSet(args)) {
                printLongHelpMessage();
                exitProgram(0);
            }
            setConfigurationsForArguments(args);
            checkForConfigurationErrors();
        } catch (InputArgumentsException e) {
            printErrorAndExit(e.getMessage());
        }
        Configuration.print();
    }

    private static void checkAtLeastOneArgument(String[] args) throws InputArgumentsException {
        if (args.length == 0) {
            throw new InputArgumentsException("Too few input arguments!");
        }
    }

    private static void printLongHelpMessage() {
        LOGGER.info(HelpPage.getLongHelpMessage());
    }

    private static boolean helpArgumentSet(String[] args) {
        for (String arg : args) {
            if (arg.equals("--help")) {
                return true;
            }
        }
        return false;
    }

    private static void printErrorAndExit(String errorMessage) {
        LOGGER.error(errorMessage);
        printShortHelpMessage();
        exitProgram(1);
    }

    private static void printShortHelpMessage() {
        String shortHelpMessage = HelpPage.getShortHelpMessage();
        LOGGER.error(shortHelpMessage);
    }

    private static void setInputFilePath(String[] args) throws InputArgumentsException {
        checkCorrectFileFormat(args[0]);
        checkFileCanBeAccessed(args[0]);
        Configuration.INPUT_FILE = args[0];
    }

    private static void checkFileCanBeAccessed(String filePath) throws InputArgumentsException {
        File f = new File(filePath);
        if (!f.isFile()) {
            throw new InputArgumentsException(filePath + " cannot be found or is not a file");
        }
        if (!f.canRead()) {
            throw new InputArgumentsException("The file " + filePath + " cannot be read");
        }
    }

    private static void checkCorrectFileFormat(String filePath) throws InputArgumentsException {
        String[] splits = filePath.split("\\.");
        if (!splits[splits.length - 1].equals("mps") && !splits[splits.length - 1].equals("mps\"") && !splits[splits.length - 1].equals("MPS")
                && !splits[splits.length - 1].equals("txt") && !splits[splits.length - 1].equals("txt\"") && !splits[splits.length - 1].equals("TXT")) {
            throw new InputArgumentsException("Input file must have .mps or .txt as ending!");
        }
    }

    private static void checkForConfigurationErrors() throws InputArgumentsException {
        if (Configuration.OUTPUT_FILE != null) {
            int outputFileLength = Configuration.OUTPUT_FILE.length();
            if (Configuration.OUTPUT_FILE.length() < 4 || !Configuration.OUTPUT_FILE.substring(outputFileLength - 4, outputFileLength).equals(".csv")) {
                throw new InputArgumentsException("Output file must end on .csv!");
            }
        }

        if (!Configuration.PRIMAL & !Configuration.INCIDENCE & !Configuration.DUAL) {
            throw new InputArgumentsException("One of the three graph representations -g primal incidence dual must be set!");
        }

        if (!Configuration.LOWER_BOUND & !Configuration.UPPER_BOUND & !Configuration.TORSO_WIDTH && !Configuration.TREE_DEPTH) {
            throw new InputArgumentsException("Either --ub --lb --to or --td must be set!");
        }

        if (Configuration.TORSO_WIDTH && !Configuration.PRIMAL) {
            throw new InputArgumentsException("Error: Option to compute torso width is only possible if graph type Configuration.PRIMAL is specified!");
        }

        if (Configuration.TREE_DEPTH && !Configuration.PRIMAL) {
            throw new InputArgumentsException("Error: Option to compute treedepth is only possible if graph type Configuration.PRIMAL is specified!");
        }
    }

    private static void exitProgram(int exitStatus) {
        System.exit(exitStatus);
    }

    private static void setConfigurationsForArguments(String[] args) throws InputArgumentsException {
        setInputFilePath(args);
        parseStructuralParameterArguments(args);
        setOutputFilePathIfNotSet();
        createOutputPathFolders();
    }

    private static void parseStructuralParameterArguments(String[] args) throws InputArgumentsException {
        boolean expectOutputFile = false, expectGraphType = false;
        for (int i = 1; i < args.length; i++) {
            switch (args[i]) {
                case "-o":
                case "-O":
                case "--output":
                    expectOutputFile = true;
                    break;

                case "-g":
                case "-G":
                case "--graph":
                    expectGraphType = true;
                    break;

                case "--obj":
                    Configuration.OBJ_FUNCTION = true;
                    break;

                case "--ub":
                case "--UB":
                case "--upperbound":
                    Configuration.UPPER_BOUND = true;
                    break;

                case "--lb":
                case "--LB":
                case "--lowerbound":
                    Configuration.LOWER_BOUND = true;
                    break;

                case "--to":
                case "--TO":
                case "--torsowidth":
                    Configuration.TORSO_WIDTH = true;
                    break;

                case "--td":
                case "--TD":
                case "--treedepth":
                    Configuration.TREE_DEPTH = true;
                    break;

                default:
                    if (expectOutputFile) {
                        parseOutputFile(args[i]);
                        expectOutputFile = false;
                        break;
                    }
                    if (expectGraphType) {
                        parseGraphType(args[i]);
                        break;
                    }
                    throw new InputArgumentsException("Unknown argument " + args[i]);
            }
        }
    }

    private static void parseOutputFile(String outputFile) {
        Configuration.OUTPUT_FILE = outputFile;
    }

    private static void parseGraphType(String arg) throws InputArgumentsException {
        String graphType = arg;
        if (graphType.equalsIgnoreCase("p") || graphType.equalsIgnoreCase("primal")) {
            Configuration.PRIMAL = true;
        } else if (graphType.equalsIgnoreCase("i") || graphType.equalsIgnoreCase("incidence")) {
            Configuration.INCIDENCE = true;
        } else if (graphType.equalsIgnoreCase("d") || graphType.equalsIgnoreCase("dual")) {
            Configuration.DUAL = true;
        } else {
            throw new InputArgumentsException("Error: Graph type that should be computed is not recognized!");
        }
    }

    private static void setOutputFilePathIfNotSet() {
        if (Configuration.OUTPUT_FILE == null) {
            int endIndex = Math.max(Configuration.INPUT_FILE.lastIndexOf("/"), Configuration.INPUT_FILE.lastIndexOf("\\"));
            String path = "./output";

            String inputFile = Configuration.INPUT_FILE.substring(endIndex + 1, Configuration.INPUT_FILE.length())
                    .replace(".txt", "")
                    .replace(".mps", "");

            String outputFile = path + "/" + inputFile;

            if (Configuration.LOWER_BOUND) {
                outputFile += "_LB";
            }
            if (Configuration.UPPER_BOUND) {
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
            if (Configuration.DUAL) {
                outputFile += "_D";
            }
            outputFile += ".csv";
            Configuration.OUTPUT_FILE = outputFile;
        }
    }

    /**
     * creates the output path folders if they do not exist yet
     */
    private static void createOutputPathFolders() {
        String outputPath = Configuration.OUTPUT_FILE;
        int lastFolderIndex = Math.max(outputPath.lastIndexOf("/"), outputPath.lastIndexOf("\\"));
        File directories = new File(Configuration.OUTPUT_FILE.substring(0, lastFolderIndex));
        directories.mkdirs();
    }
}
