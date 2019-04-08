package com.h3xstream.findsecbugs.common;

import com.h3xstream.findsecbugs.common.matcher.TaintTypes;
import org.apache.bcel.generic.Instruction;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by OP on 5/27/2018.
 */
public class TaintNode
{
    int BlockID = -1;
    int ParentID = -1;

    boolean isConditional = false;
    boolean evaluatesTo = false;
    boolean isEvaluated = false;

    List<Instruction> oListInstructions = new ArrayList<>();
    List<TaintEdges> outgoingEdges = new ArrayList<>();
    List<TaintNode> children = new ArrayList<>();
    List<VarInfo> oListTaintVarList = new ArrayList<>();

    List<IntPair> oListDefs = new ArrayList();

    List<ValueChangeLog> oListValueChangeLog = new ArrayList<>();
    private List<VarInfo> nodeVarList = new ArrayList<>();
    private List<String> functionCalls = new ArrayList<String>();

    public TaintNode(int blockID) {
        BlockID = blockID;
    }

    public int getBlockID() {
        return BlockID;
    }

    public void setBlockID(int blockID) {
        BlockID = blockID;
    }

    public List<VarInfo> getoListTaintVarList() {
        return oListTaintVarList;
    }

    public void setoListTaintVarList(List<VarInfo> oListTaintVarList) {
        this.oListTaintVarList = oListTaintVarList;
    }


    public void addFunctionCall(String strFunction)
    {
        functionCalls.add(strFunction);
    }

    public boolean addChild(TaintNode newChild)
    {
        boolean ret_val = true;
        try
        {
            //newChild.setParentID(this.getBlockID());    //todo: maybe cause error sometime later
            children.add(newChild);
            outgoingEdges.add(new TaintEdges(this, newChild));
        }
        catch(Exception ex)
        {
            ex.printStackTrace();
        }
        return ret_val;
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

    public List<TaintNode> getChildren() {
        return children;
    }

    public void setChildren(List<TaintNode> children) {
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
        ret_str += "================================= ParentID : " + getParentID() + "=================================\n\r";
        ret_str += "===== Instructions =====\n\r";
        List<Instruction> instructions = this.getoListInstructions();
        for (int i = 0; i < instructions.size(); i++)
        {
            Instruction oins = instructions.get(i);
            ret_str += oins.toString() + "\n\r";
        }

        ret_str += "===== end Instructions ====\n\r\n\r";

        ret_str += "===== Variable Taints ====\n\r\n\r";

        for(int i = 0; i < oListTaintVarList.size(); i++)
        {
            VarInfo oInfo = oListTaintVarList.get(i);
            ret_str += oInfo.toString();
        }

        ret_str += "===== End Variable Taints ====\n\r\n\r";


        ret_str += "===== Num Children =====\n\r";
        int numChildren = this.getChildren().size();

        ret_str += "" + numChildren + "\n\r";

        ret_str += "===== End Num Children =====\n\r\n\r";

        ret_str += "===== Function Calls =====\n\r\n\r";

        for(int u = 0; u < functionCalls.size(); u++)
        {
            ret_str += "" + functionCalls.get(u) + "\n\r";
        }

        ret_str += "===== End Function Calls =====\n\r\n\r";

        ret_str += "===== Changed Values ====\n\r\n\r";




        for(ValueChangeLog oLog : oListValueChangeLog)
        {
            ret_str += "VAR_INDEX : " + oLog.getVar_index() + " :: Taint Val : " + TaintTypes.typeToString(oLog.getTaint_value()) + "\n\r";
        }

        ret_str += "===== End Changed Values =====\n\r\n\r";
        ret_str += "================================= END NODE =================================\n\r";

        return ret_str;
    }

    public List<TaintEdges> getOutgoingEdges() {
        return outgoingEdges;
    }

    public void setOutgoingEdges(List<TaintEdges> outgoingEdges) {
        this.outgoingEdges = outgoingEdges;
    }

    public void setNodeVarList(List<VarInfo> nodeVarList)
    {
        if(nodeVarList != null) {
            for (VarInfo var_info : nodeVarList) {
                this.nodeVarList.add(new VarInfo(var_info));
            }
        }
    }

    public List<VarInfo> getNodeVarList() {
        return nodeVarList;
    }

    public void addValueChangeLog(ValueChangeLog valueChangeLog) {
        oListValueChangeLog.add(valueChangeLog);
    }

    public List<ValueChangeLog> getoListValueChangeLog() {
        return oListValueChangeLog;
    }

    public void setoListValueChangeLog(List<ValueChangeLog> oListValueChangeLog) {
        this.oListValueChangeLog = oListValueChangeLog;
    }

    public int getParentID()
    {
        return ParentID;
    }

    public void setParentID(int parentID)
    {
        ParentID = parentID;
    }

    public String toShortString()
    {
        String str = "";

        str = "Block ID : " + getBlockID() + " :: Children : ";

        for(TaintNode tn : getChildren())
        {
            str += tn.getBlockID() + ", ";
        }

        str += "\n\r";

        return str;
    }

    public void addDef(int ind, int intValue)
    {
        oListDefs.add(new IntPair(ind, intValue));
    }

    public List<IntPair> getoListDefs() {
        return oListDefs;
    }

    public void setoListDefs(List<IntPair> oListDefs) {
        this.oListDefs = oListDefs;
    }

    public List<String> getFunctionCalls() {
        return functionCalls;
    }

    public void setFunctionCalls(List<String> functionCalls) {
        this.functionCalls = functionCalls;
    }

    public void setValueChangeLog(ValueChangeLog valueChangeLog)
    {
        System.out.println("setValueChangeLog2");

        System.out.println("2nd check Length of the value change log is  :  " + oListValueChangeLog.size());
        for(int i = 0; i < oListValueChangeLog.size(); i++)
        {
            ValueChangeLog varInfo = oListValueChangeLog.get(i);
            System.out.println("varInfo.getIndex : " + varInfo.getVar_index() + " :: valueChangeLog.getVar_index : " + valueChangeLog.getVar_index());
            if(varInfo.getVar_index() == valueChangeLog.getVar_index())
            {
                int previous_taint_value = varInfo.getTaint_value();
                int new_taint_value = valueChangeLog.getTaint_value();

                if(checkTaintValue(previous_taint_value, new_taint_value) == 2) {

                    varInfo.setTaint_value(valueChangeLog.getTaint_value());
                    System.out.println("ValueChangeLog is set for variable  : " + valueChangeLog.getVar_index() + " :: is : " + valueChangeLog.getTaint_value());
                    break;
                }
            }
        }
    }

    public int checkTaintValue(int firstValue, int SecondValue)
    {
        int ret_val = 0;
        // 0 means both are equal 1 means first is 2 means 2nd is better
        if(firstValue != SecondValue)
        {
            if(firstValue == TaintTypes.TAINTED || firstValue == TaintTypes.SINK)
                ret_val = 1;
            else if(SecondValue == TaintTypes.TAINTED || SecondValue == TaintTypes.SINK)
                ret_val = 2;
            else if(firstValue == TaintTypes.UN_TAINTED) //cant combined with first see the else the
                ret_val = 1;
            else if(SecondValue == TaintTypes.UN_TAINTED)
                ret_val = 2;
            else
                ret_val = 1;

        }
        return ret_val;
    }

    // just to test
    public void setValueChangeLog2(ValueChangeLog valueChangeLog)
    {
        System.out.println("setValueChangeLog2");

        System.out.println("2nd check Length of the value change log is  :  " + oListValueChangeLog.size());
        for(int i = 0; i < oListValueChangeLog.size(); i++)
        {
            ValueChangeLog varInfo = oListValueChangeLog.get(i);
            System.out.println("varInfo.getIndex : " + varInfo.getVar_index() + " :: valueChangeLog.getVar_index : " + valueChangeLog.getVar_index());
            if(varInfo.getVar_index() == valueChangeLog.getVar_index())
            {
                varInfo.setTaint_value(valueChangeLog.getTaint_value());
                System.out.println("ValueChangeLog is set for variable  : " + valueChangeLog.getVar_index() + " :: is : " + valueChangeLog.getTaint_value());
                break;
            }
        }
    }
}
