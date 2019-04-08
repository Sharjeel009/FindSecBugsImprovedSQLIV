package com.h3xstream.findsecbugs.common;

import com.h3xstream.findsecbugs.common.matcher.TaintTypes;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

public class BDD
{
    BDDNode root = null;
    List<BDDNode> unprocessedNodes = new ArrayList<>();
    String logs = "";
    HashMap<Integer, BDDNode> unprocessedMap = new HashMap<>();
    int num_bits = 0;  // num_bits = 2 + 2 x num_vars
    int num_vars = 0;

    List<BDDUnprocessed> oListBDDUnprocessed = new ArrayList<>();

    public BDD(int num_vars)
    {
        this.num_vars = num_vars;
    }

    public boolean addNode(BDDNode newNode, int parentID)
    {

        boolean ret_val = true;

        //System.out.println("Adding Block  : " + newNode.getBlockID() + " to Parent : " + parentID);

        try
        {
            //logs += "root : " + root + "  \n\r";
            if(root == null)
            {
                root = newNode;
                unprocessedNodes.add(newNode);
                logs += "====root_added===== \n\r";
            }
            else
            {
                BDDNode oBDDNode = getUnprocessedNodeById(parentID);
                if(oBDDNode != null)
                {
                    oBDDNode.addChild(newNode);
                    unprocessedNodes.add(newNode);
                }
                else {

                    logs += "Error : No parent with id " + parentID + " \n\r";
                }
            }


        }
        catch(Exception ex)
        {
            logs += "====EXCEPTION IN DFG===== \n\r";
            ex.printStackTrace();
            ret_val = false;
        }
        return ret_val;
    }

    private BDDNode getUnprocessedNodeById(int Id) {

       /* TaintNode ret_node = null;

        for(int i = 0; i < unprocessedNodes.size(); i++)
        {
            TaintNode oNode = unprocessedNodes.get(i);
            if(oNode.getBlockID() == Id)
            {
                ret_node = oNode;
                break;

            }
        }

        return ret_node;*/

       return null;
    }

    public BDDNode addNodeFromTaintNode(TaintNode oNode, BDDNode bddParent)
    {
        //System.out.println("addNodeFromTaintNode");

        BDDNode oBDDNode = null;

        List<Integer> oListChangedVars = new ArrayList<>();

        try
        {
            boolean is_node_added = false; // check if there is no change log then add the ValueChangeLog

            BDDNode oNodeBDD = new BDDNode();

            for(ValueChangeLog vcl : oNode.getoListValueChangeLog())
            {
                int ind = vcl.getVar_index();

                if(oListChangedVars.contains(ind))
                {
                    Integer integer = oListChangedVars.get(ind);

                    /*
                    // means normal node
                    if(blockAccessInfo == -1)
                    {

                    }
                    */  // no need for acccess information as it is already accessed

                    // add the nodes with the changed values


                    is_node_added = true;

                    // first clear the list
                    oListChangedVars.clear();

                    // add the new ind


                    oBDDNode = addNode(oNodeBDD, bddParent);


                    oListChangedVars.add(ind);
                    oNodeBDD = new BDDNode();


                    // is normal node etcs

                }
                else
                {
                    oListChangedVars.add(ind);
                    oNodeBDD.addValueChangeLog(vcl);
                }

            }

            if(oListChangedVars.size() > 0)
            {

                oListChangedVars.clear();
                oBDDNode = addNode(oNodeBDD, bddParent);
                is_node_added = true;
            }
            if(is_node_added == false)  // node isn't added because it doesnt have any change in taint
            {
                oBDDNode = addNode(oNodeBDD, bddParent);
                oNodeBDD = new BDDNode();
            }

            // if(oListChangedVars)
        }
        catch(Exception ex)
        {
            ex.printStackTrace();
        }

        return oBDDNode;
    }

    private BDDNode addNode(BDDNode oNodeBDD, BDDNode parentNode)
    {


        BDDNode oBDDNode = null;

        try {
            if(parentNode != null)
                parentNode.addChild(oNodeBDD);
            else if(root == null)
                root = oNodeBDD;
            else
                System.out.println("-=-=-=-=-=-=-=========ERROR NO ROOT=============---------");

            oBDDNode = oNodeBDD;
            //if(no_error)

        }
        catch(Exception ex)
        {
            System.out.println("Exception :: in BDD : addNode");
            ex.printStackTrace();
        }


        return oBDDNode;
    }

    public boolean finishAdding()
    {
        boolean ret_val = true;

        try {
            while(oListBDDUnprocessed.size() > 0) {
                BDDUnprocessed oBDDUnprocessed = oListBDDUnprocessed.get(0);
                if(true)// || addNode(oBDDUnprocessed.getoNodeBDD(), oBDDUnprocessed.getBlockID(), oBDDUnprocessed.getParent_id(), false))
                    oListBDDUnprocessed.remove(oBDDUnprocessed);
            }
        }
        catch(Exception ex)
        {
            ex.printStackTrace();

            ret_val = false;
        }

        return ret_val;
    }

    public boolean anaylyzeBDD()
    {
        boolean ret_val = false;

        try
        {
            BDDNode root = this.root;
            List<ValueChangeLog> valueChangeLogs = root.getoListValueChangeLog();

            HashMap<Integer, Integer> oVarHashMap  = new HashMap<>();
            List<ValueChangeLog> tempValueChangeLogs = new ArrayList<>();

            for(ValueChangeLog vcl : valueChangeLogs)
            {
                oVarHashMap.put(vcl.getVar_index(), vcl.getTaint_value());
                tempValueChangeLogs.add(new ValueChangeLog(vcl.getVar_index(), vcl.getTaint_value()));
            }

            for(BDDNode child : root.getChildren())
            {
                if(analyzeNode(child, oVarHashMap, tempValueChangeLogs))
                {
                    return true;
                }
            }
        }
        catch(Exception ex)
        {
            ex.printStackTrace();
        }

        return ret_val;
    }

    private boolean analyzeNode(BDDNode child, HashMap<Integer, Integer> oVarHashMap, List<ValueChangeLog> tempValueChangeLogs)
    {

        System.out.println("-=-=-=-=-=-=-=-=-=-== analyzeNode");

        boolean ret_val = false;

        HashMap<Integer, Integer>  tempMap = new HashMap<>();



        Iterator it = oVarHashMap.entrySet().iterator();
        while (it.hasNext()) {
            HashMap.Entry pair = (HashMap.Entry)it.next();
            tempMap.put(Integer.parseInt(pair.getKey().toString()), Integer.parseInt(pair.getValue().toString()));
            it.remove(); // avoids a ConcurrentModificationException
        }


        List<ValueChangeLog> valueChangeLogs = child.getoListValueChangeLog();

        for(int i = 0; i < valueChangeLogs.size(); i++)   //ValueChangeLog vcl : valueChangeLogs
        {
            ValueChangeLog vcl = valueChangeLogs.get(i);
            System.out.println(" i = " + i + " :: total = " + valueChangeLogs.size());



            // if it is sink
            if(vcl.getTaint_value() == 100)
            {
                System.out.println("-=-=-=-=-=-=-=-=-=- q0 AM SINKING");
                System.out.println("-=-=-=-=-=-=-=-=-=- q0 AM VAR NUM : " + vcl.getVar_index());
                System.out.println("-=-=-=-=-=-=-=-=-=- child.getoListValueChangeLog() : " + child.getoListValueChangeLog().size());
                System.out.println("-=-=-=-=-=-=-=-=-=- valueChangeLogs : " + valueChangeLogs.size());

                int taint_val = -100;// tempMap.get(vcl.getVar_index());
                if(tempMap.containsKey(vcl.getVar_index()))
                    taint_val = tempMap.get(vcl.getVar_index());

                if(taint_val == TaintTypes.TAINTED)
                {
                    System.out.println("-=-=-=-=-=-=-=-=-=- q0 AM RETURNING");
                    return true;
                }

            }
            else
            {
                System.out.println("-----===in else===------");
                int taint_val = vcl.getTaint_value();
                System.out.println("-0-0-0-0-0-0-0-0-0-0-0-0-0-0-0-0-0-0-0-0-0-0-0-0-00 :: " + taint_val);
                tempMap.put(vcl.getVar_index(), taint_val);
            }
        }


        for(BDDNode newChild : child.getChildren())
        {
            if(analyzeNode(newChild, tempMap, tempValueChangeLogs))
            {
                return true;
            }
        }

        return ret_val;
    }

    @Override
    public String toString()
    {
        String str_val = "";


        List<BDDNode> oListUnprocessed = new
                ArrayList<>();
        oListUnprocessed.add(root);

        //System.out.println("root : " + root);
        //System.out.println("Number of children : " + root.getChildren().size());

        while(oListUnprocessed.size() > 0)
        {
            BDDNode bddNode = oListUnprocessed.get(0);
            if(bddNode != null) {
                System.out.println(bddNode.getChildren().size());
                str_val += bddNode.toString();
                oListUnprocessed.addAll(bddNode.getChildren());
            }
            oListUnprocessed.remove(bddNode);

        }

        /*for (Iterator<BDDNode> it = oListUnprocessed.iterator(); it.hasNext(); ) {
            BDDNode oNode = it.next();

            if(oNode != null) {
                str_val += oNode.toString();
                oListUnprocessed.addAll(oNode.getChildren());
            }

            it.remove();

        }*/

        return str_val;
    }


}
