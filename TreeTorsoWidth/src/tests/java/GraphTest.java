package tests.java;

import main.java.graph.Edge;
import main.java.graph.Graph;
import main.java.graph.Node;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Created by Verena on 07.06.2017.
 */
public abstract class GraphTest {
    private static final Logger LOGGER = LoggerFactory.getLogger(GraphTest.class);

    private static final int MIN_NODES = 5;
    private static final int MAX_NODES = 20;

    // probabiity that a vertex is connected with another vertex
    private static final double DENSITY = 0.2;

    // probabiity that a vertex is connected with another vertex
    private static final double PROB_INTEGER_NODE = 0.16;
    private static final String INTEGER_MARK = "_I";

    public abstract void testNodeBlockerGraph() throws InterruptedException;
    public abstract void testStarShapedGraph() throws InterruptedException;
    public abstract void testDisconnectedGraph() throws InterruptedException;
    public abstract void testRandomGraph() throws InterruptedException;

    /*
    Creates a graph that has an integer node that separates two components, which each also contain 2 integer nodes
     */
    Graph createNodeBlockerGraph() {
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

    Graph createDisconnectedGraph() {
        Graph nodeBlockerGraph = createNodeBlockerGraph();
        Graph cliqueTwo = createClique(2, "clique1_");
        Graph cliqueThree = createClique(3, "clique2_");

        // make cliqueThree to be integral nodes
        for (Node node : cliqueThree.getNodes()) {
            node.setInteger(true);
        }

        Graph disconnectedGraph = graphUnion(nodeBlockerGraph, cliqueTwo);
        disconnectedGraph = graphUnion(disconnectedGraph, cliqueThree);

        return disconnectedGraph;
    }

    private Graph graphUnion(Graph graph1, Graph graph2) {
        List<Node> nodes = new ArrayList<>();
        List<Edge> edges = new ArrayList<>();

        nodes.addAll(graph1.getNodes());
        nodes.addAll(graph2.getNodes());

        edges.addAll(graph1.getEdges());
        edges.addAll(graph2.getEdges());

        return createGraph(nodes, edges);
    }

    protected Graph createClique(int size, String nodeNamePrefix) {
        List<Node> nodes = new ArrayList<>();
        List<Edge> edges = new ArrayList<>();

        for (int i = 0; i < size; i++) {
            nodes.add(createNode(nodeNamePrefix + i , false));
        }

        for (int i = 0; i < size; i++) {
            for (int j = i+1; j < size; j++) {
                edges.add(createEdge(nodes.get(i), nodes.get(j)));
            }
        }

        return createGraph(nodes, edges);
    }

    Graph createStarShapedGraph() {

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

    Graph createGraph(List<Node> nodes, List<Edge> edges) {
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


    Graph createRandomGraph() {
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

    Node createNode(String name, boolean integer) {
        Node node = new Node();
        node.setInteger(integer);
        node.setName(integer ? name + INTEGER_MARK : name);
        return node;
    }

    Edge createEdge(Node node1, Node node2) {
        return new Edge(node1, node2);
    }

    static void printResult(String parameterComputed, int result, int graphSize, String algoName) {
        LOGGER.debug(parameterComputed + ": " + result + " of " + graphSize + " nodes with " + algoName);
    }
}
