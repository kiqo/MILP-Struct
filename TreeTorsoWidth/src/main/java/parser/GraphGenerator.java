package main.java.parser;

import main.java.main.Configuration;
import main.java.graph.Edge;
import main.java.graph.Graph;
import main.java.graph.Node;
import main.java.lp.LinearProgram;
import main.java.lp.Row;

import java.util.ArrayList;
import java.util.Collection;
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

    Collection<Row> getRows(LinearProgram lp) {
        Collection<Row> rows;
        if (Configuration.OBJ_FUNCTION) {
            // objective function is considered like a row in the matrix
            rows = lp.getRows().values();
        } else {
            // objective function just ignored
            rows = new ArrayList<>(lp.getConstraints());
        }
        return rows;
    }
}
