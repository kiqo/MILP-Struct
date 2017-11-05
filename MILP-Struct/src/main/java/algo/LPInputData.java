package main.java.algo;

import nl.uu.cs.treewidth.input.GraphInput;

/**
 * LPInputData is used as additional vertex data for vertices in graph representations of (M)ILP instances
 *
 * It is used for storing whether a certain vertex corresponds to an integer variable or non-integer variable in a
 * MILP instance.
 */
public class LPInputData extends GraphInput.InputData {

    private boolean isInteger = false;
    private boolean nodeHandled = false;

    public LPInputData( int id, String name, boolean isInteger) {
        super( id, name );
        this.isInteger = isInteger;
    }

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
