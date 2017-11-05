package main.java.graph;

/**
 * Created by Verena on 22.08.2017.
 */
public class PrimalGraphStatistics extends GraphStatistics {

    public void computeGraphData(Graph primalGraph) {
        super.graphData = super.computeGeneralGraphData(primalGraph);
        computeDensity();
    }

    protected void computeDensity() {
        if (graphData.numNodes <= 1) {
            graphData.density = 0;
        } else {
            graphData.density = (double) (2 * graphData.numEdges) / (double) (graphData.numNodes * (graphData.numNodes - 1));
        }
    }
}
