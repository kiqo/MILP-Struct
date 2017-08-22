package main.java.lp;

import main.java.lp.GraphData;
import main.java.lp.LPData;
import main.java.lp.LPStatistics;
import main.java.lp.LinearProgram;
import main.java.main.Configuration;

import java.text.DecimalFormat;

/**
 * Created by Verena on 21.08.2017.
 */
public class LPStatisticsFormatter {
    private static final String LINE_SEPARATOR = System.lineSeparator();

    private LPStatistics lpStatistics;
    private LinearProgram linearProgram;
    private LPData linearProgramData;

    public LPStatisticsFormatter(LPStatistics lpStatistics) {
        this.lpStatistics = lpStatistics;
        this.linearProgram = lpStatistics.getLinearProgram();
        this.linearProgramData = lpStatistics.getLinearProgramData();
    }

    public static String csvFormatHeader() {
        StringBuilder sb = new StringBuilder();
        sb.append("name;numVars;numCons;numIntVars;propIntVars;integerLP;minIntVars;maxIntVars;avgIntVars;avgVars;" +
                "numBoundVars;minCoeff;maxCoeff;sizeObjFun;");

        String graphDataHeader = "numNodes;numIntNodes;propIntNodes;numEdges;density;minDegree;maxDegree;avgDegree;tw_lb;tw_ub;";
        if (Configuration.PRIMAL) {
            sb.append(graphDataHeader).append("td_ub;torso_lb;torso_ub;");
        }
        if (Configuration.INCIDENCE) {
            sb.append(graphDataHeader);
        }
        if (Configuration.DUAL) {
            sb.append(graphDataHeader);
        }
        sb.append(LINE_SEPARATOR);
        return sb.toString();
    }

    public String csvFormat() {
        StringBuilder sb = new StringBuilder();
        formatLinearProgramData(sb);
        if (Configuration.PRIMAL) {
            formatPrimalGraphData(sb);
        }
        if (Configuration.INCIDENCE) {
            formatIncidenceGraphData(sb);
        }
        if (Configuration.DUAL) {
            formatDualGraphData(sb);
        }
        sb.append(LINE_SEPARATOR);
        return sb.toString();
    }

    private void formatDualGraphData(StringBuilder sb) {
        GraphData dualGraphData = lpStatistics.getDualGraphData();
        formatGraphData(sb, dualGraphData);
    }

    private void formatIncidenceGraphData(StringBuilder sb) {
        GraphData incidenceGraphData = lpStatistics.getIncidenceGraphData();
        formatGraphData(sb, incidenceGraphData);
    }

    private void formatPrimalGraphData(StringBuilder sb) {
        GraphData primalGraphData = lpStatistics.getPrimalGraphData();
        formatGraphData(sb, primalGraphData);
        if(primalGraphData.getTreeDepthUB()!=Integer.MAX_VALUE) {
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

    private void formatLinearProgramData(StringBuilder sb) {
        sb.append(linearProgram.getName()).append(";");
        sb.append(linearProgramData.numVariables).append(";");
        sb.append(linearProgramData.numConstraints).append(";");
        sb.append(linearProgramData.numIntegerVariables).append(";");
        sb.append(new DecimalFormat("0.0000").format(linearProgramData.proportionIntegerVariables)).append(";");
        sb.append(linearProgramData.isIntegerLP).append(";");
        sb.append(linearProgramData.minIntegerVariables).append(";");
        sb.append(linearProgramData.maxIntegerVariables).append(";");
        sb.append(linearProgramData.avgIntegerVariables).append(";");
        sb.append(linearProgramData.avgVariables).append(";");
        sb.append(linearProgramData.numBoundVariables).append(";");
        sb.append(linearProgramData.minCoefficient).append(";");
        sb.append(linearProgramData.maxCoefficient).append(";");
        sb.append(linearProgramData.sizeObjectiveFunction).append(";");
    }

    private void formatGraphData(StringBuilder sb, GraphData graphData) {
        sb.append(graphData.numNodes).append(";");
        sb.append(graphData.numIntegerNodes).append(";");
        sb.append(new DecimalFormat("0.0000").format(graphData.proportionIntegerNodes)).append(";");
        sb.append(graphData.numEdges).append(";");
        sb.append(new DecimalFormat("0.0000").format(graphData.density)).append(";");
        sb.append(graphData.minDegree).append(";");
        sb.append(graphData.maxDegree).append(";");
        sb.append(new DecimalFormat("0.0000").format(graphData.avgDegree)).append(";");
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
    }
}
