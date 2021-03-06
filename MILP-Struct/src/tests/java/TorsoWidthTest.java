package tests.java;

import main.java.graph.Graph;
import main.java.algo.LPInputData;
import main.java.algo.TorsoWidth;
import nl.uu.cs.treewidth.algorithm.GreedyDegree;
import nl.uu.cs.treewidth.ngraph.ListVertex;
import nl.uu.cs.treewidth.ngraph.NGraph;
import nl.uu.cs.treewidth.ngraph.NVertex;
import org.junit.Assert;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

/**
 * Tests whether the computation of the torso and of the torso-width works correctly
 */
public class TorsoWidthTest extends GraphTest implements AlgoTest {
    private static final Logger LOGGER = LoggerFactory.getLogger(TorsoWidthTest.class);

    private static final boolean SHOW_GRAPH = false;
    private static final boolean PRINT_GRAPH = false;
    private static final boolean PRINT_RESULTS = false;

    public static TorsoWidth<LPInputData> torsoWidth(Graph graph) throws InterruptedException {
        return torsoWidth((NGraph) createNGraph(graph));
    }

    public static TorsoWidth<LPInputData> torsoWidth(NGraph<LPInputData> nGraph) throws InterruptedException {
        if (SHOW_GRAPH || PRINT_GRAPH) {
            nGraph.printGraph(SHOW_GRAPH, PRINT_GRAPH);
        }
        computeTreewidthUBBefore(nGraph);
        TorsoWidth<LPInputData> torsoWidthAlgo = computeTorsowidth(nGraph);

        assertAllNodesInComponentsInteger(torsoWidthAlgo.getGraph());
        return torsoWidthAlgo;
    }

    private static void assertAllNodesInComponentsInteger(NGraph<LPInputData> graphAfter) {
        for (NGraph<LPInputData> component : graphAfter.getComponents()) {
            for (NVertex<LPInputData> node : component) {
                Assert.assertTrue(((LPInputData) node.data).isInteger());

                for (NVertex<LPInputData> neighbour : ((ListVertex<LPInputData>) node).neighbors) {
                    Assert.assertTrue(((LPInputData) neighbour.data).isInteger());
                }
            }
        }
    }

    private static TorsoWidth<LPInputData> computeTorsowidth(NGraph<LPInputData> graphBefore) throws InterruptedException {
        TorsoWidth<LPInputData> torsoWidthAlgo = new TorsoWidth<>();
        torsoWidthAlgo.setInput(graphBefore);
        torsoWidthAlgo.run();
        int torsoWidthUpperBound = torsoWidthAlgo.getUpperBound();
        if (PRINT_RESULTS) {
            printResult("TorsoWidth UB", torsoWidthUpperBound, graphBefore.getNumberOfVertices(), torsoWidthAlgo.getName());
        }
        return torsoWidthAlgo;
    }

    private static GreedyDegree<LPInputData> computeTreewidthUBBefore(NGraph<LPInputData> graphBefore) throws InterruptedException {
        GreedyDegree<LPInputData> ubAlgo = new GreedyDegree<>();
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

        TorsoWidth<LPInputData> torsoWidthAlgo = torsoWidth(nodeBlockerGraph);
        NGraph<LPInputData> resultGraph = torsoWidthAlgo.getGraph();
        NGraph<LPInputData> onlyComponent = resultGraph.getComponents().get(0);

        Assert.assertEquals(1, resultGraph.getComponents().size(), 0);
        Assert.assertEquals(3, onlyComponent.getNumberOfVertices(), 0);
        Assert.assertEquals(2, onlyComponent.getNumberOfEdges(), 0);
        Iterator<NVertex<LPInputData>> iterator = resultGraph.iterator();
        NVertex<LPInputData> nodeBlocker = iterator.next();
        Assert.assertTrue(nodeBlocker.isNeighbor(iterator.next()));
        Assert.assertTrue(nodeBlocker.isNeighbor(iterator.next()));
        Assert.assertEquals(2, nodeBlocker.getNumberOfNeighbors(), 0);
        Assert.assertEquals(1, torsoWidthAlgo.getUpperBound(), 0);
        Assert.assertEquals(1, torsoWidthAlgo.getLowerBound(), 0);
    }

    @Test
    public void testStarShapedGraph() throws InterruptedException {
        Graph starShapedGraph = createStarShapedGraph();

        TorsoWidth<LPInputData>torsoWidthAlgo = torsoWidth(starShapedGraph);
        NGraph<LPInputData> resultGraph = torsoWidthAlgo.getGraph();
        NGraph<LPInputData> onlyComponent = resultGraph.getComponents().get(0);

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

        TorsoWidth<LPInputData> torsoWidthAlgo = torsoWidth(disconnectedGraph);
        NGraph<LPInputData> resultGraph = torsoWidthAlgo.getGraph();

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
        TorsoWidth<LPInputData> torsoWidthAlgo = torsoWidth(randomGraph);
    }

    private boolean isClique(NGraph<LPInputData> graph) {
        boolean clique;
        for (NVertex<LPInputData> vertex1 : graph) {
            for (NVertex<LPInputData> vertex2 : graph) {
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
