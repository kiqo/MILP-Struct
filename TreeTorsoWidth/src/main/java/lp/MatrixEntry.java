package main.java.lp;

/**
 * Created by Verena on 07.03.2017.
 */
public class MatrixEntry {

    private Variable variable;
    private double coefficient;

    public MatrixEntry (Variable variable, double value) {
        this.variable = variable;
        this.coefficient = value;
    }

    public Variable getVariable() {
        return variable;
    }

    public void setVariable(Variable variable) {
        this.variable = variable;
    }

    public double getCoefficient() {
        return coefficient;
    }
}
