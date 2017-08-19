package tests.java;

import main.java.Configuration;
import main.java.graph.Graph;
import main.java.libtw.LPInputData;
import main.java.libtw.TorsoWidth;
import main.java.libtw.TreeWidthWrapper;
import nl.uu.cs.treewidth.algorithm.GreedyDegree;
import nl.uu.cs.treewidth.input.GraphInput;
import nl.uu.cs.treewidth.ngraph.ListVertex;
import nl.uu.cs.treewidth.ngraph.NGraph;
import nl.uu.cs.treewidth.ngraph.NVertex;
import org.junit.Assert;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

/**
 * Created by Verena on 08.04.2017.
 */
public class TorsoWidthTest extends GraphTest implements AlgoTest {
    private static final Logger LOGGER = LoggerFactory.getLogger(TorsoWidthTest.class);

    private static final boolean SHOW_GRAPH = false;
    private static final boolean PRINT_GRAPH = false;
    private static final boolean PRINT_RESULTS = false;

    public static TorsoWidth<GraphInput.InputData> torsoWidth(Graph graph) throws InterruptedException {
        return torsoWidth(createNGraph(graph));
    }

    public static TorsoWidth<GraphInput.InputData> torsoWidth(NGraph<GraphInput.InputData> nGraph) throws InterruptedException {
        if (SHOW_GRAPH || PRINT_GRAPH) {
            nGraph.printGraph(SHOW_GRAPH, PRINT_GRAPH);
        }
        computeTreewidthUBBefore(nGraph);
        TorsoWidth<GraphInput.InputData> torsoWidthAlgo = computeTorsowidth(nGraph);

        assertAllNodesInComponentsInteger(torsoWidthAlgo.getGraph());
        return torsoWidthAlgo;
    }

    private static void assertAllNodesInComponentsInteger(NGraph<GraphInput.InputData> graphAfter) {
        for (NGraph<GraphInput.InputData> component : graphAfter.getComponents()) {
            for (NVertex<GraphInput.InputData> node : component) {
                Assert.assertTrue(((LPInputData) node.data).isInteger());

                for (NVertex<GraphInput.InputData> neighbour : ((ListVertex<GraphInput.InputData>) node).neighbors) {
                    Assert.assertTrue(((LPInputData) neighbour.data).isInteger());
                }
            }
        }
    }

    private static TorsoWidth<GraphInput.InputData> computeTorsowidth(NGraph<GraphInput.InputData> graphBefore) throws InterruptedException {
        TorsoWidth<GraphInput.InputData> torsoWidthAlgo = new TorsoWidth<>();
        torsoWidthAlgo.setInput(graphBefore);
        torsoWidthAlgo.run();
        int torsoWidthUpperBound = torsoWidthAlgo.getUpperBound();
        if (PRINT_RESULTS) {
            printResult("TorsoWidth UB", torsoWidthUpperBound, graphBefore.getNumberOfVertices(), torsoWidthAlgo.getName());
        }
        return torsoWidthAlgo;
    }

    private static GreedyDegree<GraphInput.InputData> computeTreewidthUBBefore(NGraph<GraphInput.InputData> graphBefore) throws InterruptedException {
        GreedyDegree<GraphInput.InputData> ubAlgo = new GreedyDegree<>();
        ubAlgo.setInput(graphBefore);
        ubAlgo.run();
        int upperbound = ubAlgo.getUpperBound();
        if (PRINT_RESULTS) {
            printResult("Treewidth UB", upperbound, graphBefore.getNumberOfVertices(), ubAlgo.getName());
        }
        return ubAlgo;
    }

    @Test
    public void testNodeBlockerGraph() throws InterruptedException {
        Graph nodeBlockerGraph = createNodeBlockerGraph();

        TorsoWidth<GraphInput.InputData> torsoWidthAlgo = torsoWidth(nodeBlockerGraph);
        NGraph<GraphInput.InputData> resultGraph = torsoWidthAlgo.getGraph();
        NGraph<GraphInput.InputData> onlyComponent = resultGraph.getComponents().get(0);

        Assert.assertEquals(1, resultGraph.getComponents().size(), 0);
        Assert.assertEquals(3, onlyComponent.getNumberOfVertices(), 0);
        Assert.assertEquals(2, onlyComponent.getNumberOfEdges(), 0);
        Iterator<NVertex<GraphInput.InputData>> iterator = resultGraph.iterator();
        NVertex<GraphInput.InputData> nodeBlocker = iterator.next();
        Assert.assertTrue(nodeBlocker.isNeighbor(iterator.next()));
        Assert.assertTrue(nodeBlocker.isNeighbor(iterator.next()));
        Assert.assertEquals(2, nodeBlocker.getNumberOfNeighbors(), 0);
        Assert.assertEquals(1, torsoWidthAlgo.getUpperBound(), 0);
        Assert.assertEquals(1, torsoWidthAlgo.getLowerBound(), 0);
    }

    @Test
    public void testStarShapedGraph() throws InterruptedException {
        Graph starShapedGraph = createStarShapedGraph();

        TorsoWidth<GraphInput.InputData> torsoWidthAlgo = torsoWidth(starShapedGraph);
        NGraph<GraphInput.InputData> resultGraph = torsoWidthAlgo.getGraph();
        NGraph<GraphInput.InputData> onlyComponent = resultGraph.getComponents().get(0);

        Assert.assertEquals(1, resultGraph.getComponents().size(), 0);
        Assert.assertEquals(3, onlyComponent.getNumberOfVertices(), 0);
        Assert.assertEquals(3, onlyComponent.getNumberOfEdges(), 0);
        Assert.assertTrue(isClique(onlyComponent));
        Assert.assertEquals(2, torsoWidthAlgo.getUpperBound(), 0);
        Assert.assertEquals(2, torsoWidthAlgo.getLowerBound(), 0);
    }

    @Test
    public void testDisconnectedGraph() throws InterruptedException {
        Graph disconnectedGraph = createDisconnectedGraph();

        TorsoWidth<GraphInput.InputData> torsoWidthAlgo = torsoWidth(disconnectedGraph);
        NGraph<GraphInput.InputData> resultGraph = torsoWidthAlgo.getGraph();

        Assert.assertEquals(2, resultGraph.getComponents().size(), 0);
        Assert.assertEquals(3, resultGraph.getComponents().get(0).getNumberOfVertices(), 0);
        Assert.assertEquals(3, resultGraph.getComponents().get(1).getNumberOfVertices(), 0);
        Assert.assertEquals(2, resultGraph.getComponents().get(0).getNumberOfEdges(), 0);
        Assert.assertEquals(3, resultGraph.getComponents().get(1).getNumberOfEdges(), 0);
        Assert.assertEquals(2, torsoWidthAlgo.getUpperBound(), 0);
        Assert.assertEquals(2, torsoWidthAlgo.getLowerBound(), 0);
    }

    @Test
    public void testRandomGraph() throws InterruptedException {
        Graph randomGraph = createRandomGraph();
        TorsoWidth<GraphInput.InputData> torsoWidthAlgo = torsoWidth(randomGraph);
    }

    private boolean isClique(NGraph<GraphInput.InputData> graph) {
        boolean clique;
        for (NVertex<GraphInput.InputData> vertex1 : graph) {
            for (NVertex<GraphInput.InputData> vertex2 : graph) {
                if (!vertex1.equals(vertex2)) {
                    clique = vertex2.isNeighbor(vertex1) && vertex2.isNeighbor(vertex1);

                    if (!clique) {
                        return false;
                    }
                }
            }
        }
        return true;
    }
}
