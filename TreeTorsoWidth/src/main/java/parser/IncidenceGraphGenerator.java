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
public class IncidenceGraphGenerator extends GraphGenerator {

    private static int uniqueId = 0;

    /**
     *
     * @param lp
     * @return Incidence graph of the linear program
     * @throws InterruptedException
     *  The incidence graph is constructed by taking the variables of the lp and the constraints as nodes
     *  and a variable is connected by an edge to a constraint iff the variable occurs in the constraint
     */
    public Graph linearProgramToGraph(LinearProgram lp) throws InterruptedException {
        List<Node> nodes = new ArrayList<>();
        List<Edge> edges = new ArrayList<>();
        Map<String, List<Node>> neighbourNodes = new HashMap<>();

        for (Row matrixRow : getRows(lp)) {
            checkInterrupted();

            Node constraintNode = generateConstraintNode(nodes, matrixRow.getName());
            for (MatrixEntry matrixEntry : matrixRow.getEntries()) {
                Node variableNode = generateNodeIfNotExists(lp, nodes, matrixEntry);
                generateEdge(edges, constraintNode, variableNode);
                createNeighbours(neighbourNodes, constraintNode, variableNode);
            }
        }

        Graph incidenceGraph = createGraph(nodes ,edges , neighbourNodes);
        return incidenceGraph;
    }

    private Collection<Row> getRows(LinearProgram lp) {
        Collection<Row> rows;
        if (Configuration.OBJ_FUNCTION) {
            // objective function is considered like a row in the matrix
            rows = lp.getRows().values();
        } else {
            // objective function just ignored
            rows = new ArrayList<>(lp.getConstraints());
        }
        return rows;
    }

    private Node generateConstraintNode(List<Node> nodes, String nodeName) {
        Node constraintNode = new Node(nodeName, uniqueId++);
        nodes.add(constraintNode);
        return constraintNode;
    }

    private void createNeighbours(Map<String, List<Node>> neighbourNodes, Node node1, Node node2) {
        addNeighbour(neighbourNodes, node2, node1);
        addNeighbour(neighbourNodes, node1, node2);


    }

    private void addNeighbour(Map<String, List<Node>> neighbourNodes, Node node, Node neighbourNode) {
        if (!neighbourNodes.containsKey(neighbourNode.getName())) {
            List<Node> neighboursVariableNode = new ArrayList<>();
            neighboursVariableNode.add(node);
            neighbourNodes.put(neighbourNode.getName(), neighboursVariableNode);
        } else {
            neighbourNodes.get(neighbourNode.getName()).add(node);
        }
    }

    private Node generateNodeIfNotExists(LinearProgram lp, List<Node> nodes, MatrixEntry matrixEntry) {
        Node variableNode = new Node(matrixEntry.getVariable().getName(), uniqueId);
        if (!nodes.contains(variableNode)) {
            variableNode.setInteger(lp.getVariables().get(variableNode.getName()).isInteger());
            variableNode.setId(uniqueId++);
            nodes.add(variableNode);
        }
        return variableNode;
    }

    private void generateEdge(List<Edge> edges, Node constraintNode, Node variableNode) {
        // generate edges and neighbours relations
        Edge edge = new Edge(constraintNode, variableNode);
        edges.add(edge);
    }
}
