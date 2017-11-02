package tests.java;

import main.java.graph.*;
import main.java.lp.LinearProgram;
import main.java.main.Configuration;
import main.java.parser.DualGraphGenerator;
import main.java.parser.GraphGenerator;
import main.java.parser.IncidenceGraphGenerator;
import main.java.parser.PrimalGraphGenerator;
import org.junit.Assert;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.TimeoutException;

/**
 * Tests whether the statistics about a graph representation are computed and presented correctly
 */
public class GraphStatisticsTest extends StructuralParametersTest{
    private static final Logger LOGGER = LoggerFactory.getLogger(LPStatisticsTest.class);

    @Test
    public void testLPFromInputFile() throws TimeoutException, InterruptedException {
        lp = createLinearProgram("../input/tests/bienst2_small_test.mps");
        Configuration.PRIMAL = true;
        Configuration.INCIDENCE = true;
        Configuration.DUAL = true;

        GraphStatistics primalStatistics = computeStatistics(lp, new PrimalGraphGenerator(), new PrimalGraphStatistics());
        GraphStatistics incidenceStatistics = computeStatistics(lp, new IncidenceGraphGenerator(), new IncidenceGraphStatistics());
        GraphStatistics dualStatistics = computeStatistics(lp, new DualGraphGenerator(), new DualGraphStatistics());

        checkResult(primalStatistics, incidenceStatistics, dualStatistics);
    }

    private void checkResult(GraphStatistics primalStatistics, GraphStatistics incidenceGraphStatistics, GraphStatistics dualGraphStatistics) {
        Assert.assertEquals(
                "7;2;0,2857;12;0,5714;1;5;3,4286;0;0;0;0;0;" + // graphData + td_ub;torso_lb;torso_ub;
                "15;2;0,1333;23;0,4107;2;6;3,0667;0;0;" + // incidenceGraphData
                "8;0;0,0000;22;0,7857;4;7;5,5000;0;0;" + // dualGraphData
                System.lineSeparator(), new GraphStatisticsFormatter(primalStatistics, incidenceGraphStatistics, dualGraphStatistics).csvFormat());
    }

    private GraphStatistics computeStatistics(LinearProgram lp, GraphGenerator graphGenerator, GraphStatistics graphStatistics) throws InterruptedException {
        Graph graph = graphGenerator.linearProgramToGraph(this.lp);
        graphStatistics.setLpStatistics(lp.getStatistics());
        graphStatistics.computeGraphData(graph);
        return graphStatistics;
    }
}
