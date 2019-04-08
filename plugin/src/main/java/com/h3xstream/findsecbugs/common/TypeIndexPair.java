package com.h3xstream.findsecbugs.common;

public class TypeIndexPair {

    int varType = VarTypes.INT_TYPE;
    int varIndex = -1;

    public TypeIndexPair(int varIndex) {
        this.varIndex = varIndex;
    }

    public TypeIndexPair(int varType, int varIndex) {
        this.varType = varType;
        this.varIndex = varIndex;
    }

    public int getVarType() {
        return varType;
    }

    public void setVarType(int varType) {
        this.varType = varType;
    }

    public int getVarIndex() {
        return varIndex;
    }

    public void setVarIndex(int varIndex) {
        this.varIndex = varIndex;
    }
}
