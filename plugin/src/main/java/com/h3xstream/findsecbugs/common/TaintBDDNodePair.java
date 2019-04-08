package com.h3xstream.findsecbugs.common;

public class TaintBDDNodePair {

    BDDNode oBDDNode = null;
    TaintNode oTaintNode = null;

    public TaintBDDNodePair(TaintNode oTaintNode, BDDNode oBDDNode) {
        this.oBDDNode = oBDDNode;
        this.oTaintNode = oTaintNode;
    }

    public BDDNode getoBDDNode() {
        return oBDDNode;
    }

    public void setoBDDNode(BDDNode oBDDNode) {
        this.oBDDNode = oBDDNode;
    }

    public TaintNode getoTaintNode() {
        return oTaintNode;
    }

    public void setoTaintNode(TaintNode oTaintNode) {
        this.oTaintNode = oTaintNode;
    }
}
