package com.h3xstream.findsecbugs.common;

import org.apache.bcel.generic.Instruction;

import java.util.ArrayList;
import java.util.List;

public class DFGNode {

    int BlockID = -1;
    List<Def> oListDef = new ArrayList<>();
    List<Uses> oListUses = new ArrayList<>();
    boolean isConditional = false;
    boolean evaluatesTo = false;
    boolean isEvaluated = false;
    List<Instruction> oListInstructions = new ArrayList<>();
    List<DFGNode> children = new ArrayList<>();
    List<DFGEdge> outgoingEdges = new ArrayList<>();

    public DFGNode(int blockID) {
        BlockID = blockID;
    }

    public int getBlockID() {
        return BlockID;
    }

    public void setBlockID(int blockID) {
        BlockID = blockID;
    }

    public boolean addDef(Def oDef)
    {
        boolean ret_val = true;

        try
        {
            oListDef.add(oDef);
        }
        catch(Exception ex)
        {

        }

        return ret_val;
    }

    public boolean addUses(Uses oUses)
    {
        boolean ret_val = true;

        try
        {
            oListUses.add(oUses);
        }
        catch(Exception ex)
        {

        }

        return ret_val;
    }

    public boolean addChild(DFGNode newChild)
    {
        boolean ret_val = true;
        try
        {
            children.add(newChild);
            outgoingEdges.add(new DFGEdge(this, newChild));
        }
        catch(Exception ex)
        {
            ex.printStackTrace();
        }
        return ret_val;
    }
    public List<Uses> getoListUses() {
        return oListUses;
    }

    public void setoListUses(List<Uses> oListUses) {
        this.oListUses = oListUses;
    }

    public boolean isConditional() {
        return isConditional;
    }

    public void setConditional(boolean conditional) {
        isConditional = conditional;
    }

    public boolean isEvaluatesTo() {
        return evaluatesTo;
    }

    public void setEvaluatesTo(boolean evaluatesTo) {
        this.evaluatesTo = evaluatesTo;
    }

    public boolean isEvaluated() {
        return isEvaluated;
    }

    public void setEvaluated(boolean evaluated) {
        isEvaluated = evaluated;
    }

    public List<Instruction> getoListInstructions() {
        return oListInstructions;
    }

    public void setoListInstructions(List<Instruction> oListInstructions) {
        this.oListInstructions = oListInstructions;
    }

    public List<DFGNode> getChildren() {
        return children;
    }

    public void setChildren(List<DFGNode> children) {
        this.children = children;
    }

    public void addInstruction(Instruction ins) {
        oListInstructions.add(ins);
    }

    @Override
    public String toString()
    {
        String ret_str = "";

        ret_str += "================================= NODE =================================\n\r";
        ret_str += "================================= Block : " + getBlockID() + "=================================\n\r";
        ret_str += "===== Instructions =====\n\r";
        List<Instruction> instructions = this.getoListInstructions();
        for (int i = 0; i < instructions.size(); i++)
        {
            Instruction oins = instructions.get(i);
            ret_str += oins.toString() + "\n\r";
        }

        ret_str += "===== end Instructions ====\n\r\n\r";
        ret_str += "===== Def =====\n\r";
        if(this.oListDef.size() > 0) {
            List<Def> defs = this.oListDef;
            for(int i = 0; i < defs.size(); i++)
            {
                Def def1 = defs.get(i);
                ret_str += def1.toString()+ "\n\r";
            }
        }
        else
            ret_str += "no definition" + "\n\r";
        ret_str += "===== End Def =====\n\r\n\r";

        ret_str += "===== Uses =====\n\r";

        List<Uses> uses = this.getoListUses();
        for(int i = 0; i < uses.size(); i++)
        {
            Uses uses1 = uses.get(i);
            ret_str += uses1.toString()+ "\n\r";
        }

        ret_str += "===== End Uses =====\n\r\n\r";


        ret_str += "===== Num Children =====\n\r";
        int numChildren = this.getChildren().size();

        ret_str += "" + numChildren + "\n\r";

        ret_str += "===== End Num Children =====\n\r\n\r";
        ret_str += "================================= END NODE =================================\n\r";

        return ret_str;
    }
}
