package parser;

import com.sun.deploy.util.StringUtils;
import lp.*;
import sun.swing.StringUIClientPropertyKey;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

/**
 * Created by Verena on 07.03.2017.
 */
public class MILPParser {

    private final static Logger LOGGER = Logger.getLogger(MILPParser.class.getName());

    private static final int COL_0_START = 1; // usually 2, 5, 15, 25, 40 and 50
    private static final int COL_1_START = 4; //
    private static final int COL_2_START = 14;
    private static final int COL_3_START = 27;
    private static final int COL_4_START = 70;
    private static final int COL_5_START = 50;

    public LinearProgram parseMPS(String filename, boolean printLP) throws IOException {

        String[] splits = filename.split("\\.");
        if (!splits[splits.length-1].equals("mps") && !splits[splits.length-1].equals("mps\"") && !splits[splits.length-1].equals("MPS")) {
            System.out.print("parseMPS may only handle files with .mps as ending!");
            return null;
        }

        BufferedReader br = new BufferedReader(new FileReader(filename));
        LinearProgram lp = new LinearProgram();
        boolean containsDoubleVariable = false;
        boolean containsIntegerVariable = false;
        String[] lineContents;

        try {
            List<MatrixRow> constraints = new ArrayList<>();
            lp.setConstraints(constraints);
            String line = br.readLine();
            if (line.startsWith("NAME")) {
                lp.setName(line.substring(COL_1_START).trim());
            }
            Map<String, Row> rows;
            Map<String, Variable> variables = lp.getVariables();
            line = br.readLine();

            assert(line.startsWith("ROWS"));
            line = br.readLine();

            while(!line.startsWith("COLUMNS")) {

                // parse Rows
                if (line.substring(COL_0_START).startsWith("N")) {
                    // objective function
                    Row row = new Row();
                    row.setName(line.substring(COL_1_START).trim());
                    lp.setObjectiveFunction(row);
                } else {
                    // matrix row
                    MatrixRow row = new MatrixRow();
                    row.setEquality(parseEquality(line.substring(COL_0_START, COL_0_START+1)));
                    row.setName(line.substring(COL_1_START).trim());
                    constraints.add(row);
                }

                line = br.readLine();
            }
            line = br.readLine();

            // parse Columns
            rows = lp.getRows();
            boolean integerVariable = false;
            while (!line.startsWith("RHS")) {

                // checks if the following are integer variables
                if (line.substring(COL_2_START, COL_2_START+8).equals("'MARKER'")) {
                    if (line.substring(39, 47).equals("'INTORG'")) {
                        integerVariable = true;
                    }
                    if (line.substring(39, 47).equals("'INTEND'")) {
                        integerVariable = false;
                    }
                } else {
                    lineContents = getData(line);

                    Variable variable;
                    if (integerVariable) {
                        variable = new IntegerVariable(lineContents[0]);
                        containsIntegerVariable = true;
                    } else {
                        variable = new RealVariable(lineContents[0]);
                        containsDoubleVariable = true;
                    }
                    variables.put(variable.getName(), variable);

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
                line = br.readLine();
            }
            line = br.readLine();

            // parse RHS
            while (!line.startsWith("BOUNDS") && !line.startsWith("ENDATA")) {
                lineContents = getData(line);

                String rowName = lineContents[1];
                double rightHandSideValue = Double.valueOf(lineContents[2]);
                Row currentRow = rows.get(rowName);
                ((MatrixRow) currentRow).setRightHandSide(rightHandSideValue);
                line = br.readLine();
            }
            line = br.readLine();

            // parse BOUNDS (optional)
            while (line != null && !line.startsWith("ENDATA")) {
                lineContents = getData(line);

                String boundType = lineContents[0];
                String variableName = lineContents[2];
                Variable variable = variables.get(variableName);
                Number boundValue = null;
                if (lineContents.length >= 4) {
                    if (variable.isInteger()) {
                        boundValue = Integer.valueOf(lineContents[3]);
                    } else {
                        boundValue = Double.valueOf(lineContents[3]);
                    }
                } else {
                    LOGGER.warning("No upper or lower bound value for variable " + variableName);
                }

                switch (boundType) {
                    // a fixed variable has the boundvalue as upper and lower bound
                    case "FX": variable.setUpperBound(boundValue);
                               variable.setLowerBound(boundValue);break;
                    case "UP": variable.setUpperBound(boundValue); break;
                    case "LO": variable.setLowerBound(boundValue); break;
                    case "FR": break; // free variable
                    default: System.out.println("Unknown boundType " + boundType + "!");
                }
                line = br.readLine();
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            br.close();
        }

        if (containsIntegerVariable && !containsDoubleVariable) {
            lp.setIntegerLP(true);
        }

        if (printLP) {
            lp.printLP();
        }
        return lp;
    }

    private void addRowEntry(Row row, Variable<Number> variable, double coefficient) {

        if (row.getEntries() == null || row.getEntries().isEmpty()) {
            List<MatrixEntry> entries = new ArrayList<>();
            row.setEntries(entries);
        }
        row.getEntries().add(new MatrixEntry(variable, coefficient));
    }

    // returns the data without spaces
    private String[] getData(String line) {
        String[] splits = line.split(" ");
        List<String> ret = new ArrayList<>();
        for (String split : splits) {
            if (split.equals("") || split.equals(" ")) {
                continue;
            }
            ret.add(split.trim());
        }
        String[] retArr = new String[ret.size()];
        ret.toArray(retArr);
        return retArr;
    }

    private LinearProgram.Equality parseEquality(String substring) {
        switch (substring) {
            case "G" : return LinearProgram.Equality.GREATER_THAN;
            case "L" : return LinearProgram.Equality.LESS_THAN;
            case "E" : return LinearProgram.Equality.EQUAL;
        }
        return LinearProgram.Equality.EQUAL;
    }
}
