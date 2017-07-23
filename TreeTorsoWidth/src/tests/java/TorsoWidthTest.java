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

    /*
    Transforms a graph to a NGraph and then runs the torso width algorithm
     */
    public static NGraph<GraphInput.InputData> torsoWidth(Graph graph) throws InterruptedException {

        // generate NGraph for using main.java.libtw
        NGraph<GraphInput.InputData> g;
        GraphTransformator graphTransformator = new GraphTransformator();
        g = graphTransformator.graphToNGraph(graph);

        return torsoWidth(g);
    }

    /*
    Runs the torso width algorithm
    */
    public static NGraph<GraphInput.InputData> torsoWidth(NGraph<GraphInput.InputData> g) throws InterruptedException {

        GreedyDegree<GraphInput.InputData> ubAlgo = new GreedyDegree<>();
        ubAlgo.setInput(g);
        ubAlgo.run();
        int upperbound = ubAlgo.getUpperBound();
        LOGGER.debug("UB: " + upperbound + " of " + g.getNumberOfVertices() + " nodes with " + ubAlgo.getName());

        ubAlgo = new GreedyDegree<>();

        TorsoWidth<GraphInput.InputData> torsoWidthAlgo = new TorsoWidth<>(ubAlgo);
        torsoWidthAlgo.setInput(g);
        torsoWidthAlgo.run();
        int torsoWidthUpperBound = torsoWidthAlgo.getUpperBound();
        LOGGER.debug("UB TorsoWidth: " + torsoWidthUpperBound + " of " + g.getNumberOfVertices() + " nodes with " + torsoWidthAlgo.getName());

        for (NVertex<GraphInput.InputData> node : torsoWidthAlgo.getGraph()) {
            Assert.assertTrue(((LPInputData) node.data).isInteger());

            for (NVertex<GraphInput.InputData> neighbour : ((ListVertex<GraphInput.InputData>) node).neighbors) {
                Assert.assertTrue(((LPInputData) neighbour.data).isInteger());
            }
        }
        return torsoWidthAlgo.getGraph();
    }


    @Test
    public void testNodeBlockerGraph() throws InterruptedException {
        Graph nodeBlockerGraph = createNodeBlockerGraph();
        LOGGER.debug("--Node Blocker Graph--");
        NGraph<GraphInput.InputData> resultGraph = torsoWidth(nodeBlockerGraph);
        resultGraph.printGraph(true, true);

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
        LOGGER.debug("--"+ inputFile +" Graph--");
        NGraph<GraphInput.InputData> g = null;

        GraphInput input = new LPDgfReader(inputFile);
        try {
            g = input.get();
        } catch( InputException e ) {}
        NGraph<GraphInput.InputData> resultGraph = torsoWidth(g);
        resultGraph.printGraph(true, true);

        Assert.assertEquals(6, resultGraph.getNumberOfVertices(), 0);
        Assert.assertEquals(10, resultGraph.getNumberOfEdges(), 0);
        Assert.assertFalse(isClique(resultGraph));
    }

    @Test
    public void testStarShapedGraph() throws InterruptedException {
        LOGGER.debug("--Star Shaped Graph--");
        Graph starShapedGraph = createStarShapedGraph();
        NGraph<GraphInput.InputData> resultGraph = torsoWidth(starShapedGraph);
        resultGraph.printGraph(true, true);

        Assert.assertEquals(3, resultGraph.getNumberOfVertices(), 0);
        Assert.assertEquals(3, resultGraph.getNumberOfEdges(), 0);
        Assert.assertTrue(isClique(resultGraph));
    }

    private boolean isClique(NGraph<GraphInput.InputData> graph) {
        boolean clique = true;
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

        Graph g = createRandomGraph();
        GraphTransformator transformator = new GraphTransformator();
        NGraph<GraphInput.InputData> before = transformator.graphToNGraph(g);
        before.printGraph(true, true);

        LOGGER.debug("--Random Graph--");
        NGraph<GraphInput.InputData> after = torsoWidth(before);
        after.printGraph(true, true);
    }
}
