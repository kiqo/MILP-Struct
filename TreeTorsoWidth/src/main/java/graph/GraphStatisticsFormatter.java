package main.java.graph;

import main.java.main.Configuration;

import java.text.DecimalFormat;

/**
 * Created by Verena on 22.08.2017.
 */
public class GraphStatisticsFormatter {
    private static final String LINE_SEPARATOR = System.lineSeparator();
    private final GraphStatistics primalGraphStatistics;
    private final GraphStatistics incidenceGraphStatistics;
    private final GraphStatistics dualGraphStatistics;

    public GraphStatisticsFormatter(GraphStatistics primalGraphStatistics, GraphStatistics incidenceGraphStatistics, GraphStatistics dualGraphStatistics) {
        this.primalGraphStatistics = primalGraphStatistics;
        this.incidenceGraphStatistics = incidenceGraphStatistics;
        this.dualGraphStatistics = dualGraphStatistics;
    }

    public String csvFormat() {
        StringBuilder sb = new StringBuilder();
        if (Configuration.PRIMAL) {
            formatPrimalGraphData(sb);
        }
        if (Configuration.INCIDENCE) {
            formatIncidenceGraphData(sb);
        }
        if (Configuration.DUAL) {
            formatDualGraphData(sb);
        }
        return sb.toString();
    }

    private void formatDualGraphData(StringBuilder sb) {
        GraphData dualGraphData = dualGraphStatistics.getGraphData();
        formatGraphData(sb, dualGraphData);
    }

    private void formatIncidenceGraphData(StringBuilder sb) {
        GraphData incidenceGraphData = incidenceGraphStatistics.getGraphData();
        formatGraphData(sb, incidenceGraphData);
    }

    private void formatPrimalGraphData(StringBuilder sb) {
        GraphData primalGraphData = primalGraphStatistics.getGraphData();
        formatGraphData(sb, primalGraphData);
        if (primalGraphData.getTreeDepthUB() != Integer.MAX_VALUE) {
            sb.append(primalGraphData.getTreeDepthUB());
        }
        sb.append(";");
        if (primalGraphData.getTorsoWidthLB() != Integer.MIN_VALUE) {
            sb.append(primalGraphData.getTorsoWidthLB());
        }
        sb.append(";");
        if (primalGraphData.getTorsoWidthUB() != Integer.MAX_VALUE) {
            sb.append(primalGraphData.getTorsoWidthUB());
        }
        sb.append(";");
    }

    private void formatGraphData(StringBuilder sb, GraphData graphData) {
        if (graphData != null) {
            sb.append(graphData.numNodes).append(";");
            sb.append(graphData.numIntegerNodes).append(";");
            sb.append(new DecimalFormat("0.0000").format(graphData.proportionIntegerNodes)).append(";");
            sb.append(graphData.numEdges).append(";");
            sb.append(new DecimalFormat("0.0000").format(graphData.density)).append(";");
            sb.append(graphData.minDegree).append(";");
            sb.append(graphData.maxDegree).append(";");
            sb.append(new DecimalFormat("0.0000").format(graphData.avgDegree)).append(";");
            sb.append(graphData.numComponents).append(";");
            if (graphData.getTreewidthLB() != Integer.MIN_VALUE) {
                sb.append(graphData.getTreewidthLB()).append(";");
            } else {
                sb.append(";");
            }
            if (graphData.getTreewidthUB() != Integer.MAX_VALUE) {
                sb.append(graphData.getTreewidthUB()).append(";");
            } else {
                sb.append(";");
            }
        } else {
            sb.append("no result;;;;;;;;;;;");
        }
    }
}
