package main.java.graph;

/**
 * An undirected edge consisting of two nodes
 */
public class Edge {
    private Node node1;
    private Node node2;

    public Edge(Node node1, Node node2) {
        this.node1 = node1;
        this.node2 = node2;
    }

    public Node getNode1() {
        return node1;
    }

    public Node getNode2() {
        return node2;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (!(obj instanceof Edge))
            return false;
        if (obj == this)
            return true;
        if ((this.getNode1().equals(((Edge) obj).getNode1()) && this.getNode2().equals(((Edge) obj).getNode2())) ||
            (this.getNode1().equals(((Edge) obj).getNode2()) && this.getNode2().equals(((Edge) obj).getNode1()))) {
            return true;
        }
        return false;
    }

    @Override
    public int hashCode() {
        int hash = 17;
        hash = 7 * hash + this.node1.hashCode() + this.node2.hashCode();
        return hash;
    }
}
