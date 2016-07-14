package org.kezoo.bankapp.dao;


import org.kezoo.bankapp.model.Account;
import org.kezoo.bankapp.model.CorrespondentAccount;

import java.util.List;
import java.util.Map;

public interface AccountDao {
    Account getAccount(String id);
    String getCorrespondentAccountId(String bankName);
    void put(Account account);
    void put(CorrespondentAccount account);
    void putAll(Account ... account);
    List<Account> getAll();
    Map<String, Account> getAccount(String ... id);
}
