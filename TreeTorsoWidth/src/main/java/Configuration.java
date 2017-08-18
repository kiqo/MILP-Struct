package main.java;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by Verena on 22.05.2017.
 */
public class Configuration {
    private static final Logger LOGGER = LoggerFactory.getLogger(Configuration.class);
    private static final String NL = System.getProperty("line.separator");
    private static final String TAB = "\t";

    public static long TERMINATION_TIMEOUT = 30;
    public static long TIMEOUT = 120;
    public static Class<?> UPPER_BOUND_ALG = null;
    public static Class<?> LOWER_BOUND_ALG = null;
    public static final String DEFAULT_LOWER_BOUND_ALG = "nl.uu.cs.treewidth.algorithm.MaximumMinimumDegreePlusLeastC";
    public static final String DEFAULT_UPPER_BOUND_ALG = "nl.uu.cs.treewidth.algorithm.GreedyDegree";
    public static String INPUT_FILE = null;
    public static String OUTPUT_FILE = null;
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
        LOGGER.debug(sb.toString());
    }

    private static void addObjectiveFunctionInformation(StringBuilder sb) {
        if (OBJ_FUNCTION) {
            sb.append("Objective function is considered.");
        }
    }

    private static void addFileInformation(StringBuilder sb) {
        sb.append("Input file: " + INPUT_FILE + NL);
        sb.append("Output file: " + OUTPUT_FILE + NL);
    }

    private static void addDualInformation(StringBuilder sb) {
        if (DUAL) {
            sb.append("Computing for dual graph: " + NL);
            if (LOWER_BOUND) {
                sb.append(TAB + "- lower bound of tree width" + NL);
            }
            if (UPPER_BOUND) {
                sb.append(TAB + "- upper bound of tree width" + NL);
            }
            if (TORSO_WIDTH) {
                sb.append(TAB + "- lower bound of torso width" + NL);
                sb.append(TAB + "- upper bound of torso width" + NL);
            }
        }
    }

    private static void addIncidenceInformation(StringBuilder sb) {
        if (INCIDENCE) {
            sb.append("Computing for incidence graph: " + NL);
            if (LOWER_BOUND) {
                sb.append(TAB + "- lower bound of tree width" + NL);
            }
            if (UPPER_BOUND) {
                sb.append(TAB + "- upper bound of tree width" + NL);
            }
            if (TORSO_WIDTH) {
                sb.append(TAB + "- lower bound of torso width" + NL);
                sb.append(TAB + "- upper bound of torso width" + NL);
            }
        }
    }

    private static void addPrimalInformation(StringBuilder sb) {
        if (PRIMAL) {
            sb.append("Computing for primal graph: " + NL);
            if (LOWER_BOUND) {
                sb.append(TAB + "- lower bound of tree width" + NL);
            }
            if (UPPER_BOUND) {
                sb.append(TAB + "- upper bound of tree width" + NL);
            }
            if (TORSO_WIDTH) {
                sb.append(TAB + "- lower bound of torso width" + NL);
                sb.append(TAB + "- upper bound of torso width" + NL);
            }
            if (TREE_DEPTH) {
                sb.append(TAB + "- upper bound of tree depth" + NL);
            }
        }
    }
}
