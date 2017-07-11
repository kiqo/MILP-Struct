package main.java;

import main.java.graph.Graph;
import main.java.libtw.TorsoWidth;
import main.java.libtw.TreeDepth;
import main.java.lp.GraphData;
import main.java.lp.LinearProgram;
import main.java.parser.GraphGenerator;
import main.java.parser.GraphTransformator;
import main.java.parser.MILPParser;
import nl.uu.cs.treewidth.algorithm.*;
import nl.uu.cs.treewidth.input.GraphInput;
import nl.uu.cs.treewidth.ngraph.NGraph;
import nl.uu.cs.treewidth.ngraph.NTDBag;
import nl.uu.cs.treewidth.ngraph.NVertex;
import nl.uu.cs.treewidth.ngraph.NVertexOrder;
import nl.uu.cs.treewidth.timing.Stopwatch;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

import java.lang.reflect.InvocationTargetException;
import java.util.concurrent.*;

/**
 * Created by Verena on 22.05.2017.
 */
public class StructuralParametersComputation implements Callable<String> {


    private static final Logger LOGGER = LoggerFactory.getLogger(StructuralParametersComputation.class);

    private static final Stopwatch t = new Stopwatch();
    private static String fileName;
    private StringBuilder sb = new StringBuilder();

    public StructuralParametersComputation (String fileName) {
        this.fileName = fileName;
    }

    private static void computeTreeWidth(NGraph<GraphInput.InputData> g) {

        // first calculate an upper bound
        GreedyFillIn<GraphInput.InputData> ubAlgo = new GreedyFillIn<GraphInput.InputData>();
        ubAlgo.setInput( g );
        ubAlgo.run();
        int upperbound = ubAlgo.getUpperBound();

        // then calculate the exact treewidth
        TreewidthDP<GraphInput.InputData> twdp = new TreewidthDP<GraphInput.InputData>( upperbound );
        twdp.setInput( g );
        twdp.run();
        int treewidth = twdp.getTreewidth();
    }

    private static void computeTreeDecomposition(NGraph<GraphInput.InputData> g) {
        MaximumMinimumDegreePlusLeastC<GraphInput.InputData> lbAlgo = new MaximumMinimumDegreePlusLeastC<GraphInput.InputData>();
        lbAlgo.setInput( g );
        lbAlgo.run();
        int lowerbound = lbAlgo.getLowerBound();

        GreedyFillIn<GraphInput.InputData> ubAlgo = new GreedyFillIn<GraphInput.InputData>();
        ubAlgo.setInput( g );
        ubAlgo.run();
        int upperbound = ubAlgo.getUpperBound();

        NVertexOrder<GraphInput.InputData> permutation = null;

        if( lowerbound == upperbound ) {
            permutation = ubAlgo.getPermutation();
        } else {
            QuickBB<GraphInput.InputData> qbbAlgo = new QuickBB<GraphInput.InputData>();
            qbbAlgo.setInput( g );
            qbbAlgo.run();
            permutation = qbbAlgo.getPermutation();
        }

        PermutationToTreeDecomposition<GraphInput.InputData> convertor = new PermutationToTreeDecomposition<GraphInput.InputData>( permutation );
        convertor.setInput( g );
        convertor.run();
        NGraph<NTDBag<GraphInput.InputData>> decomposition = convertor.getDecomposition();

        // needs GraphViz installed
        decomposition.printGraph( true, true );
    }

    private void computeStructuralParameters(String fileName) throws IOException, InterruptedException {
        // parse input file
        MILPParser milpParser = new MILPParser();
        LinearProgram lp = milpParser.parseMPS(fileName, false);

        checkInterrupted();

        GraphGenerator graphGenerator = new GraphGenerator();
        Graph primalGraph = null;
        Graph incidenceGraph = null;

        // NGraph for using libtw
        NGraph<GraphInput.InputData> gPrimal = null, gIncidence = null;
        GraphTransformator graphTransformator = new GraphTransformator();

        if (Configuration.PRIMAL) {
            primalGraph = graphGenerator.linearProgramToPrimalGraph(lp);
            checkInterrupted();
            lp.getStatistics().computePrimalGraphData(primalGraph);
            gPrimal = graphTransformator.graphToNGraph(primalGraph);
        }
        if (Configuration.INCIDENCE) {
            incidenceGraph = graphGenerator.linearProgramToIncidenceGraph(lp);
            checkInterrupted();
            lp.getStatistics().computeIncidenceGraphData(incidenceGraph);
            gIncidence = graphTransformator.graphToNGraph(incidenceGraph);
        }
        checkInterrupted();

        // Displays and writes graph to file system using GraphViz but too slow for large graphs
        // g.printGraph(true, true);

        // compute lower bound for tree width
        if (Configuration.LOWER_BOUND) {
            if (Configuration.PRIMAL) {
                int treewidthLowerBoundPrimal = computeTWLowerBound(gPrimal);
                lp.getStatistics().getPrimalGraphData().setTreewidthLB(treewidthLowerBoundPrimal);
            }
            checkInterrupted();
            if (Configuration.INCIDENCE) {
                int treewidthLowerBoundIncidence = computeTWLowerBound(gIncidence);
                lp.getStatistics().getIncidenceGraphData().setTreewidthLB(treewidthLowerBoundIncidence);
            }
        }

        // compute upper bound for tree width
        if (Configuration.UPPER_BOUND) {
            // checkInterrupted(); - checked in the upper bound algorithm
            if (Configuration.PRIMAL) {
                int treewidthUpperBoundPrimal = computeTWUpperBound(gPrimal);
                lp.getStatistics().getPrimalGraphData().setTreewidthUB(treewidthUpperBoundPrimal);
            }
            if (Configuration.INCIDENCE) {
                int treewidthUpperBoundIncidence = computeTWUpperBound(gIncidence);
                lp.getStatistics().getIncidenceGraphData().setTreewidthUB(treewidthUpperBoundIncidence);
            }
        }

        if (Configuration.TORSO_WIDTH && Configuration.PRIMAL) {
            // checkInterrupted(); - checked in the torso width algorithm
            computeTorsoWidth(gPrimal, lp);
        }

        if (Configuration.TREE_DEPTH) {
            checkInterrupted();
            if (Configuration.PRIMAL) {
                computeTreeDepth(gPrimal, lp.getStatistics().getPrimalGraphData());
                checkInterrupted();
            }
        }

        // append statistics of current lp
        if (Configuration.OUTPUT_FILE.endsWith(".csv")) {
            sb.append(lp.getStatistics().csvFormat(Configuration.PRIMAL, Configuration.INCIDENCE));
        } else {
            sb.append(lp.getStatistics().shortDescription());
        }
    }

    private static void checkInterrupted() throws InterruptedException {
        if (Thread.currentThread().isInterrupted()) {
            throw new InterruptedException();
        }
    }

    private static UpperBound<GraphInput.InputData> createUpperBound() {
        UpperBound upperBoundAlg = null;
        try {
            upperBoundAlg = (UpperBound) Configuration.UPPER_BOUND_ALG.getConstructor().newInstance();
        } catch (InstantiationException | NoSuchMethodException | InvocationTargetException | IllegalAccessException e) {
            LOGGER.error("", e);
        }
        return upperBoundAlg;
    }


    private static LowerBound<GraphInput.InputData> createLowerBound() {
        LowerBound<GraphInput.InputData> lowerBoundAlg = null;
        try {
            lowerBoundAlg = (LowerBound<GraphInput.InputData>) Configuration.LOWER_BOUND_ALG.getConstructor().newInstance();
        } catch (InstantiationException | NoSuchMethodException | InvocationTargetException | IllegalAccessException e) {
            LOGGER.error(e.getMessage());
        }
        return lowerBoundAlg;
    }

    /*
        Computes TorsoWidth of a graph and sets the result to the lp statistics
     */
    private static void computeTorsoWidth(NGraph<GraphInput.InputData> g, LinearProgram linearProgram) throws InterruptedException {
        t.reset();
        t.start();

        TorsoWidth<GraphInput.InputData> torsoWidthAlgo = new TorsoWidth<>(createUpperBound(), createLowerBound());
        torsoWidthAlgo.setInput(g);
        torsoWidthAlgo.run();
        int torsoWidthLowerBound = torsoWidthAlgo.getLowerBound();
        int torsoWidthUpperBound = torsoWidthAlgo.getUpperBound();
        t.stop();
        long millisecondsPassed = t.getTime();
        printTimingInfo(fileName, "LB TorsoWidth", torsoWidthLowerBound, g.getNumberOfVertices(), torsoWidthAlgo.getName(), millisecondsPassed/1000);
        printTimingInfo(fileName, "UB TorsoWidth", torsoWidthUpperBound, g.getNumberOfVertices(), torsoWidthAlgo.getName(), millisecondsPassed/1000);

        GraphData primalGraphData = linearProgram.getStatistics().getPrimalGraphData();
        primalGraphData.setTorsoWidthUB(torsoWidthUpperBound);
        primalGraphData.setTorsoWidthLB(torsoWidthLowerBound);

        // get statistics of torso graph
        int minDegree = Integer.MAX_VALUE;
        int maxDegree = Integer.MIN_VALUE;

        for (NVertex<GraphInput.InputData> vertex : g) {
            int degree = vertex.getNumberOfNeighbors();
            if (degree < minDegree) {
                minDegree = degree;
            }
            if (degree > maxDegree) {
                maxDegree = degree;
            }
        }
        primalGraphData.setTorsoMinDegree(minDegree);
        primalGraphData.setTorsoMaxDegree(maxDegree);
    }

    /*
    Computes TreeDepthLB of a graph and sets the result to the lp statistics
    */
    private static void computeTreeDepth(NGraph<GraphInput.InputData> g, GraphData graphData) throws InterruptedException {
        t.reset();
        t.start();
        TreeDepth<GraphInput.InputData> treeDepthAlgo = new TreeDepth<>();
        treeDepthAlgo.setInput(g);
        treeDepthAlgo.run();
        int treeDepthUpperBound = treeDepthAlgo.getUpperBound();
        t.stop();

        printTimingInfo(fileName, "UB TreeDepth", treeDepthUpperBound, g.getNumberOfVertices(), treeDepthAlgo.getName(), t.getTime()/1000);

        graphData.setTreeDepthUB(treeDepthUpperBound);
    }

    private static void printTimingInfo(String fileName, String algorithm, int result, int graphSize, String algoName, long secondsPassed) {
        LOGGER.debug(fileName + " " + algorithm + ": " + result + " of  " + graphSize + " nodes with " + algoName
                + ", time: " + secondsPassed + "s");
    }

    private static int computeTWUpperBound(NGraph<GraphInput.InputData> g) throws InterruptedException {
        t.reset();
        t.start();
        UpperBound<GraphInput.InputData> ubAlgo = createUpperBound();
        ubAlgo.setInput(g);
        ubAlgo.run();
        int upperbound = ubAlgo.getUpperBound();
        t.stop();
        long millisecondsPassed = t.getTime();
        printTimingInfo(fileName, "UB TreeWidth", upperbound, g.getNumberOfVertices(), ubAlgo.getName(), millisecondsPassed/1000);

        return upperbound;
    }

    private static int computeTWLowerBound(NGraph<GraphInput.InputData> g) throws InterruptedException {
        t.reset();
        t.start();
        LowerBound<GraphInput.InputData> lbAlgo = createLowerBound();
        lbAlgo.setInput(g);
        lbAlgo.run();
        int lowerbound = lbAlgo.getLowerBound();
        t.stop();
        long millisecondsPassed = t.getTime();
        printTimingInfo(fileName, "LB TreeWidth", lowerbound, g.getNumberOfVertices(), lbAlgo.getName(), millisecondsPassed/1000);

        return lowerbound;
    }

    @Override
    public String call() throws IOException {
        try {
            computeStructuralParameters(fileName);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt(); // not needed?
        }
        return sb.toString();
    }
}
