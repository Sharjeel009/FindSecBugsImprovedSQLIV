package com.h3xstream.findsecbugs.common;

public class VarAttr {

    String attrName;
    String attrVal;
    String returnType;

    public VarAttr() {
    }

    public VarAttr(String attrName, String attrVal, String returnType) {
        this.attrName = attrName;
        this.attrVal = attrVal;
        this.returnType = returnType;
    }

    public String getAttrName() {
        return attrName;
    }

    public void setAttrName(String attrName) {
        this.attrName = attrName;
    }

    public String getAttrVal() {
        return attrVal;
    }

    public void setAttrVal(String attrVal) {
        this.attrVal = attrVal;
    }

    public String getReturnType() {
        return returnType;
    }

    public void setReturnType(String returnType) {
        this.returnType = returnType;
    }
}
