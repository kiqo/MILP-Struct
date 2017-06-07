package tests.java;

import main.java.graph.Graph;
import main.java.libtw.TreeDepthLB;
import nl.uu.cs.treewidth.input.GraphInput;
import nl.uu.cs.treewidth.ngraph.ListVertex;
import nl.uu.cs.treewidth.ngraph.NGraph;
import org.junit.Assert;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import main.java.parser.GraphTransformator;

import java.util.*;

/**
 * Created by Verena on 08.04.2017.
 */
public class TreeDepthTest extends GraphTest {
    private static final Logger LOGGER = LoggerFactory.getLogger(TreeDepthTest.class);

    /*
    Transforms a graph to a NGraph and then runs the tree depth lower bound algorithm
     */
    private TreeDepthLB<GraphInput.InputData> treeDepthLB(Graph graph) throws InterruptedException {

        // generate NGraph for using libtw
        NGraph<GraphInput.InputData> g;
        GraphTransformator graphTransformator = new GraphTransformator();
        g = graphTransformator.graphToNGraph(graph);

        return treeDepthLB(g);
    }

    /*
    Runs the tree depth lb algorithm
    */
    private TreeDepthLB<GraphInput.InputData> treeDepthLB(NGraph<GraphInput.InputData> g) throws InterruptedException {

        TreeDepthLB<GraphInput.InputData> treeDepthAlgo = new TreeDepthLB<>();
        treeDepthAlgo.setInput(g);
        treeDepthAlgo.run();
        int treeDepthLB = treeDepthAlgo.getLowerBound();
        printTimingInfo("LB TreeDepth: ", treeDepthLB, g.getNumberOfVertices(), treeDepthAlgo.getName());

        return treeDepthAlgo;
    }

    @Test
    public void testNodeBlockerGraph() throws InterruptedException {
        Graph nodeBlockerGraph = createNodeBlockerGraph();
        LOGGER.debug("--Node Blocker Graph--");
        TreeDepthLB<GraphInput.InputData> lbAlgoResult = treeDepthLB(nodeBlockerGraph);
        printPath(lbAlgoResult.getLongestPath());
        Assert.assertEquals(7, lbAlgoResult.getLongestPath().size(), 0);
        Assert.assertEquals(3, lbAlgoResult.getLowerBound(), 0);
    }

    @Test
    public void testStarShapedGraph() throws InterruptedException {
        LOGGER.debug("--Star Shaped Graph--");
        Graph starShapedGraph = createStarShapedGraph();
        TreeDepthLB<GraphInput.InputData> lbAlgoResult = treeDepthLB(starShapedGraph);
        // longest path is 3, lower bound is then roundUp(ld(n+1)) = 2
        Assert.assertEquals(3, lbAlgoResult.getLongestPath().size(), 0);
        Assert.assertEquals(2, lbAlgoResult.getLowerBound(), 0);
    }

    @Test
    public void testRandomGraph() throws InterruptedException {

        Graph g = createRandomGraph();
        GraphTransformator transformator = new GraphTransformator();
        NGraph<GraphInput.InputData> before = transformator.graphToNGraph(g);
        before.printGraph(true, true);

        LOGGER.debug("--Random Graph--");
        TreeDepthLB<GraphInput.InputData> lbAlgoResult = treeDepthLB(before);
        printPath(lbAlgoResult.getLongestPath());
    }

    private void printPath(List<ListVertex<GraphInput.InputData>> path) {
        StringBuilder result = new StringBuilder();
        for (int i = 0; i < path.size() - 1; i++) {
            result.append(path.get(i).data.name).append(" - ");
        }
        result.append(path.get(path.size() - 1).data.name);
        LOGGER.debug(result.toString());
    }
}
