package org.kezoo.bankapp.model;

public class CorrespondentAccount extends Account{

    private String referralBankId;

    public String getReferralBankId() {
        return referralBankId;
    }

    public void setReferralBankId(String referralBankId) {
        this.referralBankId = referralBankId;
    }

    @Override
    public String toString() {
        return String.format("{id=%s balance=%s bankId=%s referralBankId=%s}", id, balance, bankId, referralBankId);
    }
}
