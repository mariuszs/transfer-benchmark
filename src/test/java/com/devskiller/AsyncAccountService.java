package com.devskiller;

import com.devskiller.cp.AccountService;
import com.devskiller.cp.Transfer;

public class AsyncAccountService implements Runnable {

    private final AccountService accountService;
    private final Transfer transfer;

    public AsyncAccountService(AccountService accountService, Transfer transfer) {
        this.accountService = accountService;
        this.transfer = transfer;
    }

    @Override
    public void run() {
        try {
            accountService.transfer(transfer);
        } catch (Exception e) {
            //e.printStackTrace();
        }

    }
}
