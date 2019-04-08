package com.h3xstream.findsecbugs.common;

import com.h3xstream.findsecbugs.common.matcher.TaintTypes;

/**
 * Created by OP on 6/3/2018.
 */
public class ValueChangeLog {

    int var_index = -1;
    int taint_value = 100;

    public ValueChangeLog(int var_index, int taint_value)
    {
        this.var_index = var_index;
        this.taint_value = taint_value;
    }

    public int getVar_index() {
        return var_index;
    }

    public void setVar_index(int var_index) {
        this.var_index = var_index;
    }

    public int getTaint_value() {
        return taint_value;
    }

    public void setTaint_value(int taint_value) {
        this.taint_value = taint_value;
    }


}
