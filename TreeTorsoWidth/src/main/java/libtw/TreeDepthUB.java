package main.java.libtw;

import nl.uu.cs.treewidth.algorithm.UpperBound;
import nl.uu.cs.treewidth.input.GraphInput;
import nl.uu.cs.treewidth.ngraph.ListVertex;
import nl.uu.cs.treewidth.ngraph.NGraph;
import nl.uu.cs.treewidth.ngraph.NVertex;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

/**
 * The tree-depth td(G) is defined to be the minimum height of a rooted forest F
 * such that G is a subgraph of the closure of F, where the closure of F is obtained
 * by adding edges between ancestor and descendents in a tree.

 * For upper bound, the idea is to select some starting points and run a DFS from these
 * to construct an elimination tree
 * These starting points can be choosen randomly or with greedy heuristic (like taking
 * the center of some greedily constructed maximal path in the graph).
 *
 * @author Verena Dittmer
 * */
public class TreeDepthUB<D extends GraphInput.InputData> implements UpperBound<D> {
    private static final Logger LOGGER = LoggerFactory.getLogger(TreeDepthUB.class);

    private int upperBound = Integer.MAX_VALUE;
    private NGraph<D> graph;
    private List<ListVertex<D>> longestPath;

    public TreeDepthUB (List<ListVertex<D>> longestPath) {
        this.longestPath = longestPath;
    }

    public void setLongestPath(List<ListVertex<D>> longestPath) {
        this.longestPath = longestPath;
    }

    @Override
    public int getUpperBound() {
        return upperBound;
    }

    @Override
    public String getName() {
        return "TreeDepthUB";
    }

    @Override
    public void setInput(NGraph<D> g)  {
        this.graph = g;
    }

    @Override
    public void run() throws InterruptedException {
        if (longestPath == null) {
            generatePath();
        }

        // construct a DFS by using the node in the middle of the longest path as root
        ListVertex<D> rootNode = longestPath.get(longestPath.size() / 2); // TODO check for size 3 (e.g. starshapedgraph)
        DFSTree(rootNode);
    }

    private void generatePath() {

    }

    /*
     * Input is the root node, returns distance from the lowest descendant of the rootNode, i.e. the current
     * height of the tree
     */
    private int DFSTree(ListVertex<D> rootNode) {

        ((LPInputData) rootNode.data).setNodeHandled(true); // TODO check if not already set to true after computing torso width!

        int height = 0;
        for (NVertex<D> neighbor : rootNode.neighbors) {
            if (!((LPInputData) neighbor.data).isNodeHandled()) {
                int heightSubtree = DFSTree((ListVertex<D>) neighbor);
                if (heightSubtree > height) {
                    height = heightSubtree;
                }
            }
        }
        return ++height;
    }
}
