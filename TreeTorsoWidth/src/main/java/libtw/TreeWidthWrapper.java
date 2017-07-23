package main.java.libtw;

import main.java.Configuration;
import nl.uu.cs.treewidth.algorithm.LowerBound;
import nl.uu.cs.treewidth.input.GraphInput;
import nl.uu.cs.treewidth.ngraph.NGraph;
import nl.uu.cs.treewidth.ngraph.NVertex;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.InvocationTargetException;
import java.util.List;

/**
 * Created by Verena on 23.07.2017.
 */
public class TreeWidthWrapper {

    private static final Logger LOGGER = LoggerFactory.getLogger(TreeWidthWrapper.class);

    public int computeLowerBound(NGraph<GraphInput.InputData> g) throws InterruptedException {
        LowerBound<GraphInput.InputData> lowerBoundAlg = null;
        try {
            lowerBoundAlg = (LowerBound<GraphInput.InputData>) Configuration.LOWER_BOUND_ALG.getConstructor().newInstance();
        } catch (InstantiationException | NoSuchMethodException | InvocationTargetException | IllegalAccessException e) {
            LOGGER.error(e.getMessage());
        }
        lowerBoundAlg.setInput(g);
        lowerBoundAlg.run();
        int lowerbound = lowerBoundAlg.getLowerBound();
        return lowerbound;
    }


    public int computeLowerBoundWithComponents(NGraph<GraphInput.InputData> g) throws InterruptedException {
        LowerBound<GraphInput.InputData> lowerBoundAlg = null;
        try {
            lowerBoundAlg = (LowerBound<GraphInput.InputData>) Configuration.LOWER_BOUND_ALG.getConstructor().newInstance();
        } catch (InstantiationException | NoSuchMethodException | InvocationTargetException | IllegalAccessException e) {
            LOGGER.error(e.getMessage());
        }


        int lowerbound = lowerBoundAlg.getLowerBound();
        int lowerboundSubGraph = Integer.MIN_VALUE;
        for (NGraph subGraph : g.getComponents()) {
            lowerboundSubGraph = Integer.MIN_VALUE;
            lowerBoundAlg.setInput(subGraph);
            lowerBoundAlg.run();
            lowerboundSubGraph = lowerBoundAlg.getLowerBound();

            // take the maximum over all subgraph lower bounds to be the lower bound
            if (lowerboundSubGraph > lowerbound) {
                lowerbound = lowerboundSubGraph;
            }
        }

        return lowerbound;
    }



}
