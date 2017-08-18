package tests.java;

import main.java.graph.Graph;
import main.java.lp.LPStatistics;
import main.java.lp.LinearProgram;
import main.java.parser.GraphGenerator;
import main.java.parser.IncidenceGraphGenerator;
import main.java.parser.MILPParser;
import main.java.parser.PrimalGraphGenerator;
import org.junit.Assert;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.concurrent.TimeoutException;

/**
 * Created by Verena on 21.05.2017.
 */
public class LPStatisticsTest {
    private static final Logger LOGGER = LoggerFactory.getLogger(LPStatisticsTest.class);


    @Test
    public void testLPFromInputFile() throws TimeoutException, InterruptedException {
        MILPParser milpParser = new MILPParser();
        LinearProgram lp = null;
        try {
            lp = milpParser.parseMPS("../input/tests/bienst2_small_test.mps", false);
        } catch (IOException e) {
            LOGGER.error("", e);
        }
        Graph primalGraph = new PrimalGraphGenerator().linearProgramToGraph(lp);
        Graph incidenceGraph = new IncidenceGraphGenerator().linearProgramToGraph(lp);
        LPStatistics statistics = new LPStatistics(lp);
        lp.setStatistics(statistics);
        statistics.computePrimalGraphData(primalGraph);
        statistics.computeIncidenceGraphData(incidenceGraph);

        Assert.assertEquals("bienst2;7;8;2;0,2857;false;0.0;2.0;1.0;3.0;" +
                "2;-74.0;1.0;1;7;2;0,2857;13;0,6190;1;6;3,7143;0;0;0;0;0;" +
                "15;2;0,1333;25;0,4464;2;8;3,3333;0;0;"  + System.lineSeparator(), statistics.csvFormat(true, true));
    }
}
