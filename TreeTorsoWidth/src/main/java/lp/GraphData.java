package main.java.lp;



/**
 * Created by Verena on 19.04.2017.
 */
public class GraphData {
    int numNodes;
    int numIntegerNodes;
    double proportionIntegerNodes;
    int numEdges;
    double density;
    int minDegree;
    int maxDegree;
    double avgDegree;
    private int treewidthUB;
    private int treewidthLB;
    private int torsoWidthUB;
    private int torsoWidthLB;
    private int treeDepthUB;


    public int getTreeDepthUB() {
        return treeDepthUB;
    }

    public void setTreeDepthUB(int treeDepthUB) {
        this.treeDepthUB = treeDepthUB;
    }

    public int getTreewidthUB() {
        return treewidthUB;
    }

    public void setTreewidthUB(int treewidthUB) {
        this.treewidthUB = treewidthUB;
    }

    public int getTreewidthLB() {
        return treewidthLB;
    }

    public void setTreewidthLB(int treewidthLB) {
        this.treewidthLB = treewidthLB;
    }

    public int getTorsoWidthUB() {
        return torsoWidthUB;
    }

    public void setTorsoWidthUB(int torsoWidthUB) {
        this.torsoWidthUB = torsoWidthUB;
    }

    public int getTorsoWidthLB() {
        return torsoWidthLB;
    }

    public void setTorsoWidthLB(int torsoWidthLB) {
        this.torsoWidthLB = torsoWidthLB;
    }
}
