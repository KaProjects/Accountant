package org.kaleta.accountant.frontend.dialog.account;

import org.kaleta.accountant.backend.entity.Transaction;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Stanislav Kaleta on 16.04.2016.
 */
public class AccountModel {
    private final String type;
    private final String name;
    private List<Transaction> trDebit;
    private List<Transaction> trCredit;
    private int initialState;

    public AccountModel(String type, String name, List<Transaction> trDebit, List<Transaction> trCredit, int initialState){
        this.type = type;
        this.name = name;
        this.trDebit = trDebit;
        this.trCredit = trCredit;
        this.initialState = initialState;
    }

    public String getType() {
        return type;
    }

    public String getName() {
        return name;
    }

    public List<Transaction> getTrDebit() {
        return new ArrayList<>(trDebit);
    }

    public List<Transaction> getTrCredit() {
        return new ArrayList<>(trCredit);
    }

    public int getInitialState() {
        return initialState;
    }

    // TODO: 4/16/16 equals, hash code
}
