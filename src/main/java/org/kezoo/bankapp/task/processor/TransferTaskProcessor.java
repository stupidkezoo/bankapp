package org.kezoo.bankapp.task.processor;

import org.apache.ignite.IgniteMessaging;
import org.apache.ignite.Ignition;
import org.apache.ignite.transactions.Transaction;
import org.apache.ignite.transactions.TransactionConcurrency;
import org.apache.ignite.transactions.TransactionIsolation;
import org.kezoo.bankapp.Application;
import org.kezoo.bankapp.dao.AccountDao;
import org.kezoo.bankapp.model.Account;
import org.kezoo.bankapp.model.PaymentDocument;
import org.kezoo.bankapp.task.enumeration.TaskType;
import org.kezoo.bankapp.task.model.Task;
import org.kezoo.bankapp.task.model.TransferTask;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.Map;

@Service
public class TransferTaskProcessor extends TaskProcessor{

    public TransferTaskProcessor() {
        super(TaskType.TRANSFER);
    }

    private static final Logger log = LoggerFactory.getLogger(TransferTaskProcessor.class);

    @Autowired
    private AccountDao accountDao;

    private IgniteMessaging rmtMsg = Ignition.ignite().message(Ignition.ignite().cluster().forRemotes());

    @Override
    public void execute(Task task) {
        if (!taskType.equals(task.getType())) {
            return;
        }
        TransferTask transferTask = (TransferTask) task;
        PaymentDocument paymentDocument = transferTask.getPaymentDocument();
        boolean isExternalSource = transferTask.isExternalSource();

        log.info("Start processing payment document = {}", paymentDocument);
        BigDecimal amount = paymentDocument.getAmount();
        Map<String, Account> accounts = accountDao.getAccount(paymentDocument.getFromAccount(), paymentDocument.getToAccount());
        Account accountFrom = accounts.get(paymentDocument.getFromAccount());
        Account accountTo = accounts.get(paymentDocument.getToAccount());
        if (checkNull(accountFrom, paymentDocument.getFromAccount())
                || checkNull(accountTo, paymentDocument.getToAccount())) {
            return;
        }

        TransactionType type = getType(accountFrom, accountTo, isExternalSource);
        log.debug("transfer type for payment document {} is {}", paymentDocument, type);
        switch (type) {
            case FROM_EXTERNAL_FOREIGN_SOURCE: {
                String correspondentAccountId = accountDao.getCorrespondentAccountId(accountFrom.getBankId());
                if (!transfer(correspondentAccountId, accountTo.getId(), amount)) {
                    // если перевод из внешнего аккаунта не получился например из-за недостатка баланса на корреспондентском счете,
                    // то отправляем отзеркаленный перевод на внешний аккаунт. Потенциально опасно, так как происходит не в рамках одной транзакции.
                    // Комментарии по возможным решениям проблемы ниже, на :139
                    revertTransfer(accountFrom.getBankId(), paymentDocument);
                }
                break;
            }
            case FROM_EXTERNAL: {
                sendDocument(accountFrom.getBankId(), paymentDocument);
                break;
            }
            case FROM_INTERNAL_TO_EXTERNAL: {
                String correspondentAccountId = accountDao.getCorrespondentAccountId(accountTo.getBankId());
                if (transfer(accountFrom.getId(), correspondentAccountId, amount)) {
                    sendDocument(accountTo.getBankId(), paymentDocument);
                }
                break;
            }
            case FROM_INTERNAL_TO_INTERNAL: {
                transfer(accountFrom.getId(), accountTo.getId(), amount);
                break;
            }
        }
        log.info("Finished processing payment document {}", paymentDocument);
    }

    //введено для читабельности
    private enum TransactionType {
        FROM_EXTERNAL,
        FROM_EXTERNAL_FOREIGN_SOURCE,
        FROM_INTERNAL_TO_EXTERNAL,
        FROM_INTERNAL_TO_INTERNAL
    }

    private TransactionType getType(Account accountFrom, Account accountTo, boolean isExternalSource) {
        if (!isInnerAccount(accountFrom)) {
            if (isExternalSource) {
                return TransactionType.FROM_EXTERNAL_FOREIGN_SOURCE;
            } else {
                return TransactionType.FROM_EXTERNAL;
            }
        } else {
            if (isInnerAccount(accountTo)) {
                return TransactionType.FROM_INTERNAL_TO_INTERNAL;
            } else {
                return TransactionType.FROM_INTERNAL_TO_EXTERNAL;
            }
        }
    }

    private boolean transfer(String accountFromId, String accountToId, BigDecimal amount) {
        try (Transaction tx = Ignition.ignite().transactions().txStart(TransactionConcurrency.PESSIMISTIC, TransactionIsolation.REPEATABLE_READ)) {
            Map<String, Account> accounts = accountDao.getAccount(accountFromId, accountToId);
            Account accountFrom = accounts.get(accountFromId);
            Account accountTo = accounts.get(accountToId);
            if (!validate(accountFrom, accountTo, amount)) {
                log.info("account {} has balance less than amount {} to transfer, or some of accounts doesn't exist", accountFrom.toString(), amount.longValue());
                return false;
            }
            accountFrom.setBalance(accountFrom.getBalance().subtract(amount));
            accountTo.setBalance(accountTo.getBalance().add(amount));
            accountDao.putAll(accountFrom, accountTo);
            tx.commit();
            return true;
        } catch (Exception e) {
            log.error("Failed to perform transaction", e);
            return false;
        }
    }

    private void revertTransfer(String bankId, PaymentDocument paymentDocument) {
        PaymentDocument revertPaymentDocument = new PaymentDocument(paymentDocument.getToAccount(), paymentDocument.getFromAccount(), paymentDocument.getAmount());
        log.info("reverting payment {}", paymentDocument);
        sendDocument(bankId, revertPaymentDocument);
    }

    /*
    не проверяется дошло ли сообщение до получателя. поэтому если получатель неактивен, то перевод будет выполнен только наполовину.
    варианты решения:
    1. Синхронный обмен сообщениями между нодом-отправителем и нодом-получателем.
    2.  добавление сущности PaymentDocument поля 'статус' и 'идентификатор'
        хранение PaymentDocument в отдельном кеше
        прослушивание сообщений, содержащих id платежа, о получении запросов на перевод
        коммит транзакции в случае получения сообщения с id либо
        отмена платежа после таймаута, в течение которого не был получен ответ от нода-получателя
        отмена всех подвисших транзакций в случае завершения работы нода-отправителя

    1 вариант не подходит, поскольку насколько понимаю, средствами apache ignite это сделать невозможно.
    2 По ТЗ не нужен персистанс данных, поэтому падение одного из нодов/всех нодов неизбежно приведет приведет к потере данных.
      Поэтому решение 2 признано нецелесообразным в рамках тестового задания.
     */
    private void sendDocument(String bankId, PaymentDocument paymentDocument) {
        rmtMsg.send(bankId, paymentDocument);
    }

    private boolean isInnerAccount(Account account) {
        return Application.bankName.equals(account.getBankId());
    }

    private boolean validate(Account accountFrom, Account accountTo, BigDecimal amount) {
        return accountFrom != null
                && accountTo != null
                && amount != null
                && amount.compareTo(BigDecimal.ZERO) >= 0
                && accountFrom.getBalance().compareTo(amount) >= 0;
    }

    private boolean checkNull(Account account, String accountId) {
        if (account == null) {
            log.info("Account id={} doesn't exist", accountId);
            return true;
        }
        return false;
    }
}
