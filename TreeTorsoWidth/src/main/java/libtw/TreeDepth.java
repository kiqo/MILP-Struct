package main.java.libtw;

import main.java.Configuration;
import nl.uu.cs.treewidth.algorithm.GreedyFillIn;
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
 * The tree-depth td(G) is defined to be the minimum height of a rooted forest F
 * such that G is a subgraph of the closure of F, where the closure of F is obtained
 * by adding edges between ancestor and descendents in a tree.
 *
 * The treewidth is a lower bound for treedepth. Another lower bound is the logarithm
 * of the length of any path in the graph.
 *
 * For upper bound, the idea is to select some starting points and run a DFS from these.
 * These starting points can be chosen randomly or with greedy heuristic (like taking
 * the center of some greedily constructed maximal path in the graph).
 *
 * @author Verena Dittmer
 * */
public class TreeDepth<D extends GraphInput.InputData> implements LowerBound<D>, UpperBound<D> {

    class MyConverter<D extends GraphInput.InputData> implements NGraph.Convertor<D,LPInputData> {
        public LPInputData convert(NVertex<D> old ) {
            LPInputData d = new LPInputData(old.data.id, old.data.name, ((LPInputData) old.data).isInteger());
            d.setNodeHandled(false);
            return d;
        }
    }

    private static final Logger LOGGER = LoggerFactory.getLogger(TreeDepth.class);
    // times that some DFS spanning tree are generated
    private static final int NUM_ITERATIONS = 100;
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
        graph = g.copy(new MyConverter());
    }

    @Override
    public void run() throws InterruptedException {
        // Lower bound of tree depth
        int maxPathLength = Integer.MIN_VALUE;

        for (int i = 0; i < NUM_ITERATIONS; i++) {
            List<ListVertex<D>> path = findRandomPath();
            int pathLength = path.size();
            if (pathLength > maxPathLength) {
                longestPath = path;
                maxPathLength = pathLength;
            }

            // try to get better upper bound
            // construct a DFS by using the node in the middle of the longest path as root
            ListVertex<D> rootNode = path.get(path.size() / 2);
            Set<ListVertex<D>> nodesHandled = new HashSet();
            int height = DFSTreeByMaxDegreeRoot(rootNode, nodesHandled);

            if (nodesHandled.size() != graph.getNumberOfVertices()) {
                // graph is not connected
                if (!Configuration.OBJ_FUNCTION) {
                    //objective function is not considered, then compute the tree depth over all components of the graph

                    int heightComponent;
                    while (nodesHandled.size() != graph.getNumberOfVertices()) {
                        // choose new rootNode
                        Iterator<NVertex<D>> iterator = graph.iterator();
                        NVertex<D> newRootNode = null;
                        while (iterator.hasNext()) {
                            newRootNode = iterator.next();

                            if (!nodesHandled.contains(newRootNode)) {
                                break;
                            }
                        }

                        heightComponent = DFSTreeByMaxDegreeRoot((ListVertex<D>) newRootNode, nodesHandled);
                        if (heightComponent > height) {
                            height = heightComponent;
                        }
                    }
                } else {
                    // the result is not valid
                    height = Integer.MAX_VALUE;
                }
            }

            if (height < upperBound) {
                this.upperBound = height;
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
                LOGGER.warn("Vertex " + curVertex.data.name + " with id " + curVertex.data.id + " has no neighbours!");
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
     * Starts by constructing first the subtree for the neighbour of the rootNode with maximal degree in the graph
     */
    private int DFSTreeByMaxDegreeRoot(ListVertex<D> rootNode, Set<ListVertex<D>> handledVertices) {

        handledVertices.add(rootNode);
        // System.out.print(rootNode.data.name + " ");

        int height = 0;
        int maxDegree;
        NVertex<D> maxDegreeVertex;

        while (true) {
            maxDegree = Integer.MIN_VALUE;
            maxDegreeVertex = null;
            for (NVertex<D> neighbor : rootNode.neighbors) {
                if (!handledVertices.contains(neighbor) && maxDegree < neighbor.getNumberOfNeighbors()) {
                    maxDegreeVertex = neighbor;
                    maxDegree = neighbor.getNumberOfNeighbors();
                }
            }

            // all neighbours of current root node already in tree
            if (maxDegreeVertex == null) {
                break;
            }

            // take maxDegreeVertex as root for subtree
            int heightSubtree = DFSTreeByMaxDegreeRoot((ListVertex<D>) maxDegreeVertex, handledVertices);
            if (heightSubtree > height) {
                height = heightSubtree;
            }
        }

        return ++height;
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
