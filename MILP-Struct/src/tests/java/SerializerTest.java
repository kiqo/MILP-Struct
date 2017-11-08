package tests.java;

import main.java.algo.LPInputData;
import main.java.graph.*;
import main.java.main.Configuration;
import main.java.main.Serializer;
import main.java.parser.PrimalGraphGenerator;
import nl.uu.cs.treewidth.input.GraphInput;
import nl.uu.cs.treewidth.ngraph.NGraph;
import nl.uu.cs.treewidth.ngraph.NVertex;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SerializerTest extends GraphTest {

    private static final Logger LOGGER = LoggerFactory.getLogger(SerializerTest.class);

    private NGraph<GraphInput.InputData> nGraph;
    private GraphStatistics statistics;

    @Before
    public void createGraphAndStatistics() throws InterruptedException {
        lp = createLinearProgram("../input/tests/bienst2_small_test.mps");
        Graph graph = new PrimalGraphGenerator().linearProgramToGraph(lp);
        this.nGraph = createNGraph(graph);
        statistics = new PrimalGraphStatistics();
        statistics.setLpStatistics(lp.getStatistics());
        statistics.computeGraphData(graph);

        Serializer.serializeToFile(nGraph, "bienst2_small_test_deserialize_primal.ser");
        Serializer.serializeToFile(statistics, "bienst2_small_test_deserialize_primalStatistics.ser");

        Configuration.PRIMAL = true;
        Configuration.INCIDENCE = false;
        Configuration.DUAL = false;
    }

    @Test
    public void testSerializeToAndFromFile() throws InterruptedException {
        Serializer.serializeToFile(nGraph, "bienst2_small_test_primal.ser");
        Serializer.serializeToFile(statistics, "bienst2_small_test_primalStatistics.ser");
        NGraph<GraphInput.InputData> primalGraphDeserialized = (NGraph<GraphInput.InputData>) Serializer.deserializeFromFile( "bienst2_small_test_primal.ser");
        GraphStatistics primalStatisticsDeserialized = (GraphStatistics) Serializer.deserializeFromFile( "bienst2_small_test_primalStatistics.ser");

        assertSameGraph(nGraph, primalGraphDeserialized);
        assertSameStatistics(statistics, primalStatisticsDeserialized);
    }

    private void assertSameGraph(NGraph<GraphInput.InputData> primalGraph, NGraph<GraphInput.InputData> primalGraphDeserialized) {
        Assert.assertEquals(primalGraph.getNumberOfVertices(), primalGraphDeserialized.getNumberOfVertices());
        Assert.assertEquals(primalGraph.getNumberOfEdges(), primalGraphDeserialized.getNumberOfEdges());
        Assert.assertEquals(primalGraph.getComponents().size(), primalGraphDeserialized.getComponents().size());
        for (NVertex<GraphInput.InputData> vertex : primalGraph) {
            boolean vertexFound = false;
            for (NVertex<GraphInput.InputData> vertexDeserialized : primalGraphDeserialized) {
                if (vertex.data.name.equals(vertexDeserialized.data.name)) {
                    vertexFound = true;
                    assertSameData(vertex, vertexDeserialized);
                    assertSameNeighbours(vertex, vertexDeserialized);
                }
            }
            if (!vertexFound) {
                Assert.fail("Vertex " + vertex.data.name + " was not found");
            }
        }
    }

    private void assertSameData(NVertex<GraphInput.InputData> vertex, NVertex<GraphInput.InputData> vertexDeserialized) {
        Assert.assertEquals(vertex.data.id, vertexDeserialized.data.id);
        Assert.assertEquals(((LPInputData) vertex.data).isInteger(), ((LPInputData) vertexDeserialized.data).isInteger());
        Assert.assertEquals(((LPInputData) vertex.data).isNodeHandled(), ((LPInputData) vertexDeserialized.data).isNodeHandled());
    }

    private void assertSameNeighbours(NVertex<GraphInput.InputData> vertex, NVertex<GraphInput.InputData> vertexDeserialized) {
        Assert.assertEquals(vertex.getNumberOfNeighbors(), vertexDeserialized.getNumberOfNeighbors());
        for (NVertex<GraphInput.InputData> neighbour : vertex) {
            boolean neighbourFound = false;
            for (NVertex<GraphInput.InputData> neighbourDeserialized : vertexDeserialized) {
                if (neighbour.data.name.equals(neighbourDeserialized.data.name)) {
                    neighbourFound = true;
                }
            }
            if (!neighbourFound) {
                Assert.fail("Neighbour " + neighbour.data.name + " of vertex " + vertex.data.name + " was not found");
            }
        }
    }


    private void assertSameStatistics(GraphStatistics graphStatistics, GraphStatistics graphStatisticsDeserialized) {
        Assert.assertEquals(
                "7;2;0,2857;12;0,5714;1;5;3,4286;0;0;0;0;0;0;" // graphData + td_ub;torso_lb;torso_ub;
                , new GraphStatisticsFormatter(graphStatistics, null, null).csvFormat());

        Assert.assertEquals(
                "7;2;0,2857;12;0,5714;1;5;3,4286;0;0;0;0;0;0;" // graphData + td_ub;torso_lb;torso_ub;
                , new GraphStatisticsFormatter(graphStatisticsDeserialized, null, null).csvFormat());
    }

}
