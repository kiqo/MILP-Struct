import graph.Graph;
import libtw.TorsoWidth;
import lp.LinearProgram;
import nl.uu.cs.treewidth.algorithm.*;
import nl.uu.cs.treewidth.input.GraphInput;
import nl.uu.cs.treewidth.ngraph.NGraph;
import nl.uu.cs.treewidth.ngraph.NTDBag;
import nl.uu.cs.treewidth.ngraph.NVertexOrder;
import nl.uu.cs.treewidth.timing.Stopwatch;
import parser.GraphGenerator;
import parser.GraphTransformator;
import parser.MILPParser;
import sun.reflect.generics.reflectiveObjects.NotImplementedException;

import java.io.File;
import java.io.IOException;

/**
 * Created by Verena on 28.02.2017.
 */
public class Main {

    private static String INPUT_FILE = null;
    private static String OUTPUT_FILE = null;

    public static void main(String[] args) throws IOException {

        parseArguments(args);

        // TODO : input file is a directory
        File file = new File(INPUT_FILE);
        boolean fileExists = file.exists();
        boolean isDirectory = file.isDirectory();

        if (fileExists && isDirectory) {

        }

        // parse input file
        MILPParser milpParser = new MILPParser();
        LinearProgram lp = milpParser.parseMPS(INPUT_FILE, false);

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

        Stopwatch t = new Stopwatch();
        t.reset();
        t.start();
        MaximumMinimumDegreePlusLeastC<GraphInput.InputData> lbAlgo = new MaximumMinimumDegreePlusLeastC<GraphInput.InputData>();
        lbAlgo.setInput(g);
        lbAlgo.run();
        int lowerbound = lbAlgo.getLowerBound();
        t.stop();
        long millisecondsPassed = t.getTime();
        System.out.println("LB: " + lowerbound + " of " + graphSize + " nodes " + " of " + lbAlgo.getName()
                + ", time: " + millisecondsPassed / 1000 + "s");

        t.reset();
        t.start();
        GreedyDegree<GraphInput.InputData> ubAlgo = new GreedyDegree<>();
        ubAlgo.setInput(g);
        ubAlgo.run();
        int upperbound = ubAlgo.getUpperBound();
        t.stop();
        millisecondsPassed = t.getTime();
        System.out.println("UB: " + upperbound + " of " + graphSize + " nodes " + " of " + ubAlgo.getName()
                + ", time: " + millisecondsPassed / 1000 + "s");


        t.reset();
        t.start();
        GreedyDegree<GraphInput.InputData> ubAlgo2 = new GreedyDegree<>();
        TorsoWidth<GraphInput.InputData> torsoWidthAlgo = new TorsoWidth<>(ubAlgo2);
        torsoWidthAlgo.setInput(g);
        torsoWidthAlgo.run();
        int torsoWidthUpperBound = torsoWidthAlgo.getUpperBound();
        t.stop();
        millisecondsPassed = t.getTime();
        System.out.println("UB TorsoWidth: " + torsoWidthUpperBound + " of " + g.getNumberOfVertices() + " nodes of " + torsoWidthAlgo.getName()
                + ", time: " + millisecondsPassed / 1000 + "s");
    }

    private static void parseArguments(String[] args) {
        int argc = args.length;
        boolean error = false;
        String errorMessage = "Usage: TreeTorsoWidth -i inputFile|inputDirectory [-o outputFile] [-g primal|incidence|dual]";

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

        if (error) {
            System.out.println(errorMessage);
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
