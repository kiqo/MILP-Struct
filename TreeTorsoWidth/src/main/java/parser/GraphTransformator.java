package main.java.parser;

import main.java.graph.Graph;
import main.java.libtw.LPGraphInput;
import nl.uu.cs.treewidth.input.GraphInput;
import nl.uu.cs.treewidth.input.InputException;
import nl.uu.cs.treewidth.ngraph.NGraph;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by Verena on 09.03.2017.
 */
public class GraphTransformator {

    private static final Logger LOGGER = LoggerFactory.getLogger(GraphTransformator.class);

    public NGraph<GraphInput.InputData> graphToNGraph(Graph primalGraph) {
        NGraph<GraphInput.InputData> g = null;
        GraphInput input = new LPGraphInput(primalGraph);
        try {
            g = input.get();
        } catch( InputException e ) {
            LOGGER.error("", e);
        }
        return g;
    }
}
