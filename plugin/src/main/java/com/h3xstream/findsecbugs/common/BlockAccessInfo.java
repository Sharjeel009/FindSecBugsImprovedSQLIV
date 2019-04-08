package com.h3xstream.findsecbugs.common;

/**
 * Created by OP on 5/30/2018.
 */
public class BlockAccessInfo
{
    int BlockID = -1;
    int AccessInfo = -1;

    public BlockAccessInfo(int blockID, int accessInfo) {
        BlockID = blockID;
        AccessInfo = accessInfo;
    }

    public int getBlockID() {
        return BlockID;
    }

    public void setBlockID(int blockID) {
        BlockID = blockID;
    }

    public int getAccessInfo() {
        return AccessInfo;
    }

    public void setAccessInfo(int accessInfo) {
        AccessInfo = accessInfo;
    }

    @Override
    public String toString()
    {
        String ret_val = "BlockID : " + BlockID + " :: Access : ";

        if(AccessInfo == -1)
        {
            ret_val += "NORMAL";
        }

        else if(AccessInfo == 1)
        {
            ret_val += "ALWAYS_ACCESS";
        }

        else if(AccessInfo == 0)
        {
            ret_val += "NEVER_ACCESS";
        }

        return ret_val;
    }
}
