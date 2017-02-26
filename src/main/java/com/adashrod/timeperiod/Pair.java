package com.adashrod.timeperiod;

/**
 * A parameterized object that contains two objects
 */
public class Pair <T1, T2> {
    private T1 first;
    private T2 second;

    public Pair(final T1 first, final T2 second) {
        this.first = first;
        this.second = second;
    }

    public T1 getFirst() {
        return first;
    }

    public void setFirst(final T1 first) {
        this.first = first;
    }

    public T2 getSecond() {
        return second;
    }

    public void setSecond(final T2 second) {
        this.second = second;
    }

    @Override
    public String toString() {
        return String.format("<%s, %s>", first.toString(), second.toString());
    }
}
