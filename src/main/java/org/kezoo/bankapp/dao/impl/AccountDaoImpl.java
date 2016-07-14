package org.kezoo.bankapp.dao.impl;


import org.apache.ignite.IgniteCache;
import org.apache.ignite.Ignition;
import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.type.TypeReference;
import org.kezoo.bankapp.Application;
import org.kezoo.bankapp.dao.AccountDao;
import org.kezoo.bankapp.model.Account;
import org.kezoo.bankapp.model.CorrespondentAccount;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import javax.cache.Cache;
import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

@Service
public class AccountDaoImpl implements AccountDao {

    private static final Logger log = LoggerFactory.getLogger(AccountDaoImpl.class);

    private static ObjectMapper objectMapper = new ObjectMapper();

    private static final String PERSONAL_ACCOUNT_CACHE_NAME = "account";
    private static final String CORRESPONDENT_ACCOUNT_CACHE_NAME = "correspondentAccount";

    private IgniteCache<String, Account> accountCache;
    // внутренний кеш, хранящий id корреспондентских аккаунтов по названию банка, нужен только для быстродействия
    private IgniteCache<String, String> correspondentAccountCache;

    @PostConstruct
    public void init() {
        initCache();
    }

    public Account getAccount(String id) {
        return accountCache.get(id);
    }

    public Map<String, Account> getAccount(String ... id) {
        return accountCache.getAll(Stream.of(id).collect(Collectors.toSet()));
    }

    public String getCorrespondentAccountId(String bankName) {
        return correspondentAccountCache.get(bankName);
    }

    public void put(Account account) {
        accountCache.put(account.getId(), account);
    }

    public void put(CorrespondentAccount account) {
        correspondentAccountCache.put(account.getReferralBankId(), account.getId());
        accountCache.put(account.getId(), account);
    }

    public void putAll(Account ... account) {
        for (Account acc : account) {
            if (acc instanceof CorrespondentAccount)
                correspondentAccountCache.put(((CorrespondentAccount)acc).getReferralBankId(), acc.getId());
        }
        accountCache.putAll(Stream.of(account).collect(Collectors.toMap(Account::getId, c -> c)));
    }

    public List<Account> getAll() {
        final Iterable<Cache.Entry<String, Account>> iterable = () -> accountCache.iterator();
        return StreamSupport.stream(iterable.spliterator(), false).map(Cache.Entry::getValue).collect(Collectors.toList());
    }

    private void initCache() {
        log.info("Start account cache initialization");
        correspondentAccountCache = Ignition.ignite().getOrCreateCache(CORRESPONDENT_ACCOUNT_CACHE_NAME);
        accountCache = Ignition.ignite().getOrCreateCache(PERSONAL_ACCOUNT_CACHE_NAME);
        try {
            JsonNode bankRoot = objectMapper.readTree(new File("data.txt")).get(Application.bankName);

            JsonNode personalAccounts = bankRoot.get("personal");
            JsonNode correspondentAccounts = bankRoot.get("correspondent");

            // в данных дублируется bankId как ключ для набора счетов и как поле в каждом счете. Сделано для более лаконичного парсинга
            List<Account> accountList = objectMapper.readValue(personalAccounts, new TypeReference<List<Account>>() {});
            List<CorrespondentAccount> correspondentAccountList = objectMapper.readValue(correspondentAccounts
                    , new TypeReference<List<CorrespondentAccount>>() {});

            //converting list of accounts to map<id,account> and populating account cache
            accountCache.putAll(accountList.stream().collect(Collectors.toMap(Account::getId, c -> c)));
            accountCache.putAll(correspondentAccountList.stream().collect(Collectors.toMap(Account::getId, c -> c)));

            //converting list of correspondent accounts to map<referralBankId,id> and populating local cache
            correspondentAccountCache.putAll(correspondentAccountList.stream()
                    .collect(Collectors.toMap(CorrespondentAccount::getReferralBankId, CorrespondentAccount::getId)));
            log.info("Account cache successfully initialized");
        } catch (IOException e) {
            log.error("Failed to initialize account cache ", e);
        }
    }
}
