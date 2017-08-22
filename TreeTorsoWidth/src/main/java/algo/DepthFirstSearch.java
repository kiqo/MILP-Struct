package main.java.algo;

import nl.uu.cs.treewidth.input.GraphInput;
import nl.uu.cs.treewidth.ngraph.ListVertex;
import nl.uu.cs.treewidth.ngraph.NVertex;

import java.util.Iterator;
import java.util.Set;

/**
 * Created by Verena on 18.08.2017.
 */
public class DepthFirstSearch {

    /*
     * Input is the root node, returns distance from the lowest descendant of the rootNode, i.e. the current
     * height of the tree
     */
    public static int DFSTree(NVertex rootNode, Set<NVertex<GraphInput.InputData>> handledVertices) {
        handledVertices.add(rootNode);
        int height = 0;
        int heightSubtree;
        for (Iterator<NVertex<GraphInput.InputData>> it = rootNode.getNeighbors(); it.hasNext(); ) {
            NVertex neighbour = it.next();
            if (!handledVertices.contains(neighbour)) {
                heightSubtree = DFSTree(neighbour, handledVertices);
                if (heightSubtree > height) {
                    height = heightSubtree;
                }
            }
        }
        return ++height;
    }


    /*
     * Input is the root node, returns distance from the lowest descendant of the rootNode, i.e. the current
     * height of the tree
     * Starts by constructing first the subtree for the neighbour of the rootNode with maximal degree in the graph
     */
    public static int DFSTreeByMaxDegreeRoot(ListVertex rootNode, Set<ListVertex> handledVertices) {
        handledVertices.add(rootNode);

        int height = 0;
        NVertex maxDegreeVertex;

        while (true) {
            maxDegreeVertex = findMaxDegreeNeighbour(rootNode, handledVertices);

            // all neighbours of current root node already in tree
            if (maxDegreeVertex == null) {
                break;
            }

            // take maxDegreeVertex as root for subtree
            int heightSubtree = DFSTreeByMaxDegreeRoot((ListVertex) maxDegreeVertex, handledVertices);
            if (heightSubtree > height) {
                height = heightSubtree;
            }
        }
        return ++height;
    }

    private static NVertex findMaxDegreeNeighbour(ListVertex rootNode, Set<ListVertex> handledVertices) {
        NVertex maxDegreeVertex;
        int maxDegree;
        maxDegreeVertex = null;
        maxDegree = Integer.MIN_VALUE;
        for (Iterator<NVertex<GraphInput.InputData>> it = rootNode.getNeighbors(); it.hasNext(); ) {
            NVertex neighbour = it.next();
            if (!handledVertices.contains(neighbour) && maxDegree < neighbour.getNumberOfNeighbors()) {
                maxDegreeVertex = neighbour;
                maxDegree = neighbour.getNumberOfNeighbors();
            }
        }
        return maxDegreeVertex;
    }
}
