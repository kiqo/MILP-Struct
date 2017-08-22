package tests.java;

import main.java.graph.Graph;
import main.java.algo.TreeDepth;
import nl.uu.cs.treewidth.input.GraphInput;
import nl.uu.cs.treewidth.ngraph.ListVertex;
import nl.uu.cs.treewidth.ngraph.NGraph;
import org.junit.Assert;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

/**
 * Created by Verena on 08.04.2017.
 */
public class TreeDepthTest extends GraphTest implements AlgoTest{
    private static final Logger LOGGER = LoggerFactory.getLogger(TreeDepthTest.class);

    private static final boolean SHOW_GRAPH = false;
    private static final boolean PRINT_GRAPH = false;
    private static final boolean PRINT_RESULTS = false;

    private TreeDepth<GraphInput.InputData> treeDepth(Graph graph) throws InterruptedException {
        return treeDepth(createNGraph(graph));
    }

    private TreeDepth<GraphInput.InputData> treeDepth(NGraph<GraphInput.InputData> nGraph) throws InterruptedException {
        if (SHOW_GRAPH || PRINT_GRAPH) {
            nGraph.printGraph(SHOW_GRAPH, PRINT_GRAPH);
        }
        TreeDepth<GraphInput.InputData> treeDepthAlgo = new TreeDepth<>();
        treeDepthAlgo.setInput(nGraph);
        treeDepthAlgo.run();
        int treeDepthUB = treeDepthAlgo.getUpperBound();
        if (PRINT_RESULTS) {
            printResult("UB TreeDepth: ", treeDepthUB, nGraph.getNumberOfVertices(), treeDepthAlgo.getName());
        }
        return treeDepthAlgo;
    }

    @Test
    public void testNodeBlockerGraph() throws InterruptedException {
        Graph nodeBlockerGraph = createNodeBlockerGraph();

        TreeDepth<GraphInput.InputData> algoResult = treeDepth(nodeBlockerGraph);

        if (PRINT_RESULTS) {
            printPath(algoResult.getLongestPath());
        }
        Assert.assertEquals(7, algoResult.getLongestPath().size(), 0);
        Assert.assertEquals(4, algoResult.getUpperBound(), 0);
    }

    @Test
    public void testStarShapedGraph() throws InterruptedException {
        Graph starShapedGraph = createStarShapedGraph();

        TreeDepth<GraphInput.InputData> algoResult = treeDepth(starShapedGraph);

        Assert.assertEquals(3, algoResult.getLongestPath().size(), 0);
        Assert.assertEquals(2, algoResult.getUpperBound(), 0);
    }

    @Test
    public void testRandomGraph() throws InterruptedException {
        Graph randomGraph = createRandomGraph();
        TreeDepth<GraphInput.InputData> algoResult = treeDepth(randomGraph);
        if (PRINT_RESULTS) {
            printPath(algoResult.getLongestPath());
        }
    }

    @Test
    public void testDisconnectedGraph() throws InterruptedException {
        Graph disconnectedGraph = createDisconnectedGraph();

        TreeDepth<GraphInput.InputData> algoResult = treeDepth(disconnectedGraph);
        if (PRINT_RESULTS) {
            printPath(algoResult.getLongestPath());
        }

        Assert.assertEquals(7, algoResult.getLongestPath().size(), 0);
        Assert.assertEquals(4, algoResult.getUpperBound(), 0);
    }

    private void printPath(List<ListVertex<GraphInput.InputData>> path) {
        StringBuilder result = new StringBuilder("Path found: ");
        for (int i = 0; i < path.size() - 1; i++) {
            result.append(path.get(i).data.name).append(" - ");
        }
        result.append(path.get(path.size() - 1).data.name);
        LOGGER.debug(result.toString());
    }
}
