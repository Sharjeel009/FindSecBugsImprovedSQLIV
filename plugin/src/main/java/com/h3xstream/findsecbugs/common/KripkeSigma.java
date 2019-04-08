package com.h3xstream.findsecbugs.common;

public class KripkeSigma
{
    String state;
    String input_symbol;
    String next_state;

    public KripkeSigma() {
    }

    public KripkeSigma(String state, String input_symbol, String next_state) {
        this.state = state;
        this.input_symbol = input_symbol;
        this.next_state = next_state;
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

    public String getInput_symbol() {
        return input_symbol;
    }

    public void setInput_symbol(String input_symbol) {
        this.input_symbol = input_symbol;
    }

    public String getNext_state() {
        return next_state;
    }

    public void setNext_state(String next_state) {
        this.next_state = next_state;
    }
}
