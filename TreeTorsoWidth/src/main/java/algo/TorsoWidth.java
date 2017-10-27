package main.java.algo;

import main.java.main.ThreadExecutor;
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
 * and then takes (an upper or lower bound on) tree width of the resulting graph to be an upper bound for the torso width
 * <p>
 * Reference paper: Going Beyond Primal Treewidth for (M)ILP, Robert Ganian, Sebastian Ordyniak, M. S. Ramanujan.
 *
 * @author Verena Dittmer
 */
public class TorsoWidth<D extends LPInputData> extends ThreadExecutor implements UpperBound<D>, LowerBound<D> {

    class MyConverter<D extends GraphInput.InputData> implements NGraph.Convertor<D, LPInputData> {
        public LPInputData convert(NVertex<D> old) {
            LPInputData d = new LPInputData(old.data.id, old.data.name, ((LPInputData) old.data).isInteger());
            d.setNodeHandled(false);
            return d;
        }
    }

    private static final Logger LOGGER = LoggerFactory.getLogger(TorsoWidth.class);
    private int lowerBound = Integer.MIN_VALUE;
    private int upperBound = Integer.MAX_VALUE;
    private NGraph<D> graph;

    @Override
    public String getName() {
        return "TorsoWidth";
    }

    @Override
    public void setInput(NGraph<D> g) {
        MyConverter converter = new MyConverter();
        graph = g.copy(converter);
        List<NGraph<D>> components = g.getComponents();
        List<NGraph<D>> componentsCopy = new ArrayList<>();
        for (NGraph<D> oldComponent : components) {
            componentsCopy.add(oldComponent.copy(converter));
        }
        graph.setComponents(componentsCopy);
    }

    public void run() throws InterruptedException {
        // only handle the components and keep the graph itself as it was
        constructTorsoGraphOnComponents();
        computeLowerBoundOnComponents();
        computeUpperBoundOnComponents();
    }

    private void constructTorsoGraphOnComponents() throws InterruptedException {
        Iterator<NGraph<D>> iterator = graph.getComponents().iterator();
        while (iterator.hasNext()) {
            NGraph<D> component = iterator.next();
            constructTorsoForComponent(component);
            deleteComponentIfEmpty(iterator, component);
        }
    }

    private void constructTorsoForComponent(NGraph<D> component) throws InterruptedException {
        Set<NVertex<D>> verticesToRemove = new HashSet<>();
        Iterator<NVertex<D>> vertexIterator = component.iterator();
        NVertex<D> vertex;
        int iteration = 0;

        while (vertexIterator.hasNext()) {
            if (iteration++ % 10 == 0) {
                checkInterrupted();
            }

            vertex = vertexIterator.next();

            // a non-integer node not yet handled
            LPInputData vertexData = vertex.data;
            if (!vertexData.isInteger() && !vertexData.isNodeHandled()) {
                handleVertexInComponent(verticesToRemove, vertex);
            }
        }
        deleteMarkedNodes(component, verticesToRemove);
    }

    private void deleteComponentIfEmpty(Iterator<NGraph<D>> iterator, NGraph<D> component) {
        if (component.getNumberOfVertices() == 0) {
            iterator.remove();
        }
    }

    private void deleteMarkedNodes(NGraph<D> component, Set<NVertex<D>> verticesToRemove) {
        ((ListGraph<D>) component).vertices.removeAll(verticesToRemove);

        // delete nodes in neighbour lists of integer nodes
        for (NVertex<D> vertexToRemove : verticesToRemove) {
            for (NVertex<D> neighbour : vertexToRemove) {
                if (!verticesToRemove.contains(neighbour)) {
                    neighbour.removeNeighbor(vertexToRemove);
                }
            }
        }
    }

    private void handleVertexInComponent(Set<NVertex<D>> verticesToRemove, NVertex<D> startingVertex) {
        Set<NVertex<D>> currentNonIntegerSet = new HashSet<>();
        Set<NVertex<D>> currentIntegerSet = new HashSet<>();
        Set<NVertex<D>> nodesToHandle = new HashSet<>();
        nodesToHandle.add(startingVertex);
        startingVertex.data.setNodeHandled(true);

        while (!nodesToHandle.isEmpty()) {
            handleVertex(currentNonIntegerSet, currentIntegerSet, nodesToHandle);
        }
        markNodesForDeletion(verticesToRemove, currentNonIntegerSet);
        addEdgesToFormClique(currentIntegerSet);
    }

    private void handleVertex(Set<NVertex<D>> currentNonIntegerSet, Set<NVertex<D>> currentIntegerSet, Set<NVertex<D>> nodesToHandle) {
        Iterator<NVertex<D>> iterator = nodesToHandle.iterator();
        NVertex<D> next = iterator.next();
        iterator.remove();
        next.data.setNodeHandled(true);
        handleNonIntegerVertex(nodesToHandle, currentNonIntegerSet, currentIntegerSet, next);
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

    private void handleNonIntegerVertex(Set<NVertex<D>> nodesToHandle, Set<NVertex<D>> currentNonIntegerSet, Set<NVertex<D>> currentIntegerSet, NVertex<D> curVertex) {
            currentNonIntegerSet.add(curVertex);

            for (NVertex<D> neighbour : ((ListVertex<D>) curVertex).neighbors) {
                LPInputData data = neighbour.data;

                if (data.isInteger()) {
                    currentIntegerSet.add(neighbour);
                } else if (!data.isNodeHandled()) {
                    nodesToHandle.add(neighbour);
                    data.setNodeHandled(true);
                }
            }
    }

    private void computeLowerBoundOnComponents() throws InterruptedException {
        this.lowerBound = TreeWidthWrapper.computeLowerBoundWithComponents((NGraph) graph);
    }

    private void computeUpperBoundOnComponents() throws InterruptedException {
        this.upperBound = TreeWidthWrapper.computeUpperBoundWithComponents((NGraph) graph);

    }

    public NGraph<D> getGraph() {
        return graph;
    }

    @Override
    public int getUpperBound() {
        return this.upperBound;
    }

    @Override
    public int getLowerBound() {
        return this.lowerBound;
    }
}
