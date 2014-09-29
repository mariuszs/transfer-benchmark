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
    private final long initialBalance;

    private Map<Integer, Account> accounts;
    private AtomicLong transactionCount = new AtomicLong(0);
    private AtomicLong invalidTransactionCount = new AtomicLong(0);

    public AccountService(final Configuration configuration) {
        mode = configuration.getMode();
        int numberOfAccounts = configuration.getNumberOfAccounts();
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
        switch (mode) {
            case REFS:
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
                break;
            case ATOMIC:
                if (transfer(transfer, accountFrom, accountTo)) return;
                break;
            case UNSAFE:

                synchronized (this) {
                    if (transfer(transfer, accountFrom, accountTo)) return;
                }
                break;

        }
        transactionCount.incrementAndGet();

    }

    private boolean transfer(Transfer transfer, Account accountFrom, Account accountTo) {
        final boolean withdrawed = accountFrom.withdraw(transfer.getAmount());

        if (!withdrawed) {
            failed();
            return true;
        }
        try {
            TimeUnit.MILLISECONDS.sleep(1);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        accountTo.deposit(transfer.getAmount());
        return false;
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
