package main.java.main;

import main.java.graph.*;
import main.java.algo.TorsoWidth;
import main.java.algo.TreeDepth;
import main.java.algo.TreeWidthWrapper;
import main.java.lp.LPStatistics;
import main.java.lp.LinearProgram;
import main.java.lp.LPStatisticsFormatter;
import main.java.parser.*;
import nl.uu.cs.treewidth.algorithm.*;
import nl.uu.cs.treewidth.input.GraphInput;
import nl.uu.cs.treewidth.ngraph.NGraph;
import nl.uu.cs.treewidth.timing.Stopwatch;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

import java.util.concurrent.*;

/**
 * Created by Verena on 22.05.2017.
 */
public class StructuralParametersComputation implements Callable<String> {

    private static final Logger LOGGER = LoggerFactory.getLogger(StructuralParametersComputation.class);

    private static String fileName;
    private StringBuilder sb = new StringBuilder();
    private static final Stopwatch t = new Stopwatch();
    private NGraph<GraphInput.InputData> gPrimal = null, gIncidence = null, gDual = null;
    private LPStatistics lpStatistics;
    private GraphStatistics primalGraphStatistics = new PrimalGraphStatistics();
    private GraphStatistics incidenceGraphStatistics = new IncidenceGraphStatistics();
    private GraphStatistics dualGraphStatistics = new DualGraphStatistics();

    public StructuralParametersComputation (String fileName) {
        this.fileName = fileName;
    }

    @Override
    public String call() throws IOException {
        try {
            computeStructuralParameters(fileName);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        return sb.toString();
    }

    private void computeStructuralParameters(String fileName) throws IOException, InterruptedException {
        LinearProgram lp = parseLinearProgram(fileName);
        computeGraphRepresentations(lp);
        computeTWLowerBounds();
        computeTWUpperBounds();
        computeTorsoWidthOnPrimalGraph();
        computeTreeDepthOnPrimalGraph();
        formatLPStatistics();
        formatGraphStatistics();
    }

    private LinearProgram parseLinearProgram(String fileName) throws IOException, InterruptedException {
        MILPParser milpParser = new MILPParser();
        LinearProgram lp = milpParser.parseMPS(fileName);
        checkInterrupted();
        lpStatistics = lp.getStatistics();
        return lp;
    }

    private static void checkInterrupted() throws InterruptedException {
        if (Thread.currentThread().isInterrupted()) {
            throw new InterruptedException();
        }
    }

    private void computeGraphRepresentations(LinearProgram lp) throws InterruptedException {
        if (Configuration.PRIMAL) {
            gPrimal = computeNGraph(lp, new PrimalGraphGenerator(), primalGraphStatistics);
            gPrimal.addComment("Primal-");
        }
        if (Configuration.INCIDENCE) {
            gIncidence = computeNGraph(lp, new IncidenceGraphGenerator(), incidenceGraphStatistics);
            gPrimal.addComment("Incidence-");
        }
        if (Configuration.DUAL) {
            gDual = computeNGraph(lp, new DualGraphGenerator(), dualGraphStatistics);
            gPrimal.addComment("Dual-");
        }
        checkInterrupted();
    }

    public NGraph<GraphInput.InputData> computeNGraph(LinearProgram lp, GraphGenerator graphGenerator, GraphStatistics graphStatistics) throws InterruptedException {
        Graph graph;
        NGraph<GraphInput.InputData> nGraph;
        graph = graphGenerator.linearProgramToGraph(lp);
        checkInterrupted();
        graphStatistics.setLpStatistics(lpStatistics);
        graphStatistics.computeGraphData(graph);
        nGraph = GraphTransformator.graphToNGraph(graph);
        return nGraph;
    }

    private void computeTWLowerBounds() throws InterruptedException {
        if (Configuration.LOWER_BOUND) {
            if (Configuration.PRIMAL) {
                int treewidthLowerBoundPrimal = computeTWLowerBound(gPrimal);
                primalGraphStatistics.getGraphData().setTreewidthLB(treewidthLowerBoundPrimal);
            }
            checkInterrupted();
            if (Configuration.INCIDENCE) {
                int treewidthLowerBoundIncidence = computeTWLowerBound(gIncidence);
                incidenceGraphStatistics.getGraphData().setTreewidthLB(treewidthLowerBoundIncidence);
            }
            checkInterrupted();
            if (Configuration.DUAL) {
                int treewidthLowerBoundDual = computeTWLowerBound(gDual);
                dualGraphStatistics.getGraphData().setTreewidthLB(treewidthLowerBoundDual);
            }
        }
    }

    private int computeTWLowerBound(NGraph<GraphInput.InputData> g) throws InterruptedException {
        startTimer();
        int lowerbound = TreeWidthWrapper.computeLowerBoundWithComponents(g);
        stopTimer();
        printTimingInfo(g, "LB TreeWidth", lowerbound, Configuration.LOWER_BOUND_ALG.getSimpleName());
        return lowerbound;
    }

    private void computeTWUpperBounds() throws InterruptedException {
        if (Configuration.UPPER_BOUND) {
            if (Configuration.PRIMAL) {
                int treewidthUpperBoundPrimal = computeTWUpperBound(gPrimal);
                primalGraphStatistics.getGraphData().setTreewidthUB(treewidthUpperBoundPrimal);
            }            
            checkInterrupted();
            if (Configuration.INCIDENCE) {
                int treewidthUpperBoundIncidence = computeTWUpperBound(gIncidence);
                incidenceGraphStatistics.getGraphData().setTreewidthUB(treewidthUpperBoundIncidence);
            }
            checkInterrupted();
            if (Configuration.DUAL) {
                int treewidthUpperBoundDual = computeTWUpperBound(gDual);
                dualGraphStatistics.getGraphData().setTreewidthUB(treewidthUpperBoundDual);
            }
        }
    }

    private int computeTWUpperBound(NGraph<GraphInput.InputData> g) throws InterruptedException {
        startTimer();
        int upperbound = TreeWidthWrapper.computeUpperBoundWithComponents(g);
        stopTimer();
        printTimingInfo(g, "UB TreeWidth", upperbound, Configuration.UPPER_BOUND_ALG.getSimpleName());
        return upperbound;
    }

    private void computeTorsoWidthOnPrimalGraph() throws InterruptedException {
        if (Configuration.TORSO_WIDTH && Configuration.PRIMAL) {
            computeTorsoWidthOnPrimalGraph(gPrimal);
        }
    }

    private void computeTorsoWidthOnPrimalGraph(NGraph<GraphInput.InputData> g) throws InterruptedException {
        TorsoWidth torsoWidthAlgo = new TorsoWidth();
        runAlgo(g, torsoWidthAlgo);
        int torsoWidthLowerBound = torsoWidthAlgo.getLowerBound();
        int torsoWidthUpperBound = torsoWidthAlgo.getUpperBound();
        printTimingInfo(g, "LB TorsoWidth", torsoWidthLowerBound, torsoWidthAlgo.getName());
        printTimingInfo(g, "UB TorsoWidth", torsoWidthUpperBound, torsoWidthAlgo.getName());
        GraphData primalGraphData = primalGraphStatistics.getGraphData();
        primalGraphData.setTorsoWidthUB(torsoWidthUpperBound);
        primalGraphData.setTorsoWidthLB(torsoWidthLowerBound);
    }

    private static void runAlgo(NGraph<GraphInput.InputData> g, Algorithm algorithm) throws InterruptedException {
        startTimer();
        algorithm.setInput(g);
        algorithm.run();
        stopTimer();
    }

    private static void startTimer() {
        t.reset();
        t.start();
    }

    private static void stopTimer() {
        t.stop();
    }

    private void computeTreeDepthOnPrimalGraph() throws InterruptedException {
        if (Configuration.TREE_DEPTH) {
            checkInterrupted();
            if (Configuration.PRIMAL) {
                computeTreeDepth(gPrimal, primalGraphStatistics.getGraphData());
            }
        }
    }

    private static void computeTreeDepth(NGraph<GraphInput.InputData> g, GraphData graphData) throws InterruptedException {
        TreeDepth<GraphInput.InputData> treeDepthAlgo = new TreeDepth<>();
        runAlgo(g, treeDepthAlgo);
        int treeDepthUpperBound = treeDepthAlgo.getUpperBound();
        printTimingInfo(g, "UB TreeDepth", treeDepthUpperBound, treeDepthAlgo.getName());
        graphData.setTreeDepthUB(treeDepthUpperBound);
    }

    private void formatLPStatistics() {
        sb.append(new LPStatisticsFormatter(lpStatistics).csvFormat());
    }

    private void formatGraphStatistics() {
        sb.append(new GraphStatisticsFormatter(primalGraphStatistics, incidenceGraphStatistics, dualGraphStatistics).csvFormat());
    }

    private static void printTimingInfo(NGraph<GraphInput.InputData> graph, String algorithm, int result, String algoName) {
        LOGGER.info(fileName + " " + graph.getComments() + algorithm + ": " + result + " of  " + graph.getNumberOfVertices() + " nodes with " + algoName
                + ", time: " + t.getTime() / 1000 + "s");
    }
}
