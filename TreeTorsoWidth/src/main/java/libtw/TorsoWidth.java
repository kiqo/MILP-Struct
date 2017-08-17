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

    class MyConverter<D extends GraphInput.InputData> implements NGraph.Convertor<D,LPInputData> {
        public LPInputData convert(NVertex<D> old ) {
            LPInputData d = new LPInputData(old.data.id, old.data.name, ((LPInputData) old.data).isInteger());
            d.setNodeHandled(false);
            return d;
        }
    }

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
        return "TorsoWidth";
    }

    @Override
    public void setInput(NGraph<D> g)  {
        graph = g.copy(new MyConverter());
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

        int iteration = 0;
        while (vertexIterator.hasNext()) {
            checkInterruped(iteration);
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

        Map<NVertex<D>, List<NVertex<D>>> neighboursToRemove = new HashMap<>();

        // remove now all the invalid edges, i.e. edges that contain references to nodes which were deleted
        for (Iterator<NVertex<D>> iterator = graph.iterator(); iterator.hasNext(); ) {
            NVertex<D> next = iterator.next();

            for (NVertex<D> vNeighbour : next) {
                if (!vertices.contains(vNeighbour)) {

                    // vNeighbour needs to be deleted, save it to next
                    if (!neighboursToRemove.containsKey(next)) {
                        neighboursToRemove.put(next, new ArrayList<>());
                    }
                    neighboursToRemove.get(next).add(vNeighbour);
                }
            }
        }

        Iterator<Map.Entry<NVertex<D>, List<NVertex<D>>>> iterator = neighboursToRemove.entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry<NVertex<D>, List<NVertex<D>>> next = iterator.next();
            ((ListVertex<D>) next.getKey()).neighbors.removeAll(next.getValue());
        }
        computeLowerBound();
        computeUpperBound();
    }


    private void runAlternative() throws InterruptedException {
        constructTorsoGraph();
        computeLowerBound();
        computeUpperBound();
    }

    private void constructTorsoGraph() throws InterruptedException {
        Set<NVertex<D>> verticesToRemove = new HashSet<>();
        Iterator<NVertex<D>> vertexIterator = graph.iterator();

        int iteration = 0;
        while (vertexIterator.hasNext()) {
            checkInterruped(iteration++);

            NVertex<D> vertex = vertexIterator.next();

            // a non-integer node not yet handled
            if (!((LPInputData) vertex.data).isInteger() && !verticesToRemove.contains(vertex)) {
                handleComponent(verticesToRemove, vertex);
            }
        }
        deleteMarkedNodes(verticesToRemove);
    }

    private void deleteMarkedNodes(Set<NVertex<D>> verticesToRemove) {
        ((ListGraph) graph).vertices.removeAll(verticesToRemove);

        // delete nodes in neighbour lists of integer nodes
        for (NVertex<D> vertex : graph) {
            for (NVertex<D> vertexToDel : verticesToRemove) {
                vertex.removeNeighbor(vertexToDel);
            }
        }
    }

    private void handleComponent(Set<NVertex<D>> verticesToRemove, NVertex<D> startingVertex) {
        Set<NVertex<D>> currentNonIntegerSet = new HashSet<>();
        Set<NVertex<D>> currentIntegerSet = new HashSet<>();
        List<NVertex<D>> nodesToHandle = new LinkedList<>();
        nodesToHandle.add(startingVertex);

        while (!nodesToHandle.isEmpty()) {
            handleVertex(currentNonIntegerSet, currentIntegerSet, nodesToHandle);
        }
        markNodesForDeletion(verticesToRemove, currentNonIntegerSet);
        addEdgesToFormClique(currentIntegerSet);
    }

    private void handleVertex(Set<NVertex<D>> currentNonIntegerSet, Set<NVertex<D>> currentIntegerSet, List<NVertex<D>> nodesToHandle) {
        NVertex<D> next = nodesToHandle.get(0);

        if (((LPInputData) next.data).isInteger()) {
            handleIntegerVertex(currentIntegerSet, next);
        } else {
            handleNonIntegerVertex(currentNonIntegerSet, nodesToHandle, next);
        }
        ((LPInputData) next.data).setNodeHandled(true);
        nodesToHandle.remove(0);
    }

    private void markNodesForDeletion(Set<NVertex<D>> verticesToRemove, Set<NVertex<D>> currentNonIntegerSet) {
        // mark nodes in currentSet to be deleted
        verticesToRemove.addAll(currentNonIntegerSet);
    }

    private void addEdgesToFormClique(Set<NVertex<D>> currentIntegerSet) {
        // form a clique of the nodes in currentIntegerNeighbours
        for (NVertex<D> integerNode1 : currentIntegerSet) {
            for (NVertex<D> integerNode2 : currentIntegerSet) {
                if (!integerNode1.equals(integerNode2)) {
                    integerNode1.ensureNeighbor(integerNode2);
                }
            }
        }
    }

    private void handleIntegerVertex(Set<NVertex<D>> currentIntegerSet, NVertex<D> next) {
        if (!currentIntegerSet.contains(next)) {
            currentIntegerSet.add(next);
        }
    }

    private void handleNonIntegerVertex(Set<NVertex<D>> currentNonIntegerSet, List<NVertex<D>> nodesToHandle, NVertex<D> next) {
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

    private void checkInterruped(int i) throws InterruptedException {
        if ((i % 10 == 0) && Thread.currentThread().isInterrupted()) {
            throw new InterruptedException();
        }
    }

    private void computeLowerBound() throws InterruptedException {
        if (lbAlg != null) {
            lbAlg.setInput(graph);
            lbAlg.run();
            this.lowerbound = lbAlg.getLowerBound();
        }
    }

    private void computeUpperBound() throws InterruptedException {
        ubAlg.setInput(graph);
        ubAlg.run();
        this.upperbound = ubAlg.getUpperBound();
    }

    public NGraph<D> getGraph() {
        return graph;
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
