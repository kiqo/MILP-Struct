package main.java.lp;

import main.java.graph.Graph;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by Verena on 19.04.2017.
 */
public class LPStatistics {

    private static final Logger LOGGER = LoggerFactory.getLogger(LPStatistics.class);

    private LinearProgram linearProgram;
    private LPData linearProgramData;
    private Graph primalGraph;
    private GraphData primalGraphData;

    public LPStatistics(LinearProgram linearProgram) {
        this.linearProgram = linearProgram;
        computeLinearProgramData();
    }

    private void computeLinearProgramData() {
        linearProgramData = new LPData();

        linearProgramData.numVariables = linearProgram.getVariables().size();

        // variable information
        int numInteger = 0;
        int numBoundVariables = 0;
        double minBoundValue = Double.MAX_VALUE;
        double maxBoundValue = Double.MIN_VALUE;
        for (Variable var : linearProgram.getVariables().values()) {
            if (var.isInteger()) {
                numInteger++;
            }
            if (var.getLowerBound() != null && var.getUpperBound() != null) {
                numBoundVariables++;
            }
            if (var.getLowerBound() != null) {
                if (var.isInteger() && (int)var.getLowerBound() < minBoundValue) {
                    minBoundValue = (int) var.getLowerBound();
                } else if (!var.isInteger() && (double)var.getLowerBound() < minBoundValue) {
                    minBoundValue = (double) var.getLowerBound();
                }
            }
            if (var.getUpperBound() != null) {
                if (var.isInteger() && (int)var.getUpperBound() > maxBoundValue) {
                    maxBoundValue = (int) var.getUpperBound();
                } else if (!var.isInteger() && (double)var.getUpperBound() > maxBoundValue) {
                    maxBoundValue = (double) var.getUpperBound();
                }
            }
        }
        linearProgramData.numIntegerVariables = numInteger;
        linearProgramData.isIntegerLP = (numInteger == linearProgramData.numVariables);
        linearProgramData.proportionIntegerVariables = (double) numInteger / (double) linearProgramData.numVariables;
        linearProgramData.numBoundVariables = numBoundVariables;
        linearProgramData.minBoundValue = minBoundValue;
        linearProgramData.maxBoundValue = maxBoundValue;

        // matrix information
        linearProgramData.numConstraints = linearProgram.getConstraints().size();

        int numVariablesTotal = 0;
        int numIntegerVariablesTotal = 0;
        int minNumInteger = Integer.MAX_VALUE;
        int maxNumInteger = Integer.MIN_VALUE;
        double minCoefficient = Double.MAX_VALUE;
        double maxCoefficient = Double.MIN_VALUE;
        for (MatrixRow matrixRow : linearProgram.getConstraints()) {

            int numIntegerInRow = 0;
            for (MatrixEntry matrixEntry : matrixRow.getEntries()) {
                numVariablesTotal++;
                if (matrixEntry.getVariable().isInteger()) {
                    numIntegerInRow++;
                    numIntegerVariablesTotal++;
                }

                if (matrixEntry.getCoefficient() < minCoefficient) {
                    minCoefficient = matrixEntry.getCoefficient();
                }
                if (matrixEntry.getCoefficient() > maxCoefficient) {
                    maxCoefficient = matrixEntry.getCoefficient();
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
        linearProgramData.avgVariablesConstraint = numVariablesTotal / linearProgramData.numConstraints;
        linearProgramData.minCoefficient = minCoefficient;
        linearProgramData.maxCoefficient = maxCoefficient;
        linearProgramData.sizeObjectiveFunction = linearProgram.getObjectiveFunction().getEntries().size();
    }
}
