package com.devskiller.cp;

import clojure.lang.LockingTransaction;
import com.devskiller.cp.model.*;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

public class AccountService {

    private Configuration.MODE mode;
    private int numberOfAccounts;
    private long initialBalance;

    private Map<Integer, Account> accounts = new HashMap<>();
    private AtomicLong transactionCount = new AtomicLong(0);
    private AtomicLong invalidTransactionCount = new AtomicLong(0);

    public AccountService(final Configuration configuration) {
        mode = configuration.getMode();
        numberOfAccounts = configuration.getNumberOfAccounts();
        initialBalance = configuration.getInitialBalance();
        fill(mode);
    }

    public void setMode(Configuration.MODE mode) {
        this.mode = mode;
        fill(mode);
    }

    private void fill(Configuration.MODE mode) {

        for (int i = 0; i < numberOfAccounts; i++) {

            Value balance = null;
            switch (mode) {
                case REFS:
                    balance = new SafeValue(initialBalance);
                    break;
                case UNSAFE:
                    balance = new UnsafeValue(initialBalance);
                    break;
                case ATOMIC:
                    balance = new AtomicValue(initialBalance);
                    break;
            }
            accounts.put(i, new Account(i, balance));
        }
    }

    public void transfer(Transfer transfer)  {

        Account accountFrom = get(transfer.getFrom());
        Account accountTo = get(transfer.getTo());

        if (mode.equals(Configuration.MODE.REFS)) {
            try {
                LockingTransaction.runInTransaction(
                        () -> {

                            accountFrom.withdraw(transfer.getAmount());

                            TimeUnit.MILLISECONDS.sleep(1);

                            accountTo.deposit(transfer.getAmount());

                            return null;
                        });
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            final boolean withdrawed = accountFrom.withdraw(transfer.getAmount());

            if (!withdrawed) {
                failed();
                return;
            }
            try {
                TimeUnit.MILLISECONDS.sleep(1);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            accountTo.deposit(transfer.getAmount());
        }
        transactionCount.incrementAndGet();

    }

    private void failed() {
        invalidTransactionCount.incrementAndGet();
    }

    private Account get(int accountNumber) {
        return accounts.get(accountNumber);
    }

    public Map<Integer, Account> balances() {
        return Collections.unmodifiableMap(accounts);
    }

    public long getTransactionCount() {
        return transactionCount.get();
    }

    public long getInvalidTransactionCount() {
        return invalidTransactionCount.get();
    }

}
