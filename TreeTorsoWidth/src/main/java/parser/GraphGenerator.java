package main.java.parser;

import main.java.lp.GraphData;
import main.java.main.Configuration;
import main.java.graph.Edge;
import main.java.graph.Graph;
import main.java.graph.Node;
import main.java.lp.LinearProgram;
import main.java.lp.Row;

import java.util.*;


/**
 * Created by Verena on 09.03.2017.
 */
public abstract class GraphGenerator {
    protected static int vertexId = 0;
    protected List<Node> nodes = new ArrayList<>();
    protected List<Edge> edges = new ArrayList<>();
    protected Map<String, List<Node>> neighbourNodes = new HashMap<>();

    public abstract Graph linearProgramToGraph(LinearProgram lp) throws InterruptedException;

    void checkInterrupted() throws InterruptedException {
        if (Thread.currentThread().isInterrupted()) {
            throw new InterruptedException();
        }
    }

    Graph createGraph(List<Node> nodes, List<Edge> edges, Map<String, List<Node>> neighbourNodes) {
        Graph incidenceGraph = new Graph();
        incidenceGraph.setEdges(edges);
        incidenceGraph.setNeighbourNodes(neighbourNodes);
        incidenceGraph.setNodes(nodes);
        return incidenceGraph;
    }

    List<Row> getRows(LinearProgram lp) {
        ArrayList rows;
        if (Configuration.OBJ_FUNCTION) {
            // objective function is considered like a row in the matrix
            rows = new ArrayList(lp.getRows().values());
        } else {
            // objective function just ignored
            rows = new ArrayList<>(lp.getConstraints());
        }
        return rows;
    }

    protected Node generateNodeIfNotExists(String nodeName) {
        Node node =  new Node(nodeName, vertexId++);
        if (!nodes.contains(node)) {
            nodes.add(node);
            neighbourNodes.put(node.getName(), new ArrayList<>());
        }
        return node;
    }

    protected void generateEdge(Node node1, Node node2) {
        Edge edge = new Edge(node1, node2);
        edges.add(edge);
    }

    protected void createNeighbours(Node node1, Node node2) {
        addNeighbour(node2, node1);
        addNeighbour(node1, node2);
    }

    private void addNeighbour(Node node, Node neighbourNode) {
        if (!neighbourNodes.containsKey(neighbourNode.getName())) {
            List<Node> neighboursVariableNode = new ArrayList<>();
            neighboursVariableNode.add(node);
            neighbourNodes.put(neighbourNode.getName(), neighboursVariableNode);
        } else {
            neighbourNodes.get(neighbourNode.getName()).add(node);
        }
    }
}
