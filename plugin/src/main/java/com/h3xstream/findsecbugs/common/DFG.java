package com.h3xstream.findsecbugs.common;

import org.apache.bcel.generic.Instruction;

import java.util.ArrayList;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.ArrayBlockingQueue;

public class DFG {

    DFGNode root = null;
    List<DFGNode> unprocessedNodes = new ArrayList<>();
    DFGNode temp_node;
    String logs = "";

    public boolean isRootNull()
    {
        return root == null;
    }

    public boolean addNode(DFGNode newNode, int parentID)
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
                logs += "===== root_added ===== \n\r";
            }
            else
            {
                DFGNode oDFGNode = getUnprocessedNodeById(parentID);
                if(oDFGNode != null)
                {
                    oDFGNode.addChild(newNode);
                    unprocessedNodes.add(newNode);
                }
                else {

                    logs += "Error : No parent with id " + parentID + " \n\r";
                }
            }

            
        }
        catch(Exception ex)
        {
            logs += "==== EXCEPTION IN DFG ===== \n\r";
            ex.printStackTrace();
            ret_val = false;
        }
        return ret_val;
    }


    private DFGNode getUnprocessedNodeById(int Id)
    {

        DFGNode ret_node = null;

        for(int i = 0; i < unprocessedNodes.size(); i++)
        {
            DFGNode oNode = unprocessedNodes.get(i);
            if(oNode.getBlockID() == Id)
            {
                ret_node = oNode;
                break;
            }
        }

        return ret_node;
    }


    @Override
    public String toString()
    {
        String ret_str = "";
        ret_str = logs;

        ret_str += "------- starting -------\n\r";

        List<DFGNode> oList = new ArrayList<DFGNode>();
        oList.add(root);

        List<Integer> oListDone = new ArrayList<>();

        while(oList.size() > 0) {

            DFGNode oNode = oList.get(0);

            if (oNode != null) {
                if(!oListDone.contains(oNode.getBlockID())) {


                        if (oNode.getChildren().size() > 0) {
                            oList.addAll(oNode.getChildren());  // will be recursive
                            for (int i = 0; i < oList.size(); i++) {
                                DFGNode dfgNode = oList.get(i);
                                if (oListDone.contains(dfgNode.getBlockID()))
                                    oList.remove(i);
                            }


                        ret_str += oNode.toString();

                        oListDone.add(oNode.getBlockID());
                    }
                    else if (oNode == null)
                    {
                        System.out.println("Node is NULL");
                    }
                }
            }
            oList.remove(oNode);
        }

        ret_str += "------- ending -------\n\r";

        return ret_str;
    }

    public void addParentChildRelation(int parentID, int childID)
    {

        DFGNode parentNode = null;
        DFGNode childNode = null;

        for(int i = 0; i < unprocessedNodes.size(); i++)
        {
            DFGNode currNode = unprocessedNodes.get(i);
            if(currNode.getBlockID() == parentID)
                parentNode = currNode;
            else if(currNode.getBlockID() == childID)
            {
                childNode = currNode;
            }
        }

        if(parentNode != null && childNode != null)
        {
            parentNode.addChild(childNode);
        }
        else
        {
            logs += " Error : Parent or child node is null in  addParentChildRelation \n\r";
            System.out.println(" Error : Parent or child node is null in  addParentChildRelation");
        }
    }

    public void setoListBlockVars(List<BlockVar> oListBlockVars)
    {
        oListBlockVars.clear();
        oListBlockVars.addAll(oListBlockVars);
    }
}
