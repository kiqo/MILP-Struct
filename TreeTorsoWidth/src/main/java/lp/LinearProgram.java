package main.java.lp;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

/**
 * The format of ILP or MILP instances. It stores the objective function, constraints and the variables occuring
 * in which constraint or objective function.
 */
public class LinearProgram {

    private final static Logger LOGGER = LoggerFactory.getLogger(LinearProgram.class);

    public enum Equality {
        LESS_THAN(-1),
        GREATER_THAN(1),
        EQUAL(0);

        private int value;

        Equality(int value) {
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
    private Map<String, Variable> variables;
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

    public void setVariables(Map<String, Variable> variables) {
        this.variables = variables;
    }

    public Map<String, Variable> getVariables() {
        return variables;
    }

     public void printLP() {
         StringBuilder sb = new StringBuilder();
         sb.append("LinearProgram " + this.name + " (" + (integerLP ? "ILP" : "MILP") + ")" + " - Coefficients not printed" + NL);
         sb.append("Objective function " + this.getObjectiveFunction().getName() + " : ");
         for (Variable variable : this.getObjectiveFunction().getVariableEntries()) {
             sb.append(variable.getName() + " + ");
         }

         sb.append(NL + "s.t." + NL);
         for (MatrixRow row : this.getConstraints()) {
             sb.append(row.getName() + " : ");
             for (Variable variable : row.getVariableEntries()) {
                 sb.append(variable.getName() + " + ");
             }
             sb.append(" " + row.getEquality().toString() + " ");
             sb.append(row.getRightHandSide());
             sb.append(NL);
         }

         LOGGER.trace(sb.toString());
     }

}
