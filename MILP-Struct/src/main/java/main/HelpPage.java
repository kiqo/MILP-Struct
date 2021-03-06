package main.java.main;

import wagu.Block;
import wagu.Board;
import wagu.Table;

import java.util.Arrays;
import java.util.List;

/**
 * Prints out the help message if wrong input parameters or --help are supplied
 */
public class HelpPage {
    private static final String NL = System.getProperty("line.separator");
    private static final int TABLE_WIDTH = 120;
    private static final String DOUBLE_TAB = "\t\t";

    private static final String shortHelpMessage = "Usage: " + Configuration.PROGRAM_NAME + " [--help] <inputFile(.mps|.txt)> [-o <outputFile.csv>] " +
            "(--lb|--ub|--to|--td) -g (<primal>|<incidence>|<dual>) [--obj]";

    public static String getShortHelpMessage() {
        return shortHelpMessage;
    }

    public static String getLongHelpMessage() {
        return constructLongHelpMessage();
    }

    private static String constructLongHelpMessage() {
        List<String> headersList = Arrays.asList(Configuration.PROGRAM_NAME, "", "");
        List<List<String>> rowsList = createRowsList();
        Board board = new Board(TABLE_WIDTH);
        Table table = new Table(board, TABLE_WIDTH, headersList, rowsList);
        table.setGridMode(Table.GRID_NON);
        List<Integer> colWidthsListEdited = Arrays.asList(Configuration.PROGRAM_NAME.length(), 15, 120 - 15 - Configuration.PROGRAM_NAME.length());
        table.setColWidthsList(colWidthsListEdited);
        Block tableBlock = table.tableToBlocks();
        board.setInitialBlock(tableBlock);
        board.build();
        String tableString = board.getPreview();
        return NL + tableString;
    }

    private static List<List<String>> createRowsList() {
        return Arrays.asList(
                Arrays.asList("", "Description", ""),
                Arrays.asList("", "", "A framework for computing structural parameters of "),
                Arrays.asList("", "", "Integer Linear Programs (ILP) and Mixed Integer Linear "),
                Arrays.asList("", "", "Programs (MILP). It is possible to compute lower and "),
                Arrays.asList("", "", "upper bounds for treewidth, tree-depth and torso-width "),
                Arrays.asList("", "", "of the primal, incidence or dual representation of the "),
                Arrays.asList("", "", "(M)ILP instances. "),
                Arrays.asList("", "", ""),
                Arrays.asList("", "Arguments", ""),
                Arrays.asList("", "", Configuration.PROGRAM_NAME + " [--help] <inputFile(.mps|.txt)> "),
                Arrays.asList("", "", "[-o <outputFile.csv>] (--lb|--ub|--to|--td)"),
                Arrays.asList("", "", "-g (<primal>|<incidence>|<dual>) [--obj]"),
                Arrays.asList("", "", ""),
                Arrays.asList("", "", "--help"),
                Arrays.asList("", "", DOUBLE_TAB + "Display this help and exit (optional)"),
                Arrays.asList("", "", "<inputFile(.mps|.txt)>"),
                Arrays.asList("", "", DOUBLE_TAB + "Path to input file (mandatory)"),
                Arrays.asList("", "", DOUBLE_TAB + "If the file is a .mps file, the single .mps file is handled."),
                Arrays.asList("", "", DOUBLE_TAB + "If it is a .txt file, then every line corresponds to the"),
                Arrays.asList("", "", DOUBLE_TAB + "file path of a .mps file that is handled."),
                Arrays.asList("", "", "-o, --output <outputFile.csv>"),
                Arrays.asList("", "", DOUBLE_TAB + "Path to output file (optional)"),
                Arrays.asList("", "", DOUBLE_TAB + "Output file must end on .csv as a .csv file is generated."),
                Arrays.asList("", "", "-lb, --lowerbound"),
                Arrays.asList("", "", DOUBLE_TAB + "Compute the treewidth lower bound (optional)"),
                Arrays.asList("", "", "-ub, --upperbound"),
                Arrays.asList("", "", DOUBLE_TAB + "Compute the treewidth upper bound (optional)"),
                Arrays.asList("", "", "-to, --torsowidth"),
                Arrays.asList("", "", DOUBLE_TAB + "Compute the torso-width lower and upper bound (optional),"),
                Arrays.asList("", "", DOUBLE_TAB + "only possible for the primal graph "),
                Arrays.asList("", "", "-td, --treedepth"),
                Arrays.asList("", "", DOUBLE_TAB + "Compute the tree-depth upper bound (optional),"),
                Arrays.asList("", "", DOUBLE_TAB + "only possible for the primal graph "),
                Arrays.asList("", "", "-g, --graph (<primal>|<incidence>|<dual>|<p>|<i>|<d>)"),
                Arrays.asList("", "", DOUBLE_TAB + "The graph representation of the (M)ILP computed"),
                Arrays.asList("", "", DOUBLE_TAB + "(mandatory), possible to specify multiple graph "),
                Arrays.asList("", "", DOUBLE_TAB + "representations"),
                Arrays.asList("", "", "--obj"),
                Arrays.asList("", "", DOUBLE_TAB + "Consider objective function (optional), i.e. in "),
                Arrays.asList("", "", DOUBLE_TAB + "the graph representation computed it is handled "),
                Arrays.asList("", "", DOUBLE_TAB + "as if it were part of the constraint matrix"),
                Arrays.asList("", "", "One of --lb, --ub, --to, --td must be specified. "),
                Arrays.asList("", "", "Increasing the stack and/or heap size of the Java VM may be necessary. "),
                Arrays.asList("", "", ""),
                Arrays.asList("", "Examples", ""),
                Arrays.asList("", "", Configuration.PROGRAM_NAME + " ./path/inputFile.mps -g primal --lb --ub"),
                Arrays.asList("", "", Configuration.PROGRAM_NAME + " inputFile.txt -o outputFile.csv -g p --ub"),
                Arrays.asList("", "", Configuration.PROGRAM_NAME + " inputFile.txt -g primal --to --obj"),
                Arrays.asList("", "", Configuration.PROGRAM_NAME + " inputFile.txt -g primal incidence --td --to"),
                Arrays.asList("", "", ""),
                Arrays.asList("", "Example Windows", ""),
                Arrays.asList("", "", "java -Xss256m -Xmx4g -jar " + Configuration.PROGRAM_NAME + ".jar inputFile.txt -g p --lb")
        );
    }
}
