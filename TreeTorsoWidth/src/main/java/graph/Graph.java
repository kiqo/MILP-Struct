package main.java.graph;

import java.util.*;

/**
 * Created by Verena on 09.03.2017.
 */
public class Graph {
    private List<Node> nodes;
    private List<Edge> edges;
    private Map<String, List<Node>> neighbourNodes;
    private Map<String, Node> nodesMap; // TODO construct only locally?
    private boolean connected;

    public boolean isConnected() {
        return connected;
    }

    public void setConnected(boolean connected) {
        this.connected = connected;
    }

    public Map<String, Node> getNodesMap() {
        return nodesMap;
    }

    public void setNodesMap(Map<String, Node> nodesMap) {
        this.nodesMap = nodesMap;
    }

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
