package main.java.libtw;

import nl.uu.cs.treewidth.algorithm.LowerBound;
import nl.uu.cs.treewidth.algorithm.UpperBound;
import nl.uu.cs.treewidth.input.GraphInput;
import nl.uu.cs.treewidth.ngraph.NGraph;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The tree-depth td(G) is defined to be the minimum height of a rooted forest F
 * such that G is a subgraph of the closure of F, where the closure of F is obtained
 * by adding edges between ancestor and descendents in a tree.
 *
 * The treewidth is a lower bound for treedepth. Another lower bound is the logarithm
 * of the length of any path in the graph.
 *
 * For upper bound, the idea is to select some starting points and run a DFS from these.
 * These starting points can be choosen randomly or with greedy heuristic (like taking
 * the center of some greedily constructed maximal path in the graph).
 *
 * @author Verena Dittmer
 * */
public class TreeDepthUB<D extends GraphInput.InputData> implements UpperBound<D> {
    private static final Logger LOGGER = LoggerFactory.getLogger(TreeDepthLB.class);
    private NGraph<D> graph;

    @Override
    public int getUpperBound() {
        return 0;
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

    }
}
