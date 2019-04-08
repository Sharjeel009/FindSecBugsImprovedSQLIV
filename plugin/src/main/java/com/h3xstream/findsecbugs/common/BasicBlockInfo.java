package com.h3xstream.findsecbugs.common;

import edu.umd.cs.findbugs.ba.BasicBlock;

public class BasicBlockInfo {

    BasicBlock oBasicBlock = null;
    int ParentID = -1;
    boolean hasChilrenProcessed;

    public BasicBlockInfo(BasicBlock oBasicBlock, int parentID, boolean _hasChilrenProcessed) {
        this.oBasicBlock = oBasicBlock;
        ParentID = parentID;
        hasChilrenProcessed = _hasChilrenProcessed;
    }

    public BasicBlock getoBasicBlock() {
        return oBasicBlock;
    }

    public void setoBasicBlock(BasicBlock oBasicBlock) {
        this.oBasicBlock = oBasicBlock;
    }

    public int getParentID() {
        return ParentID;
    }

    public void setParentID(int parentID) {
        ParentID = parentID;
    }

    public boolean isHasChilrenProcessed() {
        return hasChilrenProcessed;
    }

    public void setHasChilrenProcessed(boolean hasChilrenProcessed) {
        this.hasChilrenProcessed = hasChilrenProcessed;
    }
}
