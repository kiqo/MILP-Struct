package main.java;

import main.java.graph.Graph;
import main.java.libtw.TorsoWidth;
import main.java.lp.GraphData;
import main.java.lp.LPStatistics;
import main.java.lp.LinearProgram;
import nl.uu.cs.treewidth.algorithm.*;
import nl.uu.cs.treewidth.input.GraphInput;
import nl.uu.cs.treewidth.ngraph.NGraph;
import nl.uu.cs.treewidth.ngraph.NTDBag;
import nl.uu.cs.treewidth.ngraph.NVertex;
import nl.uu.cs.treewidth.ngraph.NVertexOrder;
import nl.uu.cs.treewidth.timing.Stopwatch;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import main.java.parser.GraphGenerator;
import main.java.parser.GraphTransformator;
import main.java.parser.MILPParser;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.lang.reflect.InvocationTargetException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Created by Verena on 28.02.2017.
 */
public class Main {

    private static final Logger LOGGER = LoggerFactory.getLogger(Main.class);

    private static Class<?> UPPER_BOUND_ALG = null;
    private static Class<?> LOWER_BOUND_ALG = null;
    private static String INPUT_FILE = null;
    private static String OUTPUT_FILE = "./output/results.txt";
    private static String GRAPH_TYPE = "primal";
    private static boolean TORSO_WIDTH = false;
    private static boolean LOWER_BOUND = false;
    private static boolean UPPER_BOUND = false;
    private static final Stopwatch t = new Stopwatch();
    private static final String LINE_SEPARATOR = System.lineSeparator();

    static{
        // system property current.date used for the name of the log file
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy-hh-mm-ss");
        System.setProperty("current.date", dateFormat.format(new Date()));
    }

    public static void main(String[] args) throws IOException {

        init();

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

        StringBuilder sb = new StringBuilder();

        // append header
        if (OUTPUT_FILE.endsWith(".csv")) {
            sb.append(LPStatistics.csvFormatHeader());
        } else {
            sb.append(LPStatistics.shortDescriptionHeader());
        }

        for (String fileName : files) {
            try {
                LOGGER.debug("Structural Parameters: " + fileName);
                computeStructuralParameters(fileName, sb);
                LOGGER.debug("-------------------");
            } catch (Exception e) {
                // catch any exception that occurs and outprint error
                LOGGER.error("Error for file " + fileName + ":", e);
            }
        }

        // print to output file statistics about lp and primal graph
        try{
            PrintWriter writer = new PrintWriter(OUTPUT_FILE, "UTF-8");
            writer.print(sb.toString());
            writer.close();
        } catch (IOException e) {
            LOGGER.error("", e);
        }
    }

    private static void init() {
        try {
            UPPER_BOUND_ALG = Class.forName("nl.uu.cs.treewidth.algorithm.GreedyDegree");
            LOWER_BOUND_ALG = Class.forName("nl.uu.cs.treewidth.algorithm.MaximumMinimumDegreePlusLeastC");
        } catch (ClassNotFoundException e) {
            LOGGER.error("", e);
        }
    }

    private static void computeStructuralParameters(String fileName, StringBuilder sb) throws IOException {

        // parse input file
        MILPParser milpParser = new MILPParser();
        LinearProgram lp = milpParser.parseMPS(fileName, false);

        // generate primal graph
        GraphGenerator graphGenerator = new GraphGenerator();
        Graph graph = null;
        if (GRAPH_TYPE.equals("primal")) {
            graph = graphGenerator.linearProgramToPrimalGraph(lp);
            lp.getStatistics().computePrimalGraphData(graph);
        } else if (GRAPH_TYPE.equals("incidence")){
            graph = graphGenerator.linearProgramToIncidenceGraph(lp);
            lp.getStatistics().computeIncidenceGraphData(graph);

        }

        // generate NGraph for using libtw
        NGraph<GraphInput.InputData> g;
        GraphTransformator graphTransformator = new GraphTransformator();
        g = graphTransformator.graphToNGraph(graph);

        // just gets stuck for large instances - TODO try on server
        // g.printGraph(true, true);

        if (LOWER_BOUND) {
            int treewidthLowerBound = computeTWLowerBound(g);
            lp.getStatistics().getPrimalGraphData().setTreewidthLB(treewidthLowerBound);
        }

        if (UPPER_BOUND) {
            int treewidthUpperBound = computeTWUpperBound(g);
            lp.getStatistics().getPrimalGraphData().setTreewidthUB(treewidthUpperBound);
        }

        if (TORSO_WIDTH && GRAPH_TYPE.equals("primal")) {
            computeTorsoWidth(g, lp);
        }

        // append statistics of current lp
        if (OUTPUT_FILE.endsWith(".csv")) {
            sb.append(lp.getStatistics().csvFormat());
        } else {
            sb.append(lp.getStatistics().shortDescription());
        }
    }

    private static UpperBound<GraphInput.InputData> createUpperBound() {
        UpperBound upperBoundAlg = null;
        try {
            upperBoundAlg = (UpperBound) UPPER_BOUND_ALG.getConstructor().newInstance();
        } catch (InstantiationException | NoSuchMethodException | InvocationTargetException | IllegalAccessException e) {
            LOGGER.error("", e);
        }
        return upperBoundAlg;
    }


    private static LowerBound<GraphInput.InputData> createLowerBound() {
        LowerBound<GraphInput.InputData> lowerBoundAlg = null;
        try {
            lowerBoundAlg = (LowerBound<GraphInput.InputData>) LOWER_BOUND_ALG.getConstructor().newInstance();
        } catch (InstantiationException | NoSuchMethodException | InvocationTargetException | IllegalAccessException e) {
            LOGGER.error(e.getMessage());
        }
        return lowerBoundAlg;
    }

    private static void computeTorsoWidth(NGraph<GraphInput.InputData> g, LinearProgram linearProgram) {
        t.reset();
        t.start();

        TorsoWidth<GraphInput.InputData> torsoWidthAlgo = new TorsoWidth<>(createUpperBound(), createLowerBound());
        torsoWidthAlgo.setInput(g);
        torsoWidthAlgo.run();
        int torsoWidthLowerBound = torsoWidthAlgo.getLowerBound();
        int torsoWidthUpperBound = torsoWidthAlgo.getUpperBound();
        t.stop();
        long millisecondsPassed = t.getTime();
        LOGGER.debug("LB TorsoWidth: " + torsoWidthLowerBound + " of " + g.getNumberOfVertices() + " nodes of " + torsoWidthAlgo.getName()
                + ", time: " + millisecondsPassed / 1000 + "s");
        LOGGER.debug("UB TorsoWidth: " + torsoWidthUpperBound + " of " + g.getNumberOfVertices() + " nodes of " + torsoWidthAlgo.getName()
                + ", time: " + millisecondsPassed / 1000 + "s");
        GraphData primalGraphData = linearProgram.getStatistics().getPrimalGraphData();
        primalGraphData.setTorsoWidthUB(torsoWidthUpperBound);
        primalGraphData.setTorsoWidthLB(torsoWidthLowerBound);

        // get statistics of torso graph
        int minDegree = Integer.MAX_VALUE;
        int maxDegree = Integer.MIN_VALUE;

        for (NVertex<GraphInput.InputData> vertex : g) {
            int degree = vertex.getNumberOfNeighbors();
            if (degree < minDegree) {
                minDegree = degree;
            }
            if (degree > maxDegree) {
                maxDegree = degree;
            }
        }
        primalGraphData.setTorsoMinDegree(minDegree);
        primalGraphData.setTorsoMaxDegree(maxDegree);
    }

    private static int computeTWUpperBound(NGraph<GraphInput.InputData> g) {
        t.reset();
        t.start();
        UpperBound<GraphInput.InputData> ubAlgo = createUpperBound();
        ubAlgo.setInput(g);
        ubAlgo.run();
        int upperbound = ubAlgo.getUpperBound();
        t.stop();
        long millisecondsPassed = t.getTime();
        LOGGER.debug("UB: " + upperbound + " of " + g.getNumberOfVertices() + " nodes " + " of " + ubAlgo.getName()
                + ", time: " + millisecondsPassed / 1000 + "s");
        return upperbound;
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
        LOGGER.debug("LB: " + lowerbound + " of " + g.getNumberOfVertices() + " nodes " + " of " + lbAlgo.getName()
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
                case "--upperbound": UPPER_BOUND = true; break;

                case "-l":
                case "-L":
                case "--lowerbound": LOWER_BOUND = true; break;

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
                        GRAPH_TYPE = args[i];
                        break;
                    }

                    error = true;
            }
        }

        // check that either treewidth upper- or lowerbound or torsowidth are computed
        if (!LOWER_BOUND & !UPPER_BOUND & !TORSO_WIDTH) {
            LOGGER.error("Either -u -l -t must be set!");
            LOGGER.error(helpMessage);
            System.exit(1);
            return;
        }

        if (GRAPH_TYPE.equals("p") || GRAPH_TYPE.equals("primal")) {
            GRAPH_TYPE = "primal";
        } else if (GRAPH_TYPE.equals("i") || GRAPH_TYPE.equals("incidence")){
            GRAPH_TYPE = "incidence";
        } else {
            LOGGER.error("Error: Graph type that should be computed is not recognized!");
            error = true;
        }

        if (TORSO_WIDTH && GRAPH_TYPE.equals("incidence")) {
            LOGGER.warn("Warning: Ignoring option to compute incidence graph and compute primal " +
                    "graph instead (torso width can only be computed for primal graphs");
            GRAPH_TYPE = "primal";
        }

        if (error) {
            LOGGER.error(helpMessage);
            System.exit(1);
            return;
        }

        LOGGER.debug("Input: " + INPUT_FILE);
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
