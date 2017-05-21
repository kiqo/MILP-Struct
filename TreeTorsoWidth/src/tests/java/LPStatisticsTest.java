package tests.java;

import main.java.graph.Graph;
import main.java.lp.LPStatistics;
import main.java.lp.LinearProgram;
import main.java.parser.GraphGenerator;
import main.java.parser.MILPParser;
import org.junit.Assert;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

/**
 * Created by Verena on 21.05.2017.
 */
public class LPStatisticsTest {
    private static final Logger LOGGER = LoggerFactory.getLogger(LPStatisticsTest.class);


    @Test
    public void testLPFromInputFile() {
        MILPParser milpParser = new MILPParser();
        LinearProgram lp = null;
        try {
            lp = milpParser.parseMPS("../input/tests/bienst2_small_test.mps", false);
        } catch (IOException e) {
            LOGGER.error("", e);
        }
        GraphGenerator graphGenerator = new GraphGenerator();
        Graph primalGraph = graphGenerator.linearProgramToPrimalGraph(lp);
        Graph incidenceGraph = graphGenerator.linearProgramToIncidenceGraph(lp);
        LPStatistics statistics = new LPStatistics(lp);
        lp.setStatistics(statistics);
        statistics.computePrimalGraphData(primalGraph);
        statistics.computeIncidenceGraphData(incidenceGraph);
/*
        sb.append("name;numVars;numCons;numIntVars;propIntVars;integerLP;minIntVars;maxIntVars;avgIntVars;avgVars;" +
                "numBoundVars;minCoeff;maxCoeff;sizeObjFun;" +
                "numNodes;numIntNodes;propIntNodes;numEdges;density;minDegree;maxDegree;avgDegree;tw_lb;tw_ub;torso_lb;torso_ub;");*/
        Assert.assertEquals("bienst2;7;8;2;0,29;false;0.0;2.0;1.0;3.0;" +
                "2;-74.0;1.0;1;7;2;0,29;13;0,62;1;6;3,71;0;0;0;0;" + System.lineSeparator(), statistics.csvFormat());
    }
}
