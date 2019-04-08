package com.h3xstream.findsecbugs.common;

public class IntTriple {
    int first;
    int second;
    int third;

    public IntTriple() {
    }

    public IntTriple(int first, int second)
    {
        this.first = first;
        this.second = second;
    }

    public int getFirst() {
        return first;
    }

    public void setFirst(int first) {
        this.first = first;
    }

    public int getSecond() {
        return second;
    }

    public void setSecond(int second) {
        this.second = second;
    }

    public int getThird() {
        return third;
    }

    public void setThird(int third) {
        this.third = third;
    }
}
