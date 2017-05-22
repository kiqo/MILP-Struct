package main.java.lp;

import main.java.graph.Graph;
import main.java.graph.Node;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.text.DecimalFormat;
import java.util.List;
import java.util.Map;

/**
 * Created by Verena on 19.04.2017.
 */
public class LPStatistics {

    private static final Logger LOGGER = LoggerFactory.getLogger(LPStatistics.class);

    private static final String LINE_SEPARATOR = System.lineSeparator();
    private LinearProgram linearProgram;
    private LPData linearProgramData;
    private Graph primalGraph;
    private Graph incidenceGraph;
    private GraphData primalGraphData;

    public GraphData getIncidenceGraphData() {
        return incidenceGraphData;
    }

    public void setIncidenceGraphData(GraphData incidenceGraphData) {
        this.incidenceGraphData = incidenceGraphData;
    }

    private GraphData incidenceGraphData;

    public GraphData getPrimalGraphData() {
        return primalGraphData;
    }

    public LPStatistics(LinearProgram linearProgram) {
        this.linearProgram = linearProgram;
        computeLinearProgramData();
    }

    public void computePrimalGraphData(Graph primalGraph) {
        this.primalGraph = primalGraph;
        this.primalGraphData = computeGraphData(primalGraph);
    }

    public void computeIncidenceGraphData(Graph incidenceGraph) {
        this.incidenceGraph = incidenceGraph;
        this.incidenceGraphData = computeGraphData(incidenceGraph);
    }

    /*
    Sets general statistics about a graph (not tree- or torsowidth)
     */
    private GraphData computeGraphData(Graph graph) {
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
        if (graphData.numNodes <= 1) {
            graphData.density = 0;
        } else {
            graphData.density = (double) (2 * graphData.numEdges) / (double) (graphData.numNodes * (graphData.numNodes - 1));
        }

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

    private void computeLinearProgramData() {
        linearProgramData = new LPData();

        linearProgramData.numVariables = linearProgram.getVariables().size();

        // variable information
        int numInteger = 0;
        int numBoundVariables = 0;
        double minBoundValue = Double.MAX_VALUE;
        double maxBoundValue = Double.MIN_VALUE;
        for (Variable var : linearProgram.getVariables().values()) {
            if (var.isInteger()) {
                numInteger++;
            }
            if (var.getLowerBound() != null || var.getUpperBound() != null) {
                numBoundVariables++;
            }
            if (var.getLowerBound() != null) {
                if (var.isInteger() && (int)var.getLowerBound() < minBoundValue) {
                    minBoundValue = (int) var.getLowerBound();
                } else if (!var.isInteger() && (double)var.getLowerBound() < minBoundValue) {
                    minBoundValue = (double) var.getLowerBound();
                }
            }
            if (var.getUpperBound() != null) {
                if (var.isInteger() && (int)var.getUpperBound() > maxBoundValue) {
                    maxBoundValue = (int) var.getUpperBound();
                } else if (!var.isInteger() && (double)var.getUpperBound() > maxBoundValue) {
                    maxBoundValue = (double) var.getUpperBound();
                }
            }
        }
        linearProgramData.numIntegerVariables = numInteger;
        linearProgramData.isIntegerLP = (numInteger == linearProgramData.numVariables);
        linearProgramData.proportionIntegerVariables = (double) numInteger / (double) linearProgramData.numVariables;
        linearProgramData.numBoundVariables = numBoundVariables;

        // matrix information
        linearProgramData.numConstraints = linearProgram.getConstraints().size();

        int numVariablesTotal = 0;
        int numIntegerVariablesTotal = 0;
        int minNumInteger = Integer.MAX_VALUE;
        int maxNumInteger = Integer.MIN_VALUE;
        double minCoefficient = Double.MAX_VALUE;
        double maxCoefficient = Double.MIN_VALUE;
        for (MatrixRow matrixRow : linearProgram.getConstraints()) {

            int numIntegerInRow = 0;
            for (MatrixEntry matrixEntry : matrixRow.getEntries()) {
                numVariablesTotal++;
                if (matrixEntry.getVariable().isInteger()) {
                    numIntegerInRow++;
                    numIntegerVariablesTotal++;
                }

                if (matrixEntry.getCoefficient() < minCoefficient) {
                    minCoefficient = matrixEntry.getCoefficient();
                }
                if (matrixEntry.getCoefficient() > maxCoefficient) {
                    maxCoefficient = matrixEntry.getCoefficient();
                }
            }

            if (numIntegerInRow < minNumInteger) {
                minNumInteger = numIntegerInRow;
            }
            if (numIntegerInRow > maxNumInteger) {
                maxNumInteger = numIntegerInRow;
            }
        }
        linearProgramData.minIntegerVariables = minNumInteger; // per row
        linearProgramData.maxIntegerVariables = maxNumInteger;
        linearProgramData.avgIntegerVariables = numIntegerVariablesTotal / linearProgramData.numConstraints;
        linearProgramData.avgVariables = numVariablesTotal / linearProgramData.numConstraints;
        linearProgramData.minCoefficient = minCoefficient;
        linearProgramData.maxCoefficient = maxCoefficient;
        linearProgramData.sizeObjectiveFunction = linearProgram.getObjectiveFunction().getEntries().size();
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(linearProgram.getName()).append(":" + LINE_SEPARATOR);
        sb.append("\tnumVariables = " + linearProgramData.numVariables +
                "\tnumIntegerVariables =" + linearProgramData.numIntegerVariables +
                "\tisIntegerLP =" + linearProgramData.isIntegerLP +
                "\tproportionIntegerVariables =" + linearProgramData.proportionIntegerVariables +
                "\tminIntegerVariables = " + linearProgramData.minIntegerVariables +
                "\tmaxIntegerVariables =" + linearProgramData.maxIntegerVariables +
                "\tavgIntegerVariables =" + linearProgramData.avgIntegerVariables +
                "\tnumConstraints =" + linearProgramData.numConstraints +
                "\tsizeObjectiveFunction =" + linearProgramData.sizeObjectiveFunction +
                "\tavgVariables =" + linearProgramData.avgVariables +
                "\tminCoefficient =" + linearProgramData.minCoefficient +
                "\tmaxCoefficient =" + linearProgramData.maxCoefficient +
                "\tnumBoundVariables =" + linearProgramData.numBoundVariables +
                "numNodes = " +primalGraphData.numNodes +
                "\tnumIntegerNodes= " + primalGraphData.numIntegerNodes +
                "\tproportionIntegerNodes= " + primalGraphData.proportionIntegerNodes +
                "\tnumEdges= " + primalGraphData.numEdges +
                "\tdensity= " + primalGraphData.density +
                "\tminDegree= " + primalGraphData.minDegree +
                "\tmaxDegree= " + primalGraphData.maxDegree +
                "\tavgDegree= " +  primalGraphData.avgDegree);
        return sb.toString();
    }

    public static String shortDescriptionHeader() {
        StringBuilder sb = new StringBuilder();
        sb.append("name\t\tnumVars\t\tnumIntVars\tintegerLP\tnumConstr\tsizeObjFun\tnumNodes\tnumIntNodes\tnumEdges\tdensity\t\ttw_lb\ttw_ub\ttorso_lb\ttorso_ub\t");
        sb.append(LINE_SEPARATOR);
        return sb.toString();
    }

    public String shortDescription() {
        StringBuilder sb = new StringBuilder();
        sb.append(linearProgram.getName()).append(getNumTabs(String.valueOf(linearProgram.getName()), 3));
        sb.append(linearProgramData.numVariables).append(getNumTabs(String.valueOf(linearProgramData.numVariables), 3));
        sb.append(linearProgramData.numIntegerVariables).append(getNumTabs(String.valueOf(linearProgramData.numIntegerVariables), 3));
        sb.append(linearProgramData.isIntegerLP).append(getNumTabs(String.valueOf(linearProgramData.isIntegerLP), 3));
        sb.append(linearProgramData.numConstraints).append(getNumTabs(String.valueOf(linearProgramData.numConstraints), 3));
        sb.append(linearProgramData.sizeObjectiveFunction).append(getNumTabs(String.valueOf(linearProgramData.sizeObjectiveFunction), 3));
        sb.append(primalGraphData.numNodes).append(getNumTabs(String.valueOf(primalGraphData.numNodes), 3));
        sb.append(primalGraphData.numIntegerNodes).append(getNumTabs(String.valueOf(primalGraphData.numIntegerNodes), 3));
        sb.append(primalGraphData.numEdges).append(getNumTabs(String.valueOf(primalGraphData.numEdges), 3));
        sb.append(new DecimalFormat("0.00").format(primalGraphData.density)).append(getNumTabs(String.valueOf(new DecimalFormat("0.00").format(primalGraphData.density)), 3));
        if (primalGraphData.getTreewidthLB() != Integer.MIN_VALUE) {
            sb.append(primalGraphData.getTreewidthLB()).append(getNumTabs(String.valueOf(primalGraphData.getTreewidthLB()), 2));
        } else {
            sb.append("\t\t\t");
        }
        if (primalGraphData.getTreewidthUB() != Integer.MAX_VALUE) {
            sb.append(primalGraphData.getTreewidthUB()).append(getNumTabs(String.valueOf(primalGraphData.getTreewidthUB()), 2));
        } else {
            sb.append("\t\t\t");
        }
        if (primalGraphData.getTorsoWidthLB() != Integer.MIN_VALUE) {
            sb.append(primalGraphData.getTorsoWidthLB()).append(getNumTabs(String.valueOf(primalGraphData.getTorsoWidthLB()), 3));
        } else {
            sb.append("\t\t\t");
        }
        if (primalGraphData.getTorsoWidthUB() != Integer.MAX_VALUE) {
            sb.append(primalGraphData.getTorsoWidthUB()).append(getNumTabs(String.valueOf(primalGraphData.getTorsoWidthUB()), 3));
        } else {
            sb.append("\t\t\t");
        }
        sb.append(LINE_SEPARATOR);

        return sb.toString();
    }

    private String getNumTabs(String stringToFill, int tabsNeeded) {
        int numVisualTabs = (int) Math.floor((double)stringToFill.length()/4.0);
        String tabs = "";
        while (numVisualTabs < tabsNeeded) {
            tabs += "\t";
            numVisualTabs++;
        }
        return tabs;
    }

    public static String csvFormatHeader(boolean formatPrimalGraph, boolean formatIncidenceGraph) {
        StringBuilder sb = new StringBuilder();
        sb.append("name;numVars;numCons;numIntVars;propIntVars;integerLP;minIntVars;maxIntVars;avgIntVars;avgVars;" +
                "numBoundVars;minCoeff;maxCoeff;sizeObjFun;");

        String graphDataHeader = "numNodes;numIntNodes;propIntNodes;numEdges;density;minDegree;maxDegree;avgDegree;tw_lb;tw_ub;";
        if (formatPrimalGraph) {
            sb.append(graphDataHeader);
        }
        if (formatIncidenceGraph) {
            sb.append(graphDataHeader);
        }
        sb.append("torso_lb;torso_ub;");
        sb.append(LINE_SEPARATOR);
        return sb.toString();
    }

    public String csvFormat(boolean formatPrimalGraph, boolean formatIncidenceGraph) {
        StringBuilder sb = new StringBuilder();
        sb.append(linearProgram.getName()).append(";");
        sb.append(linearProgramData.numVariables).append(";");
        sb.append(linearProgramData.numConstraints).append(";");
        sb.append(linearProgramData.numIntegerVariables).append(";");
        sb.append(new DecimalFormat("0.00").format(linearProgramData.proportionIntegerVariables)).append(";");
        sb.append(linearProgramData.isIntegerLP).append(";");
        sb.append(linearProgramData.minIntegerVariables).append(";");
        sb.append(linearProgramData.maxIntegerVariables).append(";");
        sb.append(linearProgramData.avgIntegerVariables).append(";");
        sb.append(linearProgramData.avgVariables).append(";");
        sb.append(linearProgramData.numBoundVariables).append(";");
        sb.append(linearProgramData.minCoefficient).append(";");
        sb.append(linearProgramData.maxCoefficient).append(";");
        sb.append(linearProgramData.sizeObjectiveFunction).append(";");

        if (formatPrimalGraph) {
            formatGraphData(sb, primalGraphData);
        }

        if (formatIncidenceGraph) {
            formatGraphData(sb, incidenceGraphData);
        }

        sb.append(LINE_SEPARATOR);

        return sb.toString();
    }

    private void formatGraphData(StringBuilder sb, GraphData graphData) {
        sb.append(graphData.numNodes).append(";");
        sb.append(graphData.numIntegerNodes).append(";");
        sb.append(new DecimalFormat("0.00").format(graphData.proportionIntegerNodes)).append(";");
        sb.append(graphData.numEdges).append(";");
        sb.append(new DecimalFormat("0.00").format(graphData.density)).append(";");
        sb.append(graphData.minDegree).append(";");
        sb.append(graphData.maxDegree).append(";");
        sb.append(new DecimalFormat("0.00").format(graphData.avgDegree)).append(";");
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
        if (graphData.getTorsoWidthLB() != Integer.MIN_VALUE) {
            sb.append(graphData.getTorsoWidthLB()).append(";");
        } else {
            sb.append(";");
        }
        if (graphData.getTorsoWidthUB() != Integer.MAX_VALUE) {
            sb.append(graphData.getTorsoWidthUB()).append(";");
        } else {
            sb.append(";");
        }
    }
}
