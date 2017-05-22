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
public class TorsoWidthTest {
    private static final Logger LOGGER = LoggerFactory.getLogger(TorsoWidthTest.class);
    
    private static final int MIN_NODES = 5;
    private static final int MAX_NODES = 20;

    // probabiity that a vertex is connected with another vertex
    private static final double DENSITY = 0.2;

    // probabiity that a vertex is connected with another vertex
    private static final double PROB_INTEGER_NODE = 0.16;
    private static final String INTEGER_MARK = "_I";


    /*
    Transforms a graph to a NGraph and then runs the torso width algorithm
     */
    public static NGraph<GraphInput.InputData> torsoWidth(Graph graph) {

        // generate NGraph for using main.java.libtw
        NGraph<GraphInput.InputData> g;
        GraphTransformator graphTransformator = new GraphTransformator();
        g = graphTransformator.graphToNGraph(graph);

        return torsoWidth(g);
    }

    /*
    Runs the torso width algorithm
    */
    public static NGraph<GraphInput.InputData> torsoWidth(NGraph<GraphInput.InputData> g) {

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

        for (NVertex<GraphInput.InputData> node : g) {
            Assert.assertTrue(((LPInputData) node.data).isInteger());

            for (NVertex<GraphInput.InputData> neighbour : ((ListVertex<GraphInput.InputData>) node).neighbors) {
                Assert.assertTrue(((LPInputData) neighbour.data).isInteger());
            }
        }
        return g;
    }


    @Test
    public void testNodeBlockerGraph() {
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

    /*
    Creates a graph that has an integer node that separates two components, which each also contain 2 integer nodes
     */
    private Graph createNodeBlockerGraph() {
        List<Node> nodes = new ArrayList<>();
        List<Edge> edges = new ArrayList<>();

        nodes.add(createNode("nodeBlocker", true));

        // create nodes to the left and right
        for (int i = 0; i < 4; i++) {

            if (i < 3) {
                nodes.add(createNode("nodeLeft" + i , false));
                nodes.add(createNode("nodeRight" + i, false));
            }

            if (i == 3) {
                nodes.add(createNode("nodeLeft" + i, true));
                nodes.add(createNode("nodeRight" + i, true));
            }
        }

        Edge edge = new Edge(nodes.get(0), nodes.get(1));
        edges.add(edge);
        edge = new Edge(nodes.get(1), nodes.get(3));
        edges.add(edge);
        edge = new Edge(nodes.get(3), nodes.get(5));
        edges.add(edge);
        edge = new Edge(nodes.get(3), nodes.get(7));
        edges.add(edge);

        edge = new Edge(nodes.get(0), nodes.get(2));
        edges.add(edge);
        edge = new Edge(nodes.get(2), nodes.get(4));
        edges.add(edge);
        edge = new Edge(nodes.get(4), nodes.get(6));
        edges.add(edge);
        edge = new Edge(nodes.get(4), nodes.get(8));
        edges.add(edge);

        return createGraph(nodes, edges);
    }

    @Test
    public void testGraphFromInputFile() {
        String inputFile = "./../input/tests/torsowidth_test.dgf";
        LOGGER.debug("--"+ inputFile +" Graph--");
        NGraph<GraphInput.InputData> g = null;

        GraphInput input = new LPDgfReader(inputFile);
        try {
            g = input.get();
            g.addComment("test");
        } catch( InputException e ) {}
        NGraph<GraphInput.InputData> resultGraph = torsoWidth(g);
        resultGraph.printGraph(true, true);

        Assert.assertEquals(6, resultGraph.getNumberOfVertices(), 0);
        Assert.assertEquals(10, resultGraph.getNumberOfEdges(), 0);
        Assert.assertFalse(isClique(resultGraph));
    }

    @Test
    public void testStarShapedGraph() {
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

    private Graph createStarShapedGraph() {

        List<Node> nodes = new ArrayList<>();
        List<Edge> edges = new ArrayList<>();

        Node node1 = createNode("middleNode", false);
        Node node2 = createNode("outNode1", true);
        Node node3 = createNode("outNode2", true);;
        Node node4 = createNode("outNode3", true);

        nodes.add(node1);
        nodes.add(node2);
        nodes.add(node3);
        nodes.add(node4);

        Edge edge1 = new Edge(node1, node2);
        Edge edge2 = new Edge(node1, node3);
        Edge edge3 = new Edge(node1, node4);

        edges.add(edge1);
        edges.add(edge2);
        edges.add(edge3);

        return createGraph(nodes, edges);
    }

    public Graph createGraph(List<Node> nodes, List<Edge> edges) {
        Graph graph = new Graph();
        Map<String, List<Node>> neighbourNodes = new HashMap<>();

        for (Edge edge : edges) {
            List<Node> neighbours1;
            if (neighbourNodes.get(edge.getNode1().getName()) == null) {
                neighbours1 = new ArrayList<>();
            } else {
                neighbours1 = neighbourNodes.get(edge.getNode1().getName());
            }
            neighbours1.add(edge.getNode2());
            neighbourNodes.put(edge.getNode1().getName(), neighbours1);

            List<Node> neighbours2;
            if (neighbourNodes.get(edge.getNode2().getName()) == null) {
                neighbours2 = new ArrayList<>();
            } else {
                neighbours2 = neighbourNodes.get(edge.getNode2().getName());
            }
            neighbours2.add(edge.getNode1());
            neighbourNodes.put(edge.getNode2().getName(), neighbours2);
        }

        graph.setEdges(edges);
        graph.setNodes(nodes);
        graph.setNeighbourNodes(neighbourNodes);

        return graph;
    }

    @Test
    public void testRandomGraph() {

        Graph g = createRandomGraph();
        GraphTransformator transformator = new GraphTransformator();
        NGraph<GraphInput.InputData> before = transformator.graphToNGraph(g);
        before.printGraph(true, true);

        LOGGER.debug("--Random Graph--");
        NGraph<GraphInput.InputData> after = torsoWidth(before);
        after.printGraph(true, true);
    }

    public Graph createRandomGraph() {

        // creates a number in between min and max (inclusive)
        int numNodes = ThreadLocalRandom.current().nextInt(MIN_NODES, MAX_NODES + 1);

        List<Node> nodes = new ArrayList<>();
        List<Edge> edges = new ArrayList<>();

        for (int i = 0; i < numNodes; i++) {
            Node node;
            String name = "Node" + i;
            if (ThreadLocalRandom.current().nextDouble() < PROB_INTEGER_NODE) {
                node = createNode(name, true);
            } else {
                node = createNode(name, false);
            }
            nodes.add(node);
        }

        for (int i = 0; i < numNodes; i++) {
            for (int j = i + 1; j < numNodes; j++) {
                if (ThreadLocalRandom.current().nextDouble() < DENSITY) {
                    // create an edge
                    Edge edge = new Edge(nodes.get(i), nodes.get(j));
                    edges.add(edge);
                };
            }
        }

        return createGraph(nodes, edges);
    }

    public Node createNode(String name, boolean integer) {
        Node node = new Node();
        node.setInteger(integer);
        node.setName(integer ? name + INTEGER_MARK : name);
        return node;
    }
}
