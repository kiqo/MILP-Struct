package main.java.parser;

import main.java.graph.Graph;
import main.java.graph.Node;
import main.java.lp.LinearProgram;
import main.java.lp.Row;
import main.java.lp.Variable;

/**
 * Created by Verena on 18.08.2017.
 */
public class IncidenceGraphGenerator extends GraphGenerator {

    /**
     *
     * @param lp
     * @return Incidence graph of the linear program
     * @throws InterruptedException
     *  The incidence graph is constructed by taking the variables of the lp and the constraints as nodes
     *  and a variable is connected by an edge to a constraint iff the variable occurs in the constraint
     */
    public Graph linearProgramToGraph(LinearProgram lp) throws InterruptedException {
        for (Row matrixRow : getRows(lp)) {
            checkInterrupted();

            Node constraintNode = generateNode(matrixRow.getName());
            for (Variable variableEntry : matrixRow.getVariableEntries()) {
                Node variableNode = generateNodeIfNotExists(variableEntry.getName());
                variableNode.setInteger(lp.getVariables().get(variableNode.getName()).isInteger());
                generateEdge(constraintNode, variableNode);
                createNeighbours(constraintNode, variableNode);
            }
        }

        Graph incidenceGraph = createGraph(nodes, edges, neighbourNodes);
        return incidenceGraph;
    }
}
