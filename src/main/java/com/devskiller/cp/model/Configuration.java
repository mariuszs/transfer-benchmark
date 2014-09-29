package com.devskiller.cp.model;

public class Configuration {

    private Configuration(Builder builder) {
        mode = builder.mode;
        numberOfAccounts = builder.numberOfAccounts;
        initialBalance = builder.initialBalance;
    }

    public MODE getMode() {
        return mode;
    }

    public int getNumberOfAccounts() {
        return numberOfAccounts;
    }

    public long getInitialBalance() {
        return initialBalance;
    }

    public enum MODE {UNSAFE, REFS, ATOMIC}

    private MODE mode;
    private int numberOfAccounts;
    private long initialBalance;


    public static class Builder {
        private MODE mode;
        private int numberOfAccounts;
        private long initialBalance;

        public Builder mode(MODE mode) {
            this.mode = mode;
            return this;
        }

        public Builder numberOfAccounts(int numberOfAccounts) {
            this.numberOfAccounts = numberOfAccounts;
            return this;
        }

        public Builder initialBalance(long initialBalance) {
            this.initialBalance = initialBalance;
            return this;
        }

        public Builder fromPrototype(Configuration prototype) {
            mode = prototype.mode;
            numberOfAccounts = prototype.numberOfAccounts;
            initialBalance = prototype.initialBalance;
            return this;
        }

        public Configuration build() {
            return new Configuration(this);
        }
    }
}
