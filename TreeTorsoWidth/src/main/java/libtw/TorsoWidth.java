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
            LOGGER.error("TorsoWidth algorithm needs to have an upperbound algorithm defined!");
            return;
        }
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
