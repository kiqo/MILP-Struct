package main.java.algo;

import nl.uu.cs.treewidth.input.GraphInput;

/**
 * Created by Verena on 03.04.2017.
 */
public class LPInputData extends GraphInput.InputData {

    public LPInputData( int id, String name, boolean isInteger) {
        super( id, name );
        this.isInteger = isInteger;
    }

    private boolean isInteger = false;
    private boolean nodeHandled = false;

    public boolean isNodeHandled() {
        return nodeHandled;
    }

    public void setNodeHandled(boolean nodeHandled) {
        this.nodeHandled = nodeHandled;
    }

    public boolean isInteger() { return this.isInteger; }

    public void setInteger(boolean integer) {
        isInteger = integer;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        LPInputData that = (LPInputData) o;

        if (isInteger != that.isInteger) return false;
        if (nodeHandled != that.nodeHandled) return false;
        if (super.id != that.id) return false;
        return super.name.equals(that.name);
    }

    @Override
    public int hashCode() {
        int result = (isInteger ? 1 : 0);
        result = 31 * result + (nodeHandled ? 1 : 0);
        return result;
    }
}
