package main.java.main;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by Verena on 22.05.2017.
 */
public class Configuration {
    private static final Logger LOGGER = LoggerFactory.getLogger(Configuration.class);
    private static final String NL = System.getProperty("line.separator");
    private static final String TAB = "\t";

    public static String PROGRAM_NAME = "MILP-Struct";
    public static long TERMINATION_TIMEOUT = 5;
    public static long TIMEOUT = 60 * 10; // 10 min
    public static Class<?> UPPER_BOUND_ALG = null;
    public static Class<?> LOWER_BOUND_ALG = null;
    public static final String DEFAULT_LOWER_BOUND_ALG = "nl.uu.cs.treewidth.algorithm.MaximumMinimumDegreePlusLeastC";
    public static final String DEFAULT_UPPER_BOUND_ALG = "nl.uu.cs.treewidth.algorithm.GreedyDegree";
    public static String INPUT_FILE = null;
    public static String OUTPUT_FILE = null;
    public static String GRAPH_REPRESENTATIONS_FOLDER = "./graphs/";
    public static boolean PRIMAL = false;
    public static boolean INCIDENCE = false;
    public static boolean DUAL = false;
    public static boolean TORSO_WIDTH = false;
    public static boolean LOWER_BOUND = false;
    public static boolean UPPER_BOUND = false;
    public static boolean TREE_DEPTH = false;
    public static boolean OBJ_FUNCTION = false;

    public static void print() {
        StringBuilder sb = new StringBuilder();
        addFileInformation(sb);
        addPrimalInformation(sb);
        addIncidenceInformation(sb);
        addDualInformation(sb);
        addObjectiveFunctionInformation(sb);
        addTimeoutInformation(sb);
        LOGGER.info(sb.toString());
    }

    private static void addTimeoutInformation(StringBuilder sb) {
        sb.append("Timeout for one MILP instance: ").append(Configuration.TIMEOUT).append(" seconds").append(NL);
    }

    private static void addObjectiveFunctionInformation(StringBuilder sb) {
        if (OBJ_FUNCTION) {
            sb.append("Objective function is considered.").append(NL);
        }
    }

    private static void addFileInformation(StringBuilder sb) {
        sb.append("Input file: ").append(INPUT_FILE).append(NL);
        sb.append("Output file: ").append(OUTPUT_FILE).append(NL);
    }

    private static void addDualInformation(StringBuilder sb) {
        if (DUAL) {
            sb.append("Computing for dual graph: ").append(NL);
            if (LOWER_BOUND) {
                sb.append(TAB + "- lower bound of tree width").append(NL);
            }
            if (UPPER_BOUND) {
                sb.append(TAB + "- upper bound of tree width").append(NL);
            }
        }
    }

    private static void addIncidenceInformation(StringBuilder sb) {
        if (INCIDENCE) {
            sb.append("Computing for incidence graph: ").append(NL);
            if (LOWER_BOUND) {
                sb.append(TAB + "- lower bound of tree width").append(NL);
            }
            if (UPPER_BOUND) {
                sb.append(TAB + "- upper bound of tree width").append(NL);
            }
        }
    }

    private static void addPrimalInformation(StringBuilder sb) {
        if (PRIMAL) {
            sb.append("Computing for primal graph: ").append(NL);
            if (LOWER_BOUND) {
                sb.append(TAB + "- lower bound of tree width").append(NL);
            }
            if (UPPER_BOUND) {
                sb.append(TAB + "- upper bound of tree width").append(NL);
            }
            if (TORSO_WIDTH) {
                sb.append(TAB + "- lower bound of torso width").append(NL);
                sb.append(TAB + "- upper bound of torso width").append(NL);
            }
            if (TREE_DEPTH) {
                sb.append(TAB + "- upper bound of tree depth").append(NL);
            }
        }
    }

    public static void setDefaultAlgorithms() {
        try {
            Configuration.UPPER_BOUND_ALG = Class.forName(Configuration.DEFAULT_UPPER_BOUND_ALG);
            Configuration.LOWER_BOUND_ALG = Class.forName(Configuration.DEFAULT_LOWER_BOUND_ALG);
        } catch (ClassNotFoundException e) {
            LOGGER.error("", e);
        }
    }
}
