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
    private Map<String, List<Node>> neighbourNodes;

    /* @Input LinearProgram
       @Output Incidence graph of the linear program
       The incidence graph is constructed by taking the variables of the lp and the constraints as nodes
       and a variable is connected by an edge to a constraint iff the variable occurs in the constraint
    */
    public Graph linearProgramToIncidenceGraph(LinearProgram lp) throws InterruptedException {
        Graph incidenceGraph = new Graph();
        List<Node> nodes = new ArrayList<>();
        List<Edge> edges = new ArrayList<>();
        Map<String, List<Node>> neighbourNodes = new HashMap<>();

        int numVariable = 0;
        Collection<Row> rows;
        if (main.java.Configuration.OBJ_FUNCTION) {
            // objective function is considered like a row in the matrix
            rows = lp.getRows().values();
        } else {
            // objective function just ignored
            rows = new ArrayList<>(lp.getConstraints());
        }
        for (Row matrixRow : rows) {

            if (Thread.currentThread().isInterrupted()) {
                throw new InterruptedException();
            }

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
        this.neighbourNodes = neighbourNodes;
        return incidenceGraph;
    }

    /* @Input LinearProgram
       @Output Primal graph of the linear program
       The primal graph is constructed by taking the variables of the lp as nodes
       and a variable is connected by an edge to another variable b iff they occur in the same constraint or
       they occur together in the objective function
     */
    public Graph linearProgramToPrimalGraph(LinearProgram lp) throws InterruptedException {
        Graph primalGraph = new Graph();
        List<Node> nodes = new ArrayList<>();
        List<Edge> edges = new ArrayList<>();
        Map<String, List<Node>> neighbourNodes = new HashMap<>();
        Map<String, Node> nodesMap = new HashMap<>();
        List<Node> neighbours;

        int numVariable = 0;

        Node node;

        // generate nodes
        for (String variableName : lp.getVariables().keySet()) {
            node = new Node(variableName, numVariable++);
            node.setInteger(lp.getVariables().get(variableName).isInteger());
            nodes.add(node);
            nodesMap.put(node.getName(), node);
            neighbours = new ArrayList<>();
            neighbourNodes.put(variableName, neighbours);
        }
        primalGraph.setNodes(nodes);

        // generate edges for constraints
        for (MatrixRow row : lp.getConstraints()) {
            if (Thread.currentThread().isInterrupted()) {
                throw new InterruptedException();
            }
            convertRowToEdge(row, nodesMap, neighbourNodes, edges);
        }

        // definition for primal graph varies here: sometimes the objective function is considered and sometimes not
        // generate edges for objective function
        if (main.java.Configuration.OBJ_FUNCTION) {
            convertRowToEdge(lp.getObjectiveFunction(), nodesMap, neighbourNodes, edges);
        }

        primalGraph.setEdges(edges);
        primalGraph.setNeighbourNodes(neighbourNodes);
        this.neighbourNodes = neighbourNodes;

        return primalGraph;
    }

    /*
    Finds connected components in the graph and returns a list containing one vertex for each component
     */
    public static List<Node> findComponents(Graph graph) {
        List<Node> components = new ArrayList<>();

        List<Node> handledNodes = new ArrayList<>();
        for (Node node : graph.getNodes()) {
            if (!handledNodes.contains(node)) {
                DFSSearch(node, graph.getNeighbourNodes(), handledNodes);

                // add this node as representative to result set
                components.add(node);
            }
        }
        return components;
    }

    // TODO remove duplicate code in TreeDepth
    private static void DFSSearch(Node rootNode, Map<String, List<Node>> neighbourNodes, List<Node> handledNodes) {

        handledNodes.add(rootNode);

        for (Node neighbor : neighbourNodes.get(rootNode.getName())) {
            if (!handledNodes.contains(neighbor)) {
                DFSSearch(neighbor, neighbourNodes, handledNodes);
            }
        }
    }

    private void convertRowToEdge(Row row, Map<String, Node> nodesMap, Map<String, List<Node>> neighbourNodes, List<Edge> edges) throws InterruptedException {
        List<MatrixEntry> variablesInRow = row.getEntries();

        Node curNode, neighbourNode;
        String curNodeName, neighbourNodeName;
        List<Node> neighbours;
        for (int i = 0; i < variablesInRow.size(); i++) {

            if ((i % 5 == 0) && Thread.currentThread().isInterrupted()) {
                throw new InterruptedException();
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
