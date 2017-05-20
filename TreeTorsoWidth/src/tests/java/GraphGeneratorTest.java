package tests.java;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;

import main.java.graph.Graph;
import main.java.graph.Node;
import main.java.lp.LinearProgram;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.junit.Test;
import main.java.parser.GraphGenerator;
import main.java.parser.MILPParser;

import java.io.IOException;

/**
 * Created by Verena on 09.03.2017.
 */
public class GraphGeneratorTest {

    private static final Logger LOGGER = LoggerFactory.getLogger(GraphGeneratorTest.class);

    @Test
    public void testLinearProgramToPrimalGraph() {
        MILPParser milpParser = new MILPParser();
        LinearProgram lp = null;
        try {
            lp = milpParser.parseMPS("./../input/benchmarks/bienst2.mps", false);
        } catch (IOException e) {
            LOGGER.error("", e);
        }

        GraphGenerator graphGenerator = new GraphGenerator();
        Graph primalGraph = graphGenerator.linearProgramToPrimalGraph(lp);

        // assert statements
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

        for (int i = 0; i < primalGraph.getNodes().size(); i++) {
            for (int j = i+1; j < primalGraph.getNodes().size(); j++) {
                if (primalGraph.getNodes().get(i).equals(primalGraph.getNodes().get(j))) {
                    assertFalse("The same nodes stored in primalGraph.getNodes()", true);
                }
            }
        }

        for (int i = 0; i < primalGraph.getEdges().size(); i++) {
            for (int j = i+1; j < primalGraph.getEdges().size(); j++) {
                if (primalGraph.getEdges().get(i).equals(primalGraph.getEdges().get(j))) {
                    assertFalse("The same edges stored in primalGraph.getEdges()", true);
                }
            }
        }


        for (int i = 0; i < primalGraph.getEdges().size(); i++) {
            Node edge1node1 = primalGraph.getEdges().get(i).getNode1();
            Node edge1node2 = primalGraph.getEdges().get(i).getNode2();
            for (int j = i+1; j < primalGraph.getEdges().size(); j++) {
                Node edge2node1 = primalGraph.getEdges().get(j).getNode1();
                Node edge2node2 = primalGraph.getEdges().get(j).getNode2();
                if (edge1node1.equals(edge2node1) && edge1node2.equals(edge2node2)
                        || edge1node1.equals(edge2node2) && edge1node2.equals(edge2node1)) {
                    assertFalse("The same edges (with exchanged node1/node2) stored in primalGraph.getEdges()", true);
                }
            }
        }
    }
}