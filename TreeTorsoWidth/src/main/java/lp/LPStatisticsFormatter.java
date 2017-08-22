package main.java.lp;

import main.java.graph.GraphData;
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
        return sb.toString();
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
}
