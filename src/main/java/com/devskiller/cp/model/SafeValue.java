package com.devskiller.cp.model;

import clojure.lang.Ref;

public class SafeValue implements Value {

    private Ref balance;

    public SafeValue(long initialBalance) {
        this.balance = new Ref(initialBalance);
    }

    @Override
    public long get() {
        return (Long) balance.deref();
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
