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
    private final int numberOfAccounts;
    private final long initialBalance;

    private Map<Integer, Account> accounts;
    private AtomicLong transactionCount = new AtomicLong(0);
    private AtomicLong invalidTransactionCount = new AtomicLong(0);

    public AccountService(final Configuration configuration) {
        mode = configuration.getMode();
        numberOfAccounts = configuration.getNumberOfAccounts();
        initialBalance = configuration.getInitialBalance();

        accounts = new HashMap<>(numberOfAccounts);

        for (int i = 0; i < numberOfAccounts; i++) {

            accounts.put(i, new Account(i, balance()));
        }
    }

    private Value balance() {
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
        return balance;
    }

    public void transfer(Transfer transfer) {

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

            synchronized (this) {
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
