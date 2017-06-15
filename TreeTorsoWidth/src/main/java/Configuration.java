package main.java;

/**
 * Created by Verena on 22.05.2017.
 */
public class Configuration {
    public static long TERMINATION_TIMEOUT = 30;
    public static long TIMEOUT = 120;
    public static Class<?> UPPER_BOUND_ALG = null;
    public static Class<?> LOWER_BOUND_ALG = null;
    public static String INPUT_FILE = null;
    public static String OUTPUT_FILE = "./output/results.txt";
    public static String GRAPH_TYPE = "primal";
    public static boolean PRIMAL = false;
    public static boolean INCIDENCE = false;
    public static boolean TORSO_WIDTH = false;
    public static boolean LOWER_BOUND = false;
    public static boolean UPPER_BOUND = false;
    public static boolean TREE_DEPTH = false;
    public static boolean OBJ_FUNCTION = false;
}
