package com.h3xstream.findsecbugs.common;

/**
 * Created by OP on 5/27/2018.
 */
public class TaintEdges {

    private TaintNode startNode = null;
    private TaintNode EndNode = null;

    public TaintEdges(TaintNode startNode, TaintNode endNode)
    {
        this.startNode = startNode;
        EndNode = endNode;
    }

    public TaintNode getStartNode()
    {
        return startNode;
    }

    public void setStartNode(TaintNode startNode) {
        this.startNode = startNode;
    }

    public TaintNode getEndNode() {
        return EndNode;
    }

    public void setEndNode(TaintNode endNode) {
        EndNode = endNode;
    }


}
