package com.h3xstream.findsecbugs.common;

import java.util.ArrayList;
import java.util.List;

public class BDDNode {


    List<Boolean> oBits = new ArrayList<>();
    List<BDDNode> children = new ArrayList<>();
    List<ValueChangeLog> oListValueChangeLog = new ArrayList<>();

    public BDDNode(BDDNode oNodeBDD) {
        for(BDDNode child : oNodeBDD.getChildren())
        {
            children.add(child);  // can call copy constructor here too
        }

        for(ValueChangeLog vcl : oNodeBDD.getoListValueChangeLog())
        {
            oListValueChangeLog.add(new ValueChangeLog(vcl.getVar_index(), vcl.getTaint_value()));
        }
    }

    public BDDNode() {

    }

    public boolean addChild(BDDNode oNode)
    {
       boolean ret_val = true;

       try
       {
            children.add(oNode);
       }
       catch(Exception ex)
       {
           ex.printStackTrace();
           ret_val = false;
       }


       return ret_val;
    }

    public boolean addValueChangeLog(ValueChangeLog vcl)
    {
        boolean ret_val = false;

        try
        {
            oListValueChangeLog.add(vcl);
        }
        catch(Exception ex)
        {
            System.out.println("Exception : BDDNode -- addBlockAccessInfo");
        }

        return ret_val;
    }

    public List<Boolean> getoBits() {
        return oBits;
    }

    public void setoBits(List<Boolean> oBits) {
        this.oBits = oBits;
    }

    public List<BDDNode> getChildren() {
        return children;
    }

    public void setChildren(List<BDDNode> children) {
        this.children = children;
    }

    public List<ValueChangeLog> getoListValueChangeLog() {
        return oListValueChangeLog;
    }

    public void setoListValueChangeLog(List<ValueChangeLog> oListValueChangeLog) {
        this.oListValueChangeLog = oListValueChangeLog;
    }

    @Override
    public String toString()
    {
        String str_val = "";

        str_val += "======================---NODE---=====================" + "\n\r";

        str_val += "Number of children : " + children.size() + "\n\r";
        for(ValueChangeLog vcl : oListValueChangeLog) {
            str_val += "INDEX : " + vcl.getVar_index() + " VALUE : " + vcl.getTaint_value();
        }

        str_val += "====================================================" + "\n\r";


        return str_val;
    }
}
