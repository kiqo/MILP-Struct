package tests.java;

import main.java.Configuration;
import main.java.graph.Graph;
import main.java.lp.LinearProgram;
import main.java.parser.GraphTransformator;
import main.java.parser.MILPParser;
import main.java.parser.PrimalGraphGenerator;
import nl.uu.cs.treewidth.input.GraphInput;
import nl.uu.cs.treewidth.ngraph.NGraph;
import org.junit.Before;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

/**
 * Created by Verena on 19.08.2017.
 */
public class StructuralParametersTest {
    private static final Logger LOGGER = LoggerFactory.getLogger(StructuralParametersTest.class);

    protected LinearProgram lp = null;

    @Before
    public void init() {
        try {
            Configuration.UPPER_BOUND_ALG = Class.forName(Configuration.DEFAULT_UPPER_BOUND_ALG);
            Configuration.LOWER_BOUND_ALG = Class.forName(Configuration.DEFAULT_LOWER_BOUND_ALG);
        } catch (ClassNotFoundException e) {
            LOGGER.error("", e);
        }
    }

    protected LinearProgram createLinearProgram(String filePath) {
        MILPParser milpParser = new MILPParser();
        LinearProgram lp = null;
        try {
            lp = milpParser.parseMPS(filePath, false);
        } catch (IOException e) {
            LOGGER.error("", e);
        }
        return lp;
    }

    protected static NGraph<GraphInput.InputData> createNGraph(Graph graph) throws InterruptedException {
        NGraph<GraphInput.InputData> nGraph = GraphTransformator.graphToNGraph(graph);
        return nGraph;
    }

    protected Graph createPrimalGraph(LinearProgram lp) throws InterruptedException {
        Graph primalGraph = new PrimalGraphGenerator().linearProgramToGraph(lp);
        return primalGraph;
    }
}
