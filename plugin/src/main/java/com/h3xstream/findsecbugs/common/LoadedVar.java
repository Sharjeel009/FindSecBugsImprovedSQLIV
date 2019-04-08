package com.h3xstream.findsecbugs.common;

public class LoadedVar
{
    // can be a string value in case of string constant or a
    public String Value = "";
    public LoadedVarType Type = LoadedVarType.VARIABLE;



    public int getIntValue() {

        try {

            return Integer.parseInt(Value);
        }
        catch(Exception ex)
        {

        }
        return -1;
    }

    public static enum LoadedVarType
    {
        CONSTANT,
        CONSTANT_STRING,
        VARIABLE;
    }

    public LoadedVar() {
    }

    public LoadedVar(String value) {
        Value = value;
    }

    public LoadedVar(String value, LoadedVarType type) {
        Value = value;
        Type = type;
    }

    public String getValue() {
        return Value;
    }

    public void setValue(String value) {
        Value = value;
    }

    public LoadedVarType getType() {
        return Type;
    }

    public void setType(LoadedVarType type) {
        Type = type;
    }
}
