package tests.java;

import main.java.graph.Edge;
import main.java.graph.Graph;
import main.java.graph.Node;
import main.java.libtw.LPDgfReader;
import main.java.libtw.LPInputData;
import main.java.libtw.TorsoWidth;
import nl.uu.cs.treewidth.algorithm.GreedyDegree;
import nl.uu.cs.treewidth.input.GraphInput;
import nl.uu.cs.treewidth.input.InputException;
import nl.uu.cs.treewidth.ngraph.ListVertex;
import nl.uu.cs.treewidth.ngraph.NGraph;
import nl.uu.cs.treewidth.ngraph.NVertex;
import org.junit.Assert;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import main.java.parser.GraphTransformator;

import java.util.*;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Created by Verena on 08.04.2017.
 */
public class TorsoWidthTest extends GraphTest {
    private static final Logger LOGGER = LoggerFactory.getLogger(TorsoWidthTest.class);

    private static final boolean SHOW_GRAPH = false;
    private static final boolean PRINT_GRAPH = false;
    private static final boolean PRINT_RESULTS = false;

    /*
    Transforms a graph to a NGraph and then runs the torso width algorithm
     */
    public static NGraph<GraphInput.InputData> torsoWidth(Graph graph) throws InterruptedException {
        // generate NGraph for using libtw
        GraphTransformator graphTransformator = new GraphTransformator();
        NGraph<GraphInput.InputData> g = graphTransformator.graphToNGraph(graph);

        return torsoWidth(g);
    }

    /*
    Runs the torso width algorithm
    */
    public static NGraph<GraphInput.InputData> torsoWidth(NGraph<GraphInput.InputData> graphBefore) throws InterruptedException {
        if (SHOW_GRAPH || PRINT_GRAPH) {
            graphBefore.printGraph(SHOW_GRAPH, PRINT_GRAPH);
        }
        computeTreewidthUBBefore(graphBefore);
        NGraph<GraphInput.InputData> graphAfter = computeTorsowidth(graphBefore);

        if (SHOW_GRAPH || PRINT_GRAPH) {
            graphAfter.printGraph(SHOW_GRAPH, PRINT_GRAPH);
        }
        assertAllNodesInteger(graphAfter);
        return graphAfter;
    }

    private static void assertAllNodesInteger(NGraph<GraphInput.InputData> graphAfter) {
        for (NVertex<GraphInput.InputData> node : graphAfter) {
            Assert.assertTrue(((LPInputData) node.data).isInteger());

            for (NVertex<GraphInput.InputData> neighbour : ((ListVertex<GraphInput.InputData>) node).neighbors) {
                Assert.assertTrue(((LPInputData) neighbour.data).isInteger());
            }
        }
    }

    private static NGraph<GraphInput.InputData> computeTorsowidth(NGraph<GraphInput.InputData> graphBefore) throws InterruptedException {
        GreedyDegree<GraphInput.InputData> ubAlgo = new GreedyDegree<>();
        TorsoWidth<GraphInput.InputData> torsoWidthAlgo = new TorsoWidth<>(ubAlgo);
        torsoWidthAlgo.setInput(graphBefore);
        torsoWidthAlgo.run();
        int torsoWidthUpperBound = torsoWidthAlgo.getUpperBound();
        if (PRINT_RESULTS) {
            printResult("TorsoWidth UB", torsoWidthUpperBound, graphBefore.getNumberOfVertices(), torsoWidthAlgo.getName());
        }
        return torsoWidthAlgo.getGraph();
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

        NGraph<GraphInput.InputData> resultGraph = torsoWidth(nodeBlockerGraph);

        Assert.assertEquals(3, resultGraph.getNumberOfVertices(), 0);
        Assert.assertEquals(2, resultGraph.getNumberOfEdges(), 0);
        Iterator<NVertex<GraphInput.InputData>> iterator = resultGraph.iterator();
        NVertex<GraphInput.InputData> nodeBlocker = iterator.next();
        Assert.assertTrue(nodeBlocker.isNeighbor(iterator.next()));
        Assert.assertTrue(nodeBlocker.isNeighbor(iterator.next()));
        Assert.assertEquals(2, nodeBlocker.getNumberOfNeighbors(), 0);
    }

    @Test
    public void testGraphFromInputFile() throws InterruptedException {
        String inputFile = "./../input/tests/torsowidth_test.dgf";
        NGraph<GraphInput.InputData> g = null;

        GraphInput input = new LPDgfReader(inputFile);
        try {
            g = input.get();
        } catch( InputException e ) {}
        NGraph<GraphInput.InputData> resultGraph = torsoWidth(g);

        Assert.assertEquals(6, resultGraph.getNumberOfVertices(), 0);
        Assert.assertEquals(10, resultGraph.getNumberOfEdges(), 0);
        Assert.assertFalse(isClique(resultGraph));
    }

    @Test
    public void testStarShapedGraph() throws InterruptedException {
        Graph starShapedGraph = createStarShapedGraph();

        NGraph<GraphInput.InputData> resultGraph = torsoWidth(starShapedGraph);

        Assert.assertEquals(3, resultGraph.getNumberOfVertices(), 0);
        Assert.assertEquals(3, resultGraph.getNumberOfEdges(), 0);
        Assert.assertTrue(isClique(resultGraph));
    }

    @Test
    public void testDisconnectedGraph() throws InterruptedException {
        Graph disconnectedGraph = createDisconnectedGraph();

        NGraph<GraphInput.InputData> resultGraph = torsoWidth(disconnectedGraph);

        Assert.assertEquals(6, resultGraph.getNumberOfVertices(), 0);
        Assert.assertEquals(5, resultGraph.getNumberOfEdges(), 0);
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

    @Test
    public void testRandomGraph() throws InterruptedException {
        Graph randomGraph = createRandomGraph();
        NGraph<GraphInput.InputData> after = torsoWidth(randomGraph);
    }
}
