package com.h3xstream.findsecbugs.common;

public class Def {

    public int VarIndex = -1;
    public Number VarValue = 0;    // make separate class when moving from the int

    public Def(int varIndex, Number varValue) {
        VarIndex = varIndex;
        VarValue = varValue;
    }

    public int getVarIndex() {
        return VarIndex;
    }

    public void setVarIndex(int varIndex) {
        VarIndex = varIndex;
    }

    public Number getVarValue() {
        return VarValue;
    }

    public void setVarValue(Number varValue) {
        VarValue = varValue;
    }

    @Override
    public String toString()
    {

        String ret_str = "VAR_INDEX : " + getVarIndex() + " :: " + "VAR_VALUE : " + getVarValue();


        return ret_str;
    }
}
