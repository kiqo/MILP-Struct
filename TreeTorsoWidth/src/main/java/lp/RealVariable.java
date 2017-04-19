package main.java.lp;

/**
 * Created by Verena on 28.03.2017.
 */
public class RealVariable<Double> extends Variable {
    public RealVariable(String name) {
        super(name);
    }

    @Override
    public boolean isInteger() {
        return false;
    }
}
