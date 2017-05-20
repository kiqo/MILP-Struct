package main.java.libtw;

import nl.uu.cs.treewidth.input.GraphInput;

/**
 * Created by Verena on 03.04.2017.
 */
public class LPInputData extends GraphInput.InputData {

    private boolean isInteger = false;

    public boolean isNodeHandled() {
        return nodeHandled;
    }

    public void setNodeHandled(boolean nodeHandled) {
        this.nodeHandled = nodeHandled;
    }

    private boolean nodeHandled = false;

    public LPInputData( int id, String name, boolean isInteger) {
        super( id, name );
        this.isInteger = isInteger;
    }
    public boolean isInteger() { return this.isInteger; }

    public void setInteger(boolean integer) {
        isInteger = integer;
    }
}
