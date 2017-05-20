package main.java.parser;

import main.java.graph.Edge;
import main.java.graph.Graph;
import main.java.graph.Node;
import main.java.lp.LinearProgram;
import main.java.lp.MatrixEntry;
import main.java.lp.MatrixRow;
import main.java.lp.Row;

import java.util.*;

/**
 * Created by Verena on 09.03.2017.
 */
public class GraphGenerator {

    /* @Input LinearProgram
       @Output Incidence graph of the linear program
       The incidence graph is constructed by taking the variables of the lp and the constraints as nodes
       and a variable is connected by an edge to a constraint iff the variable occurs in the constraint
    */
    public Graph linearProgramToIncidenceGraph(LinearProgram lp) {
        Graph incidenceGraph = new Graph();
        List<Node> nodes = new ArrayList<>();
        List<Edge> edges = new ArrayList<>();
        Map<Node, List<Node>> neighbourNodes = new HashMap<>();

        int numVariable = 0;

        // generate nodes
        for (MatrixRow matrixRow : lp.getConstraints()) {
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
                if (!neighbourNodes.containsKey(variableNode)) {
                    List<Node> neighboursVariableNode = new ArrayList<>();
                    neighboursVariableNode.add(constraintNode);
                    neighbourNodes.put(variableNode, neighboursVariableNode);
                } else {
                    neighbourNodes.get(variableNode).add(constraintNode);
                }
            }

            neighbourNodes.put(constraintNode, neighboursConstraintNode);
        }

        incidenceGraph.setEdges(edges);
        incidenceGraph.setNeighbourNodes(neighbourNodes);
        incidenceGraph.setNodes(nodes);
        return incidenceGraph;
    }

    // TODO improve performance!
    /* @Input LinearProgram
       @Output Primal graph of the linear program
       The primal graph is constructed by taking the variables of the lp as nodes
       and a variable is connected by an edge to another variable b iff they occur in the same constraint or
       they occur together in the objective function
     */
    public Graph linearProgramToPrimalGraph(LinearProgram lp) {
        Graph primalGraph = new Graph();
        List<Node> nodes = new ArrayList<>();
        List<Edge> edges = new ArrayList<>();
        Map<Node, List<Node>> neighbourNodes = new HashMap<>();

        int numVariable = 0;

        // generate nodes
        for (String variableName : lp.getVariables().keySet()) {
            Node node = new Node();
            node.setId(numVariable++);
            node.setName(variableName);
            node.setInteger(lp.getVariables().get(variableName).isInteger());
            nodes.add(node);
        }
        primalGraph.setNodes(nodes);

        // generate edges for constraints
        for (MatrixRow row : lp.getConstraints()) {
            convertRowToEdge(row, neighbourNodes, edges);
        }

        // definition for primal graph varies here: sometimes the objective function is considered and sometimes not
        // generate edges for objective function
        // convertRowToEdge(lp.getObjectiveFunction(), neighbourNodes, edges);

        primalGraph.setEdges(edges);
        primalGraph.setNeighbourNodes(neighbourNodes);

        return primalGraph;
    }

    private void convertRowToEdge(Row row, Map<Node, List<Node>> neighbourNodes, List<Edge> edges) {
        List<MatrixEntry> variablesInRow = row.getEntries();

        MatrixEntry curVariable;
        for (int i = 0; i < variablesInRow.size(); i++) {

            curVariable = variablesInRow.get(i);
            Node curNode = new Node(curVariable.getVariable().getName());

            for (int j = i+1; j < variablesInRow.size(); j++) {

                Node neighbourNode = new Node(variablesInRow.get(j).getVariable().getName());

                List<Node> neighbours = neighbourNodes.get(curNode);
                if (neighbours == null) {
                    neighbours = new ArrayList<>();
                    neighbourNodes.put(curNode, neighbours);
                } else if (neighbours.contains(neighbourNode)){
                    // skip node if already known that they are connected
                    continue;
                }
                // add neighbourNode to curNode and curNode to neighbourNode and generate edge for them
                // create new entry in neighbourNodes
                neighbours.add(neighbourNode);

                neighbours = neighbourNodes.get(neighbourNode);
                if (neighbours == null) {
                    neighbours = new ArrayList<>();
                    neighbourNodes.put(neighbourNode, neighbours);
                }
                neighbours.add(curNode);

                Edge edge = new Edge(curNode, neighbourNode);
                edges.add(edge);
            }
        }
    }
}
