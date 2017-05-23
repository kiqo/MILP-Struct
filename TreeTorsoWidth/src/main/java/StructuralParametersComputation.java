package main.java;

import main.java.graph.Graph;
import main.java.libtw.TorsoWidth;
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
    private static StringBuilder sb;

    public StructuralParametersComputation (String fileName, StringBuilder sb) {
        this.fileName = fileName;
        this.sb = sb;
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

    private static void computeStructuralParameters(String fileName, StringBuilder sb) throws IOException, InterruptedException {
        // parse input file
        MILPParser milpParser = new MILPParser();
        LinearProgram lp = milpParser.parseMPS(fileName, false);

        checkInterrupted();

        // generate primal graph
        GraphGenerator graphGenerator = new GraphGenerator();
        Graph primalGraph = null;
        Graph incidenceGraph = null;
        if (Configuration.PRIMAL) {
            primalGraph = graphGenerator.linearProgramToPrimalGraph(lp);
            lp.getStatistics().computePrimalGraphData(primalGraph);
        }
        if (Configuration.INCIDENCE) {
            incidenceGraph = graphGenerator.linearProgramToIncidenceGraph(lp);
            lp.getStatistics().computeIncidenceGraphData(incidenceGraph);
        }

        checkInterrupted();

        // generate NGraph for using libtw
        NGraph<GraphInput.InputData> gPrimal, gIncidence;
        GraphTransformator graphTransformator = new GraphTransformator();
        gPrimal = graphTransformator.graphToNGraph(primalGraph);
        checkInterrupted();
        gIncidence = graphTransformator.graphToNGraph(incidenceGraph);
        checkInterrupted();

        // just gets stuck for large instances - TODO try on server
        // g.printGraph(true, true);

        if (Configuration.LOWER_BOUND) {
            int treewidthLowerBoundPrimal = computeTWLowerBound(gPrimal);
            checkInterrupted();
            int treewidthLowerBoundIncidence = computeTWLowerBound(gIncidence);

            lp.getStatistics().getPrimalGraphData().setTreewidthLB(treewidthLowerBoundPrimal);
            lp.getStatistics().getIncidenceGraphData().setTreewidthLB(treewidthLowerBoundIncidence);
        }

        if (Configuration.UPPER_BOUND) {
            // checkInterrupted(); - checked in the upper bound algorithm
            int treewidthUpperBoundPrimal = computeTWUpperBound(gPrimal);
            int treewidthUpperBoundIncidence = computeTWUpperBound(gIncidence);
            lp.getStatistics().getPrimalGraphData().setTreewidthUB(treewidthUpperBoundPrimal);
            lp.getStatistics().getIncidenceGraphData().setTreewidthUB(treewidthUpperBoundIncidence);
        }

        if (Configuration.TORSO_WIDTH && Configuration.PRIMAL) {
            // checkInterrupted(); - checked in the torso width algorithm
            computeTorsoWidth(gPrimal, lp);
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
        LOGGER.debug("LB TorsoWidth: " + torsoWidthLowerBound + " of " + g.getNumberOfVertices() + " nodes of " + torsoWidthAlgo.getName()
                + ", time: " + millisecondsPassed / 1000 + "s");
        LOGGER.debug("UB TorsoWidth: " + torsoWidthUpperBound + " of " + g.getNumberOfVertices() + " nodes of " + torsoWidthAlgo.getName()
                + ", time: " + millisecondsPassed / 1000 + "s");
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

    private static int computeTWUpperBound(NGraph<GraphInput.InputData> g) throws InterruptedException {
        t.reset();
        t.start();
        UpperBound<GraphInput.InputData> ubAlgo = createUpperBound();
        ubAlgo.setInput(g);
        ubAlgo.run();
        int upperbound = ubAlgo.getUpperBound();
        t.stop();
        long millisecondsPassed = t.getTime();
        LOGGER.debug("UB: " + upperbound + " of " + g.getNumberOfVertices() + " nodes " + " of " + ubAlgo.getName()
                + ", time: " + millisecondsPassed / 1000 + "s");
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
        LOGGER.debug("LB: " + lowerbound + " of " + g.getNumberOfVertices() + " nodes " + " of " + lbAlgo.getName()
                + ", time: " + millisecondsPassed / 1000 + "s");
        return lowerbound;
    }

    @Override
    public String call() throws IOException {
        try {
            computeStructuralParameters(fileName, sb);
        } catch (InterruptedException e) {
            LOGGER.warn("Warning: Interrupt Exception for " + fileName);
            Thread.currentThread().interrupt(); // not needed?
        }
        return sb.toString();
    }
}
