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
import sun.security.krb5.Config;

import java.io.IOException;

import java.util.concurrent.*;

/**
 * The structural parameter computation for one (M)ILP instance. It returns a String containing the results
 * for the current (M)ILP instance. In case a timeout occurred, i.e. the current thread was cancelled, the structural
 * parameters computation returns without a result.
 */
public class StructuralParametersComputation extends ThreadExecutor implements Callable<String> {

    private static final Logger LOGGER = LoggerFactory.getLogger(StructuralParametersComputation.class);

    private static String filePath;
    private static String fileName;
    private StringBuilder sb = new StringBuilder();
    private static final Stopwatch t = new Stopwatch();
    private static final Stopwatch totalTimer = new Stopwatch();
    private NGraph<GraphInput.InputData> gPrimal = null, gIncidence = null, gDual = null;
    private LPStatistics lpStatistics;
    private GraphStatistics primalGraphStatistics = new PrimalGraphStatistics();
    private GraphStatistics incidenceGraphStatistics = new IncidenceGraphStatistics();
    private GraphStatistics dualGraphStatistics = new DualGraphStatistics();

    public StructuralParametersComputation (String filePath) {
        this.filePath = filePath;
        int lastIndexOfSlashOrBackslash = filePath.lastIndexOf("\\");
        if (filePath.lastIndexOf("/") > lastIndexOfSlashOrBackslash) {
            lastIndexOfSlashOrBackslash = filePath.lastIndexOf("/");
        }
        this.fileName = filePath.substring(lastIndexOfSlashOrBackslash+1, filePath.length()-4);
    }

    @Override
    public String call() throws IOException {
        try {
            computeStructuralParameters(filePath);
        } catch (InterruptedException e) {
        }
        return sb.toString();
    }

    private void computeStructuralParameters(String fileName) throws IOException, InterruptedException {
        startTimer(totalTimer);
        LinearProgram lp = parseLinearProgram(fileName);
        LOGGER.debug("Finished parsing linear program");
        computeGraphRepresentations(lp);
        LOGGER.debug("Finished computing graph representations");
        computeTWLowerBounds();
        computeTWUpperBounds();
        computeTorsoWidthOnPrimalGraph();
        computeTreeDepthOnPrimalGraph();
        formatLPStatistics();
        formatGraphStatistics();
        stopTimer(totalTimer);
        addTimingInformation();
    }

    private LinearProgram parseLinearProgram(String fileName) throws IOException, InterruptedException {
        MILPParser milpParser = new MILPParser();
        LinearProgram lp = milpParser.parseMPS(fileName);
        checkInterrupted();
        lpStatistics = lp.getStatistics();
        return lp;
    }

    private void computeGraphRepresentations(LinearProgram lp) throws InterruptedException {
        if (Configuration.PRIMAL) {
            if (!deserializeGraphRepresentation(GraphType.PRIMAL)) {
                gPrimal = computeGraphRepresentation(lp, new PrimalGraphGenerator(), primalGraphStatistics, GraphType.PRIMAL);
            }
        }
        if (Configuration.INCIDENCE) {
            if (!deserializeGraphRepresentation(GraphType.INCIDENCE)) {
                gIncidence = computeGraphRepresentation(lp, new IncidenceGraphGenerator(), incidenceGraphStatistics, GraphType.INCIDENCE);
            }
        }
        if (Configuration.DUAL) {
            if (!deserializeGraphRepresentation(GraphType.DUAL)) {
                gDual = computeGraphRepresentation(lp, new DualGraphGenerator(), dualGraphStatistics, GraphType.DUAL);
            }
        }
    }

    private NGraph<GraphInput.InputData> computeGraphRepresentation(LinearProgram lp, GraphGenerator graphGenerator, GraphStatistics graphStatistics, GraphType graphType) throws InterruptedException {
        NGraph<GraphInput.InputData> graph = computeNGraph(lp, graphGenerator, graphStatistics);
        graph.addComment(graphType.toString());
        LOGGER.debug("Finished " + graphType.toString() + " graph representation");
        Serializer.serializeToFile(graph, Serializer.getDefaultGraphPath(fileName, graphType));
        Serializer.serializeToFile(graphStatistics, Serializer.getDefaultStatisticsPath(fileName, graphType));
        LOGGER.debug("Finished serializing " + graphType.toString() + " graph representation to file");
        checkInterrupted();
        return graph;
    }

    private boolean deserializeGraphRepresentation(GraphType graphType) {
        NGraph<GraphInput.InputData> graph = (NGraph<GraphInput.InputData>) Serializer.deserializeFromFile(Serializer.getDefaultGraphPath(fileName,graphType));
        GraphStatistics statistics = (GraphStatistics) Serializer.deserializeFromFile(Serializer.getDefaultStatisticsPath(fileName, graphType));

        if (graph == null || statistics == null) {
            return false;
        }
        switch (graphType) {
            case PRIMAL: gPrimal = graph; primalGraphStatistics = statistics; break;
            case DUAL: gDual = graph; dualGraphStatistics = statistics; break;
            case INCIDENCE: gIncidence = graph; incidenceGraphStatistics = statistics; break;
            default: return false;
        }
        LOGGER.debug("Deserialized from " + Serializer.getDefaultGraphPath(fileName,graphType));
        return true;
    }

    public NGraph<GraphInput.InputData> computeNGraph(LinearProgram lp, GraphGenerator graphGenerator, GraphStatistics graphStatistics) throws InterruptedException {
        Graph graph;
        NGraph<GraphInput.InputData> nGraph;
        graph = graphGenerator.linearProgramToGraph(lp);
        checkInterrupted();
        nGraph = GraphTransformator.graphToNGraph(graph);
        computeGraphStatistics(graphStatistics, graph, nGraph);
        return nGraph;
    }

    private void computeGraphStatistics(GraphStatistics graphStatistics, Graph graph, NGraph<GraphInput.InputData> nGraph) {
        graphStatistics.setLpStatistics(lpStatistics);
        graphStatistics.computeGraphData(graph);
        graphStatistics.getGraphData().setNumComponents(nGraph.getComponents().size());
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
        startTimer(t);
        int lowerbound = TreeWidthWrapper.computeLowerBoundWithComponents(g);
        stopTimer(t);
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
        startTimer(t);
        int upperbound = TreeWidthWrapper.computeUpperBoundWithComponents(g);
        stopTimer(t);
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
        startTimer(t);
        algorithm.setInput(g);
        algorithm.run();
        stopTimer(t);
    }

    private static void startTimer(Stopwatch t) {
        t.reset();
        t.start();
    }

    private static void stopTimer(Stopwatch t) {
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

    private void addTimingInformation() {
        sb.append(totalTimer.getTime() / 1000 + "s");
        sb.append(System.lineSeparator());
    }

    private static void printTimingInfo(NGraph<GraphInput.InputData> graph, String algorithm, int result, String algoName) {
        LOGGER.info(fileName + " " + graph.getComments() + " " + algorithm + ": " + result + " of " + graph.getNumberOfVertices() + " nodes with " + algoName
                + ", time: " + t.getTime() / 1000 + "s");
    }
}
