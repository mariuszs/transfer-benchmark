package com.devskiller.cp;

public class Transfer {

    private long amount;
    private int from;
    private int to;

    public Transfer(long amount, int from, int to) {
        this.amount = amount;
        this.from = from;
        this.to = to;
    }

    public long getAmount() {
        return amount;
    }

    public int getFrom() {
        return from;
    }

    public int getTo() {
        return to;
    }
}
