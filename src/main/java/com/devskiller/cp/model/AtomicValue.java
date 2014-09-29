package com.devskiller.cp.model;

import java.util.concurrent.atomic.AtomicLong;

public class AtomicValue implements Value {

    private AtomicLong balance;

    public AtomicValue(long initialBalance) {
        this.balance = new AtomicLong(initialBalance);
    }

    @Override
    public long get() {
        return balance.get();
    }

    @Override
    public void subtract(long amount) {
        balance.set(get() - amount);
    }

    @Override
    public void add(long amount) {
        balance.set(get() + amount);
    }
}
