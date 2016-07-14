package org.kezoo.bankapp.model;


import java.math.BigDecimal;

public class Account {

    protected String id;
    protected BigDecimal balance;
    protected String bankId;

    public Account(String id, BigDecimal balance, String bankId) {
        this.id = id;
        this.balance = balance;
        this.bankId = bankId;
    }

    public Account() {
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public BigDecimal getBalance() {
        return balance;
    }

    public void setBalance(BigDecimal balance) {
        this.balance = balance;
    }

    public String getBankId() {
        return bankId;
    }

    public void setBankId(String bankId) {
        this.bankId = bankId;
    }

    @Override
    public String toString() {
        return String.format("{id=%s balance=%s bankId=%s}", id, balance, bankId);
    }
}
