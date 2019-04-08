package com.h3xstream.findsecbugs.common;

public class TaintStateNamePair
{

    TaintNode taintNode;
    String stateName;
    String input_symbol = "nn"; // aa - always access, na - never access, n - normal access

    public TaintStateNamePair(TaintNode _node, String _stateName) {
        taintNode = _node;
        stateName = _stateName;
    }

    public TaintStateNamePair(TaintNode _node, String _stateName, String _input_symbol) {
        taintNode = _node;
        stateName = _stateName;
        input_symbol = _input_symbol;
    }

    public TaintNode getTaintNode() {
        return taintNode;
    }

    public void setTaintNode(TaintNode taintNode) {
        this.taintNode = taintNode;
    }

    public String getStateName() {
        return stateName;
    }

    public void setStateName(String stateName) {
        this.stateName = stateName;
    }

    public String getInput_symbol() {
        return input_symbol;
    }

    public void setInput_symbol(String input_symbol) {
        this.input_symbol = input_symbol;
    }
}
