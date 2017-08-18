package main.java.parser;

import main.java.graph.Edge;
import main.java.graph.Graph;
import main.java.graph.Node;
import main.java.lp.LinearProgram;

import java.util.List;
import java.util.Map;


/**
 * Created by Verena on 09.03.2017.
 */
public abstract class GraphGenerator {

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
}
