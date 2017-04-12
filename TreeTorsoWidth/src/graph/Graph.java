package graph;

import java.util.*;

/**
 * Created by Verena on 09.03.2017.
 */
public class Graph {
    private List<Node> nodes;
    private List<Edge> edges;
    private Map<Node, List<Node>> neighbourNodes;

    public List<Edge> getEdges() {
        return edges;
    }

    public void setEdges(List<Edge> edges) {
        this.edges = edges;
    }

    public List<Node> getNodes() {
        return nodes;
    }

    public void setNodes(List<Node> nodes) {
        this.nodes = nodes;
    }

    public Map<Node, List<Node>> getNeighbourNodes() {
        return neighbourNodes;
    }

    public void setNeighbourNodes(Map<Node, List<Node>> neighbourNodes) {
        this.neighbourNodes = neighbourNodes;
    }

    public void eliminate(Node smallestNode) {
        ListIterator<Node> it1 = neighbourNodes.get(smallestNode).listIterator();

        while(it1.hasNext()) {

            Node n1 = it1.next();
            int indexN1 = neighbourNodes.get(smallestNode).indexOf(n1);

            // start from second node onwards
            ListIterator<Node> it2 = neighbourNodes.get(smallestNode).listIterator(indexN1);
            // eigentlich: it2.next() und dann .equals weglassen
            while (it2.hasNext()) {
                Node n2 = it2.next();
                if(!n1.equals(n2)) {
                    ensureNeighbours(n1, n2);
                }
            }
        }

        // remove smallestNode from graph
        it1 = neighbourNodes.get(smallestNode).listIterator();
        while (it1.hasNext()) {
            Node curNeighbour = it1.next();
            neighbourNodes.get(curNeighbour).remove(smallestNode); //TODO check if removed
        }
        nodes.remove(smallestNode);
        neighbourNodes.remove(smallestNode);
        // TODO update edges
    }

    private void ensureNeighbours(Node n1, Node n2) {
        if (!neighbourNodes.get(n1).contains(n2)) {
            neighbourNodes.get(n1).add(n2);
            neighbourNodes.get(n2).add(n1);
        }
    }
}
