package main.java.graph;

import main.java.lp.MatrixEntry;

/**
 * Created by Verena on 09.03.2017.
 */
public class Node {
    private String name;
    private int id;
    private boolean isInteger;

    public Node() {
    }

    public Node(MatrixEntry variable) {
        this.name = variable.getVariable().getName();
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Node node = (Node) o;

        return name != null ? name.equals(node.name) : node.name == null;

    }

    @Override
    public int hashCode() {
        return name != null ? name.hashCode() : 0;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public boolean isInteger() {
        return isInteger;
    }

    public void setInteger(boolean integer) {
        isInteger = integer;
    }
}
