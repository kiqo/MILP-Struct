package main.java.lp;

/**
 * Created by Verena on 19.04.2017.
 */
public class LPData {
    public int numVariables;
    public int numIntegerVariables;
    public boolean isIntegerLP;
    public double proportionIntegerVariables;
    public double minIntegerVariables; // per row
    public double maxIntegerVariables;
    public double avgIntegerVariables;
    public int numConstraints;
    public int sizeObjectiveFunction;
    public double avgVariablesConstraint;
    public double minCoefficient;
    public double maxCoefficient;
    public int numBoundVariables;
    public double minBoundValue;
    public double maxBoundValue;
}
