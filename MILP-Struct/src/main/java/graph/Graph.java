package main.java.graph;

import java.util.*;

/**
 * The graph class used for constructing the primal, incidence and dual graph representation of MILP instances.
 */
public class Graph {
    private List<Node> nodes;
    private List<Edge> edges;
    private Map<String, List<Node>> neighbourNodes;

    public List<Edge> getEdges() {
        return edges;
    }

    public void setEdges(List<Edge> edges) {
        this.edges = edges;
    }

    public List<Node> getNodes() {
        return nodes;
    }

    public void setNodes(List<Node> nodes) {
        this.nodes = nodes;
    }

    public Map<String, List<Node>> getNeighbourNodes() {
        return neighbourNodes;
    }

    public void setNeighbourNodes(Map<String, List<Node>> neighbourNodes) {
        this.neighbourNodes = neighbourNodes;
    }
}
