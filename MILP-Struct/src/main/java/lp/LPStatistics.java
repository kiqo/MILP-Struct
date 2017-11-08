package main.java.lp;

import main.java.main.Configuration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;

/**
 * Created by Verena on 19.04.2017.
 */
public class LPStatistics implements Serializable {

    private static final Logger LOGGER = LoggerFactory.getLogger(LPStatistics.class);

    private transient LinearProgram linearProgram;
    private LPData linearProgramData;

    public LinearProgram getLinearProgram() {
        return linearProgram;
    }

    public LPData getLinearProgramData() {
        return linearProgramData;
    }

    public LPStatistics(LinearProgram linearProgram) {
        this.linearProgram = linearProgram;
        computeLinearProgramData();
    }

    private void computeLinearProgramData() {
        linearProgramData = new LPData();
        computeVariableInformation();
        computeMatrixInformation();
    }

    private void computeMatrixInformation() {
        linearProgramData.numConstraints = linearProgram.getConstraints().size();

        int numVariablesTotal = 0;
        int numIntegerVariablesTotal = 0;
        int minNumInteger = Integer.MAX_VALUE;
        int maxNumInteger = Integer.MIN_VALUE;
        Collection<Row> rows;
        if (Configuration.OBJ_FUNCTION) {
            // objective function is considered like a row in the matrix
            rows = linearProgram.getRows().values();
        } else {
            // objective function just ignored
            rows = new ArrayList<>(linearProgram.getConstraints());
        }
        for (Row matrixRow : rows) {

            int numIntegerInRow = 0;
            for (Variable matrixEntry : matrixRow.getVariableEntries()) {
                numVariablesTotal++;
                if (matrixEntry.isInteger()) {
                    numIntegerInRow++;
                    numIntegerVariablesTotal++;
                }
            }

            if (numIntegerInRow < minNumInteger) {
                minNumInteger = numIntegerInRow;
            }
            if (numIntegerInRow > maxNumInteger) {
                maxNumInteger = numIntegerInRow;
            }
        }
        linearProgramData.minIntegerVariables = minNumInteger; // per row
        linearProgramData.maxIntegerVariables = maxNumInteger;
        linearProgramData.avgIntegerVariables = numIntegerVariablesTotal / linearProgramData.numConstraints;
        linearProgramData.avgVariables = numVariablesTotal / linearProgramData.numConstraints;
        linearProgramData.sizeObjectiveFunction = linearProgram.getObjectiveFunction().getVariableEntries().size();
    }

    private void computeVariableInformation() {
        linearProgramData.numVariables = linearProgram.getVariables().size();

        // variable information
        int numInteger = 0;
        for (Variable var : linearProgram.getVariables().values()) {
            if (var.isInteger()) {
                numInteger++;
            }
        }
        linearProgramData.numIntegerVariables = numInteger;
        linearProgramData.isIntegerLP = (numInteger == linearProgramData.numVariables);
        linearProgramData.proportionIntegerVariables = (double) numInteger / (double) linearProgramData.numVariables;
    }
}
