package graph;

import java.util.*;

/**
 * Created by Verena on 09.03.2017.
 */
public class Graph {
    private List<Node> nodes;
    private List<Edge> edges;
    private Map<Node, List<Node>> neighbourNodes;

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

    public Map<Node, List<Node>> getNeighbourNodes() {
        return neighbourNodes;
    }

    public void setNeighbourNodes(Map<Node, List<Node>> neighbourNodes) {
        this.neighbourNodes = neighbourNodes;
    }
}
