package main.java.parser;

import main.java.Configuration;
import main.java.graph.Edge;
import main.java.graph.Graph;
import main.java.graph.Node;
import main.java.lp.LinearProgram;
import main.java.lp.MatrixEntry;
import main.java.lp.MatrixRow;
import main.java.lp.Row;

import java.util.*;

/**
 * Created by Verena on 18.08.2017.
 */
public class PrimalGraphGenerator extends GraphGenerator {

    private static int uniqueId = 0;

    /**
     *
     * @param lp
     * @return Primal graph of the linear program
     * @throws InterruptedException
     * The primal graph is constructed by taking the variables of the lp as nodes and a variable is connected by an edge
     * to another variable b iff they occur in the same constraint or they occur together in the objective function
     */
    public Graph linearProgramToGraph(LinearProgram lp) throws InterruptedException {
        List<Node> nodes = new ArrayList<>();
        List<Edge> edges = new ArrayList<>();
        Map<String, List<Node>> neighbourNodes = new HashMap<>();
        Map<String, Node> nodesMap = new HashMap<>();

        generateNodes(lp, nodes, neighbourNodes, nodesMap);
        generateEdges(lp, edges, neighbourNodes, nodesMap);

        Graph primalGraph = createGraph(nodes, edges, neighbourNodes);
        return primalGraph;
    }

    private void generateEdges(LinearProgram lp, List<Edge> edges, Map<String, List<Node>> neighbourNodes, Map<String, Node> nodesMap) throws InterruptedException {
        for (Row row : getRows(lp)) {
            checkInterrupted();
            convertRowToEdge(row, nodesMap, neighbourNodes, edges);
        }
    }

    private void generateNodes(LinearProgram lp, List<Node> nodes, Map<String, List<Node>> neighbourNodes, Map<String, Node> nodesMap) {
        List<Node> neighbours;
        Node node;
        for (String variableName : lp.getVariables().keySet()) {
            node = new Node(variableName, uniqueId++);
            node.setInteger(lp.getVariables().get(variableName).isInteger());
            nodes.add(node);
            nodesMap.put(node.getName(), node);
            neighbours = new ArrayList<>();
            neighbourNodes.put(variableName, neighbours);
        }
    }


    private void convertRowToEdge(Row row, Map<String, Node> nodesMap, Map<String, List<Node>> neighbourNodes, List<Edge> edges) throws InterruptedException {
        List<MatrixEntry> variablesInRow = row.getEntries();

        Node curNode, neighbourNode;
        String curNodeName, neighbourNodeName;
        List<Node> neighbours;
        for (int i = 0; i < variablesInRow.size(); i++) {
            if (i % 50 == 0) {
                checkInterrupted();
            }

            curNodeName = variablesInRow.get(i).getVariable().getName();
            curNode = nodesMap.get(curNodeName);

            for (int j = i+1; j < variablesInRow.size(); j++) {

                neighbourNodeName = variablesInRow.get(j).getVariable().getName();
                neighbours = neighbourNodes.get(curNodeName);
                if (isAlreadyNeighbour(neighbourNodeName, neighbours)) continue;

                neighbourNode = nodesMap.get(neighbourNodeName);

                // add neighbourNode to curNode and curNode to neighbourNode and generate edge for them
                neighbours.add(neighbourNode);
                neighbours = neighbourNodes.get(neighbourNode.getName());
                neighbours.add(curNode);
                edges.add(new Edge(curNode, neighbourNode));
            }
        }
    }

    private boolean isAlreadyNeighbour(String neighbourNodeName, List<Node> neighbours) {
        boolean found = false;
        for (Node neighbour : neighbours) {
            if (neighbour.getName().equals(neighbourNodeName)) {
                found = true;
            }
        }

        if (found){
            // skip node if already known that they are connected
            return true;
        }
        return false;
    }
}
