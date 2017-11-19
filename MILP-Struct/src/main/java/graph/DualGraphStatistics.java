package main.java.graph;

/**
 * Created by Verena on 22.08.2017.
 */
public class DualGraphStatistics extends GraphStatistics{

    @Override
    public void computeGraphData(Graph dualGraph) {
        this.graphData = computeGeneralGraphData(dualGraph);
        computeDensity();
    }

    @Override
    protected void computeDensity() {
        if (graphData.numNodes <= 1) {
            graphData.density = 0;
        } else {
            graphData.density = (2 * (double) graphData.numEdges) /  ((double) graphData.numNodes * (((double) graphData.numNodes) - 1.0));
        }
    }
}
