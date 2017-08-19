package tests.java;

import main.java.graph.Graph;
import main.java.libtw.TreeWidthWrapper;
import main.java.lp.LinearProgram;
import main.java.parser.GraphTransformator;
import main.java.parser.MILPParser;
import main.java.parser.PrimalGraphGenerator;
import nl.uu.cs.treewidth.input.GraphInput;
import nl.uu.cs.treewidth.ngraph.NGraph;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

/**
 * Created by Verena on 19.08.2017.
 */
public class TreeWidthWrapperTest extends StructuralParametersTest {

    private static final Logger LOGGER = LoggerFactory.getLogger(TreeWidthWrapperTest.class);

    @Test
    public void testLowerBound() throws InterruptedException {
        LinearProgram lp = createLinearProgram("../input/tests/bienst2_small_test.mps");
        NGraph<GraphInput.InputData> primalGraph = createNGraph(createPrimalGraph(lp));

        int lowerBound = TreeWidthWrapper.computeLowerBound(primalGraph);
        int lowerBoundWithComponents = TreeWidthWrapper.computeLowerBoundWithComponents(primalGraph);
    }

    @Test
    public void testUpperBound() throws InterruptedException {
        LinearProgram lp = createLinearProgram("../input/tests/bienst2_small_test.mps");
        NGraph<GraphInput.InputData> nGraph = createNGraph(createPrimalGraph(lp));

        int upperBound = TreeWidthWrapper.computeUpperBound(nGraph);
        int upperBoundWithComponents = TreeWidthWrapper.computeUpperBoundWithComponents(nGraph);
    }
}
