package main.java.main;

import main.java.graph.Graph;
import main.java.libtw.TorsoWidth;
import main.java.libtw.TreeDepth;
import main.java.libtw.TreeWidthWrapper;
import main.java.lp.GraphData;
import main.java.lp.LPStatistics;
import main.java.lp.LinearProgram;
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
        if (Configuration.PRIMAL) {
            gPrimal = computePrimalGraph(lp);
        }
        if (Configuration.INCIDENCE) {
            gIncidence = computeIncidenceGraph(lp);
        }
        if (Configuration.DUAL) {
            gDual = computeDualGraph(lp);
        }
        checkInterrupted();
        lpStatistics = lp.getStatistics();
        computeTWLowerBounds();
        computeTWUpperBounds();
        computeTorsoWidthOnPrimalGraph();
        computeTreeDepthOnPrimalGraph();
        computeStatistics();
    }

    private NGraph<GraphInput.InputData> computeDualGraph(LinearProgram lp) throws InterruptedException {
        Graph dualGraph;
        NGraph<GraphInput.InputData> gDual;
        dualGraph = new DualGraphGenerator().linearProgramToGraph(lp);
        checkInterrupted();
        lp.getStatistics().computeDualGraphData(dualGraph);
        gDual = GraphTransformator.graphToNGraph(dualGraph);
        return gDual;
    }

    private NGraph<GraphInput.InputData> computePrimalGraph(LinearProgram lp) throws InterruptedException {
        Graph primalGraph;
        NGraph<GraphInput.InputData> gPrimal;
        primalGraph = new PrimalGraphGenerator().linearProgramToGraph(lp);
        checkInterrupted();
        lp.getStatistics().computePrimalGraphData(primalGraph);
        gPrimal = GraphTransformator.graphToNGraph(primalGraph);
        return gPrimal;
    }

    private NGraph<GraphInput.InputData> computeIncidenceGraph(LinearProgram lp) throws InterruptedException {
        Graph incidenceGraph;
        NGraph<GraphInput.InputData> gIncidence;
        incidenceGraph = new IncidenceGraphGenerator().linearProgramToGraph(lp);
        checkInterrupted();
        lp.getStatistics().computeIncidenceGraphData(incidenceGraph);
        gIncidence = GraphTransformator.graphToNGraph(incidenceGraph);
        return gIncidence;
    }

    private LinearProgram parseLinearProgram(String fileName) throws IOException, InterruptedException {
        MILPParser milpParser = new MILPParser();
        LinearProgram lp = milpParser.parseMPS(fileName);
        checkInterrupted();
        return lp;
    }

    private static void checkInterrupted() throws InterruptedException {
        if (Thread.currentThread().isInterrupted()) {
            throw new InterruptedException();
        }
    }

    private void computeTWLowerBounds() throws InterruptedException {
        if (Configuration.LOWER_BOUND) {
            if (Configuration.PRIMAL) {
                int treewidthLowerBoundPrimal = computeTWLowerBound(gPrimal);
                lpStatistics.getPrimalGraphData().setTreewidthLB(treewidthLowerBoundPrimal);
            }
            checkInterrupted();
            if (Configuration.INCIDENCE) {
                int treewidthLowerBoundIncidence = computeTWLowerBound(gIncidence);
                lpStatistics.getIncidenceGraphData().setTreewidthLB(treewidthLowerBoundIncidence);
            }
            checkInterrupted();
            if (Configuration.DUAL) {
                int treewidthLowerBoundDual = computeTWLowerBound(gDual);
                lpStatistics.getDualGraphData().setTreewidthLB(treewidthLowerBoundDual);
            }
        }
    }

    private int computeTWLowerBound(NGraph<GraphInput.InputData> g) throws InterruptedException {
        startTimer();
        int lowerbound = TreeWidthWrapper.computeLowerBoundWithComponents(g);
        stopTimer();
        printTimingInfo("LB TreeWidth", lowerbound, g.getNumberOfVertices(), Configuration.LOWER_BOUND_ALG.getName());
        return lowerbound;
    }

    private void computeTWUpperBounds() throws InterruptedException {
        if (Configuration.UPPER_BOUND) {
            if (Configuration.PRIMAL) {
                int treewidthUpperBoundPrimal = computeTWUpperBound(gPrimal);
                lpStatistics.getPrimalGraphData().setTreewidthUB(treewidthUpperBoundPrimal);
            }            
            checkInterrupted();
            if (Configuration.INCIDENCE) {
                int treewidthUpperBoundIncidence = computeTWUpperBound(gIncidence);
                lpStatistics.getIncidenceGraphData().setTreewidthUB(treewidthUpperBoundIncidence);
            }
            checkInterrupted();
            if (Configuration.DUAL) {
                int treewidthUpperBoundDual = computeTWUpperBound(gDual);
                lpStatistics.getDualGraphData().setTreewidthUB(treewidthUpperBoundDual);
            }
        }
    }

    private int computeTWUpperBound(NGraph<GraphInput.InputData> g) throws InterruptedException {
        startTimer();
        int upperbound = TreeWidthWrapper.computeUpperBoundWithComponents(g);
        stopTimer();
        printTimingInfo("UB TreeWidth", upperbound, g.getNumberOfVertices(), Configuration.UPPER_BOUND_ALG.getName());
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
        printTimingInfo("LB TorsoWidth", torsoWidthLowerBound, g.getNumberOfVertices(), torsoWidthAlgo.getName());
        printTimingInfo("UB TorsoWidth", torsoWidthUpperBound, g.getNumberOfVertices(), torsoWidthAlgo.getName());
        GraphData primalGraphData = lpStatistics.getPrimalGraphData();
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
                computeTreeDepth(gPrimal, lpStatistics.getPrimalGraphData());
            }
        }
    }

    private static void computeTreeDepth(NGraph<GraphInput.InputData> g, GraphData graphData) throws InterruptedException {
        TreeDepth<GraphInput.InputData> treeDepthAlgo = new TreeDepth<>();
        runAlgo(g, treeDepthAlgo);
        int treeDepthUpperBound = treeDepthAlgo.getUpperBound();
        printTimingInfo("UB TreeDepth", treeDepthUpperBound, g.getNumberOfVertices(), treeDepthAlgo.getName());
        graphData.setTreeDepthUB(treeDepthUpperBound);
    }

    private void computeStatistics() {
        sb.append(lpStatistics.csvFormat(Configuration.PRIMAL, Configuration.INCIDENCE, Configuration.DUAL));
    }

    private static void printTimingInfo(String algorithm, int result, int graphSize, String algoName) {
        LOGGER.debug(fileName + " " + algorithm + ": " + result + " of  " + graphSize + " nodes with " + algoName
                + ", time: " + t.getTime() / 1000 + "s");
    }
}
