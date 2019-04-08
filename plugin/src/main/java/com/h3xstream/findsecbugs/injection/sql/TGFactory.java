///// def doesnt contain def

package com.h3xstream.findsecbugs.injection.sql;

import com.h3xstream.findsecbugs.common.*;
import com.h3xstream.findsecbugs.common.matcher.TaintTypes;
import edu.umd.cs.findbugs.ba.*;
import edu.umd.cs.findbugs.util.ClassName;

import javafx.scene.Parent;
import org.apache.bcel.classfile.*;
import org.apache.bcel.generic.*;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import java.io.FileNotFoundException;
import java.util.*;

/**
 * Created by OP on 5/27/2018.
 */
public class TGFactory {

    boolean constant_int_exist = false;
    ClassContext classContext;
    private ConstantPoolGen cpg;
    List<BlockVar> oListBlockVars = new ArrayList<>();
    Number val = 0;
    List<BlockAccessInfo> oListBlockAccessInfo = new ArrayList<>();

    List<String> oListSinkInstructions = new ArrayList<>();
    List<String> oListIgnoreMethods = new ArrayList<>();

    HashMap<Integer, Boolean> hashDoneChildren = new HashMap<>();
    List<VarInfo> oVarList;
    private LineNumberTable lineNumberTable;
    private LocalVariableTable localVarTable;

    private boolean isReturningClass = false;

    public TGFactory(ClassContext _classContext, ConstantPoolGen _cpg, List<BlockVar> oListBlockVars, boolean isReturningClass) {

        this.isReturningClass = isReturningClass;

        oListSinkInstructions.add("org/springframework/jdbc/core/JdbcTemplate.batchUpdate([Ljava/lang/String;)[I");
        oListSinkInstructions.add("java/sql/Statement.executeQuery(Ljava/lang/String;)Ljava/sql/ResultSet;");
        oListSinkInstructions.add("java/sql/Statement.execute(Ljava/lang/String;)Z");
        oListSinkInstructions.add("java/sql/Statement.execute(Ljava/lang/String;[I)Z");
        oListSinkInstructions.add("java/sql/Statement.executeUpdate(Ljava/lang/String;[Ljava/lang/String;)I");
        oListSinkInstructions.add("java/sql/Statement.executeUpdate(Ljava/lang/String;)I");
        oListSinkInstructions.add("java/sql/Statement.execute(Ljava/lang/String;[Ljava/lang/String;)Z");
        oListSinkInstructions.add("java/sql/CallableStatement.executeQuery()Ljava/sql/ResultSet;");
        oListSinkInstructions.add("java/sql/PreparedStatement.execute()Z");
        oListSinkInstructions.add("java/sql/CallableStatement.executeQuery()Ljava/sql/ResultSet;");
        oListSinkInstructions.add("java/sql/Statement.executeQuery(Ljava/lang/String;)Ljava/sql/ResultSet;");
        oListSinkInstructions.add("java/sql/Statement.execute(Ljava/lang/String;I)Z");
        oListSinkInstructions.add("java/sql/Statement.executeBatch()[I");

        // third party functions
        oListSinkInstructions.add("org/springframework/jdbc/core/JdbcTemplate.queryForLong(Ljava/lang/String;)J");
        oListSinkInstructions.add("org/springframework/jdbc/core/JdbcTemplate.queryForRowSet(Ljava/lang/String;)Lorg/springframework/jdbc/support/rowset/SqlRowSet;");
        oListSinkInstructions.add("org/springframework/jdbc/core/JdbcTemplate.execute(Ljava/lang/String;)V");
        oListSinkInstructions.add("org/springframework/jdbc/core/JdbcTemplate.query(Ljava/lang/String;Lorg/springframework/jdbc/core/RowMapper;)Ljava/util/List;");
        oListSinkInstructions.add("org/springframework/jdbc/core/JdbcTemplate.queryForMap(Ljava/lang/String;)Ljava/util/Map;");
        oListSinkInstructions.add("org/springframework/jdbc/core/JdbcTemplate.queryForInt(Ljava/lang/String;)I");
        oListSinkInstructions.add("org/springframework/jdbc/core/JdbcTemplate.queryForObject(Ljava/lang/String;[Ljava/lang/Object;Ljava/lang/Class;)Ljava/lang/Object;");
        oListSinkInstructions.add("org/springframework/jdbc/core/JdbcTemplate.queryForList(Ljava/lang/String;)Ljava/util/List;");

        oListIgnoreMethods.add("java/lang/StringBuilder.append(Ljava/lang/String;)Ljava/lang/StringBuilder;");
        oListIgnoreMethods.add("java/lang/StringBuilder.<init>()V");
        oListIgnoreMethods.add("java/lang/StringBuilder.toString()Ljava/lang/String;");
        oListIgnoreMethods.add("java/net/URLDecoder.decode(Ljava/lang/String;Ljava/lang/String;)Ljava/lang/String;");
        oListIgnoreMethods.add("javax/servlet/http/Cookie.getValue()Ljava/lang/String;");

        this.classContext = _classContext;
        this.cpg = _cpg;
        this.oListBlockVars.clear();
        this.oListBlockVars.addAll(oListBlockVars);
    }

    List<BasicBlockInfo> oListProcessBB = new ArrayList<>();

    public TaintGraph createTaintGraph(Method method, List<VarInfo> oVarList, LineNumberTable lineNumberTable, LocalVariableTable localVariableOrg, boolean twoVarsSameNameExist)
    {
        this.lineNumberTable = lineNumberTable;
        TaintGraph oGraph = new TaintGraph();
        this.oVarList = oVarList;
        this.localVarTable = localVariableOrg;

        // var with block_id etc
        Map<Integer, List<VarInfo>> oVarInfoMap = new HashMap<>();
        Map<Integer, List<VarInfo>> oTempVarInfoMap = new HashMap<>();


        for(int y = 0; y < oVarList.size(); y++)
        {
            VarInfo varInfo = oVarList.get(y);
            varInfo.setTaintOne(true);
        }

        int RootId = -1;

        try {
            // todo .. nullify class variables

            CFG cfg = classContext.getCFG(method);

            //-- just for testing purpose .. see which statements are missed :)
            BasicBlock currBlock2 = cfg.getEntry();
            List<BasicBlock> oList = new ArrayList<>();
            oList.add(currBlock2);
            String str_model2 = "";
            Stack<Integer> oTaintStack = new Stack<>();
            Number val = null;

            BasicBlock previousBlock = null;
            boolean BBProcessing = false;
            List<Integer> ProcessedBlocks = new ArrayList<Integer>();
            int ParentID = -1;
            List<VarInfo> oNodeVarList = new ArrayList<>();
            List<VarInfo> oTempNodeVarList = new ArrayList<>();

            Map<Integer, List<LoadedVar>> oLoadedVarsByBlock = new HashMap<>();
            Map<Integer, Stack<Integer>> oLoadedStackByBlock = new HashMap<>();

            List<LoadedVar> oLoadedVars = new ArrayList<>();
            Map<Integer, Integer> oMapLoadedInt = new HashMap<>();
            List<VarAttr> oListTempAttr = new ArrayList<>();

            for (VarInfo varInfo : oVarList) {
                oNodeVarList.add(varInfo);
            }

            for (VarInfo var_info : oVarList) {
                boolean already_exist = false;
                // have to be carefull has to clear it out later on
                for(VarInfo nodeVarList : oNodeVarList) {
                    if(nodeVarList.getIndex() == var_info.getIndex())
                    {
                        already_exist = true;
                        break;
                    }
                }
                if(!already_exist)
                    oNodeVarList.add(new VarInfo(var_info));
            }

            boolean root_added = false;

            // initialize the list according to bst

            // already adding it separately
            //oListProcessBB.add(new BasicBlockInfo(currBlock2, -1));
            initListProcessBB(cfg, currBlock2);

            while (oListProcessBB.size() > 0) {

                boolean isVarsCleared = false;
                boolean is_boi_set = false;

                //oNodeVarList.addAll(oVarList);
                // add according to index
                /*
                for (VarInfo var_info : oVarList) {
                    boolean already_exist = false;
                    // have to be carefull has to clear it out later on
                    for(VarInfo nodeVarList : oNodeVarList) {
                        if(nodeVarList.getIndex() == var_info.getIndex())
                        {
                            already_exist = true;
                            break;
                        }
                    }
                    if(!already_exist)
                        oNodeVarList.add(new VarInfo(var_info));
                }
                */


                BasicBlockInfo bbi = oListProcessBB.get(0);
                currBlock2 = oListProcessBB.get(0).getoBasicBlock();
                ParentID = oListProcessBB.get(0).getParentID();

                // always after the parentID is set
                if (oLoadedVarsByBlock.containsKey(ParentID)) {
                    List<LoadedVar> oListParentLoadedVars = oLoadedVarsByBlock.get(ParentID);
                    List<LoadedVar> oTempList = new ArrayList<>();
                    for (int i = 0; i < oListParentLoadedVars.size(); i++) {
                        LoadedVar integer = oListParentLoadedVars.get(i);
                        oTempList.add(integer);
                    }
                    oLoadedVars = oTempList;    // change reference type should not effect it
                }
                else
                {
                    System.out.println("No parent ID found to load variable from");
                    oLoadedVars = new ArrayList<>();
                }

                if(oLoadedStackByBlock.containsKey(ParentID))
                {
                    Stack<Integer> oParentLoadedStack = oLoadedStackByBlock.get(ParentID);
                    Stack<Integer> oTempTaintStack = new Stack<Integer>();

                    for(int i = 0; i < oParentLoadedStack.size(); i++)
                    {
                        oTempTaintStack.push( oParentLoadedStack.pop());
                    }

                    for(int i = 0; i < oTempTaintStack.size(); i++)
                    {
                        oTaintStack.push(oTempTaintStack.pop());
                    }
                }

                // only needed if temp are used in 2 places
                /*
                if(oTempVarInfoMap.containsKey(ParentID))
                {
                    List<LoadedVar> oListParentTempLoadedVars = oTempVarInfoMap.get(ParentID);

                }*/


                System.out.println("---==== LOADED VAR");
                for(int y = 0; y < oLoadedVars.size(); y++)
                {
                    System.out.println(oLoadedVars.get(y).getIntValue());
                }
                System.out.println("---==== END LOADED VAR");

                if (oVarInfoMap.containsKey(ParentID)) {
                    List<VarInfo> varInfos = oVarInfoMap.get(ParentID);
                    List<VarInfo> tempVarInfo = new ArrayList<>();
                    // copy in temp list so that reference error dont occur;
                    for (VarInfo vi : varInfos) {
                        // first make new one to avoid reference errors
                        VarInfo vi_new = new VarInfo(vi);
                    }

                }


                boolean ru_addNewNode = true;

                System.out.println("======================= BlockID : " + currBlock2.getId() + " : ParentID : " + ParentID + "===========================");

                if (!root_added) {
                    root_added = true;
                    TaintNode oRoot = new TaintNode(currBlock2.getId());

                    // can also use define node
                    oRoot = addTaintedData(oRoot, oVarList);
                    for (VarInfo var_info : oVarList) {
                        oRoot.addValueChangeLog(new ValueChangeLog(var_info.getIndex(), var_info.getTaintType()));
                    }

                    // todo : add here the tainted values of the parameters
                    // add parent before
                    oRoot.setParentID(-1);
                    oGraph.addNode(oRoot, -1);
                    oListBlockAccessInfo.add(new BlockAccessInfo(currBlock2.getId(), -1));

                    RootId = currBlock2.getId();

                }

                int currBlockID = currBlock2.getId();
                List<BlockAccessInfo> blockAccessInfos = getoListBlockAccessInfo();

                boolean ru_processBlock = true;

                for (BlockAccessInfo bai : blockAccessInfos) {
                    if (bai.getBlockID() == currBlockID) {
                        int accessInfo = bai.getAccessInfo();
                        if (accessInfo == 0) {
                            ru_addNewNode = false;
                        }
                    }
                }


                if (ru_processBlock) {
                    //ParentID = currBlock2.getId();


                    TaintNode currNode = new TaintNode(currBlock2.getId());


                    //EdgeTypes.RETURN_EDGE

                    //EdgeTypes.FALL_THROUGH_EDGE

                    // get is ifcmp_edge is the reason
                    BasicBlock.InstructionIterator jfk = currBlock2.instructionIterator();
                    ////System.out.println("-=-=-=-=-=-=-=-=-=--=BLOCK START : " + currBlock2.getId() + "-=-=-=-=-=-=-=-=-=-=-");
                    //boolean constant_int_exist = false;

                    while (jfk.hasNext()) {
                        InstructionHandle handle = jfk.next();

                        //handle.getPosition(); // save this position if the instruction call
                        Instruction ins = handle.getInstruction();
                        int ins_length = ins.getLength();
                        int handle_position = handle.getPosition();
                        int PC = handle_position + ins_length;

                        currNode.addInstruction(ins);

                        System.out.println("--== ins : " + ins);

                        if (ins instanceof InvokeInstruction) {
                            InvokeInstruction ii = (InvokeInstruction) ins;
                            if (getFullMethodName(ii, cpg).contains("<init>")) {
                                System.out.println("INIT INSTRUCTION : " + getFullMethodName(ii, cpg));
                                continue;
                            } else {
                                System.out.println("FULL METHOD NAME : " + getFullMethodName(ii, cpg));
                            }

                            currNode.addFunctionCall(getFullMethodName(ii, cpg));
                        }

                        System.out.println("ins : " + ins.toString());

                        if (ins instanceof IfInstruction) {


                                int result = evaluateIfInstruction(currBlock2);

                                if (result != -1) {
                                    oLoadedVars.clear();
                                    isVarsCleared = true;
                                    oTaintStack.clear();
                                }

                                if (result == -1) {
                                    System.out.println("Can't evaluate the if condition");
                                } else if (result == 1) {
                                    System.out.println("The if condition will always be true");
                                } else if (result == 0) {
                                    System.out.println("The if condition will always be false");
                                }

                                if (result != -1) {

                                    if (result == 1) // if always access block // tbc
                                    {
                                        Iterator<Edge> iei2 = cfg.outgoingEdgeIterator(currBlock2);
                                        while (iei2.hasNext()) {
                                            Edge e = iei2.next();

                                            System.out.println("ID -- : " + e.getTarget().getId() + " :: EdgeType : " + e.getType());


                                            if (e.getType() == EdgeTypes.FALL_THROUGH_EDGE) {
                                                BasicBlock thenBB = e.getTarget();
                                                oListBlockAccessInfo.add(new BlockAccessInfo(thenBB.getId(), result));
                                            } else {
                                                BasicBlock bb_Target = e.getTarget();

                                                int tempParentId = currBlock2.getId();
                                                int tempChildId = bb_Target.getId();

                                                System.out.println("$$ == tempParentId : " + tempParentId);

                                                System.out.println("$$ == tempChildId : " + tempChildId);

                                                // first remove the thing
                                                for (int l = 0; l < oListProcessBB.size(); l++) {
                                                    BasicBlockInfo ru_bbi = oListProcessBB.get(l);
                                                    BasicBlock ru_currBlock2 = oListProcessBB.get(l).getoBasicBlock();
                                                    int ru_ParentID = oListProcessBB.get(l).getParentID();
                                                    int ru_childID = ru_currBlock2.getId();

                                                    if (ru_ParentID == tempParentId && ru_childID == tempChildId) {
                                                        System.out.println("$$ == Child removed ParentID : " + ru_ParentID + " :: ChildID : " + ru_childID);
                                                        //oListProcessBB.remove(l);
                                                        oListBlockAccessInfo.add(new BlockAccessInfo(ru_childID, 0));
                                                    }
                                                }

                                                oGraph.removeChildFromParent(tempParentId, tempChildId);


                                                System.out.println("OTHER EDGES : " + e.getType());
                                            }
                                        }
                                    } else if (result == 0) {
                                        //ru_addNewNode = false; // cant make this block unaccessible

                                        System.out.println("-========================================================================================================================================================================================================================================");

                                        System.out.println("-========================================================================================================================================================================================================================================");

                                        System.out.println("-========================================================================================================================================================================================================================================");

                                        System.out.println("-========================================================================================================================================================================================================================================");

                                        System.out.println("-========================================================================================================================================================================================================================================");

                                        System.out.println("-========================================================================================================================================================================================================================================");


                                        Iterator<Edge> iei2 = cfg.outgoingEdgeIterator(currBlock2);
                                        while (iei2.hasNext()) {
                                            Edge e = iei2.next();

                                            System.out.println("ID -- : " + e.getTarget().getId() + " :: EdgeType : " + e.getType());

                                            if (e.getType() != EdgeTypes.FALL_THROUGH_EDGE) {


                                                BasicBlock bb_Target = e.getTarget();

                                                int tempParentId = currBlock2.getId();
                                                int tempChildId = bb_Target.getId();

                                                System.out.println("$$ == tempParentId : " + tempParentId);

                                                System.out.println("$$ == tempChildId : " + tempChildId);

                                                // first remove the thing
                                                for (int l = 0; l < oListProcessBB.size(); l++) {
                                                    BasicBlockInfo ru_bbi = oListProcessBB.get(l);
                                                    BasicBlock ru_currBlock2 = oListProcessBB.get(l).getoBasicBlock();
                                                    int ru_ParentID = oListProcessBB.get(l).getParentID();
                                                    int ru_childID = ru_currBlock2.getId();

                                                    if (ru_ParentID == tempParentId && ru_childID == tempChildId) {
                                                        System.out.println("$$ == Child removed ParentID : " + ru_ParentID + " :: ChildID : " + ru_childID);
                                                        //oListProcessBB.remove(l);
                                                        oListBlockAccessInfo.add(new BlockAccessInfo(ru_childID, 0));
                                                    }
                                                }

                                                oGraph.removeChildFromParent(tempParentId, tempChildId);


                                                System.out.println("OTHER EDGES : " + e.getType());

                                                BasicBlock thenBB = e.getTarget();
                                                oListBlockAccessInfo.add(new BlockAccessInfo(thenBB.getId(), result));

                                            } else {
                                                BasicBlock elseBB = e.getTarget();
                                                // elseBB can be accessible or not
                                                oListBlockAccessInfo.add(new BlockAccessInfo(elseBB.getId(), 1));
                                                System.out.println("OTHER EDGES : " + e.getType());
                                            }
                                        }

                                    }


                                }

                                oLoadedVars.clear();
                                oTaintStack.clear();
                                isVarsCleared = true;
                        }

                        boolean modelII = true;

                        if (ins instanceof InvokeInstruction) {


                            // if it stays -1 means there is an error
                            int objectIndex = -1;
                            InvokeInstruction invoke_ins = (InvokeInstruction) ins;
                            String fullMethodName = getFullMethodName(invoke_ins, cpg);

                            if (fullMethodName.contains("doSomething"))  //tbc : get new improved
                            {

                            } else {
                                // by default it will return a tainted value if a tainted variable is used
                                if (!oListIgnoreMethods.contains(fullMethodName)) {
                                    boolean isTainted = false;

                                    System.out.println("-------------===== Total Taint oLoadedVars Size : " + oLoadedVars.size());

                                    while (oTaintStack.size() > 0) {
                                        Integer oInt = oTaintStack.pop();
                                        if (oInt.intValue() == TaintTypes.TAINTED) {
                                            isTainted = true;
                                        }
                                        // if is taint kill
                                    }


                                    String str_method = getFullMethodName(invoke_ins, cpg);

                                    System.out.println("----=== str_method : " + str_method);

                                    if (isTaintSinkInstruction(ins)) {
                                        System.out.println("--== Taint Sink instruction found");

                                        for (LoadedVar temp_ind : oLoadedVars) {
                                            int intValue = temp_ind.getIntValue();
                                            System.out.println("###The type of the variable found is : " + intValue);
                                            // means that there is
                                            if (intValue != -1 && intValue <= oVarList.size() - 1) {
                                                int varListIndex = getVarListIndex(oVarList, intValue);
                                                VarInfo varInfo = oVarList.get(varListIndex);

                                                System.out.println("##The type of variable found is : " + varInfo.getType());

                                                if (varInfo.getType().equals("java.sql.CallableStatement") || varInfo.getType().equals("java.sql.PreparedStatement")) {
                                                    System.out.println("java.sql.CallableStatement was loaded trying to get the index");
                                                    // str will get the index of the variable to sink
                                                    String str = varInfo.getAttrByName("query");
                                                    if (!str.equals("")) {
                                                        int var_index = Integer.parseInt(str);

                                                        currNode.addValueChangeLog(new ValueChangeLog(var_index, 100));
                                                        System.out.println("===== The variable at the index " + var_index + " :: has SINK");
                                                    } else {
                                                        System.out.println("Error in TGFactory  : the callable statements have no argument");
                                                    }
                                                } else if (varInfo.getType().equals("java.lang.String") || oVarList.get(varListIndex).getType().equals("java.lang.String[]") || oVarList.get(varListIndex).getType().equals("java.util.List")) {
                                                    int index = varInfo.getIndex();
                                                    currNode.addValueChangeLog(new ValueChangeLog(index, 100));
                                                    System.out.println("===== The variable at the index " + index + " :: has SINK");
                                                }
                                            }

                                    /*
                                    // in some cases like constant string
                                    if(temp_ind.getIntValue() != -1) {
                                        currNode.addValueChangeLog(new ValueChangeLog(temp_ind.getIntValue(), 100));
                                        System.out.println("===== The variable at the index " + temp_ind.getIntValue() + " :: has SINK");
                                    }
                                    */
                                        }
                                    } else if (isTaintIntroducingInvokeInstruction(ins)) // like system.in
                                    {
                                        oTaintStack.add(TaintTypes.TAINTED);
                                    } else if (isTaintKillingTaintInvokeInstruction(ins)) {
                                        oTaintStack.add(TaintTypes.UN_TAINTED);
                                    } else if (isTainted) {
                                        System.out.println("---=== Is tainted is added to the stuff");
                                        oTaintStack.add(TaintTypes.TAINTED);
                                    } else {
                                        oTaintStack.add(TaintTypes.UNKNOWN);
                                    }
                                }
                            }


                            // means that a variable mathod is called : virtual is used for maps : Invoke instance method; dispatch based on class
                            if (modelII && (ins instanceof INVOKEINTERFACE || ins instanceof INVOKEVIRTUAL) && !oListIgnoreMethods.contains(fullMethodName)) {
                                boolean is_clear_var = true;
                                boolean isClearVarsAllCost = true;

                                String returnType = "";

                                if (ins instanceof INVOKEINTERFACE) {
                                    INVOKEINTERFACE ins_ii = (INVOKEINTERFACE) ins;
                                    returnType = ins_ii.getReturnType(cpg).toString();
                                } else if (ins instanceof INVOKEVIRTUAL) {
                                    INVOKEVIRTUAL ins_iv = (INVOKEVIRTUAL) ins;
                                    returnType = ins_iv.getReturnType(cpg).toString();

                                    String fullMethodName2 = getFullMethodName(invoke_ins, cpg);
                                    if(fullMethodName2.equals("javax/servlet/http/Cookie.getValue()Ljava/lang/String;"))
                                    {
                                        isClearVarsAllCost = false;
                                    }
                                }



                                //int index = ins_ii.getIndex();
                                //index = getModifiedVarIndex(index, PC);

                                for (LoadedVar f : oLoadedVars) {
                                    int intValue = f.getIntValue();
                                    if (intValue != -1 && intValue < oVarList.size() - 1) {
                                        VarInfo varInfo = oVarList.get(intValue);

                                        System.out.println("LOADED VAR ID : " + varInfo.getIndex());
                                        System.out.println("LOADED VAR TYPE : " + varInfo.getType());
                                    }
                                }


                            /*
                            VarInfo loadedVar = oVarList.get(index);
                            System.out.println("-- THE TYPE : " + loadedVar.getType());
                            */


                                if (oLoadedVars.size() > 0) {
                                    LoadedVar loadedVar = oLoadedVars.get(0);
                                    oLoadedVars.remove(0);  // no further use of the object
                                    if (loadedVar.getType() == LoadedVar.LoadedVarType.VARIABLE) {
                                        int value = loadedVar.getIntValue();
                                        if (value != -1) {
                                            objectIndex = value;
                                        } else {
                                            System.out.println("Error in TGFactory : Loaded variable is not ");
                                        }
                                    } else {
                                        System.out.println("Error in TGFactory : Loaded variable at index 0 is not a variable");
                                    }

                                } else {
                                    System.out.println("Error in TGFactory : INVOKEINTERFACE called but no variable loaded found");
                                }

                                System.out.println("--== Clearing the variables ==--");

                                if(isClearVarsAllCost) {
                                    // only clear because no need to save taint
                                    isVarsCleared = true;

                                    oTaintStack.clear();
                                }

                                System.out.println("--== Clearing the variables ==--");

                                // only clear because no need to save taint
                                isVarsCleared = true;

                                oTaintStack.clear();

                                System.out.println("After clearing the variable size : " + oLoadedVars.size());


                                if (objectIndex != -1) {
                                    // now we can say that the method is called for the variable at the index  objectIndex
                                    VarInfo varInfo = oVarList.get(objectIndex);
                                    String name = varInfo.getName();
                                    String type = varInfo.getType();

                                    boolean already_handled = false;

                                    System.out.println("Method of a variable by the index : " + objectIndex + " is called");
                                    System.out.println("Method of a variable by the name : " + name + " is called");
                                    System.out.println("Method of a variable by the type : " + varInfo.getType() + " is called");

                                    ////System.out.println("THE TYPE IS : " + type);
                                    ////System.out.println("THE SIZE IS : " + oLoadedVars.size());
                                    if (type.equals("java.sql.Connection")) //("java.sql.CallableStatement")) //"java/sql/Connection.prepareCall(Ljava/lang/String;)Ljava/sql/CallableStatement;"))
                                    {
                                        already_handled = true;
                                        if (oLoadedVars.size() > 0) {
                                            LoadedVar loadedVar2 = oLoadedVars.get(0);
                                            int intValue = loadedVar2.getIntValue();// means that the value must
                                            ////System.out.println("THE INT VALUE IS : " + intValue);
                                            if (intValue != -1) {
                                                // get the object whose function is called
                                                //String returnType = ins_ii.getReturnType(cpg).toString();
                                                String attrName = "query";
                                                String attrVal = "" + intValue;

                                                System.out.println("--== Storing the attribute name query");

                                                oListTempAttr.add(new VarAttr(attrName, attrVal, returnType));
                                            } else {
                                                System.out.println("Error in TGFactory, the index is -1");
                                            }
                                        } else {
                                            System.out.println("Error in TGFactory : No variable to set as prepare call");
                                        }
                                    }
                                    //  must be enclosed in type
                                    else if (fullMethodName.equals("java/util/HashMap.put(Ljava/lang/Object;Ljava/lang/Object;)Ljava/lang/Object;")) {
                                        is_clear_var = true;
                                        already_handled = true;
                                        String temp_key = "";
                                        List<Integer> taint_type = new ArrayList<>();
                                        // now load the variables
                                        for (int f = 0; f < oLoadedVars.size(); f++) {
                                            LoadedVar currLoadedVar = oLoadedVars.get(f);
                                            // first one is index and second one is
                                            if (f == 0) {
                                                if (currLoadedVar.getType() == LoadedVar.LoadedVarType.CONSTANT_STRING) {
                                                    temp_key = currLoadedVar.getValue();
                                                } else {
                                                    System.out.println("Error in TGFactory : Error handling Maps");
                                                }
                                            } else if (f == 1) {
                                                if (currLoadedVar.getType() == LoadedVar.LoadedVarType.CONSTANT_STRING) {
                                                    taint_type.add(TaintTypes.UN_TAINTED);
                                                } else {
                                                    int var_index = currLoadedVar.getIntValue();
                                                    if (var_index > 0)
                                                    {
                                                        taint_type = getTaintValueOfVarAtIndex(oNodeVarList, oTempNodeVarList, var_index, false);
                                                    }
                                                }

                                                break; // nothing needed further
                                            }
                                        }   // end of for loop


                                        varInfo.putMapValue(temp_key, taint_type);
                                        oVarList.set(objectIndex, varInfo); // re-save just to check if thats the problem
                                    }

                                    else if (fullMethodName.equals("java/util/HashMap.get(Ljava/lang/Object;)Ljava/lang/Object;") || fullMethodName.equals("java/util/Map.get(Ljava/lang/Object;)Ljava/lang/Object;"))
                                    {


                                        System.out.println("-========================================================================================================================================================================================================================================");
                                        System.out.println("-========================================================================================================================================================================================================================================");
                                        System.out.println("-========================================================================================================================================================================================================================================");
                                        System.out.println("-========================================================================================================================================================================================================================================");
                                        System.out.println("-========================================================================================================================================================================================================================================");
                                        System.out.println("-========================================================================================================================================================================================================================================");


                                        already_handled = true;

                                        String ind_to_load = "";
                                        for (int f = 0; f < oLoadedVars.size(); f++) {

                                            LoadedVar currLoadedVar = oLoadedVars.get(f);

                                            if (f == 0)
                                            {
                                                if (currLoadedVar.getType() == LoadedVar.LoadedVarType.CONSTANT_STRING) {
                                                    ind_to_load = currLoadedVar.getValue();
                                                }
                                                else
                                                {
                                                    System.out.println("Error in TGFactory : Error handling Maps");
                                                }

                                                break;
                                            }


                                        }

                                        List<Integer> mapValues = varInfo.getMapValue(ind_to_load);

                                        already_handled = true;

                                        if(mapValues.size() > 0) {
                                            for (int mapValue : mapValues)
                                                if (mapValue != -100) {

                                            /*
                                                // load the variable again //
                                                int index = varInfo.getIndex();
                                                currNode.addValueChangeLog(new ValueChangeLog(index, mapValue));
                                                oLoadedVars.clear(); /////// tbc : replace when taint is also transfered without var
                                                isVarsCleared = false;  // dont clear up at the end
                                                oTaintStack.clear();

                                                oLoadedVars.add(new LoadedVar("" + index, LoadedVar.LoadedVarType.VARIABLE));
                                            */
                                                    oTaintStack.push(mapValue);
                                                    System.out.println("...---___ Map Value get and is : " + mapValue);
                                                }
                                        }
                                        else
                                        {
                                            oVarList.clear();
                                            oTaintStack.clear();

                                            is_clear_var = false;
                                            // if the map is init from request.getMap??
                                            oLoadedVars.add(new LoadedVar("" + varInfo.getIndex(),LoadedVar.LoadedVarType.VARIABLE));
                                        }
                                    } else if (fullMethodName.equals("java/util/List.add(Ljava/lang/Object;)Z")) {
                                        already_handled = true;
                                        //String temp_key = "";
                                        List<Integer> taint_type = new ArrayList<>();
                                        // varInfo contains the variable that is a list
                                        //String ind_to_load = "";
                                        for (int f = 0; f < oLoadedVars.size(); f++) {

                                            LoadedVar currLoadedVar = oLoadedVars.get(f);

                                            if (currLoadedVar.getType() == LoadedVar.LoadedVarType.CONSTANT_STRING) {
                                                taint_type.add(TaintTypes.UN_TAINTED);
                                            } else {
                                                int var_index = currLoadedVar.getIntValue();
                                                if (var_index > 0) {
                                                    taint_type = getTaintValueOfVarAtIndex(oNodeVarList, oTempNodeVarList, var_index, false);
                                                }
                                            }


                                        }

                                        //for(int g:taint_type)
                                        varInfo.addListTaintVal(taint_type);
                                    } else if (fullMethodName.equals("java/util/List.remove(I)Ljava/lang/Object;")) {
                                        already_handled = true;
                                        String ind_to_load = "";
                                        for (int f = 0; f < oLoadedVars.size(); f++) {

                                            LoadedVar currLoadedVar = oLoadedVars.get(f);

                                            if (f == 0) {
                                                if (currLoadedVar.getType() == LoadedVar.LoadedVarType.CONSTANT) {
                                                    ind_to_load = currLoadedVar.getValue();
                                                } else {
                                                    System.out.println("Error in TGFactory : Error handling List");
                                                }

                                                break;
                                            }
                                        }

                                        int int_val = -200;
                                        if (!ind_to_load.equals(""))
                                            int_val = Integer.parseInt(ind_to_load);

                                        if (int_val == -200) {
                                            varInfo.invokeListMethod_Remove(int_val);
                                        } else {
                                            System.out.println("Error in TGFactory : no int to remove List index");
                                        }
                                    } else if (fullMethodName.equals("java/util/List.get(I)Ljava/lang/Object;")) {
                                        already_handled = true;
                                        String ind_to_load = "";
                                        for (int f = 0; f < oLoadedVars.size(); f++) {

                                            LoadedVar currLoadedVar = oLoadedVars.get(f);

                                            if (f == 0) {
                                                if (currLoadedVar.getType() == LoadedVar.LoadedVarType.CONSTANT) {
                                                    ind_to_load = currLoadedVar.getValue();
                                                } else {
                                                    System.out.println("Error in TGFactory : Error handling Maps");
                                                }

                                                break;
                                            }
                                        }

                                        int int_val = -200;
                                        if (!ind_to_load.equals(""))
                                            int_val = Integer.parseInt(ind_to_load);

                                        if (int_val == -200) {
                                            List<Integer> integers = varInfo.invokeListMethod_Get(int_val);


                                            for (int y : integers) {
                                                System.out.println("--0-0-0-0-0-0-0- List Values : " + y);
                                                oTaintStack.push(y);
                                            }
                                        } else {
                                            System.out.println("Error in TGFactory : no int to remove List index");
                                        }
                                    } else if (fullMethodName.equals("java/lang/String.charAt(I)C")) {

                                        already_handled = true;
                                        is_clear_var = false;
                                        String temp_key = "";
                                        int ind = -300;
                                        List<Integer> taint_type = new ArrayList<>();
                                        // now load the variables
                                        for (int f = 0; f < oLoadedVars.size(); f++) {
                                            LoadedVar currLoadedVar = oLoadedVars.get(f);
                                            // first one is index and second one is
                                            if (f == 0) {
                                                if (currLoadedVar.getType() == LoadedVar.LoadedVarType.CONSTANT) {
                                                    ind = currLoadedVar.getIntValue();
                                                } else {
                                                    int var_index = currLoadedVar.getIntValue();
                                                    if (var_index > 0) {
                                                        taint_type = getTaintValueOfVarAtIndex(oNodeVarList, oTempNodeVarList, var_index, false);
                                                    }
                                                }

                                                break; // nothing needed further
                                            }
                                        }   // end of for loop

                                        try {

                                            String str_value = varInfo.get_str_value();

                                            System.out.println("String value of the variable is : " + str_value);
                                            System.out.println("ind  : " + ind);

                                            char c = varInfo.callStringMethod_charAt(ind);

                                            int int_val = (int) c;
                                            System.out.println("0-0--0-0-0-0-0-0-0-0-0-0-0-0-0-0-0 The int value is : " + int_val);
                                            oLoadedVars.clear();
                                            oLoadedVars.add(new LoadedVar("" + int_val, LoadedVar.LoadedVarType.CONSTANT));
                                        } catch (Exception ex) {
                                            ex.printStackTrace();
                                        }

                                        // varInfo.putMapValue(temp_key, taint_type);
                                        // oVarList.set(objectIndex, varInfo); // re-save just to check if thats the problem
                                    } else {
                                        if (varInfo != null)
                                            oTaintStack.push(varInfo.taintType);    // save the taint of the functionf
                                    }
                                    if(already_handled) {
                                        // if the variable that is calling the function is tainted
                                        List<Integer> taintValueOfVarAtIndex = getTaintValueOfVarAtIndex(oNodeVarList, oTempNodeVarList, varInfo.getIndex(), false);

                                        System.out.println("Taint value at the index : " + varInfo.getIndex() + " :: " + taintValueOfVarAtIndex);

                                        for (int u : taintValueOfVarAtIndex) {
                                            if (u == TaintTypes.TAINTED) {
                                                oTaintStack.push(TaintTypes.TAINTED);
                                            }
                                        }
                                    }

                                }
                                else
                                {
                                    System.out.println("$$$ ========-------- objectIndex = -1");
                                }


                                if (is_clear_var && isClearVarsAllCost) {
                                    // separated from
                                    oLoadedVars.clear();
                                    isVarsCleared = true;
                                }

                            } else if (!oListIgnoreMethods.contains(fullMethodName)) {
                                boolean isTainted = false;
                                boolean isTaintKill = false;

                                while (oTaintStack.size() > 0) {
                                    Integer oInt = oTaintStack.pop();
                                    if (oInt.intValue() == TaintTypes.TAINTED)
                                        isTainted = true;
                                    if (oInt.intValue() == TaintTypes.UN_TAINTED)
                                        isTaintKill = true;
                                }

                                oLoadedVars.clear();
                                isVarsCleared = true;
                                if (isTainted) {
                                    oTaintStack.add(TaintTypes.TAINTED);
                                }

                            }

                        /*else if(ins instanceof INVOKESTATIC)
                        {
                            VarInfo varInfo = oVarList.get(objectIndex);
                            String name = varInfo.getName();
                            String type = varInfo.getType();


                        }*/


                        } else if (ins instanceof LDC) {
                            LDC ins_ldc = (LDC) ins;
                            String value = ins_ldc.getValue(cpg).toString();
                            System.out.println("--== LDC : " + value + " stored");
                            oTaintStack.add(TaintTypes.UN_TAINTED);
                            oLoadedVars.add(new LoadedVar(value, LoadedVar.LoadedVarType.CONSTANT_STRING));

                        } else if (ins instanceof TABLESWITCH) {
                            TABLESWITCH ins_tableswitch = (TABLESWITCH) ins;

                            int[] matchs = ins_tableswitch.getMatchs();
                            int[] indices = ins_tableswitch.getIndices();
                            InstructionHandle[] targets = ins_tableswitch.getTargets();



                            for (int u = 0; u < indices.length; u++) {
                                int curr_indices = indices[u];
                                try
                                {
                                    int curr_match = matchs[u];
                                    System.out.println("-- match index : " + curr_match);
                                }
                                catch(Exception ex)
                                {
                                    ex.printStackTrace();
                                }
                                System.out.println("-- switch index : " + curr_indices);
                            }

                            // Load first thingy
                            if (oLoadedVars.size() > 0) {
                                LoadedVar loadedVar = oLoadedVars.get(0);
                                if (loadedVar.getType() == LoadedVar.LoadedVarType.CONSTANT) {
                                    String value = loadedVar.getValue();
                                    int int_value = Integer.parseInt(value);
                                    System.out.println("****************** int_value : " + int_value);

                                    int found_index = -1;

                                    for (int i = 0; i < targets.length; i++) {
                                        InstructionHandle temp_handle = targets[0];
                                        Instruction instruction = temp_handle.getInstruction();
                                        if (matchs[i] == int_value) {
                                            System.out.println("$$$$$$$$$$-- found the index to be : " + i);
                                            found_index = i;
                                            break;
                                            //System.out.println("_+++_+_+++++_++++ : " + matchs[i] + " :: " + int_value);
                                        }
                                        //System.out.println("_+++_+_+++++_++++ : " + matchs[i] + " :: " + int_value);
                                        //temp_handle;
                                        //System.out.println("-- switch instruction Matches : " + matchs[i] + " :: Targets : " + targets[i] + " :: Target_Position : " + targets[i].getPosition());

                                    }

                                    int ru_GetID = -100;

                                    if (found_index != -1) {
                                        Iterator<Edge> iei2 = cfg.outgoingEdgeIterator(currBlock2);
                                        int index = 0;
                                        while (iei2.hasNext()) {
                                            Edge e = iei2.next();
                                            BasicBlock target = e.getTarget();

                                            int result = -1;

                                            System.out.println(" -- index : " + index + " :: " + " found index : " + found_index);

                                            if (index == found_index)
                                                result = 1; // condition true
                                            else
                                                result = 0;

                                            if(result == 1) {
                                                ru_GetID = target.getId();
                                                oListBlockAccessInfo.add(new BlockAccessInfo(target.getId(), result));
                                            }
                                            else if(ru_GetID != target.getId())
                                            {
                                                oListBlockAccessInfo.add(new BlockAccessInfo(target.getId(), result));
                                                System.out.println("$$ == Child removed" + " :: ChildID : " + target.getId());

                                            }

                                            /*
                                            int tempChildId = target.getId();
                                            int tempParentId = currBlock2.getId();

                                            if(result == 0)
                                            {
                                                for(int l = 0; l < oListProcessBB.size(); l++) {
                                                    BasicBlockInfo ru_bbi = oListProcessBB.get(l);
                                                    BasicBlock ru_currBlock2 = oListProcessBB.get(l).getoBasicBlock();
                                                    int ru_ParentID = oListProcessBB.get(l).getParentID();
                                                    int ru_childID = ru_currBlock2.getId();

                                                    if(ru_ParentID == tempParentId && ru_childID == tempChildId)
                                                    {
                                                        System.out.println("$$ == Child removed ParentID : " + ru_ParentID + " :: ChildID : " + ru_childID);
                                                        //oListProcessBB.remove(l);
                                                        oListBlockAccessInfo.add(new BlockAccessInfo(ru_childID, 0));
                                                    }
                                                }
                                            }
                                            */

                                        /*if (e.getType() == EdgeTypes.FALL_THROUGH_EDGE) {
                                            BasicBlock thenBB = e.getTarget();
                                            oListBlockAccessInfo.add(new BlockAccessInfo(thenBB.getId(), result));
                                        }
                                        else
                                        {
                                            System.out.println("OTHER EDGES : " + e.getType());
                                        }*/

                                            index++;
                                        }
                                    } else {
                                        System.out.println("Error in TG Factory : cant find the thingy");
                                    }
                                } else {
                                    System.out.println("Error in TGFactory : First value is not a constant");
                                }


                            } else {
                                System.out.println("Error in TGFactory : the oLoadedVars size is equal to zero");
                            }



                        /*for(int i = 0; i < targets.length; i++)
                        {
                            InstructionHandle temp_handle = targets[0];
                            Instruction instruction = temp_handle.getInstruction();
                            //temp_handle;
                            System.out.println("-- switch instruction Matches : " + matchs[i] + " :: Targets : " + targets[i] + " :: Target_Position : " + targets[i].getPosition());
                        }*/
                        }
                        else if (ins instanceof ICONST)  // loads an integer
                        {
                            ICONST ins_iconst = (ICONST) ins;
                            Number value = ins_iconst.getValue();

                            int i = value.intValue();
                            oLoadedVars.add(new LoadedVar(i + "", LoadedVar.LoadedVarType.CONSTANT));
                        }
                        else if(ins instanceof SIPUSH)
                        {
                            SIPUSH ins_sipush = (SIPUSH) ins;
                            Number value = ins_sipush.getValue();

                            int i = value.intValue();
                            oLoadedVars.add(new LoadedVar(i + "", LoadedVar.LoadedVarType.CONSTANT));
                        }
                        else if(ins instanceof BIPUSH)
                        {
                            BIPUSH ins_bipush = (BIPUSH) ins;
                            Number value = ins_bipush.getValue();

                            int i = value.intValue();
                            oLoadedVars.add(new LoadedVar(i + "", LoadedVar.LoadedVarType.CONSTANT));
                        }
                        else if(ins instanceof IADD)
                        {
                            int result = 0;

                            for(int i = 0; i < oLoadedVars.size() ; i++)
                            {
                                LoadedVar lv = oLoadedVars.get(i);
                                if(lv.getType() == LoadedVar.LoadedVarType.CONSTANT)
                                    result += lv.getIntValue();
                                else
                                    System.out.println("Error in TGFactory : NON CONSTANT ENCOUNTERED");
                            }
                            oLoadedVars.clear();
                            oLoadedVars.add(new LoadedVar(result + "", LoadedVar.LoadedVarType.CONSTANT));
                        }
                        else if (ins instanceof ISTORE) {
                            ISTORE ins_istore = (ISTORE) ins;
                            int index = ins_istore.getIndex();//ins_istore.getValue();

                            if (oLoadedVars.size() > 0) {
                                LoadedVar loadedVar = oLoadedVars.get(0);
                                if (loadedVar.getType() == LoadedVar.LoadedVarType.CONSTANT) {
                                    String value = loadedVar.getValue();
                                    int int_value = Integer.parseInt(value);

                                    System.out.println("Map value put from : "  + index + " :: int_value : " +int_value);
                                    oMapLoadedInt.put(index, int_value);
                                }
                            } else {
                                System.out.println("Error in TGFactory : (ins instanceof ISTORE) doesn't have constant : ");
                            }

                            oLoadedVars.clear();
                            isVarsCleared = true;
                            oTaintStack.clear();

                        } else if (ins instanceof ILOAD) {
                            ILOAD ins_iload = (ILOAD) ins;
                            int index = ins_iload.getIndex();

                            if (oMapLoadedInt.containsKey(index)) {
                                int temp_value = oMapLoadedInt.get(index);
                                oLoadedVars.add(new LoadedVar(temp_value + "", LoadedVar.LoadedVarType.CONSTANT));
                            } else {
                                System.out.println("Message from TGFactory : ILOAD is not loading any constant");
                            }
                        } else if (ins instanceof ASTORE) // ommitting the arrays for now StoreInstruction
                        {
                            boolean isTainted = false;
                            boolean isTaintKill = false;

                            StoreInstruction oIns = (StoreInstruction) ins;
                            int ind_original = oIns.getIndex();
                            int ind = getModifiedVarIndex(ind_original, PC);
                            System.out.println("$$ == Variable to store is : " + ind);
                            System.out.println("......:::::::: Instruction Data Type : ");

                            String str_value = "";

                            if (ind >= 0)    // not -1
                            {
                                for (LoadedVar t : oLoadedVars)  // could have been a better solution
                                {
                                    System.out.println("$$== " + t.getValue() + " :: " + t.getType() + " :: ");

                                    if (t.getType() == LoadedVar.LoadedVarType.VARIABLE) {
                                        int intValue = t.getIntValue();
                                        if (intValue > 0) {
                                            VarInfo varInfo = oVarList.get(intValue);

                                            if (varInfo.getType().equals("java.lang.String")) {
                                                str_value += "%STRING_" + intValue + "%";   // to do ... see if the value is set ... use value instead
                                            } else {
                                                str_value += "%VAR_" + intValue + "%";
                                            }

                                            currNode.addDef(ind, intValue);



                                            System.out.println("%%%%%%%%%% =-=-=-=-=-=-==== Use Def Pair saved Def : " + ind + " Use : " + intValue);

                                            int oNodeListIndex = getListIndex(oVarList, intValue);
                                            //VarInfo varInfo = oVarList.get(varListIndex);

                                            if(twoVarsSameNameExist)
                                            {
                                                VarInfo varInfo2 = oVarList.get(intValue);
                                                VarInfo originalDefVar = oVarList.get(ind);
                                                for(int f = 0; f < oVarList.size(); f++)
                                                {
                                                    VarInfo duplicateDefVar = oVarList.get(f);

                                                    if(duplicateDefVar.getName().equals(originalDefVar.getName()))
                                                    {
                                                        if(duplicateDefVar.getIndex() != ind) {
                                                            currNode.addDef(duplicateDefVar.getIndex(), intValue) ;
                                                            System.out.println("Extra def added Def : " + duplicateDefVar.getIndex() + " :: " + " Use : " + intValue);
                                                        }
                                                    }
                                                }
                                            }

                                            List<Integer> taintValueOfVarAtIndex = getTaintValueOfVarAtIndex(oNodeVarList, oTempNodeVarList, intValue, false);
                                            for (int u : taintValueOfVarAtIndex) {
                                                System.out.println("Loaded var type with taint is " + TaintTypes.typeToString(u));
                                                oTaintStack.push(u);
                                            }
                                        }
                                    } else if (t.getType() == LoadedVar.LoadedVarType.CONSTANT_STRING) {
                                        str_value += t.getValue();
                                    } else if (t.getType() == LoadedVar.LoadedVarType.CONSTANT) {
                                        str_value += t.getValue();
                                    }
                                }

                                VarInfo varInfo1 = oNodeVarList.get(ind);
                                varInfo1.setStringValue(str_value);
                                System.out.println("String Value Set to : " + str_value);

                                while (oTaintStack.size() > 0) {
                                    Integer oInt = oTaintStack.pop();
                                    if (oInt.intValue() == TaintTypes.TAINTED)
                                        isTainted = true;
                                    if (oInt.intValue() == TaintTypes.UN_TAINTED)
                                        isTaintKill = true;
                                }

                                if (oIns.getType(cpg) instanceof ObjectType) {
                                    System.out.println(((ObjectType) oIns.getType(cpg)).getClassName());
                                } else {
                                    System.out.println(oIns.getType(cpg).toString());
                                }

                                int temp_ind = getVarListIndex(oVarList, ind);
                                if (temp_ind > 0)
                                    System.out.println("Store Index Name : " + oVarList.get(temp_ind).getName());

                                //varInfo.setName(lvg.getName());
                                int position = handle.getPosition();
                                int sourceLine = lineNumberTable.getSourceLine(position);
                                LineNumber[] lineNumberTable1 = lineNumberTable.getLineNumberTable();


                                System.out.println("### The Index is : " + ind);
                                System.out.println("### The Position is : " + position);
                                System.out.println("### The line number is : " + sourceLine);
                                System.out.println("### The instruction of is : " + ins.getLength());
                                //System.out.println("### The length is : " + handle.get);
                                InstructionTargeter[] targeters = handle.getTargeters();

                                int PC2 = position + ins.getLength();

                                //getModifiedVarIndex(ind, PC);

                                for (int u = 0; u < lineNumberTable1.length; u++) {
                                    LineNumber ln = lineNumberTable1[u];
                                    if (ln.getLineNumber() == sourceLine) {
                                        System.out.println("### The Start PC is : " + ln.getStartPC());
                                        LocalVariable localVariable = getLocalVariableAtPC(localVariableOrg, ind, ln.getStartPC());
                                  /*  if(localVariable != null)
                                        System.out.println("### And Finally the name of the variable is : " + localVariable.getName());
                                    else
                                        System.out.println("### And Finally the name of the variable is : NULL");


                                    localVariable = getLocalVariableAtPC(localVariableOrg, ind, PC);

                                    System.out.println("--== Second Try ==--");
    */
                                        if (localVariable != null)
                                            System.out.println("### And Finally the name of the variable is : " + localVariable);
                                        else
                                            System.out.println("### And Finally the name of the variable is : NULL");

                                    }
                                }


                                int listIndex = getListIndex(oNodeVarList, ind);
                                if (listIndex > 0) {
                                    int taint_val = TaintTypes.UNKNOWN;
                                    if (isTainted) {
                                        taint_val = TaintTypes.TAINTED;
                                    } else if (isTaintKill)
                                        taint_val = TaintTypes.UN_TAINTED;

                                    // adding taints only to string
                                    int varListIndex = getVarListIndex(oVarList, ind);

                                    if (ins instanceof ASTORE) {
                                        if (oListTempAttr.size() > 0) {
                                            for (VarAttr f : oListTempAttr) {
                                                VarInfo varInfo = oVarList.get(varListIndex);
                                                // to do put an extra check for the type
                                                varInfo.addAttr(f.getAttrName(), f.getAttrVal());
                                            }
                                        }
                                    }

                                    oListTempAttr.clear();  // clear the attributes anyways

                                    if (varListIndex > -1) {
                                        ////System.out.println("The value type is : " + oVarList.get(varListIndex).getType());
                                        if (oVarList.get(varListIndex).getType().equals("java.lang.String") || oVarList.get(varListIndex).getType().equals("java.lang.String[]") || oVarList.get(varListIndex).getType().equals("java.util.Map") || oVarList.get(varListIndex).getType().equals("java.util.Enumeration") || oVarList.get(varListIndex).getType().equals("javax.servlet.http.Cookie[]") || oVarList.get(varListIndex).getType().equals("java.util.List") || oVarList.get(varListIndex).getType().equals("javax.servlet.http.Cookie")) {
                                            oNodeVarList.get(listIndex).addTaintType(taint_val);

                                            if (oVarList.get(varListIndex).getType().equals("java.lang.String")) {
                                                ///////
                                            }


                                            System.out.println("Taint Value Changed : " + listIndex + " :: " + "To The Value : " + TaintTypes.typeToString(taint_val));
                                            // taint value changed here
                                            currNode.addValueChangeLog(new ValueChangeLog(ind, taint_val)); // ind was the previous option
                                            if(twoVarsSameNameExist)
                                            {
                                                VarInfo varInfo2 = oVarList.get(ind);
                                                System.out.println("^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^");
                                                System.out.println("Running same name stuff");
                                                System.out.println("^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^");
                                                for(int f = 0; f < oVarList.size(); f++)
                                                {
                                                    VarInfo varInfo = oVarList.get(f);

                                                    if(varInfo.getName().equals(varInfo2.getName()) && varInfo.getIndex() != varInfo2.getIndex()) {
                                                        currNode.addValueChangeLog(new ValueChangeLog(varInfo.getIndex(), taint_val)); // ind was the previous option
                                                    }

                                                    if(varInfo.getName().equals(varInfo2.getName()))
                                                    {
                                                        System.out.println("^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^");
                                                        System.out.println("Same name found at " + varInfo.getIndex() + " :: " + varInfo2.getIndex());
                                                        System.out.println("^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^");

                                                    }
                                                }
                                            }
                                        }
                                    }


                                } else {
                                    ////System.out.println("Error : the listIndex is incorrect .. can't find the variable");
                                }
                                oLoadedVars.clear();
                                System.out.println(".:: Clearing Vars ::.");
                                isVarsCleared = true;
                            }
                            else
                            {
                                // the index doesnt exist in variable but it should be stored in temp variables
                                VarInfo tempVarInfo = new VarInfo(ind_original);

                                // for loop same as above but no def use pair
                                for (LoadedVar t : oLoadedVars)  // could have been a better solution
                                {
                                    System.out.println("$$== " + t.getValue() + " :: " + t.getType() + " :: ");

                                    if (t.getType() == LoadedVar.LoadedVarType.VARIABLE) {
                                        int intValue = t.getIntValue();
                                        if (intValue > 0) {
                                            VarInfo varInfo = oVarList.get(intValue);

                                            if (varInfo.getType().equals("java.lang.String")) {
                                                str_value += "%STRING_" + intValue + "%";   // to do ... see if the value is set ... use value instead
                                            } else {
                                                str_value += "%VAR_" + intValue + "%";
                                            }

                                            int oNodeListIndex = getListIndex(oVarList, intValue);
                                            //VarInfo varInfo = oVarList.get(varListIndex);


                                            List<Integer> taintValueOfVarAtIndex = getTaintValueOfVarAtIndex(oNodeVarList, oTempNodeVarList, intValue, false);
                                            for (int u : taintValueOfVarAtIndex) {
                                                System.out.println("Loaded var type with taint is " + TaintTypes.typeToString(u));
                                                oTaintStack.push(u);
                                            }
                                        }
                                    } else if (t.getType() == LoadedVar.LoadedVarType.CONSTANT_STRING) {
                                        str_value += t.getValue();
                                    } else if (t.getType() == LoadedVar.LoadedVarType.CONSTANT) {
                                        str_value += t.getValue();
                                    }
                                }

                                //instead of using the original thing it would be wise to add the things to varInfo
                                //VarInfo varInfo1 = oNodeVarList.get(ind);
                                tempVarInfo.setStringValue(str_value);
                                System.out.println("String Value Set to : " + str_value);

                                while (oTaintStack.size() > 0) {
                                    Integer oInt = oTaintStack.pop();
                                    if (oInt.intValue() == TaintTypes.TAINTED)
                                        isTainted = true;
                                    if (oInt.intValue() == TaintTypes.UN_TAINTED)
                                        isTaintKill = true;
                                }


                                int taint_val = TaintTypes.UNKNOWN;
                                if (isTainted) {
                                    taint_val = TaintTypes.TAINTED;
                                } else if (isTaintKill)
                                    taint_val = TaintTypes.UN_TAINTED;

                                tempVarInfo.setAddTaintType(taint_val);

                                System.out.println("$$$$$ ======= tempVarInfo" + tempVarInfo.getIndex() + " :: " + TaintTypes.typeToString(taint_val));

                                oTempNodeVarList.add(tempVarInfo);


                            }

                        } else if (ins instanceof ARETURN) {
                            // else give an error in case of sinking of another operator
                            if (isReturningClass) {
                                for (LoadedVar temp_ind : oLoadedVars) {
                                    if (temp_ind.getType() == LoadedVar.LoadedVarType.VARIABLE) {

                                        int intValue = temp_ind.getIntValue();

                                        if (intValue != -1 && intValue <= oVarList.size() - 1) {
                                            int varListIndex = getVarListIndex(oVarList, intValue);
                                            VarInfo varInfo = oVarList.get(varListIndex);


                                            currNode.addValueChangeLog(new ValueChangeLog(varInfo.getIndex(), 100));
                                            System.out.println("===== The variable at the index " + varInfo.getIndex() + " :: has SINK");
                                        }
                                    }
                                }
                            }

                        }
                    /*else if(ins instanceof ASTORE)
                    {
                        ASTORE ins_astore = (ASTORE) ins;
                        int ind = ins_astore.getIndex();

                    }*/
                        else if (ins instanceof ALOAD) {
                            ALOAD ins_aload = (ALOAD) ins;
                            int index_original = ins_aload.getIndex();
                            int index = getModifiedVarIndex(index_original, PC);
                            //int temp_ind = getVarListIndex(oVarList, index);

                            if (index > 0) {
                                System.out.println("Loaded Index Name : " + oVarList.get(index).getName());
                                int sourceLine = lineNumberTable.getSourceLine(handle_position);
                                System.out.println("Source Line : " + sourceLine);


                                if (!doesLoadedVarContainsIndex(oLoadedVars, index))
                                    oLoadedVars.add(new LoadedVar("" + index));

                                List<Integer> oInt = getTaintValueOfVarAtIndex(oNodeVarList, oTempNodeVarList, index, false);
                                String ret_val = "";
                                for (int u : oInt) {
                                    System.out.println("--=== TaintType :  " + TaintTypes.typeToString(u));
                                    oTaintStack.add(u);
                                }
                            }
                            // it might not be in the main list but might be in the temp list
                            else
                            {
                                int tempvar_index = getTempNodeVarIndex(oTempNodeVarList, index_original);

                                // not storing loaded var there is no need
                                //oLoadedVars.add(new LoadedVar("" + index));

                                if(tempvar_index > -1)
                                {

                                        List<Integer> oInt = oTempNodeVarList.get(tempvar_index).getoListTaintType();

                                        for (int u : oInt)
                                        {
                                            System.out.println("--=== TaintType :  " + TaintTypes.typeToString(u));
                                            oTaintStack.add(u);
                                        }

                                }
                                else
                                {
                                    System.out.println("$$$$$$$======== The variable couldn't be found even in the temp list");
                                }
                            }
                        } else if (ins instanceof NEW) {

                        } else if (ins instanceof DUP) {

                        } else if (ins instanceof IFNULL) {

                        } else if (ins instanceof ATHROW) {

                        } else if (ins instanceof RETURN) {

                        } else if (ins instanceof POP) {

                        } else if (ins instanceof IFNULL) {

                        }
                        else if(ins instanceof ARRAYLENGTH)
                        {
                            // the loaded var must be an array which is not needed anymore
                            oLoadedVars.clear();
                            oTaintStack.clear();
                            isVarsCleared = true;
                        }


                    /*
                    if(ins instanceof StackProducer && ins instanceof  StackConsumer)
                    {

                    }
                    if (ins instanceof StackProducer) {
                        boolean isTainted = false;

                        if(!isIgnoreInstruction(ins))
                        if (ins instanceof StackConsumer)
                            while (oTaintStack.size() > 0) {
                                Integer oInt = oTaintStack.pop();
                                if (oInt.intValue() == TaintTypes.TAINTED) {
                                    isTainted = true;
                                }

                                // if is taint kill
                            }

                        if (ins instanceof LDC) {
                            oTaintStack.add(TaintTypes.UN_TAINTED);
                        }

                        if (ins instanceof ALOAD)// || ins instanceof AALOAD)
                        {
                            ALOAD ins_aload = (ALOAD) ins;
                            int ind = ins_aload.getIndex();

                            // Type type = ins_aload.getType(cpg);
                            // String className = ((ObjectType) type).getClassName();
                            // System.out.println("== Var Type : " + className);


                            oLoadedVars.add(ind);

                            System.out.println("===========The variable is loaded : " + ind);

                            Integer oInt = getTaintValueOfVarAtIndex(oNodeVarList, ind);

                            String ret_val = "";

                            oTaintStack.add(oInt);
                        }
                        //else if(ins instanceof NEW)
                        ///{
                            // NEW new_ins = (NEW) ins;
                            // new_ins.getLoadClassType(cpg);
                            // new_ins.getIndex();
                            // System.out.println("======= New Variable created at the index : " + new_ins.getIndex() + " :: of type : " + new_ins.getLoadClassType(cpg));
                        //}
                        else if(ins instanceof ICONST)
                        {
                            ICONST ins_iconst = (ICONST)ins;
                            System.out.println("----- ==== The constant value is : " + ins_iconst.getValue());
                        }

                        else if(ins instanceof AALOAD)
                        {
                            //GETSTATIC st = (GETSTATIC) ins;

                            //System.out.println(st.getIndex());

                            AALOAD ins_aaload = (AALOAD) ins;

                            ins_aaload.toString(cpg.getConstantPool());
                            ins_aaload.consumeStack(cpg);

                            Type type = ins_aaload.getType(cpg);


                            //ALOAD ins_aload = (ALOAD)ins;
                            //int ind = ins_aload.getIndex();

                            //System.out.println("---------- GetMethod : " + type.getMethod);

                            System.out.println("---------- Signature : " + type.getSignature());
                            System.out.println("---------- Size : " + type.getSize());
                            System.out.println("---------- toString : " + type.toString());
                            ///type
                        }

                        if (ins instanceof InvokeInstruction) {
                            if (isTaintSinkInstruction(ins)) {

                            }
                            else if (isTaintIntroducingInvokeInstruction(ins)) // like system.in
                            {
                                oTaintStack.add(TaintTypes.TAINTED);
                            } else if (isTaintKillingTaintInvokeInstruction(ins)) {
                                oTaintStack.add(TaintTypes.UN_TAINTED);
                            } else if (isTainted) {
                                oTaintStack.add(TaintTypes.TAINTED);
                            } else {
                                oTaintStack.add(TaintTypes.UNKNOWN);
                            }
                        }
                    }

                    if (ins instanceof StackConsumer) {
                        boolean isTainted = false;
                        boolean isTaintKill = false;


                        if (ins instanceof InvokeInstruction) {
                            if (isTaintSinkInstruction(ins)) {

                                if (oLoadedVars.size() == 0) {
                                    System.out.println("-=-=-=-=-=-=-=-=-=--=-=-=-=-=-=-=-=-=-=-=-");
                                    System.out.println("-=-=-=-=-=-=-=-=-=-=NO VAR TO SINK-=-=-=-=-=-=-");
                                    System.out.println("-=-=-=-=-=-=-=-=-=--=-=-=-=-=-=-=-=-=-=-=-");
                                }
                                else
                                {
                                    System.out.println("There are some variables loaded");
                                }

                                // get all the indexes of the variables that are loaded
                                for (Integer temp_ind : oLoadedVars) {
                                    currNode.addValueChangeLog(new ValueChangeLog(temp_ind, 100));
                                    System.out.println("===== The variable at the index " + temp_ind + " :: has SINK");
                                }
                            }
                        }

                        if (!(ins instanceof AASTORE) && !(ins instanceof StackProducer))   // more proper solution will be produce stack
                        {
                            isVarsCleared = true;
                            oLoadedVars.clear();
                            System.out.println("==VARS CLEARED==");
                        }

                        while (oTaintStack.size() > 0) {
                            Integer oInt = oTaintStack.pop();
                            if (oInt.intValue() == TaintTypes.TAINTED)
                                isTainted = true;
                            if (oInt.intValue() == TaintTypes.UN_TAINTED)
                                isTaintKill = true;
                        }

                        if (isTainted || isTaintKill) {
                            if (ins instanceof StoreInstruction) // ommitting the arrays for now
                            {
                                StoreInstruction oIns = (StoreInstruction) ins;
                                int ind = oIns.getIndex();

                                int listIndex = getListIndex(oNodeVarList, ind);
                                if (listIndex > 0) {
                                    int taint_val = TaintTypes.UNKNOWN;
                                    if (isTainted) {
                                        taint_val = TaintTypes.TAINTED;
                                    } else if (isTaintKill)
                                        taint_val = TaintTypes.UN_TAINTED;

                                    oNodeVarList.get(listIndex).addTaintType(taint_val);

                                    System.out.println("Taint Value Changed : " + listIndex + " :: " + "To The Value : " + TaintTypes.typeToString(taint_val));
                                    // taint value changed here
                                    currNode.addValueChangeLog(new ValueChangeLog(ind, taint_val)); // ind was the previous option
                                } else {
                                    ////System.out.println("Error : the listIndex is incorrect .. can't find the variable");
                                }
                            }
                        }

                        oTaintStack.clear();
                    }*/


                    }


                    ////System.out.println("-=-=-=-=-=-=-=-=-=--=BLOCK END-=-=-=-=-=-=-=-=-=-=-");

                    // if the parent of the current node has the specific stuff and the stuff is not added to list then add it
                    /*if (ParentID >= 0) {

                        BlockAccessInfo oParent = null;
                        boolean does_exist = false;

                        int id = getIndexBlockAccessInfoById(currBlock2.getId());


                        // if it doesn't exist in the list
                        if (id == -1) {
                            int parent_id = getIndexBlockAccessInfoById(ParentID);

                            if (parent_id != -1 && (parent_id <= oListBlockAccessInfo.size() - 1)) {
                                oParent = oListBlockAccessInfo.get(parent_id);
                                if (oParent != null) {
                                    oListBlockAccessInfo.add(new BlockAccessInfo(currBlock2.getId(), oParent.getAccessInfo()));
                                }
                            } else {
                                System.out.println("Error : The parent of oBAI is null, ParentID : " + ParentID);
                            }
                        } else
                            is_boi_set = true;

                    }*/

                    oList.add(currBlock2);
                    ProcessedBlocks.add(currBlock2.getId());


                    if (!is_boi_set) {
                        oListBlockAccessInfo.add(new BlockAccessInfo(currBlock2.getId(), -1));
                        //>COMSystem.out.println("Adding BAI of  : " + currBlock2.getId());
                    }

                    currNode.setNodeVarList(oNodeVarList);

                    //currNode.setParentID(ParentID);   // will be trouble if multiple parents
                    if (ru_addNewNode) //ru_addNewNode)
                    {
                        oGraph.addNode(currNode, ParentID);
                    }
                    else
                    {
                        TaintNode taintNode = new TaintNode(currNode.getBlockID());
                        taintNode.setNodeVarList(currNode.getNodeVarList());
                        oGraph.addNode(taintNode, ParentID);
                    }

                    List<LoadedVar> oListTemp = new ArrayList<>();
                    if (oLoadedVarsByBlock.containsKey(ParentID) && !isVarsCleared && ParentID != RootId) {
                        oListTemp = oLoadedVarsByBlock.get(ParentID);
                    } else {
                        System.out.println("Error in TGFactory : ParentID not found in oLoadedVarsByBlock");
                    }


                    // already added
                    for (int i = 0; i < oLoadedVars.size(); i++)
                    {
                        LoadedVar currVar = oLoadedVars.get(i);
                        if (!oListTemp.contains(currVar)) {
                            oListTemp.add(currVar);
                        }
                    }

                    oLoadedVars = oListTemp;

                    // dont need this
                    if (oLoadedVars.size() > 0) {
                        oLoadedVarsByBlock.put(bbi.getoBasicBlock().getId(), oLoadedVars);

                        //System.out.println("-- BlockID : " + bbi.getoBasicBlock().getId() + " :: ");
                        for (LoadedVar temp_int : oLoadedVars) {
                            System.out.print(temp_int.getValue() + " , ");
                        }
                    }

                    if(oTaintStack.size() > 0){
                        oLoadedStackByBlock.put(bbi.getoBasicBlock().getId(), oTaintStack);
                    }

                    oListProcessBB.remove(bbi);   // currBlock2 is child of the current node
                    BBProcessing = false;
                    oVarInfoMap.put(bbi.getoBasicBlock().getId(), oNodeVarList);
                    oTempVarInfoMap.put(bbi.getoBasicBlock().getId(), oTempNodeVarList);
                }
            }
        }
        catch (Exception ex)
        {
            ex.printStackTrace();
        }

        oGraph = doSecondIteration(oGraph);
        //oGraph = doSecondIteration(oGraph);

        return oGraph;
    }

    private int getTempNodeVarIndex(List<VarInfo> oTempNodeVarList, int index) {
        int ret_val = -1;

        try
        {
            for(int i = 0; i < oTempNodeVarList.size(); i++)
            {
                VarInfo vi = oTempNodeVarList.get(i);

                if(vi.getIndex() == index) {
                    ret_val = i;
                    break;
                }
            }
        }
        catch (Exception ex)
        {
            ex.printStackTrace();
        }

        return ret_val;
    }


    private TaintGraph doSecondIteration(TaintGraph oGraph)
    {


        int parent_id = -1;

        try
        {
            List<TaintNode> oList = new ArrayList<TaintNode>();
            oList.add(oGraph.getRoot());
            List<IntTriple> oListDone = new ArrayList<>();

            while(oList.size() > 0) {
                TaintNode oNodePair = oList.get(0);
                if(oNodePair != null) {

                    if(true) //!oListDone.contains(oNodePair.getBlockID()))
                    {
                        //System.out.println("Taint node id : " + oNodePair.getBlockID());


                        if(true) {

                            if (oNodePair.getChildren().size() > 0) {
                                boolean add_all_children = true;

                                int int_value = 0;
                                for (int i = 0; i < oNodePair.getChildren().size(); i++) {

                                    TaintNode taintNode = oNodePair.getChildren().get(i);

                                    boolean alreadyDone = false;
                                    for(int m = 0; m < oListDone.size(); m++)
                                    {
                                        IntTriple intPair = oListDone.get(m);
                                        if(intPair.getFirst() == oNodePair.getBlockID() && intPair.getSecond() == taintNode.getBlockID())
                                        {
                                            intPair.setThird(intPair.getThird() + 1);

                                            if(intPair.getThird() >= 3) {
                                                alreadyDone = true;
                                                break;
                                            }
                                        }
                                    }

                                    if(!alreadyDone) {

                                        List<VarInfo> nodeVarListParent = oNodePair.getNodeVarList();

                                        List<IntPair> intPairs = taintNode.getoListDefs();
                                        List<VarInfo> nodeVarList = taintNode.getNodeVarList();

                                        System.out.println("$$$$== Taint Transfer : ParentID : " + oNodePair.getBlockID() + " ChildID : " + taintNode.getBlockID());

                                        if (taintNode.getBlockID() == 34) {
                                            System.out.println("##-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-##");
                                            System.out.println("Size of intPairs : " + intPairs.size());

                                            VarInfo varInfo = nodeVarList.get(4);
                                            VarInfo varInfo1 = nodeVarListParent.get(4);

                                            varInfo.setTaintTypeIfNotExist(varInfo1.getTaintTypePrioritized());

                                            System.out.println("varInfo1.getTaintTypePrioritized() : " + varInfo1.getTaintTypePrioritized());

                                            for (IntPair intpair : intPairs) {
                                                System.out.println(" :: " + intpair.getFirst() + " : " + intpair.getSecond());
                                            }
                                        }

                                        for (int q = 0; q < nodeVarListParent.size(); q++) {


                                            //VarInfo varInfo;

                                            //if(q < nodeVarList.size())
                                            VarInfo varInfo = nodeVarList.get(q);

                                            VarInfo varInfo1 = nodeVarListParent.get(q);
                                            varInfo.setTaintTypeIfNotExist(varInfo1.getTaintTypePrioritized());
                                            nodeVarList.set(q, varInfo);

                                            //System.out.println("q is : " + varInfo1.getTaintTypePrioritized());
                                        }


                                        for (IntPair intpair : intPairs) {
                                            int first = intpair.getFirst();
                                            int second = intpair.getSecond();

                                            VarInfo defVar = nodeVarList.get(first);
                                            VarInfo useVar = nodeVarList.get(second);

                                            defVar.addTaintType(useVar.getTaintTypePrioritized()); // has to be previous block ???
                                            taintNode.setValueChangeLog(new ValueChangeLog(first, useVar.getTaintTypePrioritized()));
                                            nodeVarList.set(first, defVar);

                                            if (taintNode.getBlockID() == 34)
                                            {
                                                System.out.println("##-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-##");
                                                System.out.println("first : " + first + " :: second : " + second + " :: taint_Val :  " + useVar.getTaintTypePrioritized());
                                                System.out.println("Length of the value change log is  :  " + taintNode.getoListValueChangeLog().size());
                                                taintNode.setValueChangeLog2(new ValueChangeLog(first, useVar.getTaintTypePrioritized()));

                                            }

                                        }
                                        oList.add(taintNode);
                                    }
                                    oListDone.add(new IntTriple(oNodePair.getBlockID(), taintNode.getBlockID()));
                                }
                            }
                        }
                        else
                        {
                            System.out.println("Error Parent is null");
                        }
                    }
                }
                else if(oNodePair == null)
                {
                    System.out.println("Node is NULL");
                }
                oList.remove(oNodePair);
            }
        }
        catch(Exception ex)
        {
            System.out.println("============================");
            ex.printStackTrace();
            System.out.println("============================");
        }

        return oGraph;
    }

    private int getModifiedVarIndex(int ind, int pc) {
        int ret_index = -1;

        try
        {
            LocalVariable localVariableAtPC = getLocalVariableAtPC(this.localVarTable, ind, pc);
            if(localVariableAtPC != null) {
                // no checks because in the case of null ... an exception must rise so we know there is a problem
                String name = localVariableAtPC.getName();  // duplicate names will handle here
                for (VarInfo vi : this.oVarList) {
                    if (vi.getName().equals(name)) {
                        ret_index = vi.getIndex();
                        break;
                    }
                }
            }
            else
            {
                System.out.println("..:: Default variable used in getModifiedVarIndex");

                printPossibleVariableAtPC(this.localVarTable, ind, pc);
            }
        }
        catch(Exception ex)
        {
            ex.printStackTrace();
        }

        return ret_index;
    }

    private void printPossibleVariableAtPC(LocalVariableTable lvt, int index, int pc)
    {
        int length = lvt.getTableLength();
        LocalVariable[] lvs = lvt.getLocalVariableTable();

        for(int i = 0; i < length; i++) {
            if (true) {
                int startPC = lvs[i].getStartPC();
                //System.out.println(".:: The pc provided is : " + pc);
                //System.out.println(".:: The Start PC is : " + startPC);
                //System.out.println(".:: The END PC is : " + (startPC + lvs[i].getLength()));
                if ((pc >= startPC) && (pc < (startPC + lvs[i].getLength())))
                   System.out.println(":: " + lvs[i].getIndex());
            }
        }

    }

    private int getVarListIndex(List<VarInfo> oVarList, int ind) {

        for(int f = 0; f < oVarList.size(); f++)
        {
            VarInfo varInfo = oVarList.get(f);

            if(varInfo.getIndex() == ind)
                return f;
        }

        return -1;
    }

    public static LocalVariable getLocalVariableAtPC(LocalVariableTable lvt, int index, int pc) {
        int length = lvt.getTableLength();
        LocalVariable[] lvs = lvt.getLocalVariableTable();

        for(int i = 0; i < length; i++) {
            if (lvs[i].getIndex() == index) {
                int startPC = lvs[i].getStartPC();
                System.out.println(".:: The pc provided is : " + pc);
                System.out.println(".:: The Start PC is : " + startPC);
                System.out.println(".:: The END PC is : " + (startPC + lvs[i].getLength()));
                if ((pc >= startPC) && (pc < (startPC + lvs[i].getLength())))
                    return lvs[i];
            }
        }

        return null;
    }


    private boolean doesLoadedVarContainsIndex(List<LoadedVar> oLoadedVars, int index)
    {
        for(LoadedVar f : oLoadedVars)
        {
            if(f.getIntValue() == index)
                return true;
        }

        return false;
    }

    private boolean isIgnoreInstruction(Instruction ins)
    {
        boolean ret_val = false;

        if(ins instanceof INVOKEINTERFACE)
        {
            InvokeInstruction invoke_ins = (InvokeInstruction) ins;
            String str_method = getFullMethodName(invoke_ins, cpg);

            if(oListIgnoreMethods.contains(str_method))
                ret_val = true;
        }

        return ret_val;
    }

    private void initListProcessBB(CFG cfg, BasicBlock currBlock) {
        if (cfg.getNumOutgoingEdges(currBlock) > 0) {
            Iterator<Edge> iei = cfg.outgoingEdgeIterator(currBlock);


            while (iei.hasNext()) {

                Edge next = iei.next();
                BasicBlock currBlock2x = next.getTarget();
                if (!hashDoneChildren.containsKey(currBlock2x.getId())) //currBlock2x != null && currBlock.getId() < currBlock2x.getId()) {
                {
                    if(!listBBContainsBlock(currBlock2x.getId(), currBlock.getId()) && currBlock2x.getId() != 1) {
                        //System.out.println("ChildID : " + currBlock2x.getId() + " :: ParentID : " + currBlock2x.getId())
                        oListProcessBB.add(new BasicBlockInfo(currBlock2x, currBlock.getId(), false));
                        initListProcessBB(cfg, currBlock2x);
                    }
                }

            }


        }
    }

    private boolean listBBContainsBlock(int child_id, int parent_id)
    {
        boolean ret_val = false;

        for(int i = 0; i < oListProcessBB.size(); i++)
        {
            BasicBlockInfo basicBlockInfo = oListProcessBB.get(i);
            if(basicBlockInfo.getoBasicBlock().getId() == child_id && basicBlockInfo.getParentID() == parent_id)
                return true;
        }

        return ret_val;
    }


    private List<Integer> loadVarsFromParent(BasicBlock currBlock2) {

        BasicBlock.InstructionIterator jfk = currBlock2.instructionIterator();

        while (jfk.hasNext()) {
            InstructionHandle next = jfk.next();
            Instruction instruction = next.getInstruction();

            //System.out.println("== ins : " + instruction.toString());
        }

        return new ArrayList<>();

    }

    private boolean isTaintSinkInstruction(Instruction ins) {
        boolean ret_val = false;

        try {
            InvokeInstruction invoke_ins = (InvokeInstruction) ins;

            String str_method = getFullMethodName(invoke_ins, cpg);
            if (oListSinkInstructions.contains(str_method))
            {
                ret_val = true;
            }

        } catch (Exception ex) {

        }

        return ret_val;
    }

    private String getFullMethodName(InvokeInstruction invoke, ConstantPoolGen cpg) {
        return ClassName.toSlashedClassName(invoke.getReferenceType(cpg).toString())
                + "." + invoke.getMethodName(cpg) + invoke.getSignature(cpg);



    }

    private List<Integer> getTaintValueOfVarAtIndex(List<VarInfo> oNodeVarList, List<VarInfo> oTempNodeVarList, int ind, boolean look_in_secondary) throws FileNotFoundException {
        List<Integer> ret_val = null;
        String str = "";

        for (VarInfo var_ind : oNodeVarList)
        {
            /*
            if (ind == 0 || ind == 1 || ind == 2) {
                str += "-=-=-=-= var_ind : " + var_ind.getIndex() + " :: needed : " + ind + " :: var_ind.taintvalue : " + TaintTypes.typeToString(var_ind.getTaintType()) + " :: Last Value : " + TaintTypes.typeToString(var_ind.getLastValue());
            }

            PrintWriter out_vars = new PrintWriter("D:\\temp_output\\variables_" + ind + ".txt");
            out_vars.println(str);
            out_vars.close();
            */

            if (var_ind.getIndex() == ind)
            {
                ret_val = var_ind.getoListTaintType();
                //////if(ind == 3)
                    //////var_ind.print_all_taint_vals();
                // System.out.println("The last value of Taint is : " + var_ind.getLastValue());
                break;
            }
        }

        if(ret_val == null && look_in_secondary)
        {
            System.out.println("$$$$$$$$ ======= getTaintValueOfVarAtIndex ret_val is null looking for secondary variable");

            for (VarInfo var_ind : oTempNodeVarList)
            {
                if (var_ind.getIndex() == ind)
                {
                    ret_val = var_ind.getoListTaintType();
                    break;
                }
            }
        }
        else if(ret_val == null)
        {
            ret_val = new ArrayList<>();
        }

        return ret_val;
    }

    private int getIndexBlockAccessInfoById(int index) {
        int ret_val = -1;

        try {
            for (int i = 0; i < oListBlockAccessInfo.size(); i++) {
                BlockAccessInfo oBAI = oListBlockAccessInfo.get(i);

                if (oBAI.getBlockID() == index) {
                    ret_val = i;
                    break;
                }
            }
        } catch (Exception ex) {

        }

        return ret_val;
    }

    private int evaluateIfInstruction(BasicBlock currBlock2x) {

        int ret_val = -1;
        BlockVar oBlockVar = null;

        System.out.println("The Block size is : " + oListBlockVars.size());

        for (int i = 0; i < oListBlockVars.size(); i++) {
            BlockVar currBlock = oListBlockVars.get(i);

            //System.out.println("currBlock.getBlockID() : " + currBlock.getBlockID() + " :: " + "currBlock2x.getId() : " + currBlock2x.getId());

            if (currBlock.getBlockID() == currBlock2x.getId()) {
                oBlockVar = currBlock;
                break;
            }
        }


        try {
            int int_result = 0;
            if (oBlockVar != null) {
                BasicBlock.InstructionIterator jfk = currBlock2x.instructionIterator();

                Instruction ifInstruction = null;
                List<Instruction> oInstList = new ArrayList<>();

                Stack<Number> oStack = new Stack<>();


                while (jfk.hasNext()) {
                    InstructionHandle insHand = jfk.next();
                    Instruction instruction = insHand.getInstruction();

                    // no need for int store because it will be same
                    // int_store(oBlockVar.getInt_list() , instruction); // store if the integer is in place
                    //const_push_instruction(instruction);

                    if (is_instruction_can_usein_if(instruction)) {
                        oInstList.add(instruction);
                    } else if (is_if_instruction(instruction)) {
                        ifInstruction = instruction;
                    } else {
                        oInstList.clear();
                    }
                }

                ExpressionTree extree = new ExpressionTree();

                for (int u = 0; u < oInstList.size(); u++) {
                    Instruction ins = oInstList.get(u);

                    boolean num_set = false;
                    Number temp_val = -1;
                    String node_val = "";

                    if (ins instanceof ConstantPushInstruction) {
                        ConstantPushInstruction inv = (ConstantPushInstruction) ins;
                        temp_val = inv.getValue();
                        num_set = true;

                        if(u + 1 < oInstList.size())
                        {
                            Instruction next_ins = oInstList.get(u+1);
                            if(next_ins instanceof ISTORE)
                            {
                                num_set = false;
                            }
                        }
                    }

                    if (ins instanceof ILOAD) {
                        ILOAD inv = (ILOAD) ins;
                        int temp_ind = inv.getIndex(); // just for two indices right now

                        temp_val = get_int_val(oBlockVar.getInt_list(), temp_ind);
                        num_set = true;
                    }

                    if (ins instanceof ISUB) {
                        Number first = oStack.pop();
                        Number second = oStack.pop();

                        int_result = first.intValue() - second.intValue();
                        oStack.push(int_result);
                    }
                    else if (ins instanceof IADD) {

                        Number first = oStack.pop();
                        Number second = oStack.pop();

                        int_result = first.intValue() + second.intValue();
                        oStack.push(int_result);
                    }
                    else if (ins instanceof IMUL) {
                        Number first = oStack.pop();
                        Number second = oStack.pop();

                        int_result = first.intValue() * second.intValue();
                        oStack.push(int_result);
                    }
                    else if (ins instanceof IDIV) {
                        Number first = oStack.pop();
                        Number second = oStack.pop();

                        int_result = first.intValue() / second.intValue();
                        oStack.push(int_result);
                    }


                    if (num_set) {
                        num_set = false;
                        node_val = "" + temp_val;
                        oStack.push(temp_val);
                        System.out.println("|||||||||||||| pushed : " + temp_val);
                    }



                    //extree.addNode(node_val);
                }

                String str = "" + oStack.pop();

                if (ifInstruction instanceof IF_ICMPLE)
                    str += "<"; //extree.addNode("<");    // <
                else if (ifInstruction instanceof IF_ICMPGE)
                    str += ">"; //extree.addNode(">");    // >

                str += oStack.pop();

                ScriptEngineManager mgr = new ScriptEngineManager();
                ScriptEngine engine = mgr.getEngineByName("JavaScript");
                ////String foo = extree.get_expression(null, true);

                ///System.out.println(foo);
                System.out.println("The expression to evaluate is : " + str);


                String result = "";
                if(!str.contains("null"))
                    result = "" + engine.eval(str);

                System.out.println("====The result is : " + result);

                if (result.toUpperCase().equals("TRUE"))
                    ret_val = 1;
                else if (result.toUpperCase().equals("FALSE"))
                    ret_val = 0;
                else
                    ret_val = -1;

                System.out.println("ret_val : " + ret_val);
            } else {
                System.out.println("Error Block Var is null");
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        return ret_val;
    }

    private Number get_int_val(List<IntInfo> int_list, int temp_ind) {
        Number num = -1;

        for (int o = 0; o < int_list.size(); o++) {
            if (int_list.get(o).getIndex() == temp_ind) {
                num = int_list.get(o).getValue();
                break;
            }
        }

        return num;
    }

    private boolean is_instruction_can_usein_if(Instruction instruction) {
        boolean ret_val = false;

        if (instruction instanceof ILOAD || instruction instanceof ISTORE || instruction instanceof ISUB || instruction instanceof IADD || instruction instanceof IMUL || instruction instanceof IDIV || instruction instanceof SIPUSH || instruction instanceof BIPUSH) {
            ret_val = true;
        }

        return ret_val;
    }

    private boolean is_if_instruction(Instruction instruction) {
        boolean ret_val = false;

        if (instruction instanceof IfInstruction) {
            ret_val = true;
        }

        return ret_val;
    }

    private void const_push_instruction(Instruction ins) {
        if (ins instanceof ConstantPushInstruction) {
            ConstantPushInstruction inv = (ConstantPushInstruction) ins;

            //InjectionPoint p;


            constant_int_exist = true;
            val = inv.getValue();


        }
    }

    private void int_store(List<IntInfo> int_list, Instruction ins) {


        if (ins instanceof ISTORE) {
            if (!constant_int_exist)
                val = -1; // int has unknown val


            ISTORE inv = (ISTORE) ins;
            int ind = inv.getIndex();

            boolean contains_var = false;

            for (int ii = 0; ii < int_list.size(); ii++) {
                IntInfo currIntInfo = int_list.get(ii);
                if (currIntInfo.getIndex() == ind) {

                    contains_var = true;
                    // change the value
                    currIntInfo.setValue(val);
                    int_list.set(ii, currIntInfo);
                    break;
                }
            }
            if (!contains_var) {
                int_list.add(new IntInfo(ind, val));
            }
        }

        constant_int_exist = false;
        val = 0;
    }

    private int getListIndex(List<VarInfo> oNodeVarList, int ind) {
        int ret_val = -1;

        for (int i = 0; i < oNodeVarList.size(); i++) {
            VarInfo oInfo = oNodeVarList.get(i);
            if (oInfo.getIndex() == ind)
                return i;
        }

        return ret_val;
    }

    private TaintNode addTaintedData(TaintNode oRoot, List<VarInfo> oVarList) {

        for (int i = 0; i < oVarList.size(); i++) {
            VarInfo oInfo = oVarList.get(i);
            oInfo.addTaintToList();
        }

        oRoot.setoListTaintVarList(oVarList);

        return oRoot;
    }

    public boolean isTaintIntroducingInvokeInstruction(Instruction ins) {
        // temporary just do the little thingy


        return false;
    }

    public boolean isTaintKillingTaintInvokeInstruction(Instruction ins) {
        return false;
    }

    public boolean isVarConsumingInstuction(Instruction ins) {
        return false;
    }

    public boolean isStoreInstruction() {
        return false;
    }

    public List<BlockAccessInfo> getoListBlockAccessInfo() {
        return oListBlockAccessInfo;
    }

    public void setoListBlockAccessInfo(List<BlockAccessInfo> oListBlockAccessInfo) {
        this.oListBlockAccessInfo = oListBlockAccessInfo;
    }
}
