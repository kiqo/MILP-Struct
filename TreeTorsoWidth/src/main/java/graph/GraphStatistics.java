package main.java.graph;

import main.java.lp.LPStatistics;

import java.util.List;
import java.util.Map;

/**
 * Created by Verena on 22.08.2017.
 */
public abstract class GraphStatistics {
    protected GraphData graphData;

    public abstract void computeGraphData(Graph graph);
    protected abstract void computeDensity();

    public void setLpStatistics(LPStatistics lpStatistics) {
        this.lpStatistics = lpStatistics;
    }

    LPStatistics lpStatistics;

    public GraphData getGraphData() {
        return graphData;
    }

    protected GraphData computeGeneralGraphData(Graph graph) {
        GraphData graphData = new GraphData();
        graphData.numNodes = graph.getNodes().size();
        int numIntegerNodes = 0;
        for (Node node : graph.getNodes()) {
            if (node.isInteger()) {
                numIntegerNodes++;
            }
        }
        graphData.numIntegerNodes = numIntegerNodes;
        graphData.proportionIntegerNodes = (double) numIntegerNodes / (double) graphData.numNodes;
        graphData.numEdges = graph.getEdges().size();

        int sumDegree = 0;
        int minDegree = Integer.MAX_VALUE;
        int maxDegree = Integer.MIN_VALUE;
        for (Map.Entry<String, List<Node>> entry : graph.getNeighbourNodes().entrySet()) {
            int degree = entry.getValue().size();
            sumDegree += degree;
            if (degree < minDegree) {
                minDegree = degree;
            }
            if (degree > maxDegree) {
                maxDegree = degree;
            }
        }
        graphData.minDegree = minDegree;
        graphData.maxDegree = maxDegree;
        graphData.avgDegree = (double) sumDegree / (double) graphData.numNodes;

        return graphData;
    }
}
