package main.java.libtw;

import nl.uu.cs.treewidth.input.GraphInput;
import nl.uu.cs.treewidth.ngraph.NVertex;

import java.util.ArrayList;
import java.util.List;

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
}
