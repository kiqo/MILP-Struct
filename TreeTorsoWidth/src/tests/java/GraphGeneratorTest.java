package tests.java;

import main.java.Configuration;
import main.java.graph.Edge;
import main.java.graph.Graph;
import main.java.graph.Node;
import main.java.lp.MatrixEntry;
import main.java.lp.MatrixRow;
import main.java.parser.IncidenceGraphGenerator;
import main.java.parser.PrimalGraphGenerator;
import org.junit.Assert;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.junit.Test;

import java.io.IOException;
import java.util.concurrent.TimeoutException;

import static org.junit.Assert.*;

/**
 * Created by Verena on 09.03.2017.
 */
public class GraphGeneratorTest extends GraphTest {

    private static final Logger LOGGER = LoggerFactory.getLogger(GraphGeneratorTest.class);

    @Test
    public void testLinearProgramToPrimalGraph() throws TimeoutException, InterruptedException {
        if (lp == null) {
            parseInputLP();
        }
        Graph primalGraph = createPrimalGraph(lp);

        assertNotNull(primalGraph.getEdges());
        assertNotNull(primalGraph.getNodes());
        assertNotNull(primalGraph.getNeighbourNodes());

        boolean nodeFound;
        for (String variableName : lp.getVariables().keySet()) {
            nodeFound = false;
            for (Node node : primalGraph.getNodes()) {
                if (node.getName().equals(variableName)) {
                    nodeFound = true;
                    break;
                }
            }
            assertFalse("There does not exist a node for the variable " + variableName, !nodeFound);
        }

        correctGraph(primalGraph);
    }

    @Test
    public void testLinearProgramToIncidenceGraph() throws InterruptedException {
        if (lp == null) {
            parseInputLP();
        }

        Graph incidenceGraph = new IncidenceGraphGenerator().linearProgramToGraph(lp);

        // number of nodes
        int numNodesExpected = lp.getConstraints().size() + lp.getVariables().size();
        if (Configuration.OBJ_FUNCTION) {
            numNodesExpected++;
        }
        Assert.assertEquals(numNodesExpected, incidenceGraph.getNodes().size(), 0);

        // objective function also in the nodes
        Node objFuncNode = new Node();
        objFuncNode.setName(lp.getObjectiveFunction().getName());
        if (Configuration.OBJ_FUNCTION) {
            Assert.assertTrue(incidenceGraph.getNodes().contains(objFuncNode));
        }
        Assert.assertNotNull(incidenceGraph.getNodes().get(0).getName());

        // check all edges
        for (MatrixRow matrixRow : lp.getConstraints()) {
            Node constraintNode = new Node(matrixRow.getName());
            Assert.assertTrue(incidenceGraph.getNodes().contains(constraintNode));
            Assert.assertEquals(constraintNode.getName(), matrixRow.getName());

            for (MatrixEntry matrixEntry : matrixRow.getEntries()) {
                Node variableNode = new Node(matrixEntry.getVariable().getName());
                Assert.assertTrue(incidenceGraph.getNodes().contains(variableNode));
                Assert.assertTrue(incidenceGraph.getNeighbourNodes().get(constraintNode.getName()).contains(variableNode));
                Assert.assertTrue(incidenceGraph.getNeighbourNodes().get(variableNode.getName()).contains(constraintNode));
                Edge edge = new Edge(variableNode, constraintNode);
                Assert.assertTrue(incidenceGraph.getEdges().contains(edge));
            }
        }

        correctGraph(incidenceGraph);
    }

    private void correctGraph(Graph graph) {
        for (int i = 0; i < graph.getNodes().size(); i++) {
            for (int j = i+1; j < graph.getNodes().size(); j++) {
                if (graph.getNodes().get(i).equals(graph.getNodes().get(j))) {
                    assertFalse("The same nodes stored in graph.getNodes()", true);
                }
            }
        }

        for (int i = 0; i < graph.getEdges().size(); i++) {
            for (int j = i+1; j < graph.getEdges().size(); j++) {
                if (graph.getEdges().get(i).equals(graph.getEdges().get(j))) {
                    assertFalse("The same edges stored in graph.getEdges()", true);
                }
            }
        }


        for (int i = 0; i < graph.getEdges().size(); i++) {
            Node edge1node1 = graph.getEdges().get(i).getNode1();
            Node edge1node2 = graph.getEdges().get(i).getNode2();
            for (int j = i+1; j < graph.getEdges().size(); j++) {
                Node edge2node1 = graph.getEdges().get(j).getNode1();
                Node edge2node2 = graph.getEdges().get(j).getNode2();
                if (edge1node1.equals(edge2node1) && edge1node2.equals(edge2node2)
                        || edge1node1.equals(edge2node2) && edge1node2.equals(edge2node1)) {
                    assertFalse("The same edges (with exchanged node1/node2) stored in graph.getEdges()", true);
                }
            }
        }
    }
}