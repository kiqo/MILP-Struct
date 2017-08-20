package main.java.main;

import wagu.Block;
import wagu.Board;
import wagu.Table;

import java.util.Arrays;
import java.util.List;

/**
 * Created by Verena on 20.08.2017.
 */
public class HelpPage {
    private static final String NL = System.getProperty("line.separator");
    private static final String SINGLE_TAB = "\t";
    private static final String DOUBLE_TAB = "\t\t";

    private static final String shortHelpMessage = "Usage: TreeTorsoWidth [--help] <inputFile(.mps|.txt)> [-o <outputFile(.txt|.csv)>] " +
            "(--lb|--ub|--to|--td) -g (<primal>|<incidence>|<dual>) [--obj]" + NL +
            "See TreeTorsoWidth --help for more information";

    public static String getShortHelpMessage() {
        return shortHelpMessage;
    }

    public static String getLongHelpMessage() {
        return constructLongHelpMessage();
    }

    private static String constructLongHelpMessage() {
        StringBuilder sb = new StringBuilder();
        appendNameOfProgram(sb);
        appendDescription(sb);
        return sb.toString();
    }

    private static void appendDescription(StringBuilder sb) {
        List<String> headersList = Arrays.asList("NAME", "GENDER", "MARRIED", "AGE", "SALARY($)");
        List<List<String>> rowsList = Arrays.asList(
                Arrays.asList("Eddy", "Male", "No", "23", "1200.27"),
                Arrays.asList("Libby", "Male", "No", "17", "800.50"),
                Arrays.asList("Rea", "Female", "No", "30", "10000.00"),
                Arrays.asList("Deandre", "Female", "No", "19", "18000.50"),
                Arrays.asList("Alice", "Male", "Yes", "29", "580.40"),
                Arrays.asList("Alyse", "Female", "No", "26", "7000.89"),
                Arrays.asList("Venessa", "Female", "No", "22", "100700.50")
        );
//bookmark 1
        Board board = new Board(75);
        Table table = new Table(board, 75, headersList, rowsList);
        Block tableBlock = table.tableToBlocks();
        board.setInitialBlock(tableBlock);
        board.build();
        String tableString = board.getPreview();
        System.out.println(tableString);
    }

    private static void appendNameOfProgram(StringBuilder sb) {
        sb.append(Configuration.PROGRAM_NAME).append(NL);
    }
}
