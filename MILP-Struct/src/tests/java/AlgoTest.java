package tests.java;

/**
 * Specifies certain graphs that need to be tested on by an algorithm
 */
public interface AlgoTest {

    void testNodeBlockerGraph() throws InterruptedException;
    void testStarShapedGraph() throws InterruptedException;
    void testDisconnectedGraph() throws InterruptedException;
    void testRandomGraph() throws InterruptedException;
}
