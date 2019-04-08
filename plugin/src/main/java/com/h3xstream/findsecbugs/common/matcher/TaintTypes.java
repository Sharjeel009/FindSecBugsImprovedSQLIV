package com.h3xstream.findsecbugs.common.matcher;

/**
 * Created by OP on 5/27/2018.
 */
public class TaintTypes {

    public static int TAINTED = 0;
    public static int UN_TAINTED = 1;
    public static int UNKNOWN = 2;
    public static int SINK = 100;

    public static String typeToString(int type)
    {
        String ret_val = "";

        if(type == TAINTED)
        {
            ret_val = "TAINTED";
        }
        else if(type == UN_TAINTED)
        {
            ret_val = "UN_TAINTED";
        }
        else if(type == SINK)
        {
            ret_val = "SINK";
        }
        else
            ret_val = "UNKNOWN";

        return ret_val;
    }
}
