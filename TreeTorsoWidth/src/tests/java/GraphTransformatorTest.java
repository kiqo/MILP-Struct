package tests.java;

import main.java.graph.Edge;
import main.java.graph.Graph;
import main.java.graph.Node;
import main.java.libtw.LPInputData;
import main.java.parser.GraphTransformator;
import nl.uu.cs.treewidth.input.GraphInput;
import nl.uu.cs.treewidth.ngraph.ListVertex;
import nl.uu.cs.treewidth.ngraph.NGraph;
import nl.uu.cs.treewidth.ngraph.NVertex;
import org.junit.Assert;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

/**
 * Created by Verena on 18.08.2017.
 */
public class GraphTransformatorTest extends GraphTest{

    private static final Logger LOGGER = LoggerFactory.getLogger(GraphTransformatorTest.class);

    private static final boolean SHOW_GRAPH = false;
    private static final boolean PRINT_GRAPH = false;
    private static final boolean PRINT_RESULTS = false;


    @Test
    public void testNodeBlockerGraph() throws InterruptedException {
        Graph nodeBlockerGraph = createNodeBlockerGraph();

        NGraph result = graphToNGraph(nodeBlockerGraph);

        assertSameGraph(nodeBlockerGraph, result);
        assertOneComponent(result);
    }

    @Test
    public void testStarShapedGraph() throws InterruptedException {
        Graph starShapedGraph = createStarShapedGraph();

        NGraph result = graphToNGraph(starShapedGraph);

        assertSameGraph(starShapedGraph, result);
        assertOneComponent(result);
    }

    private void assertOneComponent(NGraph result) {
        Assert.assertEquals(1, result.getComponents().size(), 0);
        List<NGraph> components = result.getComponents();
        assertSameNGraph(result, components.get(0));
    }

    private void assertSameNGraph(NGraph<GraphInput.InputData> graph1, NGraph<GraphInput.InputData> graph2) {
        Assert.assertEquals(graph1.getNumberOfVertices(), graph2.getNumberOfVertices());
        Assert.assertEquals(graph1.getNumberOfEdges(), graph2.getNumberOfEdges());
        boolean found = false;
        for (NVertex<GraphInput.InputData> vertex1 : graph1) {
            for (NVertex<GraphInput.InputData> vertex2 : graph2) {
                if (vertex1.equals(vertex2)) {
                    found = true;
                }
            }
        }
        Assert.assertTrue(found);
    }

    private void assertSameGraph(Graph graph, NGraph<GraphInput.InputData> nGraph) {
        assertCorrectVertices(graph, nGraph);
        assertCorrectEdges(graph, nGraph);
    }

    private void assertCorrectEdges(Graph graph, NGraph<GraphInput.InputData> nGraph) {
        Assert.assertEquals(graph.getEdges().size(), nGraph.getNumberOfEdges());
        for (Edge edge : graph.getEdges()) {
            NVertex<GraphInput.InputData> vertex1 = null;
            NVertex<GraphInput.InputData> vertex2 = null;
            for (NVertex<GraphInput.InputData> vertex : nGraph) {
                if (edge.getNode1().getName().equals(vertex.data.name)) {
                    vertex1 = vertex;
                }
                if (edge.getNode2().getName().equals(vertex.data.name)) {
                    vertex2 = vertex;
                }
            }
            Assert.assertNotNull(vertex1);
            Assert.assertNotNull(vertex2);
            boolean edgeExists = vertex1.isNeighbor(vertex2) && vertex2.isNeighbor(vertex1);
            Assert.assertTrue(edgeExists);
        }
    }

    private void assertCorrectVertices(Graph graph, NGraph<GraphInput.InputData> nGraph) {
        Assert.assertEquals(graph.getNodes().size(), nGraph.getNumberOfVertices());
        for (Node node : graph.getNodes()) {
            boolean found = false;
            for (NVertex<GraphInput.InputData> vertex : nGraph) {
                if (vertex.data.name.equals(node.getName())) {
                    found = true;
                }
            }
            Assert.assertTrue(found);
        }
    }

    private void assertCorrectGraph(NGraph<GraphInput.InputData> graph) {
        for (NVertex<GraphInput.InputData> vertex : graph) {
            assertCorrectNeighbours(vertex);
            assertCorrectData(vertex);
        }

    }

    private void assertCorrectData(NVertex<GraphInput.InputData> vertex) {
        Assert.assertNotNull(vertex.data.name);
        Assert.assertFalse(((LPInputData) vertex.data).isNodeHandled());
    }

    private void assertCorrectNeighbours(NVertex<GraphInput.InputData> vertex) {
        for (NVertex<GraphInput.InputData> neighbour : vertex) {
            assertVertexOccursOnceAsNeighbour(vertex, neighbour);
            assertVertexOccursOnceAsNeighbour(neighbour, vertex);
        }
    }

    private void assertVertexOccursOnceAsNeighbour(NVertex<GraphInput.InputData> vertex, NVertex<GraphInput.InputData> neighbour) {
        int countVertex = 0;
        for (NVertex<GraphInput.InputData> neighbourNeighbour : neighbour) {
            if (neighbourNeighbour.equals(vertex)) {
                countVertex++;
            }
        }
        Assert.assertEquals(1, countVertex, 0);
    }

    @Test
    public void testDisconnectedGraph() throws InterruptedException {
        Graph disconnectedGraph = createDisconnectedGraph();

        NGraph result = graphToNGraph(disconnectedGraph);

        assertSameGraph(disconnectedGraph, result);
        Assert.assertEquals(3, result.getComponents().size(), 0);
        List<NGraph> components = result.getComponents();
        assertSameGraph(createNodeBlockerGraph(), components.get(0));
        assertSameGraph(createClique(2, "clique1_"), components.get(1));
        assertSameGraph(createClique(3, "clique2_"), components.get(2));
    }

    @Test
    public void testRandomGraph() throws InterruptedException {
        Graph randomGraph = createRandomGraph();

        NGraph result = graphToNGraph(randomGraph);

        assertSameGraph(randomGraph, result);
    }

    private NGraph graphToNGraph(Graph graph) {
        GraphTransformator transformator = new GraphTransformator();

        NGraph<GraphInput.InputData> nGraph =  transformator.graphToNGraph(graph);

        if (SHOW_GRAPH || PRINT_GRAPH) {
            nGraph.printGraph(SHOW_GRAPH, PRINT_GRAPH);
        }
        assertCorrectGraph(nGraph);

        return nGraph;
    }

}
