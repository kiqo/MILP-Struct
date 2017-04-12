package lp;

/**
 * Created by Verena on 28.03.2017.
 */
public class IntegerVariable<Integer> extends Variable {
    public IntegerVariable(String name) {
        super(name);
    }

    @Override
    public boolean isInteger() {
        return true;
    }
}
