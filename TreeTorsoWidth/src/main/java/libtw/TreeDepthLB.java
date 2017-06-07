package main.java.libtw;

import nl.uu.cs.treewidth.algorithm.LowerBound;
import nl.uu.cs.treewidth.input.GraphInput;
import nl.uu.cs.treewidth.ngraph.ListGraph;
import nl.uu.cs.treewidth.ngraph.ListVertex;
import nl.uu.cs.treewidth.ngraph.NGraph;
import nl.uu.cs.treewidth.ngraph.NVertex;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Iterator;
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

    private static final int NUM_ITERATIONS = 1000;
    private int lowerBound = Integer.MIN_VALUE;
    private List<ListVertex<D>> longestPath = null;
    private NGraph<D> graph;

    @Override
    public int getLowerBound() {
        return lowerBound;
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
        int maxPathLength = Integer.MIN_VALUE;

        for (int i = 0; i < NUM_ITERATIONS; i++) {
            List<ListVertex<D>> path = findRandomPath();
            int pathLength = path.size();
            if (pathLength > maxPathLength) {
                longestPath = path;
                maxPathLength = pathLength;
            }
        }

        lowerBound = (int) Math.ceil(Math.log(maxPathLength) / Math.log(2.0));
    }

    private List<ListVertex<D>> findRandomPath() {
        ArrayList<NVertex<D>> vertices = ((ListGraph) graph).vertices;

        // random start at some vertex
        Random rand = new Random();
        int startVertexIndex = rand.nextInt(graph.getNumberOfVertices()-1);

        ListVertex<D> curVertex = (ListVertex<D>) vertices.get(startVertexIndex);
        List<ListVertex<D>> pathFound = new ArrayList<>();
        pathFound.add(curVertex);
        int numIterations = 0; // debug purposes TODO

        while (true) {
            numIterations++;

            // get the last vertex in the path
            curVertex = pathFound.get(pathFound.size()-1);

            if (curVertex.getNumberOfNeighbors() == 0) {
                // may only happen if the graph is not connected
                break;
            }

            // first try to randomly select a neighbour to be the next vertex in the path
            ListVertex<D> nextVertex = (ListVertex<D>) curVertex.neighbors.get(rand.nextInt(curVertex.getNumberOfNeighbors() - 1));
            if (!pathFound.contains(nextVertex)) {
                pathFound.add(nextVertex);
            } else {

                // iterate over all neighbours and take the first that is not yet in the path
                Iterator<NVertex<D>> iter = curVertex.getNeighbors();

                boolean pathIncreased = false;
                while (!pathIncreased && iter.hasNext()) {
                    ListVertex<D> listVertex = (ListVertex<D>) iter.next();
                    if (!pathFound.contains(listVertex)) {
                        pathFound.add(listVertex);
                        pathIncreased = true;
                    }
                }

                if (!pathIncreased) {
                    break;
                }
            }
        }

        return pathFound;
    }
}
