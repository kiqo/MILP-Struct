package libtw;

import nl.uu.cs.treewidth.input.DgfReader;
import nl.uu.cs.treewidth.input.InputException;
import nl.uu.cs.treewidth.ngraph.ListGraph;
import nl.uu.cs.treewidth.ngraph.ListVertex;
import nl.uu.cs.treewidth.ngraph.NGraph;
import nl.uu.cs.treewidth.ngraph.NVertex;
import nl.uu.cs.treewidth.output.Output;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.LineNumberReader;
import java.util.Hashtable;

/**
 * Created by Verena on 09.04.2017.
 */
public class LPDgfReader extends DgfReader {
    public LPDgfReader(String filename) {
        super(filename);
    }

    public NGraph<InputData> get() throws InputException {

        NGraph<InputData> g = new ListGraph<InputData>();

        // Get a LineNumberReader, either from filename or from reader.
        LineNumberReader in;
        if( filename!=null ) {
            // Open the file; throw InputException if not worky
            FileReader fr;
            try {
                fr = new FileReader(filename);
            } catch (FileNotFoundException e) { throw new InputException(e); }
            in = new LineNumberReader( fr );
        } else {
            // Use the supplied Reader
            assert reader != null;
            in = new LineNumberReader( reader );
        }

        // Read the dgf format and remember:
        //  - number of vertices
        //  - integer pairs for where the edges are
        //  - vertex weights (not used)
        //  - parameter used for determining if node is integer
        int numVertices = 0;
        Hashtable<String, NVertex<InputData>> vertices = new Hashtable<String, NVertex<InputData>>();

        boolean directGraph = false;
        boolean loop = false;

        NVertex<InputData> vertexPrototype = new ListVertex<InputData>();

        String line;
        try {
            // read the entire file line by line.
            while( (line=in.readLine()) !=null ) {

                // tokenize the line
                String[] tokens = line.split("\\s");
                assert tokens.length > 0;

                // switch on the first token
                String command = tokens[0];
                if( command.equals("") ) {
                    // empty line
                    continue;

                } else if( command.equals("c") ) {
                    // (snip "c " off the front)
                    if(line.length()>1)
                        g.addComment( line.substring(2) );
                } else if( command.equals("p") ) {
                    // problem specification
                    if( tokens.length>=2 ) {
                        numVertices = Integer.parseInt( tokens[2] ); //-> number of vertices specified in the file
                    } else {
                        System.out.println( "Faulty problem specification at line " + in.getLineNumber() + ": " + line );
                    }

                } else if( command.equals("n") ) {
                    // node command of form 'n name value'
                    String name;
                    //double value; //-> The value is never used. If you want to use it, add it to the vertex here
                    name = tokens[1];
                    //value = Double.parseDouble(tokens[2]);

                    if( !vertices.containsKey(name) ) {
                        //If there vertex isn't created yet, create it:
                        NVertex<InputData> v = vertexPrototype.newOfSameType( new LPInputData(vertices.size(),name,false) );
                        vertices.put(name, v);
                        g.addVertex( v );
                    } else {
                        //Node is already in list, so do nothing.
                        //You would want to set the weight here, if you are using it.
                    }
                } else if( command.equals("e") ) {
                    // edge command, remember it in `edges'
                    if( tokens.length>=2 ) {
                        String nameA, nameB;
                        nameA = tokens[1];
                        nameB = tokens[2];

                        NVertex<InputData> v1, v2;

                        //Check if vertices_id's are already in vertex list. If not, add them.
                        boolean newNode = false;
                        //The first vertex
                        if( !vertices.containsKey(nameA) ) {
                            v1 = vertexPrototype.newOfSameType( new LPInputData(vertices.size(),nameA,false) );
                            vertices.put(v1.data.name, v1);
                            g.addVertex( v1 );
                            newNode = true;
                        } else {
                            v1 = vertices.get(nameA);
                        }

                        //The second vertex
                        if( !vertices.containsKey(nameB) ) {
                            v2 = vertexPrototype.newOfSameType( new LPInputData(vertices.size(),nameB,false) );
                            vertices.put(v2.data.name, v2);
                            g.addVertex( v2 );
                            newNode = true;
                        } else {
                            v2 = vertices.get(nameB);
                        }
                        boolean edgeExists = false;
                        if(!newNode) edgeExists = v1.isNeighbor(v2);
                        if(v1 == v2)
                            loop = true;
                        else if(!edgeExists)
                            g.addEdge(v1, v2);
                        else
                            directGraph = true;
                    } else {
                        System.out.println( "Faulty edge command at line " + in.getLineNumber() + ": " + line );
                    }

                } else if( command.equals("x") ) {
                    // Parameter Descriptors
                    // Form: "x PARAM VALUE" where PARAM is a node and VALUE is 0 or 1 for integer node
                    String param;
                    boolean value;
                    param = tokens[1];
                    //value = Double.parseDouble(tokens[2]);

                    if( !vertices.containsKey(param) ) {
                        //If there vertex isn't created yet, create it:
                        NVertex<InputData> v = vertexPrototype.newOfSameType( new LPInputData(vertices.size(),param, Boolean.valueOf(tokens[2])));
                        vertices.put(param, v);
                        g.addVertex( v );
                    } else {
                        value = Integer.valueOf(tokens[2]) > 0;
                        ((LPInputData) vertices.get(param).data).setInteger(value);
                    }

                } else if( command.equals("d") ) {
                    // Geometric Descriptors
                    // Form: "d DIM METRIC"
                    // Not used here

                } else if( command.equals("v") ) {
                    // Lines starting with v are a vertex embedding descriptor line
                    // Form: "v X1 X2 X3 ... XD"
                    // Note: these lines must appear after the d descriptor
                    // Not used here

                } else {
                    System.out.println( "Unknown command '" + command + "' at line " + in.getLineNumber() );

                }
            }
        } catch (IOException e) {
            throw new InputException(e);
        }

        if( directGraph )
            warning("You have loaded a  multigraph. Duplicate edges have been removed!");
        if( loop )
            warning("You have loaded a  multigraph. Loops have been removed!");


        int edgelessVertices = numVertices- vertices.size();
        if(edgelessVertices > 0) {
            warning("There are "+edgelessVertices+" vertices which are not connected to other vertices");
        }
        while(numVertices > vertices.size()) {
            NVertex<InputData> v = vertexPrototype.newOfSameType( new InputData( vertices.size(), "New_Vertex_"+vertices.size()) );
            vertices.put(v.data.name, v);
            g.addVertex( v );

        }

        confirmProperIDs( g );

        try {
            in.close();
        } catch (IOException e) {}

        // Done.
        return g;
    }

    private void confirmProperIDs( NGraph<InputData> g ) {
        boolean bugged = false;

        // see if the IDs are 0..size-1
        int size = g.getNumberOfVertices();
        boolean[] idUsed = new boolean[ size ];
        for( NVertex<InputData> v : g ) {
            int id = v.data.id;
            if( id<0 || id>=size ) bugged = true;
            else idUsed[id] = true;
        }
        for( boolean b : idUsed ) if( !b ) bugged = true;

        if( bugged ) {
            String source = filename==null? "[a stream]" : filename;
            Output.bugreport( "The IDs that DgfReader generates for a graph should be 0..size-1, but they were not when loading '"+source+"'" );
        }
    }

    private void warning( String warning) {
        return;
        //System.out.println("\r\n***** Warning *****");
        //System.out.println(warning);
        //System.out.println("*******************\r\n");
    }
}

