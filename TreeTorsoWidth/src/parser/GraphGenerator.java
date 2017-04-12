package parser;

import graph.Edge;
import graph.Graph;
import graph.Node;
import lp.LinearProgram;
import lp.MatrixEntry;
import lp.MatrixRow;
import lp.Row;

import java.util.*;

/**
 * Created by Verena on 09.03.2017.
 */
public class GraphGenerator {

    private static int numVariable = 0;
    private Graph primalGraph = new Graph();
    private List<Node> nodes = new ArrayList<>();
    private List<Edge> edges = new ArrayList<>();
    private Map<Node, List<Node>> neighbourNodes = new HashMap<>();

    // TODO improve performance!
    public Graph linearProgramToPrimalGraph(LinearProgram lp) {
        // primal graph is constructed by taking the variables of the lp as nodes
        // a variable a is connected by an edge to variable b iff they occur in the same constraint or
        // they occur together in the objective function

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
            convertRowToEdge(row);
        }
        // generate edges for objective function
        convertRowToEdge(lp.getObjectiveFunction());

        primalGraph.setEdges(edges);
        primalGraph.setNeighbourNodes(neighbourNodes);

        return primalGraph;
    }

    private void convertRowToEdge(Row row) {
        List<MatrixEntry> variablesInRow = row.getEntries();

        MatrixEntry curVariable = null;
        for (int i = 0; i < variablesInRow.size(); i++) {

            curVariable = variablesInRow.get(i);
            Node curNode = variableToNode(curVariable);

            if (!neighbourNodes.containsKey(curNode)) {
                // create new entry in neighbourNodes
                neighbourNodes.put(curNode, new ArrayList<>());
            }

            for (int j = i+1; j < variablesInRow.size(); j++) {
                Node neighbourNode = variableToNode(variablesInRow.get(j));

                // skip node if already known that they are connected
                if (neighbourNodes.get(curNode).contains(neighbourNode)) {
                    continue;
                }

                // add neighbourNode to curNode and curNode to neighbourNode and generate edge for them
                neighbourNodes.get(curNode).add(neighbourNode);
                if (!neighbourNodes.containsKey(neighbourNode)) {
                    // create new entry in neighbourNodes
                    neighbourNodes.put(neighbourNode, new ArrayList<>());
                }
                neighbourNodes.get(neighbourNode).add(curNode);
                Edge edge = new Edge(curNode, neighbourNode);
                edges.add(edge);
            }
        }
    }

    private Node variableToNode(MatrixEntry entry) {
        return nodes.stream().filter((p) -> p.getName().equals(entry.getVariable().getName())).findFirst().get();
    }
}
