package main.java.parser;

import main.java.graph.Graph;
import main.java.graph.Node;
import main.java.lp.LinearProgram;
import main.java.lp.Row;
import main.java.lp.Variable;

import java.util.*;

/**
 * Created by Verena on 18.08.2017.
 */
public class PrimalGraphGenerator extends GraphGenerator {

    /**
     *
     * @param lp the input (M)ILP of which the dual graph is constructed
     * @return Primal graph of the linear program
     * @throws InterruptedException in case the thread is set to cancelled
     * The primal graph is constructed by taking the variables of the lp as nodes and a variable is connected by an edge
     * to another variable b iff they occur in the same constraint or they occur together in the objective function
     */
    public Graph linearProgramToGraph(LinearProgram lp) throws InterruptedException {
        for (Row row : getRows(lp)) {
            checkInterrupted();
            convertRow(row, lp);
        }

        Graph primalGraph = createGraph(nodes, edges, neighbourNodes);
        return primalGraph;
    }

    private void convertRow(Row row, LinearProgram lp) throws InterruptedException {
        List<Variable> variablesInRow = row.getVariableEntries();
        for (int i = 0; i < variablesInRow.size(); i++) {
            convertVariable(lp, variablesInRow, i);
        }
    }

    private void convertVariable(LinearProgram lp, List<Variable> variablesInRow, int i) throws InterruptedException {
        String variableName;
        Node curNode;
        String neighbourNodeName;
        Node neighbourNode;
        if (i % 10 == 0) {
            checkInterrupted();
        }
        variableName = variablesInRow.get(i).getName();
        curNode = generateNodeIfNotExists(variableName);
        curNode.setInteger(lp.getVariables().get(variableName).isInteger());

        for (int j = i+1; j < variablesInRow.size(); j++) {
            neighbourNodeName = variablesInRow.get(j).getName();
            neighbourNode = generateNodeIfNotExists(neighbourNodeName);
            neighbourNode.setInteger(lp.getVariables().get(neighbourNodeName).isInteger());

            if (!isAlreadyNeighbour(neighbourNodeName, neighbourNodes.get(variableName))) {
                generateEdge(curNode, neighbourNode);
                createNeighbours(curNode, neighbourNode);
            }
        }
    }

    private boolean isAlreadyNeighbour(String neighbourNodeName, List<Node> neighbours) {
        for (Node neighbour : neighbours) {
            if (neighbour.getName().equals(neighbourNodeName)) {
                return true;
            }
        }
        return false;
    }
}
