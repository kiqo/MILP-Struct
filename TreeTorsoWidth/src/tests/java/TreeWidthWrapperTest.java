package tests.java;

import main.java.algo.TreeWidthWrapper;
import nl.uu.cs.treewidth.input.GraphInput;
import nl.uu.cs.treewidth.ngraph.NGraph;
import org.junit.Assert;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by Verena on 19.08.2017.
 */
public class TreeWidthWrapperTest extends StructuralParametersTest {

    private static final Logger LOGGER = LoggerFactory.getLogger(TreeWidthWrapperTest.class);

    @Test
    public void testLowerBound() throws InterruptedException {
        lp = createLinearProgram("../input/tests/bienst2_small_test_components.mps");
        NGraph<GraphInput.InputData> primalGraph = createNGraph(createPrimalGraph(lp));

        int lowerBound = TreeWidthWrapper.computeLowerBound(primalGraph);
        int lowerBoundWithComponents = TreeWidthWrapper.computeLowerBoundWithComponents(primalGraph);

        Assert.assertEquals(5, lowerBoundWithComponents);
        Assert.assertEquals(5, lowerBound);
    }

    @Test
    public void testUpperBound() throws InterruptedException {
        lp = createLinearProgram("../input/tests/bienst2_small_test_components.mps");
        NGraph<GraphInput.InputData> nGraph = createNGraph(createPrimalGraph(lp));

        int upperBound = TreeWidthWrapper.computeUpperBound(nGraph);
        int upperBoundWithComponents = TreeWidthWrapper.computeUpperBoundWithComponents(nGraph);

        Assert.assertEquals(5, upperBound);
        Assert.assertEquals(5, upperBoundWithComponents);
    }
}
