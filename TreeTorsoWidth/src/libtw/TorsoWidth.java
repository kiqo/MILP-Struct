package libtw;

import graph.Graph;
import nl.uu.cs.treewidth.algorithm.LowerBound;
import nl.uu.cs.treewidth.algorithm.UpperBound;
import nl.uu.cs.treewidth.input.GraphInput;
import nl.uu.cs.treewidth.ngraph.ListGraph;
import nl.uu.cs.treewidth.ngraph.ListVertex;
import nl.uu.cs.treewidth.ngraph.NGraph;
import nl.uu.cs.treewidth.ngraph.NVertex;

import java.util.*;
import java.util.stream.Collectors;

/**
 * The TorsoWidth algorithm collapses all the non-integer vertices (a ∞-torso is created)
 * and then takes the tree width of the resulting graph to be the torso width<br/>
 *
 * Reference paper: Going Beyond Primal Treewidth for (M)ILP, Robert Ganian, Sebastian Ordyniak, M. S. Ramanujan.
 *
 * @author Verena Dittmer
 *
 */
public class TorsoWidth<D extends GraphInput.InputData> implements UpperBound<GraphInput.InputData>, LowerBound<GraphInput.InputData> {
    private int lowerbound = Integer.MIN_VALUE;
    private int upperbound = Integer.MAX_VALUE;
    private NGraph<GraphInput.InputData> graph;
    private LowerBound<GraphInput.InputData> lbAlg;
    private UpperBound<GraphInput.InputData> ubAlg;

    public static void setFastAlgorithm(boolean fastAlgorithm) {
        FAST_ALGORITHM = fastAlgorithm;
    }

    private static boolean FAST_ALGORITHM = true;

    public TorsoWidth(UpperBound<GraphInput.InputData> ubAlg){
        this.ubAlg = ubAlg;
    }

    public TorsoWidth(UpperBound<GraphInput.InputData> ubAlg, LowerBound<GraphInput.InputData> lbAlg){
        this.ubAlg = ubAlg;
        this.lbAlg = lbAlg;
    }

    @Override
    public String getName() {
        return "TorsoWidth" + (FAST_ALGORITHM ? "(Fast implementation)" : "");
    }

    @Override
    public void setInput(NGraph<GraphInput.InputData> g)  {
        this.graph = g; // TODO make a deep copy
    };

    public void run() {

        if (this.ubAlg == null) {
            System.out.println("Error: TorsoWidth algorithm needs to haven an upperbound algorithm defined!");
            return;
        }

        System.out.println("Num vertices before TorsoWidthAlg" + (FAST_ALGORITHM ? "(Fast implementation)" : "") + ": " + graph.getNumberOfVertices());
        System.out.println("Num edges before TorsoWidthAlg" + (FAST_ALGORITHM ? "(Fast implementation)" : "") + ": " + graph.getNumberOfEdges());

        if (FAST_ALGORITHM) {
            runAlternative();
            return;
        }

        // create a ∞-torso which is a graph obtained by collapsing (at least) all the non-integer vertices
        Iterator<NVertex<GraphInput.InputData>> vertexIterator = graph.iterator();
        NVertex<GraphInput.InputData> vertex;

        System.out.println("Num vertices before TorsoWidthAlg: " + graph.getNumberOfVertices());
        System.out.println("Num edges before TorsoWidthAlg: " + graph.getNumberOfEdges());


        while (vertexIterator.hasNext()) {
            vertex = vertexIterator.next();
            LPInputData data = (LPInputData) vertex.data;

            // vertex corresponds to a real valued variable
            if (!data.isInteger()) {
                Iterator<NVertex<GraphInput.InputData>> iter1 = vertex.getNeighbors();
                while (iter1.hasNext()) {
                    NVertex<GraphInput.InputData> neighbour1 = iter1.next();

                    Iterator<NVertex<GraphInput.InputData>> iter2 = vertex.getNeighbors();
                    while (iter2.hasNext()) {
                        NVertex<GraphInput.InputData> neighbour2 = iter2.next();
                        if (!neighbour1.equals(neighbour2)) {
                            neighbour1.ensureNeighbor(neighbour2);
                        }
                    }
                }
                vertexIterator.remove();
            }
        }

        ArrayList<NVertex<D>> vertices = ((ListGraph) graph).vertices;

        Map<NVertex<GraphInput.InputData>, List<NVertex<GraphInput.InputData>>> vNeighboursToRemove = new HashMap<>();

        // remove now all the invalid edges, i.e. edges that contain references to nodes which were deleted
        for (Iterator<NVertex<GraphInput.InputData>> iterator = graph.iterator(); iterator.hasNext(); ) {
            NVertex<GraphInput.InputData> next = iterator.next();

            for (NVertex<GraphInput.InputData> vNeighbour : next) {
                if (!vertices.contains(vNeighbour)) {

                    // vNeighbour needs to be deleted, save it to next
                    if (!vNeighboursToRemove.containsKey(next)) {
                        vNeighboursToRemove.put(next, new ArrayList<>());
                    }
                    vNeighboursToRemove.get(next).add(vNeighbour);
                }
            }
        }

        Iterator<Map.Entry<NVertex<GraphInput.InputData>, List<NVertex<GraphInput.InputData>>>> iterator = vNeighboursToRemove.entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry<NVertex<GraphInput.InputData>, List<NVertex<GraphInput.InputData>>> next = iterator.next();
            ((ListVertex<D>) next.getKey()).neighbors.removeAll(next.getValue());
        }

        System.out.println("Num vertices after TorsoWidthAlg: " + graph.getNumberOfVertices());
        System.out.println("Num edges after TorsoWidthAlg: " + graph.getNumberOfEdges());


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
    private void runAlternative() {

        Set<NVertex<GraphInput.InputData>> verticesToRemove = new HashSet<>();
        Iterator<NVertex<GraphInput.InputData>> vertexIterator = graph.iterator();

        while (vertexIterator.hasNext()) {
            NVertex<GraphInput.InputData> vertex = vertexIterator.next();

            // a non-integer node not yet handled
            if (!((LPInputData) vertex.data).isInteger() && !verticesToRemove.contains(vertex)) {

                // choose this vertex as a starting vertex for the current connected component
                Set<NVertex<GraphInput.InputData>> currentNonIntegerSet = new HashSet<>();
                Set<NVertex<GraphInput.InputData>> currentIntegerSet = new HashSet<>();
                List<NVertex<GraphInput.InputData>> nodesToHandle = new LinkedList<>();
                nodesToHandle.add(vertex);

                int maxIndex = 0;
                while (!nodesToHandle.isEmpty()) {
                    NVertex<GraphInput.InputData> next = nodesToHandle.get(0);

                    if (((LPInputData) next.data).isInteger()) {
                        if (!currentIntegerSet.contains(next)) {
                            currentIntegerSet.add(next);
                        }
                    } else {
                        if (!currentNonIntegerSet.contains(next)) {
                            currentNonIntegerSet.add(next);
                            List<NVertex<GraphInput.InputData>> verticesNotHandled = new ArrayList<>();
                            for (Object neighbour : ((ListVertex<GraphInput.InputData>) next).neighbors) {
                                NVertex n = (NVertex<LPInputData>) neighbour;
                                if (!((LPInputData) n.data).isNodeHandled()) {
                                    verticesNotHandled.add(n);
                                }
                            }

                            nodesToHandle.addAll(verticesNotHandled);
                        }
                    }
                    nodesToHandle.remove(0);
                }

                // mark nodes in currentSet to be deleted
                verticesToRemove.addAll(currentNonIntegerSet);

                // form a clique of the nodes in currentIntegerNeighbours
                for (NVertex<GraphInput.InputData> integerNode1 : currentIntegerSet) {
                    for (NVertex<GraphInput.InputData> integerNode2 : currentIntegerSet) {
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
        for (NVertex<GraphInput.InputData> vertex : graph) {
            for (NVertex<GraphInput.InputData> vertexToDel : verticesToRemove) {
                vertex.removeNeighbor(vertexToDel);
            }
        }

        System.out.println("Num vertices after TorsoWidthAlgAlt: " + graph.getNumberOfVertices());
        System.out.println("Num edges after TorsoWidthAlgAlt: " + graph.getNumberOfEdges());

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


    @Override
    public int getUpperBound() {
        return this.upperbound;
    }

    @Override
    public int getLowerBound() {
        return this.lowerbound;
    }
}
