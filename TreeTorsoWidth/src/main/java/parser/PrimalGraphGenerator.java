package main.java.parser;

import main.java.Configuration;
import main.java.graph.Edge;
import main.java.graph.Graph;
import main.java.graph.Node;
import main.java.lp.LinearProgram;
import main.java.lp.MatrixEntry;
import main.java.lp.Row;

import java.util.*;

/**
 * Created by Verena on 18.08.2017.
 */
public class PrimalGraphGenerator extends GraphGenerator {


    /**
     *
     * @param lp
     * @return Incidence graph of the linear program
     * @throws InterruptedException
     *  The incidence graph is constructed by taking the variables of the lp and the constraints as nodes
     *  and a variable is connected by an edge to a constraint iff the variable occurs in the constraint
     */
    public Graph linearProgramToGraph(LinearProgram lp) throws InterruptedException {
        Graph incidenceGraph = new Graph();
        List<Node> nodes = new ArrayList<>();
        List<Edge> edges = new ArrayList<>();
        Map<String, List<Node>> neighbourNodes = new HashMap<>();

        int numVariable = 0;
        Collection<Row> rows;
        if (Configuration.OBJ_FUNCTION) {
            // objective function is considered like a row in the matrix
            rows = lp.getRows().values();
        } else {
            // objective function just ignored
            rows = new ArrayList<>(lp.getConstraints());
        }
        for (Row matrixRow : rows) {
            checkInterrupted();

            Node constraintNode = new Node(matrixRow.getName(), numVariable++);
            nodes.add(constraintNode);
            List<Node> neighboursConstraintNode = new ArrayList<>();

            for (MatrixEntry matrixEntry : matrixRow.getEntries()) {
                Node variableNode = new Node(matrixEntry.getVariable().getName(), numVariable++);

                if (!nodes.contains(variableNode)) {
                    variableNode.setInteger(lp.getVariables().get(variableNode.getName()).isInteger());
                    nodes.add(variableNode);
                }

                // generate edges and neighbours relations
                Edge edge = new Edge(constraintNode, variableNode);
                edges.add(edge);

                neighboursConstraintNode.add(variableNode);

                // add constraintNode as neighbour to variable node
                if (!neighbourNodes.containsKey(variableNode.getName())) {
                    List<Node> neighboursVariableNode = new ArrayList<>();
                    neighboursVariableNode.add(constraintNode);
                    neighbourNodes.put(variableNode.getName(), neighboursVariableNode);
                } else {
                    neighbourNodes.get(variableNode.getName()).add(constraintNode);
                }
            }

            neighbourNodes.put(constraintNode.getName(), neighboursConstraintNode);
        }

        incidenceGraph.setEdges(edges);
        incidenceGraph.setNeighbourNodes(neighbourNodes);
        incidenceGraph.setNodes(nodes);
        return incidenceGraph;
    }
}
