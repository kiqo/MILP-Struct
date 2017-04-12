import graph.Graph;
import graph.Node;
import nl.uu.cs.treewidth.algorithm.UpperBound;
import nl.uu.cs.treewidth.input.GraphInput;
import nl.uu.cs.treewidth.ngraph.NGraph;

import java.util.Iterator;

/**
 * Created by Verena on 14.03.2017.
 */
public class MyGreedyDegree< D extends GraphInput.InputData> implements UpperBound<GraphInput.InputData> {

    private Graph graph;
    private int upperBound = Integer.MIN_VALUE;
   // private NVertexOrder<GraphInput.InputData> permutation = new NVertexOrder<>();

    @Override
    public int getUpperBound() {
        return this.upperBound;
    }

    @Override
    public String getName() {
        return "My implementation of GreedyDegree";
    }

    @Override
    public void setInput(NGraph graph) {
    }

    public void setInput(Graph graph) {
        this.graph = graph;
    }


    @Override
    public void run() {
        int graphSize = this.graph.getNodes().size(); // TODO check
        while(graphSize > 0) {
            int minDegree = graphSize;
            Node smallestNode = null;
            Iterator<Node> iterator = this.graph.getNodes().iterator();

            while(iterator.hasNext()) {
                Node node = iterator.next();
                int numNeighbours = this.graph.getNeighbourNodes().get(node).size();  // TODO improve with map that stores neighbour size
                if(numNeighbours < minDegree) {
                    minDegree = numNeighbours;
                    smallestNode = node;
                }
            }
            //  this.permutation.order.add(v);
            this.upperBound = Math.max(this.upperBound, minDegree);
            this.graph.eliminate(smallestNode);
            graphSize--;
        }
    }
}
