import graph.Graph;
import libtw.TorsoWidth;
import lp.LinearProgram;
import nl.uu.cs.treewidth.algorithm.*;
import nl.uu.cs.treewidth.input.GraphInput;
import nl.uu.cs.treewidth.ngraph.NGraph;
import nl.uu.cs.treewidth.ngraph.NTDBag;
import nl.uu.cs.treewidth.ngraph.NVertexOrder;
import nl.uu.cs.treewidth.timing.Stopwatch;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import parser.GraphGenerator;
import parser.GraphTransformator;
import parser.MILPParser;
import sun.reflect.generics.reflectiveObjects.NotImplementedException;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Verena on 28.02.2017.
 */
public class Main {

    private static final Logger LOGGER = LoggerFactory.getLogger(Main.class);

    private static Class<?> UPPER_BOUND_ALG = null;
    private static Class<?> LOWER_BOUND_ALG = null;
    private static String INPUT_FILE = null;
    private static String OUTPUT_FILE = null;
    private static boolean TORSO_WIDTH = false;
    private static boolean LOWER_BOUND = false;
    private static boolean UPPER_BOUND = false;
    private static final Stopwatch t = new Stopwatch();

    public static void main(String[] args) throws IOException {

        init();
        LOGGER.info("Hi, {}", "test");

        parseArguments(args);

        boolean isTxt = INPUT_FILE.substring(INPUT_FILE.length()-3, INPUT_FILE.length()).equals("txt");

        List<String> files = new ArrayList<>();
        if (isTxt) {
            // read all the files that need to be processed
            BufferedReader br = new BufferedReader(new FileReader(INPUT_FILE));

            String line;
            while ((line = br.readLine()) != null) {
                files.add(line);
            }
        } else {
            files.add(INPUT_FILE);
        }

        for (String fileName : files) {
            System.out.println("Structural Parameters: " + fileName);
            computeStructuralParameters(fileName);
            System.out.println("-------------------");
        }
    }

    private static void init() {
        try {
            UPPER_BOUND_ALG = Class.forName("nl.uu.cs.treewidth.algorithm.GreedyDegree");
            LOWER_BOUND_ALG = Class.forName("nl.uu.cs.treewidth.algorithm.MaximumMinimumDegreePlusLeastC");
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

    private static void computeStructuralParameters(String fileName) throws IOException {

        // parse input file
        MILPParser milpParser = new MILPParser();
        LinearProgram lp = milpParser.parseMPS(fileName, false);

        // generate primal graph
        GraphGenerator graphGenerator = new GraphGenerator();
        Graph primalGraph = graphGenerator.linearProgramToPrimalGraph(lp);
        int graphSize = primalGraph.getNodes().size();

        // generate NGraph for using libtw
        NGraph<GraphInput.InputData> g;
        GraphTransformator graphTransformator = new GraphTransformator();
        g = graphTransformator.graphToNGraph(primalGraph);

        // just gets stuck for large instances - TODO try on server
        // g.printGraph(true, true);

        if (LOWER_BOUND) {
            computeTWLowerBound(g);
        }

        if (UPPER_BOUND) {
            computeTWUpperBound(g);
        }

        if (TORSO_WIDTH) {
            computeTorsoWidth(g);
        }
    }

    private static UpperBound<GraphInput.InputData> createUpperBound() {
        UpperBound upperBoundAlg = null;
        try {
            upperBoundAlg = (UpperBound) UPPER_BOUND_ALG.getConstructor().newInstance();
        } catch (InstantiationException | NoSuchMethodException | InvocationTargetException | IllegalAccessException e) {
            e.printStackTrace();
        }
        return upperBoundAlg;
    }


    private static LowerBound<GraphInput.InputData> createLowerBound() {
        LowerBound<GraphInput.InputData> lowerBoundAlg = null;
        try {
            lowerBoundAlg = (LowerBound<GraphInput.InputData>) LOWER_BOUND_ALG.getConstructor().newInstance();
        } catch (InstantiationException | NoSuchMethodException | InvocationTargetException | IllegalAccessException e) {
            e.printStackTrace();
        }
        return lowerBoundAlg;
    }

    private static void computeTorsoWidth(NGraph<GraphInput.InputData> g) {
        t.reset();
        t.start();

        TorsoWidth<GraphInput.InputData> torsoWidthAlgo = new TorsoWidth<>(createUpperBound());
        torsoWidthAlgo.setInput(g);
        torsoWidthAlgo.run();
        int torsoWidthUpperBound = torsoWidthAlgo.getUpperBound();
        t.stop();
        long millisecondsPassed = t.getTime();
        System.out.println("UB TorsoWidth: " + torsoWidthUpperBound + " of " + g.getNumberOfVertices() + " nodes of " + torsoWidthAlgo.getName()
                + ", time: " + millisecondsPassed / 1000 + "s");
    }

    private static void computeTWUpperBound(NGraph<GraphInput.InputData> g) {
        t.reset();
        t.start();
        UpperBound<GraphInput.InputData> ubAlgo = createUpperBound();
        ubAlgo.setInput(g);
        ubAlgo.run();
        int upperbound = ubAlgo.getUpperBound();
        t.stop();
        long millisecondsPassed = t.getTime();
        System.out.println("UB: " + upperbound + " of " + g.getNumberOfVertices() + " nodes " + " of " + ubAlgo.getName()
                + ", time: " + millisecondsPassed / 1000 + "s");
    }

    private static int computeTWLowerBound(NGraph<GraphInput.InputData> g) {
        t.reset();
        t.start();
        LowerBound<GraphInput.InputData> lbAlgo = createLowerBound();
        lbAlgo.setInput(g);
        lbAlgo.run();
        int lowerbound = lbAlgo.getLowerBound();
        t.stop();
        long millisecondsPassed = t.getTime();
        System.out.println("LB: " + lowerbound + " of " + g.getNumberOfVertices() + " nodes " + " of " + lbAlgo.getName()
                + ", time: " + millisecondsPassed / 1000 + "s");
        return lowerbound;
    }


    private static void parseArguments(String[] args) {
        int argc = args.length;
        boolean error = false;
        String helpMessage = "Usage: TreeTorsoWidth -i inputFile.mps|inputFile.txt (-u|-l|-t) [-o outputFile] [-g primal|incidence|dual]";

        if (argc <= 1) {
            error = true;
        }

        boolean expectInputFile = false, expectOutputFile = false , expectGraphType = false;
        for (int i = 0; i < argc; i++) {
            switch (args[i]) {
                case "-i":
                case "-I":
                case "--input": expectInputFile = true; break;

                case "-o":
                case "-O":
                case "--output": expectOutputFile = true; break;

                case "-g":
                case "-G":
                case "--graph": expectGraphType = true; break;

                case "-u":
                case "-U":
                case "--upperbound": UPPER_BOUND = true; break; // TODO set UPPER_BOUND_ALG

                case "-l":
                case "-L":
                case "--lowerbound": LOWER_BOUND = true; break; // TODO set LOWER_BOUND_ALG

                case "-t":
                case "-T":
                case "--torsowidth": TORSO_WIDTH = true; break;

                default:
                    if (expectInputFile) {
                        expectInputFile = false;
                        INPUT_FILE = args[i];
                        break;
                    }
                    if (expectOutputFile) {
                        expectOutputFile = false;
                        OUTPUT_FILE = args[i];
                        break;

                    }
                    if (expectGraphType) {
                        expectGraphType = false;
                        throw new NotImplementedException();
                    }

                    error = true;
            }
        }

        // check that either treewidth upper- or lowerbound or torsowidth are computed
        if (!LOWER_BOUND & !UPPER_BOUND & !TORSO_WIDTH) {
            System.out.println("Either -u -l -t must be set!");
            System.out.println(helpMessage);
            System.exit(1);
            return;
        }

        if (error) {
            System.out.println(helpMessage);
            System.exit(1);
            return;
        }

        System.out.println("Input: " + INPUT_FILE);
    }

    private static void computeTreeWidth(NGraph<GraphInput.InputData> g) {

        // first calculate an upper bound
        GreedyFillIn<GraphInput.InputData> ubAlgo = new GreedyFillIn<GraphInput.InputData>();
        ubAlgo.setInput( g );
        ubAlgo.run();
        int upperbound = ubAlgo.getUpperBound();

        // then calculate the exact treewidth
        TreewidthDP<GraphInput.InputData> twdp = new TreewidthDP<GraphInput.InputData>( upperbound );
        twdp.setInput( g );
        twdp.run();
        int treewidth = twdp.getTreewidth();
    }

    private static void computeTreeDecomposition(NGraph<GraphInput.InputData> g) {
        MaximumMinimumDegreePlusLeastC<GraphInput.InputData> lbAlgo = new MaximumMinimumDegreePlusLeastC<GraphInput.InputData>();
        lbAlgo.setInput( g );
        lbAlgo.run();
        int lowerbound = lbAlgo.getLowerBound();

        GreedyFillIn<GraphInput.InputData> ubAlgo = new GreedyFillIn<GraphInput.InputData>();
        ubAlgo.setInput( g );
        ubAlgo.run();
        int upperbound = ubAlgo.getUpperBound();

        NVertexOrder<GraphInput.InputData> permutation = null;

        if( lowerbound == upperbound ) {
            permutation = ubAlgo.getPermutation();
        } else {
            QuickBB<GraphInput.InputData> qbbAlgo = new QuickBB<GraphInput.InputData>();
            qbbAlgo.setInput( g );
            qbbAlgo.run();
            permutation = qbbAlgo.getPermutation();
        }

        PermutationToTreeDecomposition<GraphInput.InputData> convertor = new PermutationToTreeDecomposition<GraphInput.InputData>( permutation );
        convertor.setInput( g );
        convertor.run();
        NGraph<NTDBag<GraphInput.InputData>> decomposition = convertor.getDecomposition();

        // needs GraphViz installed
        decomposition.printGraph( true, true );
    }
}
