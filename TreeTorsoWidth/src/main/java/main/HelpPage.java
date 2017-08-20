package main.java.main;

/**
 * Created by Verena on 20.08.2017.
 */
public class HelpPage {
    private static final String NL = System.getProperty("line.separator");

    private static final String helpMessage = "Usage: TreeTorsoWidth [--help] <inputFile(.mps|.txt)> [-o <outputFile(.txt|.csv)>] " +
            "(--lb|--ub|--to|--td) -g (<primal>|<incidence>|<dual>) [--obj]" + NL +
            "See TreeTorsoWidth --help for more information";

    public static String getShortHelpMessage() {
        return helpMessage;
    }

    public static String getLongHelpMessage() {
        return helpMessage;
    }
}
