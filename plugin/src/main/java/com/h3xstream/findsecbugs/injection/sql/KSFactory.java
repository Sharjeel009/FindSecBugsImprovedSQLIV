package com.h3xstream.findsecbugs.injection.sql;

import com.h3xstream.findsecbugs.common.*;
import com.h3xstream.findsecbugs.common.matcher.TaintTypes;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class KSFactory
{

    List<BlockAccessInfo> oBAI = new ArrayList<>();

    Kripke kripke = new Kripke();
    HashMap<Integer, Integer> oHashBlockIDStateID = new HashMap<>();
    List<VarInfo> oVarList = null;
    int numVar = -1;
    boolean isReturningClass;

    public Kripke createKripke(TaintGraph oGraph, List<VarInfo> oVarList, List<BlockAccessInfo> blockAccessInfos, boolean isReturningClass, int numParameters)
    {
        this.isReturningClass = isReturningClass;

        this.kripke = new Kripke();
        this.oBAI = blockAccessInfos;
        this.oVarList = oVarList;
        setSigma();

        numVar = oVarList.size();

        try
        {
            if(isReturningClass)
            {
                List<Integer> oZeroLList = new ArrayList<>();
                oZeroLList.add(0);
                oZeroLList.add(0);
                for(int i = 0; i < numVar; i++)
                {
                    oZeroLList.add(0);
                    oZeroLList.add(0);
                }

                List<Integer> oLList = new ArrayList<>();
                oLList.add(0);
                oLList.add(0);
                for(int i = 0; i < numVar; i++)
                {
                    oLList.add(0);
                    oLList.add(0);
                }

                addKripkeState("initS", null, new HashMap<>(),"00", "", oLList);
                addKripkeState("initSLast", "initS", new HashMap<>(),"00", "nn", oZeroLList);

                // 0 the parameter is this which will never be tainted
                for(int i = 1; i < numParameters; i++)
                {
                    oLList = new ArrayList<>();
                    oLList.add(0);
                    oLList.add(0);
                    for(int m = 0; m < numVar; m++)
                    {
                        oLList.add(0);
                        oLList.add(0);
                    }
                    if(i == 1) {
                        addKripkeState("initS" + i, "initS", new HashMap<>(), "00", "nn", oLList);
                        addKripkeState("initSLast", "initS" + i, new HashMap<>(), "00", "nn", oZeroLList);
                    }

                    if(i != 1)//for(int j = i; j > 0; j--)
                    {
                        oLList = new ArrayList<>();
                        oLList.add(0);
                        oLList.add(0);
                        for(int m = 0; m < numVar; m++)
                        {
                            oLList.add(0);

                            if(m <= i && m != 0)
                                oLList.add(1);
                            else
                                oLList.add(0);
                        }

                        List<Integer> oLList2 = new ArrayList<>();
                        oLList2.add(0);
                        oLList2.add(0);
                        for(int m = 0; m < numVar; m++)
                        {
                            oLList2.add(0);

                            if(m == i && m != 0)
                                oLList2.add(1);
                            else
                                oLList2.add(0);
                        }


                        // tbc need to improve
                        addKripkeState("initS" + i, "initS" + (i-1), new HashMap<>(), "00", "t" + i , oLList2);

                        addKripkeState("initS" + i + "t", "initS" + (i-1) + "t", new HashMap<>(), "00", "t" + i , oLList);
                        addKripkeState("initSLast", "initS" + i + "t", new HashMap<>(),"00", "nn", oZeroLList);

                    }

                    else
                    {
                        oLList = new ArrayList<>();
                        oLList.add(0);
                        oLList.add(0);
                        for(int m = 0; m < numVar; m++)
                        {
                            oLList.add(0);

                            if(m == i && m != 0)
                                oLList.add(1);
                            else
                                oLList.add(0);
                        }

                        addKripkeState("initS" + i + "t", "initS" + i, new HashMap<>(), "00", "t" + i, oLList);
                        addKripkeState("initSLast", "initS" + i + "t", new HashMap<>(),"00", "nn", oZeroLList);

                    }
                }
            }


            List<TaintStateNamePair> oList = new ArrayList<TaintStateNamePair>();
            oList.add(new TaintStateNamePair(oGraph.getRoot(), null));
            List<Integer> oListDone = new ArrayList<>();

            while(oList.size() > 0) {
                TaintStateNamePair oNodePair = oList.get(0);
                if(oNodePair.getTaintNode() != null) {

                    if(true) //!oListDone.contains(oNodePair.getBlockID()))
                    {
                        System.out.println("Taint node id : " + oNodePair.getTaintNode().getBlockID());
                        String str = getBlockAccessInfoStr(blockAccessInfos, oNodePair.getTaintNode().getBlockID());
                        int bai = getBlockAccessInfo(blockAccessInfos, oNodePair.getTaintNode().getBlockID());
                        String oParent = addStateFromBlock(oNodePair.getTaintNode(), "" + oNodePair.getTaintNode().getBlockID(), oNodePair.getStateName(), str, oNodePair.getInput_symbol());   // todo : temp parent_id is 0

                        if(bai == 1)    // always access .. means that others will have to sss
                        {

                        }

                        if(oParent != null) {

                            if (oNodePair.getTaintNode().getChildren().size() > 0) {
                                boolean add_all_children = true;
                                TaintNode aa_node = null;

                                for (TaintNode tempNode : oNodePair.getTaintNode().getChildren())
                                {
                                    int child_bai = getBlockAccessInfo(blockAccessInfos, tempNode.getBlockID());
                                    if(child_bai == 1)
                                    {
                                        add_all_children = false;
                                        aa_node = tempNode;
                                        break;
                                    }
                                }


                                int int_value = 0;
                                for (int i = 0; i < oNodePair.getTaintNode().getChildren().size(); i++)
                                {
                                    TaintNode tempNode = oNodePair.getTaintNode().getChildren().get(i);
                                    String input_state = oNodePair.getInput_symbol() + i;


                                    this.kripke.addInputSymbol(input_state);

                                    if (!oListDone.contains(tempNode.getBlockID()))
                                    {
                                        String child_input_symbol = "nn";
                                        if(!add_all_children)
                                        {
                                            int child_bai = getBlockAccessInfo(blockAccessInfos, tempNode.getBlockID());
                                            if(child_bai == 1)
                                                child_input_symbol = "aa";
                                            else
                                                child_input_symbol  = "na";
                                        }

                                        else
                                        {
                                            int child_bai = getBlockAccessInfo(blockAccessInfos, tempNode.getBlockID());
                                            if(child_bai == 1)
                                                child_input_symbol = "aa";
                                            else if(child_bai == 0)
                                                child_input_symbol = "na";
                                        }

                                        oList.add(new TaintStateNamePair(tempNode, oParent, child_input_symbol));
                                        int_value++;

                                        this.kripke.addInputSymbol(child_input_symbol);
                                    }
                                    else
                                    {
                                        // careful with the tempNode.getBlockID, as if naming conventions in the Kripke are changed it wont work
                                        this.kripke.addSigma(new KripkeSigma(oParent, input_state , "" + tempNode.getBlockID()));
                                    }
                                }
                            }
                        }
                        else
                        {
                            System.out.println("Error Parent is null");
                        }
                    }
                    oListDone.add(oNodePair.getTaintNode().getBlockID());
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

        return this.kripke;
    }



    private String getBlockAccessInfoStr(List<BlockAccessInfo> blockAccessInfos, int blockID)
    {
        String ret_val = "";
        int bai = getBlockAccessInfo(blockAccessInfos, blockID);
        if(bai == 1) {
            ret_val = "01";
        }
        else if(bai == 0)
        {
            ret_val = "10";
        }
        else
        {
            ret_val = "00";
        }

        return ret_val;
    }

    private String addStateFromBlock(TaintNode taintNode, String name, String stateName, String bai, String input_symbol)
    {
        String parentStateName = "";

        List<Integer> oListChangedVars = new ArrayList<>();
        HashMap<Integer, Integer> oMap = new HashMap<>();

        try
        {
            boolean is_node_added = false; // check if there is no change log then add the ValueChangeLog

            for(ValueChangeLog vcl : taintNode.getoListValueChangeLog())
            {
                int ind = vcl.getVar_index();
                if(oListChangedVars.contains(ind))
                {
                    is_node_added = true;

                    oListChangedVars.clear();
                    parentStateName = addKripkeState(name, stateName, oMap, bai, input_symbol, null);


                    oListChangedVars.add(ind);
                    oMap = new HashMap<>();
                    name += "a";

                    oMap.put(vcl.getVar_index(), vcl.getTaint_value());
                }
                else
                {
                    oListChangedVars.add(ind);
                    oMap.put(vcl.getVar_index(), vcl.getTaint_value());
                }

            }

            if(oListChangedVars.size() > 0)
            {

                oListChangedVars.clear();
                parentStateName = addKripkeState(name, stateName, oMap, bai, input_symbol, null);
                is_node_added = true;
            }
            if(is_node_added == false)  // node isn't added because it doesnt have any change in taint
            {
                parentStateName = addKripkeState(name, stateName, oMap, bai, input_symbol, null);
                oMap = new HashMap<>();
                name += "a";
            }

            // if(oListChangedVars)
        }
        catch(Exception ex)
        {
            ex.printStackTrace();
        }
        return parentStateName;
    }

    private void setSigma()
    {

    }

    private String addKripkeState(String child, String parent, HashMap<Integer, Integer> oMap, String bai, String input_symbol, List<Integer> oProvidedList)
    {

        List<Integer> oList = new ArrayList<>();

        if(oProvidedList != null) {
            for(int currInt : oProvidedList)
                oList.add(currInt);
        }
        else
        {
            oList = makeOutputString(oMap, bai);
        }

        kripke.addS(child);
        kripke.addL(child, oList);

        if(parent == null)
        {
            if(isReturningClass)
            {
                if(oProvidedList == null)
                {
                    kripke.addSigma(new KripkeSigma("initSLast", "nn", child));
                }
                else
                {

                    kripke.setI(child);
                }
            }
            else {

                kripke.setI(child);
            }
        }
        else
        {
            // the relationship is from parent to child
            kripke.addSigma(new KripkeSigma(parent, input_symbol, child));
        }

        return child;
    }

    private List<Integer> makeOutputString(HashMap<Integer,Integer> oMap, String bai) {

        List<Integer> oList = new ArrayList<>();
        if(bai == "00")
        {
            oList.add(0);
            oList.add(0);
        }
        else if(bai == "01")
        {
            oList.add(0);
            oList.add(1);
        }
        else if(bai == "10")
        {
            oList.add(1);
            oList.add(0);
        }
        else
        {
            oList.add(1);
            oList.add(1);
        }

        for(VarInfo varInfo : oVarList)
        {
            int first_bit = 0;
            int second_bit = 0;

            if(oMap.containsKey(varInfo.getIndex()))
            {
                Integer int_taint = oMap.get(varInfo.getIndex());
                if(int_taint == TaintTypes.TAINTED)
                {
                    first_bit = 0;
                    second_bit = 1;
                }
                else if(int_taint == TaintTypes.UN_TAINTED)
                {
                    first_bit = 1;
                    second_bit = 0;
                }
                else if(int_taint == TaintTypes.SINK)
                {
                    first_bit = 1;
                    second_bit = 1;
                }
            }
            // else first and second

            oList.add(first_bit);
            oList.add(second_bit);
        }

        // loop through the variables and
        return oList;
    }


    private ArrayList<Integer> getNodeBits(TaintNode oNode, int blockAccessInfo)
    {
        ArrayList<Integer> oList = new ArrayList<>();

        try
        {

        }
        catch (Exception ex)
        {
            ex.printStackTrace();
        }

        return oList;
    }

    public BDD createBDD(TaintGraph oGraph, List<VarInfo> oVarList, List<BlockAccessInfo> blockAccessInfos)
    {
        BDD oBDD = new BDD(oVarList.size());
        int parent_id = -1;

        try
        {
            List<TaintBDDNodePair> oList = new ArrayList<TaintBDDNodePair>();
            oList.add(new TaintBDDNodePair(oGraph.getRoot(), null));
            List<Integer> oListDone = new ArrayList<>();

            while(oList.size() > 0) {
                TaintBDDNodePair oNodePair = oList.get(0);
                if(oNodePair.getoTaintNode() != null) {

                    if(true)
                    {
                        BDDNode oParent = oBDD.addNodeFromTaintNode(oNodePair.getoTaintNode(), oNodePair.getoBDDNode());   // todo : temp parent_id is 0

                        if(oParent != null) {

                            if (oNodePair.getoTaintNode().getChildren().size() > 0) {
                                boolean add_all_children = true;
                                for (TaintNode tempNode : oNodePair.getoTaintNode().getChildren()) {

                                    int ret_val = getBlockAccessInfo(blockAccessInfos, tempNode.getBlockID());
                                    if (ret_val == 1 && add_all_children)    // 1 means always access the block
                                    {
                                        add_all_children = false;
                                        oList.add(new TaintBDDNodePair(tempNode, oParent));
                                    }
                                    else if (ret_val == 1) {
                                        System.out.println("Error : duplicate nodes with always access block acess in KSFactory.java");
                                    }

                                }

                                if (add_all_children) {
                                    for (TaintNode tempNode : oNodePair.getoTaintNode().getChildren()) {
                                        if (!oListDone.contains(tempNode.getBlockID())) {
                                            System.out.println("-=-=-=-=-=-=-=-=-=-=-=-=-=-= Adding Block with ID : " + tempNode.getBlockID());
                                            if (getBlockAccessInfo(blockAccessInfos, tempNode.getBlockID()) != 0)  // 0 means never access the block
                                            {
                                                oList.add(new TaintBDDNodePair(tempNode, oParent));
                                            }
                                            else
                                                System.out.println("\n\r getBlockAccessInfo(blockAccessInfos, tempNode.getBlockID()) == 0");
                                        }
                                        else
                                        {
                                            System.out.println("Block ID Ignored : " + tempNode.getBlockID());
                                        }
                                    }
                                }
                            }
                        }
                        else
                        {
                            System.out.println("Error Parent is null");
                        }

                    }
                    oListDone.add(oNodePair.getoTaintNode().getBlockID());
                }
                else if(oNodePair == null)
                {
                    System.out.println("Node is NULL");
                }
                oList.remove(oNodePair);

            }

            oBDD.finishAdding();
        }
        catch(Exception ex)
        {
            System.out.println("============================ Exception Occured ============================");
            ex.printStackTrace();
            System.out.println("===========================================================================");
        }

        return oBDD;
    }



    private int getBlockAccessInfo(List<BlockAccessInfo> blockAccessInfos, int blockID) {

        int ret_val = -1;

        try
        {
            for(BlockAccessInfo blockAccessInfo : blockAccessInfos)
            {
                if(blockAccessInfo.getBlockID() == blockID)
                    return blockAccessInfo.getAccessInfo();
            }
        }
        catch(Exception ex)
        {
            System.out.println("============================ Exception Occured ============================");
            ex.printStackTrace();
            System.out.println("===========================================================================");
        }
        return ret_val;

    }
}
