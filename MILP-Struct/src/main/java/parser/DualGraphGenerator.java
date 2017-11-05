package main.java.parser;

import main.java.graph.Graph;
import main.java.graph.Node;
import main.java.lp.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;


/**
 * Created by Verena on 21.08.2017.
 */
public class DualGraphGenerator extends GraphGenerator {

    /**
     *
     * @param lp the input (M)ILP of which the dual graph is constructed
     * @return Dual Graph
     * @throws InterruptedException in case the thread is set to cancelled
     * The dual graph has as nodes the constraints of the linear program and has an edge between two constraint
     * nodes iff there is a variable that occurs in both constraints
     */
    @Override
    public Graph linearProgramToGraph(LinearProgram lp) throws InterruptedException {
        List<Row> constraints = getRows(lp);
        int numConstraints = constraints.size();
        Map<String, Node> constraintNodes = new HashMap<>();
        for (int i = 0; i < numConstraints; i++) {
            checkInterrupted();
            Row constraint1 = constraints.get(i);
            Node constraintNode1 = createNodeIfNotExists(constraintNodes, constraint1.getName());
            for (int j = i+1; j < numConstraints; j++) {
                Row constraint2 = constraints.get(j);
                Node constraintNode2 = createNodeIfNotExists(constraintNodes, constraint2.getName());
                if (haveCommonVariable(constraint1, constraint2)) {
                    generateEdge(constraintNode1, constraintNode2); // TODO fix gc overhead limit exceeded
                    createNeighbours(constraintNode1, constraintNode2);
                }
            }
        }

        Graph dualGraph = createGraph(nodes, edges, neighbourNodes);
        return dualGraph;
    }

    private Node createNodeIfNotExists(Map<String, Node> constraintNodes, String constraintName) {
        Node constraintNode1 = constraintNodes.get(constraintName);
        if (constraintNode1 == null) {
            constraintNode1 = generateNode(constraintName);
            constraintNodes.put(constraintName, constraintNode1);
        }
        return constraintNode1;
    }

    private boolean haveCommonVariable(Row constraint1, Row constraint2) {
        for (Variable entryVariable1 : constraint1.getVariableEntries()) {
            for (Variable entryVariable2 : constraint2.getVariableEntries()) {
                if (entryVariable1.getName().equals(entryVariable2.getName())) {
                    return true;
                }
            }
        }
        return false;
    }
}
