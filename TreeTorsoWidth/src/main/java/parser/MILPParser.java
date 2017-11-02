package main.java.parser;

import main.java.lp.*;
import main.java.main.ThreadExecutor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.*;

/**
 * Parses a (M)ILP instance in the MPS format. Note that some fields in the MPS file format,
 * like the RANGES or BOUNDS section are not taken on in the LinearProgram.
 */
public class MILPParser extends ThreadExecutor {

    private final static Logger LOGGER = LoggerFactory.getLogger(MILPParser.class);

    private static final int COL_0_START = 1; // usually 2, 5, 15, 25, 40 and 50
    private static final int COL_1_START = 4;
    private static final int COL_2_START = 14;

    public LinearProgram parseMPS(String filename) throws IOException, InterruptedException {
        checkCorrectFileFormat(filename);
        LinearProgram lp = constructLP(filename);
        computeLPStatistics(lp);
        return lp;
    }

    private void checkCorrectFileFormat(String filename) throws IOException {
        String[] splits = filename.split("\\.");
        if (!splits[splits.length - 1].equals("mps") && !splits[splits.length - 1].equals("mps\"") && !splits[splits.length - 1].equals("MPS")) {
            throw new IOException("parseMPS may only handle files with .mps as ending!");
        }
    }

    private LinearProgram constructLP(String filename) throws IOException, InterruptedException {
        LinearProgram lp = new LinearProgram();
        FileInputStream inputStream;
        Scanner sc = null;
        try  {
            inputStream = new FileInputStream(filename);
            sc = new Scanner(inputStream, "UTF-8");
            parseName(lp, sc);
            parseRows(lp, sc);
            parseColumns(lp, sc);
            parseRHS(lp, sc);
            parseOptionalBounds(lp, sc);
            sc.close();
        } catch (IOException e) {
            LOGGER.error("", e);
            sc.close();
        }
        return lp;
    }

    private void parseName(LinearProgram lp, Scanner sc) throws IOException {
        String line = sc.nextLine();
        if (line.startsWith("NAME")) {
            lp.setName(line.substring(COL_1_START).trim());
        }
    }

    private void parseRows(LinearProgram lp, Scanner sc) throws IOException, InterruptedException {
        List<MatrixRow> constraints = new ArrayList<>();

        String line = sc.nextLine();
        assert (line.startsWith("ROWS"));
        line = sc.nextLine();

        int iteration = 0;
        while (!line.startsWith("COLUMNS")) {
            if (iteration++ % 10 == 0) {
                checkInterrupted();
            }
            if (line.substring(COL_0_START).startsWith("N")) {
                // objective function
                Row row = new Row();
                row.setName(line.substring(COL_1_START).trim());
                row.setVariableEntries(new ArrayList<>());
                lp.setObjectiveFunction(row);
            } else {
                // matrix row
                MatrixRow row = new MatrixRow();
                row.setEquality(parseEquality(line.substring(COL_0_START, COL_0_START + 1)));
                row.setName(line.substring(COL_1_START).trim());
                row.setVariableEntries(new ArrayList<>());
                constraints.add(row);
            }

            line = sc.nextLine();
        }
        lp.setConstraints(constraints);
    }

    private LinearProgram.Equality parseEquality(String substring) {
        switch (substring) {
            case "G":
                return LinearProgram.Equality.GREATER_THAN;
            case "L":
                return LinearProgram.Equality.LESS_THAN;
            case "E":
                return LinearProgram.Equality.EQUAL;
        }
        return LinearProgram.Equality.EQUAL;
    }

    private void parseColumns(LinearProgram lp, Scanner sc) throws IOException, InterruptedException {
        Map<String, Variable> variables = new HashMap<>();
        lp.setVariables(variables);
        String[] lineContents;
        Variable variable;
        String line = sc.nextLine();
        Map<String, Row> rows = lp.getRows();
        boolean integerVariable = false;
        boolean integerLP = true;
        int iteration = 0;
        while (!line.startsWith("RHS")) {
            if (iteration++ % 10 == 0) {
                checkInterrupted();
            }
            // checks if the following are integer variables
            if (swapIntegerVariable(line)) {
                if (line.substring(39, 47).equals("'INTORG'")) {
                    integerVariable = true;
                }
                if (line.substring(39, 47).equals("'INTEND'")) {
                    integerVariable = false;
                }
            } else {
                lineContents = getDataWithoutSpaces(line);
                if (integerVariable) {
                    variable = new IntegerVariable(lineContents[0]);
                } else {
                    variable = new RealVariable(lineContents[0]);
                    integerLP = false;
                }
                variables.put(variable.getName(), variable);
                createRowEntries(lineContents, rows, variable);
            }
            line = sc.nextLine();
        }
        lp.setIntegerLP(integerLP);
    }

    private boolean swapIntegerVariable(String line) {
        return line.substring(COL_2_START, COL_2_START + 8).equals("'MARKER'");
    }

    private void createRowEntries(String[] lineContents, Map<String, Row> rows, Variable variable) {
        String rowName = lineContents[1];
        Row currentRow = rows.get(rowName);
        // double coefficient = Double.valueOf(lineContents[2]);
        addRowEntry(currentRow, variable);

        if (lineContents.length >= 5) {
            // create for another row a matrix entry for this variable
            rowName = lineContents[3];
            currentRow = rows.get(rowName);
            // coefficient = Double.valueOf(lineContents[4]);
            addRowEntry(currentRow, variable);
        }
    }

    private void addRowEntry(Row row, Variable variable) {

        if (row.getVariableEntries() == null || row.getVariableEntries().isEmpty()) {
            List<Variable> entries = new ArrayList<>();
            row.setVariableEntries(entries);
        }
        row.getVariableEntries().add(variable);
    }

    private void parseRHS(LinearProgram lp, Scanner sc) throws IOException {
        Map<String, Row> rows = lp.getRows();
        String line = sc.nextLine();
        String[] lineContents;
        while (!line.startsWith("BOUNDS") && !line.startsWith("ENDATA")) {
            // netdiversion.mps has an empty ranges section
            if (line.startsWith("RANGES")) {
               line = sc.nextLine();
               continue;
            }

            lineContents = getDataWithoutSpaces(line);

            String rowName = lineContents[1];
            double rightHandSideValue = Double.valueOf(lineContents[2]);
            Row currentRow = rows.get(rowName);
            ((MatrixRow) currentRow).setRightHandSide(rightHandSideValue);

            if (lineContents.length >= 5) {
                // parse for another constraint the rhs
                rowName = lineContents[3];
                rightHandSideValue = Double.valueOf(lineContents[2]);
                currentRow = rows.get(rowName);
                ((MatrixRow) currentRow).setRightHandSide(rightHandSideValue);
            }

            line = sc.nextLine();
        }
        // only occurring in netdiversion.mps
        if (line.startsWith("RANGES")) {
            LOGGER.debug("RANGES section detected");
            sc.nextLine();
        }
    }

    private void parseOptionalBounds(LinearProgram lp, Scanner sc) throws IOException {
        if (sc.hasNext()) {
            String line = sc.nextLine();
            while (line != null && !line.startsWith("ENDATA")) {
                // Currently do nothing with the bounds
                line = sc.nextLine();
            }
        }
    }

    private Number getBoundValue(String[] lineContents, String variableName, Variable variable) {
        Number boundValue = null;
        if (lineContents.length >= 4) {
            if (variable.isInteger()) {
                // needed such that 1.0000 does not throw a NumberFormatException
                double value = Double.valueOf(lineContents[3]);
                boundValue = (int) value;
            } else {
                boundValue = Double.valueOf(lineContents[3]);
            }
        } else {
            LOGGER.trace("No upper or lower bound value for variable " + variableName);
        }
        return boundValue;
    }

    private void computeLPStatistics(LinearProgram lp) {
        LPStatistics statistics = new LPStatistics(lp);
        lp.setStatistics(statistics);
    }

    private String[] getDataWithoutSpaces(String line) {
        if (line.startsWith(" ") || line.startsWith("\t")) {
            line = line.replaceFirst("\\s+", "");
        }
        return line.split("\\s+");
    }
}
