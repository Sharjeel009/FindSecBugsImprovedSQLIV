package com.h3xstream.findsecbugs.injection.sql;

import com.h3xstream.findsecbugs.common.*;
import com.h3xstream.findsecbugs.injection.InjectionPoint;
import edu.umd.cs.findbugs.ba.BasicBlock;
import edu.umd.cs.findbugs.ba.CFG;
import edu.umd.cs.findbugs.ba.ClassContext;
import edu.umd.cs.findbugs.ba.Edge;
import edu.umd.cs.findbugs.util.ClassName;

import org.apache.bcel.classfile.Method;
import org.apache.bcel.generic.*;

import java.io.PrintWriter;
import java.util.*;
import java.util.concurrent.ArrayBlockingQueue;

/**
 * Created by OP on 5/20/2018.
 */
public class DFGFactory {

    ClassContext classContext;
    String str_out_dfg = "";
    String str_instructions = "";

    private List<IntInfo> int_list = new ArrayList<>();
    private String str_invoke_instructions;
    private ConstantPoolGen cpg;

    public List<BlockVar> oListBlockVars = new ArrayList<>();

    public DFGFactory(ClassContext _classContext, ConstantPoolGen _cpg)
    {
        this.classContext = _classContext;
        this.cpg = _cpg;
    }


    public DFG createDFG(Method method)
    {
        DFG oDFG = new DFG();

        try
        {
            int_list = new ArrayList<>();
            CFG cfg = classContext.getCFG(method);

            BasicBlock currBlock2 = cfg.getEntry();
            List<BasicBlock> oList = new ArrayList<>();
            oList.add(currBlock2);
            Stack<Instruction> oInstructionStack = new Stack<>();
            Stack<Instruction> oConstantInstructionStack = new Stack<>();
            Number val = null;
            Queue<TypeIndexPair> oInstructionQueue = new ArrayBlockingQueue<>(100);

            List<Integer> ProcessedBlocks = new ArrayList<Integer>();
            int ParentID = -1;

            boolean root_added = false;

            while (oList.size() > 0) {
                currBlock2 = oList.get(0);
                if(!root_added)
                {
                    root_added = true;
                    DFGNode oRoot = new DFGNode(currBlock2.getId());
                    oDFG.addNode(oRoot, -1);
                }

                ParentID = currBlock2.getId();

                if (cfg.getNumOutgoingEdges(currBlock2) > 0)
                {

                    Iterator<Edge> iei = cfg.outgoingEdgeIterator(currBlock2);

                    while (iei.hasNext())
                    {

                        Edge next = iei.next();
                        BasicBlock currBlock2x = next.getTarget();
                        if (!ProcessedBlocks.contains(currBlock2x.getId())) {
                            DFGNode currNode = new DFGNode(currBlock2x.getId());

                            BasicBlock.InstructionIterator jfk = currBlock2x.instructionIterator();

                            while (jfk.hasNext())
                            {
                                InstructionHandle handle = jfk.next();
                                Instruction ins = handle.getInstruction();
                                currNode.addInstruction(ins);

                                if(ins instanceof InvokeInstruction)
                                {
                                    str_invoke_instructions += ins.toString() + "\n\r";
                                    InvokeInstruction ii = (InvokeInstruction)ins;
                                    str_invoke_instructions += "Full Method Name : " + getFullMethodName(ii, cpg) + "\n\r";
                                }

                                str_instructions += ins.toString() + "\n\r";

                                if (isIntegerInstruction(ins) || isIntegerUsingInstruction(ins)) {

                                    if (isIntegerUsingInstruction(ins)) {
                                        Queue<TypeIndexPair> oTempQueue = new ArrayBlockingQueue<TypeIndexPair>(100);
                                        oTempQueue.addAll(oInstructionQueue);

                                        while (oTempQueue.size() > 0) {
                                            TypeIndexPair oPair = oTempQueue.remove();

                                            Uses oUses = new Uses(oPair.getVarType(), oPair.getVarIndex());
                                            currNode.addUses(oUses);
                                        }
                                    }

                                    if (ins instanceof ILOAD)
                                    {
                                        ILOAD inv = (ILOAD) ins;
                                        int ind = inv.getIndex();

                                        oInstructionQueue.add(new TypeIndexPair(ind));

                                    }
                                    else if (ins instanceof SIPUSH || ins instanceof BIPUSH)
                                    {
                                        oConstantInstructionStack.push(ins);
                                    }
                                    else if (isArithmeticIntegerInstruction(ins))
                                    {
                                        Queue<TypeIndexPair> oTempQueue = new ArrayBlockingQueue<TypeIndexPair>(100);
                                        oTempQueue.addAll(oInstructionQueue);
                                        Number num1 = null;
                                        Number num2 = null;

                                        if (oTempQueue.size() >= 2)
                                        {
                                            TypeIndexPair oPair1 = oTempQueue.remove();
                                            TypeIndexPair oPair2 = oTempQueue.remove();

                                            num1 = get_int_val(oPair1.getVarIndex());
                                            num2 = get_int_val(oPair2.getVarIndex());
                                        }
                                        else if (oTempQueue.size() >= 1)
                                        {
                                            TypeIndexPair oPair1 = oTempQueue.remove();
                                        }
                                        if (num1 != null && num2 == null && oConstantInstructionStack.size() > 0)
                                        {
                                            num2 = getValByConstantInstruction(oConstantInstructionStack.pop());
                                        }

                                        str_out_dfg += "num1 : " + num1 + "num2 : " + num2 + "\n\r";

                                        if (num1 != null && num2 != null)
                                        {
                                            if (ins instanceof ISUB)
                                                val = num1.intValue() - num2.intValue();
                                            else if (ins instanceof IADD)
                                                val = num1.intValue() + num2.intValue();
                                            else if (ins instanceof IDIV) {
                                                if (num2.intValue() != 0)
                                                    val = num1.intValue() / num2.intValue();
                                            } else if (ins instanceof IMUL)
                                                val = num1.intValue() * num2.intValue();
                                        }
                                    }
                                    else
                                    {
                                        if (ins instanceof ISTORE)
                                        {
                                            if (val == null && oConstantInstructionStack.size() > 0)
                                            {
                                                Instruction ins2 = oConstantInstructionStack.pop();
                                                if (ins2 != null)
                                                    val = getValByConstantInstruction(ins2);
                                            }

                                            ISTORE inv = (ISTORE) ins;
                                            int ind = inv.getIndex();

                                            boolean contains_var = false;

                                            for (int ii = 0; ii < int_list.size(); ii++)
                                            {
                                                IntInfo currIntInfo = int_list.get(ii);
                                                if (currIntInfo.getIndex() == ind)
                                                {

                                                    contains_var = true;

                                                    currIntInfo.setValue(val);
                                                    int_list.set(ii, currIntInfo);

                                                }
                                            }
                                            if (!contains_var)
                                            {
                                                int_list.add(new IntInfo(ind, val));
                                            }

                                            currNode.addDef(new Def(ind, val));
                                            val = null;
                                        }
                                    }
                                }
                            }


                            oList.add(currBlock2x);
                            ProcessedBlocks.add(currBlock2x.getId());

                            BlockVar oBlockVar = new BlockVar(currBlock2x.getId(), int_list);
                            oListBlockVars.add(oBlockVar);

                            oDFG.addNode(currNode, ParentID);

                            oInstructionQueue.clear();
                            oInstructionStack.clear();
                            oConstantInstructionStack.clear();
                        }

                        else
                        {
                            oDFG.addParentChildRelation(currBlock2.getId(), currBlock2x.getId());
                        }
                    }
                }

                oList.remove(currBlock2);
            }

            PrintWriter out5 = new PrintWriter("D:\\temp_output\\invoke_instructions.txt");
            out5.println(str_invoke_instructions);
            out5.close();
        }
        catch(Exception ex)
        {

        }
        return oDFG;
    }

    private boolean isIntegerUsingInstruction(Instruction ins) {
        if (ins instanceof InvokeInstruction || ins instanceof  ISTORE)
            return true;
        return false;
    }


    private boolean isIntegerInstruction(Instruction ins) {

        if(ins instanceof ILOAD || ins instanceof ISTORE || ins instanceof SIPUSH || ins instanceof BIPUSH
                || ins instanceof IF_ICMPEQ || ins instanceof IF_ICMPGE || ins instanceof IF_ICMPGT
                || ins instanceof IF_ICMPLE || ins instanceof IF_ICMPLT || ins instanceof IF_ICMPNE
                || isArithmeticIntegerInstruction(ins))
            return true;

        return false;



    }

    private boolean isArithmeticIntegerInstruction(Instruction ins) {   // todo : a lot to add
        return ins instanceof ISUB || ins instanceof IADD || ins instanceof IDIV || ins instanceof IMUL;
    }

    private Number getValByConstantInstruction(Instruction ins) {

        ConstantPushInstruction inv = (ConstantPushInstruction) ins;

        InjectionPoint p;
        Number val = inv.getValue();

        return val;

    }

    private boolean is_instruction_can_usein_if(Instruction instruction)
    {
        boolean ret_val = false;

        if(instruction instanceof ILOAD || instruction instanceof ISUB || instruction instanceof SIPUSH)
        {
            ret_val = true;
        }

        return ret_val;
    }

    private boolean is_if_instruction(Instruction instruction)
    {
        boolean ret_val = false;

        if(instruction instanceof IfInstruction)
        {
            ret_val = true;
        }

        return ret_val;
    }

    private Number get_int_val(int temp_ind)
    {
        Number num = null;

        for(int o = 0; o < int_list.size(); o++) {

            int ind = int_list.get(o).getIndex();
            num = int_list.get(o).getValue();

            if(int_list.get(o).getIndex() == temp_ind)
            {
                num = int_list.get(o).getValue();
                break;
            }

        }

        return num;
    }

    private String getFullMethodName(InvokeInstruction invoke, ConstantPoolGen cpg) {
        return ClassName.toSlashedClassName(invoke.getReferenceType(cpg).toString())
                + "." + invoke.getMethodName(cpg) + invoke.getSignature(cpg);
    }

    public List<BlockVar> getoListBlockVars() {
        return oListBlockVars;
    }

    public void setoListBlockVars(List<BlockVar> oListBlockVars) {
        this.oListBlockVars = oListBlockVars;
    }
}
