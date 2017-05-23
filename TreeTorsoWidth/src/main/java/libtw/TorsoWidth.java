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
 * The TorsoWidth algorithm collapses all the non-integer vertices (a ∞-torso is created)
 * and then takes the tree width of the resulting graph to be the torso width<br/>
 *
 * Reference paper: Going Beyond Primal Treewidth for (M)ILP, Robert Ganian, Sebastian Ordyniak, M. S. Ramanujan.
 *
 * @author Verena Dittmer
 *
 */
public class TorsoWidth<D extends GraphInput.InputData> implements UpperBound<D>, LowerBound<D> {
    private static final Logger LOGGER = LoggerFactory.getLogger(TorsoWidth.class);

    private int lowerbound = Integer.MIN_VALUE;
    private int upperbound = Integer.MAX_VALUE;
    private NGraph<D> graph;
    private LowerBound<D> lbAlg;
    private UpperBound<D> ubAlg;
    private static boolean FAST_ALGORITHM = true;

    public TorsoWidth(UpperBound<D> ubAlg){
        this.ubAlg = ubAlg;
    }

    public TorsoWidth(UpperBound<D> ubAlg, LowerBound<D> lbAlg){
        this.ubAlg = ubAlg;
        this.lbAlg = lbAlg;
    }

    @Override
    public String getName() {
        return "TorsoWidth" + (FAST_ALGORITHM ? "(Fast implementation)" : "");
    }

    @Override
    public void setInput(NGraph<D> g)  {
        this.graph = g; // TODO make a deep copy
    };

    public void run() throws InterruptedException {

        if (this.ubAlg == null) {
            LOGGER.error("TorsoWidth algorithm needs to haven an upperbound algorithm defined!");
            return;
        }

        LOGGER.debug("Num vertices before " + getName() + ": " + graph.getNumberOfVertices());
        LOGGER.debug("Num edges before " + getName() + ": " + graph.getNumberOfEdges());

        if (FAST_ALGORITHM) {
            runAlternative();
            return;
        }

        // create a ∞-torso which is a graph obtained by collapsing (at least) all the non-integer vertices
        Iterator<NVertex<D>> vertexIterator = graph.iterator();
        NVertex<D> vertex;

        int i = 0;
        while (vertexIterator.hasNext()) {
            if ((i % 10 == 0) && Thread.currentThread().isInterrupted()) {
                throw new InterruptedException();
            }
            vertex = vertexIterator.next();
            LPInputData data = (LPInputData) vertex.data;

            // vertex corresponds to a real valued variable
            if (!data.isInteger()) {
                Iterator<NVertex<D>> iter1 = vertex.getNeighbors();
                while (iter1.hasNext()) {
                    NVertex<D> neighbour1 = iter1.next();

                    Iterator<NVertex<D>> iter2 = vertex.getNeighbors();
                    while (iter2.hasNext()) {
                        NVertex<D> neighbour2 = iter2.next();
                        if (!neighbour1.equals(neighbour2)) {
                            neighbour1.ensureNeighbor(neighbour2);
                        }
                    }
                }
                vertexIterator.remove();
            }
        }

        ArrayList<NVertex<D>> vertices = ((ListGraph) graph).vertices;

        Map<NVertex<D>, List<NVertex<D>>> vNeighboursToRemove = new HashMap<>();

        // remove now all the invalid edges, i.e. edges that contain references to nodes which were deleted
        for (Iterator<NVertex<D>> iterator = graph.iterator(); iterator.hasNext(); ) {
            NVertex<D> next = iterator.next();

            for (NVertex<D> vNeighbour : next) {
                if (!vertices.contains(vNeighbour)) {

                    // vNeighbour needs to be deleted, save it to next
                    if (!vNeighboursToRemove.containsKey(next)) {
                        vNeighboursToRemove.put(next, new ArrayList<>());
                    }
                    vNeighboursToRemove.get(next).add(vNeighbour);
                }
            }
        }

        Iterator<Map.Entry<NVertex<D>, List<NVertex<D>>>> iterator = vNeighboursToRemove.entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry<NVertex<D>, List<NVertex<D>>> next = iterator.next();
            ((ListVertex<D>) next.getKey()).neighbors.removeAll(next.getValue());
        }

        LOGGER.debug("Num vertices after " + getName() + ": " + graph.getNumberOfVertices());
        LOGGER.debug("Num edges after " + getName() + ": " + graph.getNumberOfEdges());

        // compute lowerbound of treewidth of collapsed graph
        if (lbAlg != null) {
            lbAlg.setInput(graph);
            lbAlg.run();
            this.lowerbound = lbAlg.getLowerBound();
        }
        // compute upperbound of treewidth of collapsed graph
        ubAlg.setInput(graph);
        ubAlg.run();
        this.upperbound = ubAlg.getUpperBound();

    }

    /*
    Instead of eliminating each non-integer vertex, find a vertex set of non-integer nodes of a connected component in
    the graph and collapse this set in the graph by connecting the neighbours of such a set
     */
    private void runAlternative() throws InterruptedException {

        Set<NVertex<D>> verticesToRemove = new HashSet<>();
        Iterator<NVertex<D>> vertexIterator = graph.iterator();

        int i = 0;
        while (vertexIterator.hasNext()) {
            if ((i % 10 == 0) && Thread.currentThread().isInterrupted()) {
                throw new InterruptedException();
            }
            NVertex<D> vertex = vertexIterator.next();

            // a non-integer node not yet handled
            if (!((LPInputData) vertex.data).isInteger() && !verticesToRemove.contains(vertex)) {

                // choose this vertex as a starting vertex for the current connected component
                Set<NVertex<D>> currentNonIntegerSet = new HashSet<>();
                Set<NVertex<D>> currentIntegerSet = new HashSet<>();
                List<NVertex<D>> nodesToHandle = new LinkedList<>();
                nodesToHandle.add(vertex);

                while (!nodesToHandle.isEmpty()) {
                    NVertex<D> next = nodesToHandle.get(0);

                    if (((LPInputData) next.data).isInteger()) {
                        if (!currentIntegerSet.contains(next)) {
                            currentIntegerSet.add(next);
                        }
                    } else {
                        if (!currentNonIntegerSet.contains(next)) {
                            currentNonIntegerSet.add(next);

                            // old : until now as fast as new implementation ?
                            // nodesToHandle.addAll(((ListVertex) next).neighbors);

                            // new :
                            List<NVertex<D>> notYetHandled = new ArrayList<>();
                            for (NVertex<D> neighbour : ((ListVertex<D>) next).neighbors) {
                                LPInputData data = (LPInputData) neighbour.data;
                                if (!data.isNodeHandled()) {
                                    notYetHandled.add(neighbour);
                                    data.setNodeHandled(true);
                                } else if (data.isInteger()) {
                                    // make sure that the integer node is handled again even though it was before already
                                    notYetHandled.add(neighbour);
                                }
                            }
                            nodesToHandle.addAll(notYetHandled); // TODO only those not yet contained
                        }
                    }
                    ((LPInputData) next.data).setNodeHandled(true);
                    nodesToHandle.remove(0);
                }

                // mark nodes in currentSet to be deleted
                verticesToRemove.addAll(currentNonIntegerSet);

                // form a clique of the nodes in currentIntegerNeighbours
                for (NVertex<D> integerNode1 : currentIntegerSet) {
                    for (NVertex<D> integerNode2 : currentIntegerSet) {
                        if (!integerNode1.equals(integerNode2)) {
                            integerNode1.ensureNeighbor(integerNode2);
                        }
                    }
                }
            }
        }

        // delete nodes
        ((ListGraph) graph).vertices.removeAll(verticesToRemove);

        // delete nodes in neighbour lists of integer nodes
        for (NVertex<D> vertex : graph) {
            for (NVertex<D> vertexToDel : verticesToRemove) {
                vertex.removeNeighbor(vertexToDel);
            }
        }

        LOGGER.debug("Num vertices after " + getName() + ": " + graph.getNumberOfVertices());
        LOGGER.debug("Num edges after " + getName() + ": " + graph.getNumberOfEdges());

        // compute lowerbound of treewidth of collapsed graph
        if (lbAlg != null) {
            lbAlg.setInput(graph);
            lbAlg.run();
            this.lowerbound = lbAlg.getLowerBound();
        }
        // compute upperbound of treewidth of collapsed graph
        ubAlg.setInput(graph);
        ubAlg.run();
        this.upperbound = ubAlg.getUpperBound();

    }

    public static void setFastAlgorithm(boolean fastAlgorithm) {
        FAST_ALGORITHM = fastAlgorithm;
    }

    @Override
    public int getUpperBound() {
        return this.upperbound;
    }

    @Override
    public int getLowerBound() {
        return this.lowerbound;
    }
}
