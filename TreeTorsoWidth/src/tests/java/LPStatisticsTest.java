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
        // "numBoundVars;sizeObjFun;"
        Assert.assertEquals("bienst2;7;8;2;0,2857;false;0.0;2.0;1.0;2.0;1;",
                            new LPStatisticsFormatter(statistics).csvFormat());
    }

    private LPStatistics computeStatistics() throws InterruptedException {
        LPStatistics statistics = new LPStatistics(lp);
        lp.setStatistics(statistics);
        return statistics;
    }
}
