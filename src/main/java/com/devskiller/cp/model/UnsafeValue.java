package com.devskiller.cp.model;

public class UnsafeValue implements Value {
    private long balance;

    public UnsafeValue(long balance) {
        this.balance = balance;
    }

    @Override
    public long get() {
        return balance;
    }

    @Override
    public void subtract(long amount) {
        balance -= amount;
    }

    @Override
    public void add(long amount) {
        balance += amount;
    }
}
