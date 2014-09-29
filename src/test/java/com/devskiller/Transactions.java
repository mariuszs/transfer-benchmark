package com.devskiller;

import com.devskiller.cp.AccountService;
import com.devskiller.cp.Transfer;
import com.devskiller.cp.model.Account;
import com.devskiller.cp.model.Configuration;
import junitparams.JUnitParamsRunner;
import junitparams.Parameters;
import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Random;
import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;

import static com.jayway.awaitility.Awaitility.await;
import static org.assertj.core.api.BDDAssertions.then;

@RunWith(JUnitParamsRunner.class)
public class Transactions {

    private final static Random rnd = new Random();
    public static final int NUMBER_OF_ACCOUNTS = 100;
    public static final int INITIAL_BALANCE = 100;
    public static final int NUMBER_OF_TRANSACTIONS = 10_000;

    private static List<Transfer> transactions = new ArrayList<>();
    private AccountService accountService;

    @BeforeClass
    public static void init() throws Exception {
        for (int i = 0; i < NUMBER_OF_TRANSACTIONS; i++) {
            final Transfer transfer = new Transfer(
                    rnd.nextInt(5),
                    rnd.nextInt(NUMBER_OF_ACCOUNTS),
                    rnd.nextInt(NUMBER_OF_ACCOUNTS));
            transactions.add(transfer);
        }
    }

    @Before
    public void setUp() throws Exception {

        accountService = new AccountService(new Configuration.Builder()
                .initialBalance(INITIAL_BALANCE)
                .mode(Configuration.MODE.UNSAFE)
                .numberOfAccounts(NUMBER_OF_ACCOUNTS)
                .build());
    }

    @After
    public void tearDown() throws Exception {
//        accountService.reset();
    }

    @Test
    @Parameters({"REFS", "UNSAFE", "ATOMIC"})
    public void refs(String modeName) throws Exception {
        final Configuration.MODE mode = Configuration.MODE.valueOf(modeName);
        accountService.setMode(mode);

        System.out.println("-------------------------");
        System.out.println("Mode " + mode);
        final long startTime = System.currentTimeMillis();
        transactions.parallelStream().forEach(accountService::transfer);
        long estimatedTime = System.currentTimeMillis() - startTime;
        System.out.println(estimatedTime + "ms");

        report();
        await().atMost(1, TimeUnit.MINUTES).until(transferCompleted());
        then(total()).isEqualTo(NUMBER_OF_ACCOUNTS * INITIAL_BALANCE);
    }

    public void report() throws InterruptedException {

        final Collection<Account> accounts = accountService.balances().values();
        System.out.println(String.format("Total balance : $%s\t Number of transactions: %s (invalid: %s)\t\t min=$%s.\t max=$%s,\t average=$%s",
                        total(),
                        String.valueOf(accountService.getTransactionCount()),
                        String.valueOf(accountService.getInvalidTransactionCount()),
                        accounts.stream().mapToLong(Account::balance).min().orElseGet(() -> -1),
                        accounts.stream().mapToLong(Account::balance).max().orElseGet(() -> -1),
                        accounts.stream().mapToLong(Account::balance).average().orElseGet(() -> -1))
        );
    }

    private Callable<Boolean> transferCompleted() {
        return () -> accountService.getInvalidTransactionCount() + accountService.getTransactionCount() == NUMBER_OF_TRANSACTIONS;
    }

    private long total() {
        return accountService.balances().values().stream().mapToLong(Account::balance).sum();
    }

}
