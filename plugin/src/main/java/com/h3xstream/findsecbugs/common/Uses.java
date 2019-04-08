package com.h3xstream.findsecbugs.common;

public class Uses {
    int Vartype = VarTypes.INT_TYPE;
    int VarIndex = -1;

    public Uses(int vartype, int varIndex) {
        Vartype = vartype;
        VarIndex = varIndex;
    }

    public Uses(int varIndex) {
        VarIndex = varIndex;
    }

    public int getVartype() {
        return Vartype;
    }

    public void setVartype(int vartype) {
        Vartype = vartype;
    }

    public String getVartypeString() {
        String ret_var = "";

        switch(Vartype) {
            case VarTypes.INT_TYPE:
                ret_var = "INT";
                break;
            case VarTypes.ARRAY_TYPE:
                ret_var = "ARRAY";
                break;
            case VarTypes.BOOLEAN_TYPE:
                ret_var = "BOOLEAN";
                break;
            case VarTypes.STRING_TYPE:
                ret_var = "STRING";
                break;
            default:
                ret_var = "UNKNOWN";
        }

        return ret_var;
    }

    public int getVarIndex() {
        return VarIndex;
    }

    public void setVarIndex(int varIndex) {
        VarIndex = varIndex;
    }

    @Override
    public String toString()
    {
        String ret_str = "TYPE : " + getVartypeString() + " :: " + "VAR_INDEX : " + getVarIndex();
        return ret_str;
    }
}
