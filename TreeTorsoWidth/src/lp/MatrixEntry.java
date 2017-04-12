package lp;

/**
 * Created by Verena on 07.03.2017.
 */
public class MatrixEntry {

    private Variable variable;
    private double value;

    public MatrixEntry (Variable variable, double value) {
        this.variable = variable;
        this.value = value;
    }

    public Variable getVariable() {
        return variable;
    }

    public void setVariable(Variable variable) {
        this.variable = variable;
    }

    public double getValue() {
        return value;
    }

    public void setValue(double value) {
        this.value = value;
    }
}
