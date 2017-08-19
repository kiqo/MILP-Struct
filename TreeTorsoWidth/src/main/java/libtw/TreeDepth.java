package main.java.libtw;

import main.java.Configuration;
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
public class TreeDepth<D extends GraphInput.InputData> implements UpperBound<D> {

    class MyConverter<D extends GraphInput.InputData> implements NGraph.Convertor<D,LPInputData> {
        public LPInputData convert(NVertex<D> old ) {
            LPInputData d = new LPInputData(old.data.id, old.data.name, ((LPInputData) old.data).isInteger());
            d.setNodeHandled(false);
            return d;
        }
    }

    private static final Logger LOGGER = LoggerFactory.getLogger(TreeDepth.class);
    private static final int NUM_DFS_TREE_GENERATION = 100;
    private static final boolean MAX_DEGREE_HEURISTIC = true;

    private int upperBound = Integer.MAX_VALUE;

    public List<ListVertex<D>> getLongestPath() {
        return longestPath;
    }

    private List<ListVertex<D>> longestPath = null;
    private NGraph<D> graph;

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

        for (int i = 0; i < NUM_DFS_TREE_GENERATION; i++) {
            List<ListVertex<D>> path = findRandomPath();
            maxPathLength = checkLongestPath(maxPathLength, path);

            Set<ListVertex<D>> nodesHandled = new HashSet<>();
            ListVertex<D> rootNode = computeRootNodeByPath(path);
            int height = constructDFSTreeWithRootNode(rootNode, nodesHandled);

            if (!allNodesOfGraphHandled(nodesHandled)) {
                // graph is not connected
                if (!Configuration.OBJ_FUNCTION) {
                    height = computeTreeDepthOverComponents(nodesHandled, height);
                } else {
                    // the result is not valid
                    height = Integer.MAX_VALUE;
                }
            }
            updateUpperBound(height);
        }
    }

    private ListVertex<D> computeRootNodeByPath(List<ListVertex<D>> path) {
        return path.get(path.size() / 2);
    }

    private int constructDFSTreeWithRootNode(ListVertex<D> rootNode, Set nodesHandled) {
        int height;
        if (MAX_DEGREE_HEURISTIC) {
            height = DepthFirstSearch.DFSTreeByMaxDegreeRoot(rootNode, nodesHandled);
        } else {
            height = DepthFirstSearch.DFSTree(rootNode, nodesHandled);
        }
        return height;
    }

    private void updateUpperBound(int height) {
        if (height < upperBound) {
            this.upperBound = height;
        }
    }

    private boolean allNodesOfGraphHandled(Set<ListVertex<D>> nodesHandled) {
        return nodesHandled.size() == graph.getNumberOfVertices();
    }

    private int computeTreeDepthOverComponents(Set<ListVertex<D>> nodesHandled, int maxHeight) {
        int heightComponent;
        while (!allNodesOfGraphHandled(nodesHandled)) {
            NVertex<D> newRootNode = computeRootNodeInDifferentComponent(nodesHandled);
            heightComponent = constructDFSTreeWithRootNode((ListVertex<D>) newRootNode, nodesHandled);
            maxHeight = checkTreeDepthIncreases(maxHeight, heightComponent);
        }
        return maxHeight;
    }

    private int checkTreeDepthIncreases(int maxHeight, int upperBoundComponent) {
        if (upperBoundComponent > maxHeight) {
            maxHeight = upperBoundComponent;
        }
        return maxHeight;
    }

    private NVertex<D> computeRootNodeInDifferentComponent(Set<ListVertex<D>> nodesHandled) {
        Iterator<NVertex<D>> iterator = graph.iterator();
        NVertex<D> newRootNode = null;
        while (iterator.hasNext()) {
            newRootNode = iterator.next();

            if (!nodesHandled.contains(newRootNode)) {
                break;
            }
        }
        return newRootNode;
    }

    private int checkLongestPath(int maxPathLength, List<ListVertex<D>> path) {
        int pathLength = path.size();
        if (pathLength > maxPathLength) {
            longestPath = path;
            maxPathLength = pathLength;
        }
        return maxPathLength;
    }

    private List<ListVertex<D>> findRandomPath() {
        Random rand = new Random();
        ListVertex<D> curVertex = getRandomVertex(rand);

        List<ListVertex<D>> pathFound = new ArrayList<>();
        pathFound.add(curVertex);

        boolean startVertexHandledAgain = false;
        while (true) {
            // get the last vertex in the path
            curVertex = pathFound.get(pathFound.size()-1);

            if (hasNoNeighbours(curVertex)) {
                break;
            }

            // first try to randomly select a neighbour to be the next vertex in the path
            boolean pathIncreased = addRandomNeighbourNotInPath(rand, curVertex, pathFound);
            if (!pathIncreased) {
                pathIncreased = addFirstNeighbourNotInPath(curVertex, pathFound);

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

    private ListVertex<D> getRandomVertex(Random rand) {
        ArrayList<NVertex<D>> vertices = ((ListGraph) graph).vertices;
        int startVertexIndex = randomVertexIndex(rand, graph.getNumberOfVertices() - 1);
        return (ListVertex<D>) vertices.get(startVertexIndex);
    }

    private boolean addRandomNeighbourNotInPath(Random rand, ListVertex<D> curVertex, List<ListVertex<D>> pathFound) {
        ListVertex<D> nextVertex = (ListVertex<D>) curVertex.neighbors.get(randomVertexIndex(rand, curVertex.getNumberOfNeighbors()));
        if (!pathFound.contains(nextVertex)) {
            pathFound.add(nextVertex);
            return true;
        }
        return false;
    }

    private boolean hasNoNeighbours(ListVertex<D> curVertex) {
        if (curVertex.getNumberOfNeighbors() == 0) {
            // may only happen if the graph is not connected
            LOGGER.warn("Vertex " + curVertex.data.name + " with id " + curVertex.data.id + " has no neighbours!");
            return true;
        }
        return false;
    }

    private boolean addFirstNeighbourNotInPath(ListVertex<D> curVertex, List<ListVertex<D>> pathFound) {
        Iterator<NVertex<D>> iter = curVertex.getNeighbors();

        boolean pathIncreased = false;
        while (!pathIncreased && iter.hasNext()) {
            ListVertex<D> listVertex = (ListVertex<D>) iter.next();
            if (!pathFound.contains(listVertex)) {
                pathFound.add(listVertex);
                pathIncreased = true;
            }
        }
        return pathIncreased;
    }

    private int randomVertexIndex(Random rand, int bound) {
        return rand.nextInt(bound);
    }
}
