package main.java.lp;

import main.java.graph.Graph;
import main.java.graph.Node;
import main.java.main.Configuration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collection;
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
    private GraphData primalGraphData;
    private GraphData incidenceGraphData;
    private GraphData dualGraphData;

    public GraphData getPrimalGraphData() {
        return primalGraphData;
    }

    public GraphData getIncidenceGraphData() {
        return incidenceGraphData;
    }

    public GraphData getDualGraphData() {
        return dualGraphData;
    }

    public LinearProgram getLinearProgram() {
        return linearProgram;
    }

    public LPData getLinearProgramData() {
        return linearProgramData;
    }

    public LPStatistics(LinearProgram linearProgram) {
        this.linearProgram = linearProgram;
        computeLinearProgramData();
    }

    private void computeLinearProgramData() {
        linearProgramData = new LPData();
        computeVariableInformation();
        computeMatrixInformation();
    }

    private void computeMatrixInformation() {
        linearProgramData.numConstraints = linearProgram.getConstraints().size();

        int numVariablesTotal = 0;
        int numIntegerVariablesTotal = 0;
        int minNumInteger = Integer.MAX_VALUE;
        int maxNumInteger = Integer.MIN_VALUE;
        double minCoefficient = Double.MAX_VALUE;
        double maxCoefficient = Double.MIN_VALUE;
        Collection<Row> rows;
        if (Configuration.OBJ_FUNCTION) {
            // objective function is considered like a row in the matrix
            rows = linearProgram.getRows().values();
        } else {
            // objective function just ignored
            rows = new ArrayList<>(linearProgram.getConstraints());
        }
        for (Row matrixRow : rows) {

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

    private void computeVariableInformation() {
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
                if (var.isInteger() && (int) var.getLowerBound() < minBoundValue) {
                    minBoundValue = (int) var.getLowerBound();
                } else if (!var.isInteger() && (double) var.getLowerBound() < minBoundValue) {
                    minBoundValue = (double) var.getLowerBound();
                }
            }
            if (var.getUpperBound() != null) {
                if (var.isInteger() && (int) var.getUpperBound() > maxBoundValue) {
                    maxBoundValue = (int) var.getUpperBound();
                } else if (!var.isInteger() && (double) var.getUpperBound() > maxBoundValue) {
                    maxBoundValue = (double) var.getUpperBound();
                }
            }
        }
        linearProgramData.numIntegerVariables = numInteger;
        linearProgramData.isIntegerLP = (numInteger == linearProgramData.numVariables);
        linearProgramData.proportionIntegerVariables = (double) numInteger / (double) linearProgramData.numVariables;
        linearProgramData.numBoundVariables = numBoundVariables;
    }

    public void computeDualGraphData(Graph primalGraph) {
        this.dualGraphData = computeGraphData(primalGraph);
        if (dualGraphData.numNodes <= 1) {
            dualGraphData.density = 0;
        } else {
            dualGraphData.density = (double) (2 * dualGraphData.numEdges) / (double) (dualGraphData.numNodes * (primalGraphData.numNodes - 1));
        }
    }

    public void computePrimalGraphData(Graph primalGraph) {
        this.primalGraphData = computeGraphData(primalGraph);
        if (primalGraphData.numNodes <= 1) {
            primalGraphData.density = 0;
        } else {
            primalGraphData.density = (double) (2 * primalGraphData.numEdges) / (double) (primalGraphData.numNodes * (primalGraphData.numNodes - 1));
        }
    }

    public void computeIncidenceGraphData(Graph incidenceGraph) {
        this.incidenceGraphData = computeGraphData(incidenceGraph);
        // define the density for the incidence graph to be num edges / left side * right side of the bipartite graph
        // i.e. the number of edges divided by the maximum possible number of edges
        incidenceGraphData.density = (double) (incidenceGraphData.numEdges) / (double) (linearProgramData.numConstraints * linearProgramData.numVariables);
    }

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

    public static String csvFormatHeader(boolean formatPrimalGraph, boolean formatIncidenceGraph) {
        StringBuilder sb = new StringBuilder();
        sb.append("name;numVars;numCons;numIntVars;propIntVars;integerLP;minIntVars;maxIntVars;avgIntVars;avgVars;" +
                "numBoundVars;minCoeff;maxCoeff;sizeObjFun;");

        String graphDataHeader = "numNodes;numIntNodes;propIntNodes;numEdges;density;minDegree;maxDegree;avgDegree;tw_lb;tw_ub;";
        if (formatPrimalGraph) {
            sb.append(graphDataHeader).append("td_ub;torso_lb;torso_ub;");
        }
        if (formatIncidenceGraph) {
            sb.append(graphDataHeader);
        }
        if (formatIncidenceGraph) {
            sb.append(graphDataHeader);
        }
        sb.append(LINE_SEPARATOR);
        return sb.toString();
    }

    public String csvFormat(boolean formatPrimalGraph, boolean formatIncidenceGraph, boolean formatDualGraph) {
        StringBuilder sb = new StringBuilder();
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

        if (formatPrimalGraph) {
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

        if (formatIncidenceGraph) {
            formatGraphData(sb, incidenceGraphData);
        }

        if (formatDualGraph) {
            formatGraphData(sb, dualGraphData);
        }

        sb.append(LINE_SEPARATOR);

        return sb.toString();
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
