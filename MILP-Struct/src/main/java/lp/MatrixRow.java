package main.java.lp;

/**
 * Created by Verena on 07.03.2017.
 */
public class MatrixRow extends Row {

    private LinearProgram.Equality equality;
    private double rightHandSide;

    public LinearProgram.Equality getEquality() {
        return equality;
    }

    public void setEquality(LinearProgram.Equality equality) {
        this.equality = equality;
    }

    public double getRightHandSide() {
        return rightHandSide;
    }

    public void setRightHandSide(double rightHandSide) {
        this.rightHandSide = rightHandSide;
    }
}
