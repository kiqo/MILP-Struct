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
        StringBuilder sbColumn = new StringBuilder();
        StringBuilder sbDescription = new StringBuilder();
        sbDescription.append("Linear program statistics;;;;;;;;;;;");
        sbColumn.append("name;numVars;numCons;numIntVars;propIntVars;integerLP;minIntVars;maxIntVars;avgIntVars;avgVars;sizeObjFun;");

        String graphDataHeader = "numNodes;numIntNodes;propIntNodes;numEdges;density;minDegree;maxDegree;avgDegree;numComponents;tw_lb;tw_ub;";
        if (Configuration.PRIMAL) {
            sbDescription.append("Primal graph statistics").append(";;;;;;;;;;;;;;");
            sbColumn.append(graphDataHeader).append("td_ub;torso_lb;torso_ub;");
        }
        if (Configuration.INCIDENCE) {
            sbDescription.append("Incidence graph statistics").append(";;;;;;;;;;;");
            sbColumn.append(graphDataHeader);
        }
        if (Configuration.DUAL) {
            sbDescription.append("Dual graph statistics").append(";;;;;;;;;;;;");
            sbColumn.append(graphDataHeader);
        }
        sbColumn.append("totalTime");
        sbDescription.append(LINE_SEPARATOR);
        sbColumn.append(LINE_SEPARATOR);
        return sbDescription.append(sbColumn).toString();
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
        sb.append(linearProgramData.sizeObjectiveFunction).append(";");
    }
}
