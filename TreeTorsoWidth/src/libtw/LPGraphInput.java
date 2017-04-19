package libtw;

import graph.Graph;
import graph.Node;
import nl.uu.cs.treewidth.input.GraphInput;
import nl.uu.cs.treewidth.input.InputException;
import nl.uu.cs.treewidth.ngraph.ListGraph;
import nl.uu.cs.treewidth.ngraph.ListVertex;
import nl.uu.cs.treewidth.ngraph.NGraph;
import nl.uu.cs.treewidth.ngraph.NVertex;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Hashtable;
import java.util.List;
import java.util.Map;

/**
 * Created by Verena on 09.03.2017.
 */
public class LPGraphInput implements GraphInput {

    private static final Logger LOGGER = LoggerFactory.getLogger(LPGraphInput.class);

    private Graph graph;

    public LPGraphInput(Graph graph) {
        this.graph = graph;
    }

    @Override
    public NGraph<InputData> get() throws InputException {
        NGraph<InputData> g = new ListGraph<>();

        Hashtable<String, NVertex<InputData>> vertices = new Hashtable<>();
        NVertex<LPInputData> vertexPrototype = new ListVertex<>();

        // create vertices for NGraph
        for (Node node : graph.getNodes()) {
            if( !vertices.containsKey(node.getName()) ) {
                //If there vertex isn't created yet, create it where InputData as additional data for a vertex (id, name)
                NVertex<InputData> v = vertexPrototype.newOfSameType(new LPInputData(node.getId(), node.getName(), node.isInteger()));
                vertices.put(node.getName(), v);
                g.addVertex(v);
            }
        }

        // create edges for NGraph
        for (Map.Entry<Node, List<Node>> nodeNeighboursPair : graph.getNeighbourNodes().entrySet()) {

            NVertex<InputData> v1, v2;
            Node curNode = nodeNeighboursPair.getKey();

            v1 = vertices.get(curNode.getName());

            for (Node neighbour : nodeNeighboursPair.getValue()) {
                v2 = vertices.get(neighbour.getName());

                boolean edgeExists = v1.isNeighbor(v2);
                if (edgeExists && !v2.isNeighbor(v1)) {
                    LOGGER.error("Directed edge found for node " + v1.data.name);
                }

                if (!edgeExists) {
                    // add (undirected) edge, i.e. add v1 as neighbour of v2 and the other way around
                    g.addEdge(v1, v2);
                }
            }
        }

        return g;
    }
}
