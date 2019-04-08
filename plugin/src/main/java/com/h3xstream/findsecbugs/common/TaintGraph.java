package com.h3xstream.findsecbugs.common;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by OP on 5/27/2018.
 */
public class TaintGraph {

    TaintNode root = null;
    List<TaintNode> unprocessedNodes = new ArrayList<>();
    TaintNode temp_node;
    String logs = "";


    public boolean isRootNull()
    {
        return root == null;
    }

    public boolean addNode(TaintNode newNode, int parentID)
    {
        boolean ret_val = true;

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
                TaintNode oTaintNode = getUnprocessedNodeById(parentID);
                TaintNode oNewExistingNode = getUnprocessedNodeById(newNode.getBlockID());
                if(oNewExistingNode != null && oTaintNode != null)
                {
                    oTaintNode.addChild(oNewExistingNode);
                }
                else if(oTaintNode != null)
                {
                    oTaintNode.addChild(newNode);
                    unprocessedNodes.add(newNode);
                }
                else
                {
                    logs += "Error : No parent with id " + parentID + " and Block ID :  " + newNode.getBlockID() +  " \n\r";
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


    private TaintNode getUnprocessedNodeById(int Id) {

        TaintNode ret_node = null;

        for(int i = 0; i < unprocessedNodes.size(); i++)
        {
            TaintNode oNode = unprocessedNodes.get(i);
            if(oNode.getBlockID() == Id)
            {
                ret_node = oNode;
                break;

            }
        }

        return ret_node;
    }

    public String toStringFromEdges()
    {
        String ret_str = "";
        ret_str = logs;

        ret_str += "------- starting -------\n\r";

        List<TaintNode> oList = new ArrayList<TaintNode>();
        oList.add(root);

        List<Integer> oListDone = new ArrayList<>();

        while(oList.size() > 0) {
            TaintNode oNode = oList.get(0);

            if(oNode != null) {

                List<TaintEdges> outgoingEdges = oNode.getOutgoingEdges();

                if(outgoingEdges.size() > 0)
                {
                    for(int i = 0; i < outgoingEdges.size(); i++)
                    {
                        TaintEdges oEdge = outgoingEdges.get(i);
                        TaintNode endNode = oEdge.getEndNode();
                        if(!oListDone.contains(endNode.getBlockID()))
                        {
                            oList.add(endNode);
                        }
                    }
                }

                /*if (oNode.getChildren().size() > 0) {
                    oList.addAll(oNode.getChildren());  // will be recursive
                }*/


                ret_str += oNode.toString();

                oListDone.add(oNode.getBlockID());
            }
            else if(oNode == null)
            {
                System.out.println("Node is NULL");
            }
            oList.remove(oNode);

        }

        ret_str += "------- ending -------\n\r";

        return ret_str;
    }

    @Override
    public String toString()
    {
        String ret_str = "";
        ret_str = logs;

        ret_str += "------- starting -------\n\r";

        List<TaintNode> oList = new ArrayList<TaintNode>();
        oList.add(root);

        List<Integer> oListDone = new ArrayList<>();

        while(oList.size() > 0) {
            TaintNode oNode = oList.get(0);

            if(oNode != null) {

                if(!oListDone.contains(oNode.getBlockID())) {
                    if (oNode.getChildren().size() > 0) {
                        for (TaintNode tempNode : oNode.getChildren()) {
                            if (!oListDone.contains(tempNode.getBlockID()))
                                oList.add(tempNode);
                            //oList.addAll(oNode.getChildren());  // will be recursive
                        }
                    }

                    ret_str += oNode.toString();
                }
                oListDone.add(oNode.getBlockID());
            }
            else if(oNode == null)
            {
                System.out.println("Node is NULL");
            }
            oList.remove(oNode);

        }

        ret_str += "------- ending -------\n\r";

        return ret_str;
    }

    public void addParentChildRelation(int parentID, int childID) {
        TaintNode parentNode = null;
        TaintNode childNode = null;

        for(int i = 0; i < unprocessedNodes.size(); i++)
        {
            TaintNode currNode = unprocessedNodes.get(i);

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

    public TaintNode getRoot() {
        return root;
    }

    public void setRoot(TaintNode root) {
        this.root = root;
    }


    public String toStringShort()
    {
        String ret_str = "";
        ret_str = logs;



        List<TaintNode> oList = new ArrayList<TaintNode>();
        oList.add(root);

        List<Integer> oListDone = new ArrayList<>();

        while(oList.size() > 0) {
            TaintNode oNode = oList.get(0);

            if(oNode != null) {

                if(!oListDone.contains(oNode.getBlockID()))
                {
                    if (oNode.getChildren().size() > 0) {
                        for (TaintNode tempNode : oNode.getChildren()) {
                            if (!oListDone.contains(tempNode.getBlockID()))
                                oList.add(tempNode);
                            //oList.addAll(oNode.getChildren());  // will be recursive
                        }
                    }

                    ret_str += oNode.toShortString();
                }
                oListDone.add(oNode.getBlockID());
            }
            else if(oNode == null)
            {
                System.out.println("Node is NULL");
            }
            oList.remove(oNode);

        }



        return ret_str;
    }

    public void removeChildFromParent(int tempParentId, int tempChildId)
    {
        List<TaintNode> oList = new ArrayList<TaintNode>();
        oList.add(root);

        List<Integer> oListDone = new ArrayList<>();

        while(oList.size() > 0) {
            TaintNode oNode = oList.get(0);

            if(oNode != null) {

                if(!oListDone.contains(oNode.getBlockID())) {
                    if (oNode.getChildren().size() > 0) {
                        for (TaintNode tempNode : oNode.getChildren()) {
                            if(tempParentId == oNode.getBlockID() && tempChildId == tempNode.getBlockID())
                            {
                                oNode.getChildren().remove(tempNode);
                            }
                            if (!oListDone.contains(tempNode.getBlockID()))
                                oList.add(tempNode);
                            //oList.addAll(oNode.getChildren());  // will be recursive
                        }
                    }
                }
                oListDone.add(oNode.getBlockID());
            }
            else if(oNode == null)
            {
                System.out.println("Node is NULL");
            }
            oList.remove(oNode);

        }
    }
}
