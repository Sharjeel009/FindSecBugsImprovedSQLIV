package com.h3xstream.findsecbugs.common;

public class ExpressionTree {
    Node root = null;
    boolean is_root_val_set = false;

    //add node type
    public void addNode(String val)
    {
        if(root == null)
        {
            root = new Node(val);
        }
        else if(is_root_val_set)
        {
            root.setValue(val);
            is_root_val_set = false;
        }
        else
        {
            is_root_val_set = true;
            Node temp_root = new Node("");
            temp_root.setLeft(root);
            temp_root.setRight(new Node(val));
            root = temp_root;
        }
    }

    public String get_expression(Node t, boolean starting)
    {
        String ret_val = "";

        if(starting)
        {
            t = this.root;
        }

        if (t != null) {
            String str = get_expression(t.getLeft(), false);
            if(str != null)
            ret_val += str;
            ret_val += t.getValue();
            str = get_expression(t.getRight(), false);
            if(str != null)
            ret_val += str;
        }

        return ret_val;
    }
}
