package main.java.parser;

import main.java.graph.Graph;
import main.java.algo.LPGraphInput;
import nl.uu.cs.treewidth.input.GraphInput;
import nl.uu.cs.treewidth.input.InputException;
import nl.uu.cs.treewidth.ngraph.NGraph;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Transforms the graph format 'Graph' to the graph format used in LibTW 'NGraph'
 */
public class GraphTransformator {

    private static final Logger LOGGER = LoggerFactory.getLogger(GraphTransformator.class);

    public static NGraph<GraphInput.InputData> graphToNGraph(Graph graph) {
        NGraph<GraphInput.InputData> g = null;
        GraphInput input = new LPGraphInput(graph);
        try {
            g = input.get();
        } catch( InputException e ) {
            LOGGER.error("", e);
        }
        return g;
    }
}
