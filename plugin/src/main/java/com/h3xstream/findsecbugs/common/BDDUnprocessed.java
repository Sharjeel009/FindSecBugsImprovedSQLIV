package com.h3xstream.findsecbugs.common;

public class BDDUnprocessed {

    int BlockID;
    int parent_id;
    BDDNode oNodeBDD;

    public BDDUnprocessed(int _BlockID, int _parent_id, BDDNode _oNodeBDD)
    {
        this.BlockID = _BlockID;
        this.parent_id = _parent_id;
        this.oNodeBDD = _oNodeBDD;
    }

    public int getBlockID() {
        return BlockID;
    }

    public void setBlockID(int blockID) {
        BlockID = blockID;
    }

    public int getParent_id() {
        return parent_id;
    }

    public void setParent_id(int parent_id) {
        this.parent_id = parent_id;
    }

    public BDDNode getoNodeBDD() {
        return oNodeBDD;
    }

    public void setoNodeBDD(BDDNode oNodeBDD) {
        this.oNodeBDD = oNodeBDD;
    }
}
