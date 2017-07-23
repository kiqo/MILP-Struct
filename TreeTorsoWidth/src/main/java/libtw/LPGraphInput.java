package main.java.libtw;

import main.java.graph.Graph;
import main.java.graph.Node;
import nl.uu.cs.treewidth.input.GraphInput;
import nl.uu.cs.treewidth.input.InputException;
import nl.uu.cs.treewidth.ngraph.ListGraph;
import nl.uu.cs.treewidth.ngraph.ListVertex;
import nl.uu.cs.treewidth.ngraph.NGraph;
import nl.uu.cs.treewidth.ngraph.NVertex;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

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



        // find components of the graph TODO test
        g.setComponents(new ArrayList<>());
        int verticesFound = 0;
        while (verticesFound != graph.getNodes().size()) {
            for (NVertex<InputData> vertex : g) {
                boolean vertexFound = false;
                for (NGraph<InputData> subGraph: g.getComponents()) {
                    for (NVertex nVertex : subGraph) {
                        if (nVertex.equals(vertex)) {
                            vertexFound = true;
                        }
                    }
                }

                if (!vertexFound) {
                    ArrayList<NVertex<InputData>> handledVertices = new ArrayList<>();
                    DFSTree(vertex, handledVertices);
                    verticesFound += handledVertices.size();

                    // create component graph
                    NGraph<InputData> gSub = new ListGraph<>();
                    ((ListGraph) gSub).vertices = handledVertices;
                    g.getComponents().add(gSub);
                }
            }
        }

        // create edges for NGraph
        for (Map.Entry<String, List<Node>> nodeNeighboursPair : graph.getNeighbourNodes().entrySet()) {

            NVertex<InputData> v1, v2;
            String curNodeName = nodeNeighboursPair.getKey();

            v1 = vertices.get(curNodeName);

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

    /*
     * Input is the root node, returns distance from the lowest descendant of the rootNode, i.e. the current
     * height of the tree
     */
    private static int DFSTree(NVertex rootNode, List<NVertex<InputData>> handledVertices) {

        handledVertices.add(rootNode);
        // System.out.print(rootNode.data.name + " ");

        int height = 0;
        for (Iterator<NVertex> it = rootNode.getNeighbors(); it.hasNext(); ) {
            NVertex neighbor = it.next();
            if (!handledVertices.contains(neighbor)) {
                int heightSubtree = DFSTree(neighbor, handledVertices);
                if (heightSubtree > height) {
                    height = heightSubtree;
                }
            }
        }
        return ++height;
    }
}
