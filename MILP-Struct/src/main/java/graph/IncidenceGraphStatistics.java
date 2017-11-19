package main.java.graph;

import main.java.lp.LPData;

/**
 * Created by Verena on 22.08.2017.
 */
public class IncidenceGraphStatistics extends GraphStatistics {

    public void computeGraphData(Graph incidenceGraph) {
        this.graphData = computeGeneralGraphData(incidenceGraph);
        computeDensity();

    }

    protected void computeDensity() {
        // define the density for the incidence graph to be num edges / left side * right side of the bipartite graph
        // i.e. the number of edges divided by the maximum possible number of edges
        LPData lpData = lpStatistics.getLinearProgramData();
        graphData.density = (double) (graphData.numEdges) / ((double) lpData.numConstraints * (double) lpData.numVariables);
    }
}
