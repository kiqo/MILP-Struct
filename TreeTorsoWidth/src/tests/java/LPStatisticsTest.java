package tests.java;

import main.java.graph.Graph;
import main.java.lp.LPStatistics;
import main.java.lp.LPStatisticsFormatter;
import main.java.main.Configuration;
import main.java.parser.DualGraphGenerator;
import main.java.parser.IncidenceGraphGenerator;
import main.java.parser.PrimalGraphGenerator;
import org.junit.Assert;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.TimeoutException;

/**
 * Created by Verena on 21.05.2017.
 */
public class LPStatisticsTest extends StructuralParametersTest {
    private static final Logger LOGGER = LoggerFactory.getLogger(LPStatisticsTest.class);

    @Test
    public void testLPFromInputFile() throws TimeoutException, InterruptedException {
        lp = createLinearProgram("../input/tests/bienst2_small_test.mps");
        LPStatistics statistics = computeStatistics();
        checkResult(statistics);
    }

    private void checkResult(LPStatistics statistics) {
        Configuration.PRIMAL = true;
        Configuration.INCIDENCE = true;
        Configuration.DUAL = true;
        // lpDataHeader: "name;numVars;numCons;numIntVars;propIntVars;integerLP;minIntVars;maxIntVars;avgIntVars;avgVars;" +
        // "numBoundVars;minCoeff;maxCoeff;sizeObjFun;"
        // graphDataHeader = "numNodes;numIntNodes;propIntNodes;numEdges;density;minDegree;maxDegree;avgDegree;tw_lb;tw_ub;";
        Assert.assertEquals("bienst2;7;8;2;0,2857;false;0.0;2.0;1.0;2.0;" + //lpData
                "2;-74.0;1.0;1;" +
                "7;2;0,2857;12;0,5714;1;5;3,4286;0;0;0;0;0;" + // primalGraphData + td_ub;torso_lb;torso_ub;
                "15;2;0,1333;23;0,4107;2;6;3,0667;0;0;" + // incidenceGraphData
                "8;0;0,0000;22;0,7857;4;7;5,5000;0;0;" + // dualGraphData
                System.lineSeparator(), new LPStatisticsFormatter(statistics).csvFormat());
    }

    private LPStatistics computeStatistics() throws InterruptedException {
        Graph primalGraph = new PrimalGraphGenerator().linearProgramToGraph(lp);
        Graph incidenceGraph = new IncidenceGraphGenerator().linearProgramToGraph(lp);
        Graph dualGraph = new DualGraphGenerator().linearProgramToGraph(lp);
        LPStatistics statistics = new LPStatistics(lp);
        lp.setStatistics(statistics);
        statistics.computePrimalGraphData(primalGraph);
        statistics.computeIncidenceGraphData(incidenceGraph);
        statistics.computeDualGraphData(dualGraph);
        return statistics;
    }
}
