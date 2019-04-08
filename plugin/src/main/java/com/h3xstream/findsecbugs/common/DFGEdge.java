package com.h3xstream.findsecbugs.common;

/**
 * Created by OP on 5/20/2018.
 */
public class DFGEdge {

    DFGNode startNode = null;
    DFGNode EndNode = null;

    public DFGNode getStartNode() {
        return startNode;
    }

    public void setStartNode(DFGNode startNode) {
        this.startNode = startNode;
    }

    public DFGNode getEndNode() {
        return EndNode;
    }

    public void setEndNode(DFGNode endNode) {
        EndNode = endNode;
    }

    public DFGEdge(DFGNode startNode, DFGNode endNode) {
        this.startNode = startNode;
        EndNode = endNode;
    }
}
