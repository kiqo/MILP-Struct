package main.java.lp;

import java.io.Serializable;

/**
 * Used for storing parameters of the (M)ILP
 */
public class LPData implements Serializable {
    public int numVariables;
    public int numIntegerVariables;
    public boolean isIntegerLP;
    public double proportionIntegerVariables;
    public int minIntegerVariables; // per row
    public int maxIntegerVariables;
    public double avgIntegerVariables;
    public int numConstraints;
    public int sizeObjectiveFunction;
    public double avgVariables;
}
