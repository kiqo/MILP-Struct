package parser;

import graph.Graph;
import libtw.LPGraphInput;
import nl.uu.cs.treewidth.input.GraphInput;
import nl.uu.cs.treewidth.input.InputException;
import nl.uu.cs.treewidth.ngraph.NGraph;

/**
 * Created by Verena on 09.03.2017.
 */
public class GraphTransformator {

    public NGraph<GraphInput.InputData> graphToNGraph(Graph primalGraph) {
        NGraph<GraphInput.InputData> g = null;
        GraphInput input = new LPGraphInput(primalGraph);
        try {
            g = input.get();
        } catch( InputException e ) {
            e.printStackTrace();
        }
        return g;
    }
}
