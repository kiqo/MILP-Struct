package main.java.lp;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by Verena on 07.03.2017.
 */
public class LinearProgram {

    private final static Logger LOGGER = LoggerFactory.getLogger(LinearProgram.class);

    public enum Equality {
        LESS_THAN(-1),
        GREATER_THAN(1),
        EQUAL(0);

        private int value;

        private Equality(int value) {
            this.value = value;
        }

        public int getValue() {
            return value;
        }

        @Override
        public String toString() {
            switch (this.value) {
                case -1: return "<=";
                case 1: return ">=";
                case 0: return "=";
                default: return "?";
            }
        }
    };

    private String name;
    private Row objectiveFunction;
    private List<MatrixRow> constraints;
    private Map<String, Variable> variables = new HashMap<>();
    private boolean integerLP = false;
    private LPStatistics statistics;
    private static final String NL = System.getProperty("line.separator");

    public LPStatistics getStatistics() {
        return statistics;
    }

    public void setStatistics(LPStatistics statistics) {
        this.statistics = statistics;
    }

    public boolean isIntegerLP() {
        return integerLP;
    }

    public void setIntegerLP(boolean integerLP) {
        this.integerLP = integerLP;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Row getObjectiveFunction() {
        return objectiveFunction;
    }

    public void setObjectiveFunction(Row objectiveFunction) {
        this.objectiveFunction = objectiveFunction;
    }

    public List<MatrixRow> getConstraints() {
        return constraints;
    }

    public void setConstraints(List<MatrixRow> constraints) {
        this.constraints = constraints;
    }

    public Map<String, Row> getRows() {
        Map<String, Row> rows = new HashMap<>();
        if (objectiveFunction != null) {
            rows.put(objectiveFunction.getName(), objectiveFunction);
        }
        for (Row constraint : constraints) {
            rows.put(constraint.getName(), constraint);
        }
        return rows;
    }

    public Map<String, Variable> getVariables() {
        return variables;
    }

     public void printLP() {
         StringBuilder sb = new StringBuilder();
         sb.append("LinearProgram " + this.name + " (" + (integerLP ? "ILP" : "MILP") + ")" + NL);
         sb.append("Objective function " + this.getObjectiveFunction().getName() + " : ");
         for (MatrixEntry matrixEntry : this.getObjectiveFunction().getEntries()) {
             sb.append(matrixEntry.getCoefficient() + " " + matrixEntry.getVariable().getName() + " + ");
         }

         sb.append(NL + "s.t." + NL);
         for (MatrixRow row : this.getConstraints()) {
             sb.append(row.getName() + " : ");
             for (MatrixEntry matrixEntry : row.getEntries()) {
                 sb.append(matrixEntry.getCoefficient() + " " + matrixEntry.getVariable().getName() + " + ");
             }
             sb.append(" " + row.getEquality().toString() + " ");
             sb.append(row.getRightHandSide());
             sb.append(NL);
         }

         LOGGER.trace(sb.toString());
     }

}
