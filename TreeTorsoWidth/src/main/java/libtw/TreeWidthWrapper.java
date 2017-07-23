package main.java.libtw;

import main.java.Configuration;
import nl.uu.cs.treewidth.algorithm.LowerBound;
import nl.uu.cs.treewidth.algorithm.UpperBound;
import nl.uu.cs.treewidth.input.GraphInput;
import nl.uu.cs.treewidth.ngraph.NGraph;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.InvocationTargetException;

/**
 * Wrapper class to use the lower- and upper bound algorithms of libtw, but with the possiblity
 * to obtain better lower- and upper bounds by considering that the graph may be disconnected
 *
 * Created by Verena on 23.07.2017.
 */
public class TreeWidthWrapper {

    private static final Logger LOGGER = LoggerFactory.getLogger(TreeWidthWrapper.class);

    /*
    Computes the lower bound of the graph without considering that the graph may be disconnected
     */
    public static int computeLowerBound(NGraph<GraphInput.InputData> g) throws InterruptedException {
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

    /*
    Computes the lower bound of the graph by taking the maximum of the treewidth of each component
     */
    public static int computeLowerBoundWithComponents(NGraph<GraphInput.InputData> g) throws InterruptedException {
        LowerBound<GraphInput.InputData> lowerBoundAlg = null;
        try {
            lowerBoundAlg = (LowerBound<GraphInput.InputData>) Configuration.LOWER_BOUND_ALG.getConstructor().newInstance();
        } catch (InstantiationException | NoSuchMethodException | InvocationTargetException | IllegalAccessException e) {
            LOGGER.error(e.getMessage());
        }


        int lowerbound = Integer.MIN_VALUE;
        int lowerboundSubGraph;
        for (NGraph subGraph : g.getComponents()) {
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

    public int computeUpperBoundWithComponents(NGraph<GraphInput.InputData> g) throws InterruptedException {
        UpperBound<GraphInput.InputData> ubAlgo = null;
        try {
            ubAlgo = (UpperBound<GraphInput.InputData>) Configuration.UPPER_BOUND_ALG.getConstructor().newInstance();
        } catch (InstantiationException | NoSuchMethodException | InvocationTargetException | IllegalAccessException e) {
            LOGGER.error(e.getMessage());
        }

        int upperbound = Integer.MIN_VALUE;
        int upperboundSubGraph;
        for (NGraph subGraph : g.getComponents()) {
            ubAlgo.setInput(subGraph);
            ubAlgo.run();
            upperboundSubGraph = ubAlgo.getUpperBound();

            // take the maximum over all subgraph upper bounds to be the upper bound
            if (upperboundSubGraph > upperbound) {
                upperbound = upperboundSubGraph;
            }
        }
        return upperbound;
    }


    public int computeUpperBound(NGraph<GraphInput.InputData> g) throws InterruptedException {
        UpperBound<GraphInput.InputData> ubAlgo = null;
        try {
            ubAlgo = (UpperBound<GraphInput.InputData>) Configuration.UPPER_BOUND_ALG.getConstructor().newInstance();
        } catch (InstantiationException | NoSuchMethodException | InvocationTargetException | IllegalAccessException e) {
            LOGGER.error(e.getMessage());
        }
        ubAlgo.setInput(g);
        ubAlgo.run();
        int upperbound = ubAlgo.getUpperBound();
        return upperbound;
    }
}
