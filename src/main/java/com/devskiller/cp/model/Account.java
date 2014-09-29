package com.devskiller.cp.model;

public class Account {

    private final Integer id;

    private Value balance;

    public Account(Integer id, Value initialBalance) {
        this.id = id;
        balance = initialBalance;
    }

    public boolean withdraw(final long amount) {
        if (balance() < amount) {
            return false;
        }

        balance.subtract(amount);
        return true;

    }

    public void deposit(final long amount) {
        balance.add(amount);
    }

    @Override
    public String toString() {
        return "Account{" +
                "id=" + id +
                ", balance=" + balance() +
                '}';
    }

    public long balance() {
        return balance.get();
    }

    public void set(long initialBalance) {
        if(balance() > initialBalance) {

        }
    }
}
