package org.kezoo.bankapp.model;


import java.math.BigDecimal;

public class PaymentDocument {

    public PaymentDocument() {
    }

    public PaymentDocument(String fromAccount, String toAccount, BigDecimal amount) {
        this.fromAccount = fromAccount;
        this.toAccount = toAccount;
        this.amount = amount;
    }

    private String fromAccount;
    private String toAccount;
    private BigDecimal amount;

    public String getFromAccount() {
        return fromAccount;
    }

    public void setFromAccount(String fromAccount) {
        this.fromAccount = fromAccount;
    }

    public String getToAccount() {
        return toAccount;
    }

    public void setToAccount(String toAccount) {
        this.toAccount = toAccount;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    @Override
    public String toString() {
        return String.format("{fromAccount=%s toAccount=%s amount=%s}", fromAccount, toAccount, amount);
    }
}
