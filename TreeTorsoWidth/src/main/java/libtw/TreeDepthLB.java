package main.java.libtw;

import nl.uu.cs.treewidth.algorithm.LowerBound;
import nl.uu.cs.treewidth.input.GraphInput;
import nl.uu.cs.treewidth.ngraph.ListGraph;
import nl.uu.cs.treewidth.ngraph.NGraph;
import nl.uu.cs.treewidth.ngraph.NVertex;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * A lower bound for the tree-depth is the logarithm of the length of any path in the
 * graph, more exactly for a Path Pn with length n:
 * td(Pn) = roundUp(log2(n + 1))
 *
 * @author Verena Dittmer
 * */
public class TreeDepthLB<D extends GraphInput.InputData> implements LowerBound<D> {
    private static final Logger LOGGER = LoggerFactory.getLogger(TreeDepthLB.class);
    private NGraph<D> graph;

    @Override
    public int getLowerBound() {
        return 0;
    }

    @Override
    public String getName() {
        return "TreeDepthLB";
    }

    @Override
    public void setInput(NGraph<D> g)  {
        this.graph = g;
    }

    @Override
    public void run() throws InterruptedException {
        ArrayList<NVertex<D>> vertices = ((ListGraph) graph).vertices;

        // random start at some vertex
        Random rand = new Random();
        int startVertexIndex = rand.nextInt(graph.getNumberOfVertices()-1);

        NVertex<D> curVertex = vertices.get(startVertexIndex);
        List<NVertex<D>> curPath = new ArrayList<>();
        curPath.add(curVertex);

        //TODO
    }
}
