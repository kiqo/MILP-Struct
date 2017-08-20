package main.java.main;

import main.java.libtw.TorsoWidth;
import main.java.libtw.TreeDepth;
import main.java.lp.GraphData;
import main.java.lp.LinearProgram;
import main.java.main.Configuration;
import main.java.main.StructuralParametersComputation;
import main.java.parser.GraphTransformator;
import main.java.parser.PrimalGraphGenerator;
import nl.uu.cs.treewidth.input.GraphInput;
import nl.uu.cs.treewidth.ngraph.NGraph;

import java.io.IOException;

/**
 * Created by Verena on 19.08.2017.
 */
public class PrimalStructuralParametersComputation extends StructuralParametersComputation {

    public PrimalStructuralParametersComputation(String fileName) {
        super(fileName);
    }

    private void computeStructuralParameters(LinearProgram lp) throws IOException, InterruptedException {
        createGraph(lp);
    }

    private void createGraph(LinearProgram lp) throws InterruptedException {
        graph = new PrimalGraphGenerator().linearProgramToGraph(lp);
        checkInterrupted();
        lp.getStatistics().computePrimalGraphData(graph);
        nGraph = GraphTransformator.graphToNGraph(graph);
    }

    public void computeTreeWidthLowerBound() throws InterruptedException {
        int treewidthUpperBound = computeTWLowerBound(nGraph);
        lp.getStatistics().getPrimalGraphData().setTreewidthLB(treewidthUpperBound);
    }

    public void computeTreeWidthUpperBound() throws InterruptedException {
        int treewidthUpperBound = computeTWLowerBound(nGraph);
        lp.getStatistics().getPrimalGraphData().setTreewidthUB(treewidthUpperBound);
    }

    public void computeTorsoWidth() throws InterruptedException {
        if (Configuration.TORSO_WIDTH) {
            computeTorsoWidthOnPrimalGraph(nGraph, lp);
        }
    }

    public void computeTreeDepth() throws InterruptedException {
        if (Configuration.TREE_DEPTH) {
            checkInterrupted();
            computeTreeDepth(nGraph, lp.getStatistics().getPrimalGraphData());
        }
    }

    /*
    Computes TorsoWidth of a graph and sets the result to the lp statistics
 */
    private static void computeTorsoWidthOnPrimalGraph(NGraph<GraphInput.InputData> g, LinearProgram linearProgram) throws InterruptedException {
        t.reset();
        t.start();
        TorsoWidth<GraphInput.InputData> torsoWidthAlgo = new TorsoWidth();
        torsoWidthAlgo.setInput(g);
        torsoWidthAlgo.run();
        int torsoWidthLowerBound = torsoWidthAlgo.getLowerBound();
        int torsoWidthUpperBound = torsoWidthAlgo.getUpperBound();
        t.stop();
        long millisecondsPassed = t.getTime();
        printTimingInfo("LB TorsoWidth", torsoWidthLowerBound, g.getNumberOfVertices(), torsoWidthAlgo.getName());
        printTimingInfo("UB TorsoWidth", torsoWidthUpperBound, g.getNumberOfVertices(), torsoWidthAlgo.getName());
        GraphData primalGraphData = linearProgram.getStatistics().getPrimalGraphData();
        primalGraphData.setTorsoWidthUB(torsoWidthUpperBound);
        primalGraphData.setTorsoWidthLB(torsoWidthLowerBound);
    }

    private static void computeTreeDepth(NGraph<GraphInput.InputData> g, GraphData graphData) throws InterruptedException {
        t.reset();
        t.start();
        TreeDepth<GraphInput.InputData> treeDepthAlgo = new TreeDepth<>();
        treeDepthAlgo.setInput(g);
        treeDepthAlgo.run();
        int treeDepthUpperBound = treeDepthAlgo.getUpperBound();
        t.stop();
        printTimingInfo("UB TreeDepth", treeDepthUpperBound, g.getNumberOfVertices(), treeDepthAlgo.getName());
        graphData.setTreeDepthUB(treeDepthUpperBound);
    }

}
