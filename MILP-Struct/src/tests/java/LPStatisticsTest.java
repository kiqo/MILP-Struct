package tests.java;

import main.java.lp.LPStatistics;
import main.java.lp.LPStatisticsFormatter;
import main.java.main.Configuration;
import org.junit.Assert;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.TimeoutException;

/**
 * Tests whether the LPStatistics are generated correctly
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
        Assert.assertEquals("bienst2;7;8;2;0,2857;false;0;2;1,1;2,9;1;",
                            new LPStatisticsFormatter(statistics).csvFormat());
    }

    private LPStatistics computeStatistics() throws InterruptedException {
        LPStatistics statistics = new LPStatistics(lp);
        lp.setStatistics(statistics);
        return statistics;
    }
}
