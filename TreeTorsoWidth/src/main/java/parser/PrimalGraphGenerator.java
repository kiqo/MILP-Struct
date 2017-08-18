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
        List<Node> neighbours;

        int uniqueId = 0;
        Node node;
        // generate nodes
        for (String variableName : lp.getVariables().keySet()) {
            node = new Node(variableName, uniqueId++);
            node.setInteger(lp.getVariables().get(variableName).isInteger());
            nodes.add(node);
            nodesMap.put(node.getName(), node);
            neighbours = new ArrayList<>();
            neighbourNodes.put(variableName, neighbours);
        }

        // generate edges for constraints
        for (MatrixRow row : lp.getConstraints()) {
            checkInterrupted();
            convertRowToEdge(row, nodesMap, neighbourNodes, edges);
        }

        // definition for primal graph varies here: sometimes the objective function is considered and sometimes not
        // generate edges for objective function
        if (Configuration.OBJ_FUNCTION) {
            convertRowToEdge(lp.getObjectiveFunction(), nodesMap, neighbourNodes, edges);
        }

        Graph primalGraph = createGraph(nodes, edges, neighbourNodes);
        return primalGraph;
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

                // check if neighbourNode is already in neighbours of curNode
                boolean found = false;
                for (Node neighbour : neighbours) {
                    if (neighbour.getName().equals(neighbourNodeName)) {
                        found = true;
                    }
                }

                if (found){
                    // skip node if already known that they are connected
                    continue;
                }

                neighbourNode = nodesMap.get(neighbourNodeName);

                // add neighbourNode to curNode and curNode to neighbourNode and generate edge for them
                neighbours.add(neighbourNode);
                neighbours = neighbourNodes.get(neighbourNode.getName());
                neighbours.add(curNode);

                edges.add(new Edge(curNode, neighbourNode));
            }
        }
    }
}
