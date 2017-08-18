package main.java.parser;

import main.java.graph.Graph;
import main.java.lp.LinearProgram;


/**
 * Created by Verena on 09.03.2017.
 */
public abstract class GraphGenerator {

    public abstract Graph linearProgramToGraph(LinearProgram lp) throws InterruptedException;

    void checkInterrupted() throws InterruptedException {
        if (Thread.currentThread().isInterrupted()) {
            throw new InterruptedException();
        }
    }
}
