package main.java.parser;

import main.java.graph.Graph;
import main.java.graph.Node;
import main.java.lp.*;


/**
 * Created by Verena on 21.08.2017.
 */
public class DualGraphGenerator extends GraphGenerator {

    /**
     *
     * @param lp
     * @return Dual Graph
     * @throws InterruptedException
     * The dual graph has as nodes the constraints of the linear program and has an edge between two constraint
     * nodes iff there is a variable that occurs in both constraints
     */
    @Override
    public Graph linearProgramToGraph(LinearProgram lp) throws InterruptedException {
        for (Row constraint1 : getRows(lp)) {
            Node constraintNode1 = generateNodeIfNotExists(constraint1.getName());
            for (Row constraint2 : getRows(lp)) {
                Node constraintNode2 = generateNodeIfNotExists(constraint2.getName());
                if (haveCommonVariable(constraint1, constraint2)) {
                    generateEdge(constraintNode1, constraintNode2);
                    createNeighbours(constraintNode1, constraintNode2);
                }
            }
        }
        Graph dualGraph = createGraph(nodes, edges, neighbourNodes);
        return dualGraph;
    }

    private boolean haveCommonVariable(Row constraint1, Row constraint2) {
        for (MatrixEntry entryConstraint1 : constraint1.getEntries()) {
            for (MatrixEntry entryConstraint2 : constraint2.getEntries()) {
                if (entryConstraint1.getVariable().equals(entryConstraint2.getVariable())) {
                    return true;
                }
            }
        }
        return false;
    }
}
