package main.java.libtw;

import nl.uu.cs.treewidth.algorithm.LowerBound;
import nl.uu.cs.treewidth.algorithm.UpperBound;
import nl.uu.cs.treewidth.input.GraphInput;
import nl.uu.cs.treewidth.ngraph.ListGraph;
import nl.uu.cs.treewidth.ngraph.ListVertex;
import nl.uu.cs.treewidth.ngraph.NGraph;
import nl.uu.cs.treewidth.ngraph.NVertex;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

/**
 * A lower bound for the tree-depth is the logarithm of the length of any path in the
 * graph, more exactly for a Path Pn with length n:
 * td(Pn) = roundUp(log2(n + 1))
 *
 * @author Verena Dittmer
 * */
public class TreeDepth<D extends GraphInput.InputData> implements LowerBound<D>, UpperBound<D> {
    private static final Logger LOGGER = LoggerFactory.getLogger(TreeDepth.class);
    // times that some random path is generated
    private static final int NUM_ITERATIONS_LB = 1000;
    // times that some DFS spanning tree is generated, should be smaller than NUM_ITERATIONS_LB
    private static final int NUM_ITERATIONS_UB = 100;
    private int lowerBound = Integer.MIN_VALUE;
    private int upperBound = Integer.MAX_VALUE;

    /*
    returns the longest path found by a random algorithm
     */
    public List<ListVertex<D>> getLongestPath() {
        return longestPath;
    }

    private List<ListVertex<D>> longestPath = null;
    private NGraph<D> graph;

    @Override
    public int getLowerBound() {
        return lowerBound;
    }

    @Override
    public int getUpperBound() {
        return this.upperBound;
    }

    @Override
    public String getName() {
        return "TreeDepth";
    }

    @Override
    public void setInput(NGraph<D> g)  {
        this.graph = g;
    }

    @Override
    public void run() throws InterruptedException {
        // Lower bound of tree depth
        int maxPathLength = Integer.MIN_VALUE;

        for (int i = 0; i < NUM_ITERATIONS_LB; i++) {
            List<ListVertex<D>> path = findRandomPath();
            int pathLength = path.size();
            if (pathLength > maxPathLength) {
                longestPath = path;
                maxPathLength = pathLength;
            }

            if (i % (NUM_ITERATIONS_LB/NUM_ITERATIONS_UB) == 0) {
                // try to get better upper bound
                // construct a DFS by using the node in the middle of the longest path as root
                // TODO do it for longest path as well?
                ListVertex<D> rootNode = path.get(path.size() / 2);
                Set<ListVertex<D>> nodesHandled = new HashSet();
                int height = DFSTree(rootNode, nodesHandled);
                // System.out.println("   " + height);
                if (nodesHandled.size() != graph.getNumberOfVertices()) {
                    // graph is not connected, the obtained result is not valid
                    height = Integer.MAX_VALUE;
                }



                if (height < upperBound) {
                    this.upperBound = height;
                }
            }
        }
        lowerBound = (int) Math.ceil(Math.log(maxPathLength + 1) / Math.log(2.0));
    }

    private List<ListVertex<D>> findRandomPath() {
        ArrayList<NVertex<D>> vertices = ((ListGraph) graph).vertices;

        // random start at some vertex
        Random rand = new Random();
        int startVertexIndex = rand.nextInt(graph.getNumberOfVertices()-1);

        ListVertex<D> curVertex = (ListVertex<D>) vertices.get(startVertexIndex);
        List<ListVertex<D>> pathFound = new ArrayList<>();
        pathFound.add(curVertex);

        boolean startVertexHandledAgain = false;
        while (true) {

            // get the last vertex in the path
            curVertex = pathFound.get(pathFound.size()-1);

            if (curVertex.getNumberOfNeighbors() == 0) {
                // may only happen if the graph is not connected
                break;
            }

            // first try to randomly select a neighbour to be the next vertex in the path
            ListVertex<D> nextVertex = (ListVertex<D>) curVertex.neighbors.get(rand.nextInt(curVertex.getNumberOfNeighbors()));
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
                    if (startVertexHandledAgain) {
                        // path cannot be increased anymore so stop
                        break;
                    } else {
                        // go back to start vertex and try to increase path from there
                        startVertexHandledAgain = true;
                        // reverse current path found such that start vertex is at the end
                        Collections.reverse(pathFound);
                    }
                }
            }
        }

        return pathFound;
    }


    /*
     * Input is the root node, returns distance from the lowest descendant of the rootNode, i.e. the current
     * height of the tree
     */
    private int DFSTree(ListVertex<D> rootNode, Set<ListVertex<D>> handledVertices) {

        handledVertices.add(rootNode);
        // System.out.print(rootNode.data.name + " ");

        int height = 0;
        for (NVertex<D> neighbor : rootNode.neighbors) {
            if (!handledVertices.contains(neighbor)) {
                int heightSubtree = DFSTree((ListVertex<D>) neighbor, handledVertices);
                if (heightSubtree > height) {
                    height = heightSubtree;
                }
            }
        }
        return ++height;
    }
}
