package main.java.parser;

import main.java.lp.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sun.rmi.runtime.Log;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by Verena on 07.03.2017.
 */
public class MILPParser {

    private final static Logger LOGGER = LoggerFactory.getLogger(MILPParser.class);

    private static final int COL_0_START = 1; // usually 2, 5, 15, 25, 40 and 50
    private static final int COL_1_START = 4;
    private static final int COL_2_START = 14;

    public LinearProgram parseMPS(String filename) throws IOException {
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

    private LinearProgram constructLP(String filename) throws IOException {
        LinearProgram lp = new LinearProgram();
        BufferedReader br = null;
        try  {
            br = new BufferedReader(new FileReader(filename));
            parseName(lp, br);
            parseRows(lp, br);
            parseColumns(lp, br);
            parseRHS(lp, br);
            parseOptionalBounds(lp, br);
            br.close();
        } catch (IOException e) {
            LOGGER.error("", e);
            br.close();
        }
        return lp;
    }

    private void parseName(LinearProgram lp, BufferedReader br) throws IOException {
        String line = br.readLine();
        if (line.startsWith("NAME")) {
            lp.setName(line.substring(COL_1_START).trim());
        }
    }

    private void parseRows(LinearProgram lp, BufferedReader br) throws IOException {
        List<MatrixRow> constraints = new ArrayList<>();

        String line = br.readLine();
        assert (line.startsWith("ROWS"));
        line = br.readLine();

        while (!line.startsWith("COLUMNS")) {
            if (line.substring(COL_0_START).startsWith("N")) {
                // objective function
                Row row = new Row();
                row.setName(line.substring(COL_1_START).trim());
                row.setEntries(new ArrayList<>());
                lp.setObjectiveFunction(row);
            } else {
                // matrix row
                MatrixRow row = new MatrixRow();
                row.setEquality(parseEquality(line.substring(COL_0_START, COL_0_START + 1)));
                row.setName(line.substring(COL_1_START).trim());
                row.setEntries(new ArrayList<>());
                constraints.add(row);
            }

            line = br.readLine();
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

    private void parseColumns(LinearProgram lp, BufferedReader br) throws IOException {
        Map<String, Variable> variables = new HashMap<>();
        lp.setVariables(variables);
        String[] lineContents;
        Variable variable;
        String line = br.readLine();
        Map<String, Row> rows = lp.getRows();
        boolean integerVariable = false;
        boolean integerLP = true;
        while (!line.startsWith("RHS")) {
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
            line = br.readLine();
        }
        lp.setIntegerLP(integerLP);
    }

    private boolean swapIntegerVariable(String line) {
        return line.substring(COL_2_START, COL_2_START + 8).equals("'MARKER'");
    }

    private void createRowEntries(String[] lineContents, Map<String, Row> rows, Variable variable) {
        String rowName = lineContents[1];
        Row currentRow = rows.get(rowName);
        double coefficient = Double.valueOf(lineContents[2]);
        addRowEntry(currentRow, variable, coefficient);

        if (lineContents.length >= 5) {
            // create for another row a matrix entry for this variable
            rowName = lineContents[3];
            currentRow = rows.get(rowName);
            coefficient = Double.valueOf(lineContents[4]);
            addRowEntry(currentRow, variable, coefficient);
        }
    }

    private void addRowEntry(Row row, Variable variable, double coefficient) {

        if (row.getEntries() == null || row.getEntries().isEmpty()) {
            List<MatrixEntry> entries = new ArrayList<>();
            row.setEntries(entries);
        }
        row.getEntries().add(new MatrixEntry(variable, coefficient));
    }

    private void parseRHS(LinearProgram lp, BufferedReader br) throws IOException {
        Map<String, Row> rows = lp.getRows();
        String line = br.readLine();
        String[] lineContents;
        while (!line.startsWith("BOUNDS") && !line.startsWith("ENDATA")) {
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

            line = br.readLine();
        }
        // only occurring in netdiversion.mps
        if (line.startsWith("RANGES")) {
            LOGGER.debug("RANGES section detected");
            br.readLine();
        }
    }

    private void parseOptionalBounds(LinearProgram lp, BufferedReader br) throws IOException {
        Map<String, Variable> variables = lp.getVariables();
        String line = br.readLine();
        String[] lineContents;

        while (line != null && !line.startsWith("ENDATA")) {
            lineContents = getDataWithoutSpaces(line);

            String boundType = lineContents[0];
            String variableName = lineContents[2];
            Variable variable = variables.get(variableName);
            Number boundValue = getBoundValue(lineContents, variableName, variable);
            setBoundValue(boundType, variable, boundValue);
            line = br.readLine();
        }
    }

    /*
    Parses the boundType according to the definitions in  http://miplib.zib.de/miplib3/mps_format.txt, section E.
    */
    private void setBoundValue(String boundType, Variable variable, Number boundValue) {
        if (boundValue != null) {
            switch (boundType) {
                // a fixed variable has the boundvalue as upper and lower bound
                case "FX":
                    variable.setUpperBound(boundValue);
                    variable.setLowerBound(boundValue);
                    break;
                case "UP":
                    variable.setUpperBound(boundValue);
                    break;
                case "LO":
                    variable.setLowerBound(boundValue);
                    break;
                case "FR":
                    break; // free variable
                case "MI":
                    variable.setLowerBound(variable.isInteger() ? Integer.MIN_VALUE : Double.MIN_VALUE);
                    break;
                case "BV":
                    // binary variable, should be integer variable
                    variable.setLowerBound(0);
                    variable.setUpperBound(1);
                    break;
                case "LI":
                    // lower bound as integer variable
                    variable.setLowerBound(boundValue);
                    break;
                default:
                    LOGGER.debug("Unknown boundType " + boundType + "!");
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
        return line.replaceFirst("\\s+", "").split("\\s+");
    }
}
