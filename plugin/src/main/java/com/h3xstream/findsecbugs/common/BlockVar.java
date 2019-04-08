package com.h3xstream.findsecbugs.common;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by OP on 5/30/2018.
 */
public class BlockVar
{
    private int BlockID;
    private List<IntInfo> int_list = new ArrayList<>();

    public BlockVar(int blockID, List<IntInfo> _int_list) {
        BlockID = blockID;

        for(IntInfo int_info : _int_list)
        {
            int_list.add(new IntInfo(int_info.getIndex(), int_info.getValue())); // otherwise the
        }

        //this.int_list.addAll(int_list); // just to not modify it later on
    }

    public int getBlockID() {
        return BlockID;
    }

    public void setBlockID(int blockID) {
        BlockID = blockID;
    }

    public List<IntInfo> getInt_list() {
        return int_list;
    }

    public void setInt_list(List<IntInfo> int_list) {
        this.int_list = int_list;
    }

    @Override
    public String toString()
    {
        String ret_val = "";

        try
        {
            ret_val += "=================== BlockID : " + getBlockID() + "\n\r";
            for(int i = 0; i < int_list.size(); i++)
            {
                IntInfo intInfo = int_list.get(i);
                ret_val += "INDEX : " + intInfo.getIndex() + " :: " + " VALUE : " + intInfo.getValue() + "\n\r";
            }
            ret_val += "===================================================== " + "\n\r";
        }
        catch(Exception ex)
        {

        }

        return ret_val;
    }
}
