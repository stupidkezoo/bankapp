package org.kezoo.bankapp.service.impl;

import org.apache.ignite.Ignition;
import org.kezoo.bankapp.Application;
import org.kezoo.bankapp.dao.AccountDao;
import org.kezoo.bankapp.enumeration.ConsoleCommand;
import org.kezoo.bankapp.model.Account;
import org.kezoo.bankapp.model.PaymentDocument;
import org.kezoo.bankapp.service.ConsoleCommandListener;
import org.kezoo.bankapp.task.service.TaskApi;
import org.kezoo.bankapp.task.service.TaskExecutionQueue;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.math.BigDecimal;

@Service
public class ConsoleCommandListenerImpl implements ConsoleCommandListener {

    private static final Logger log = LoggerFactory.getLogger(ConsoleCommandListenerImpl.class);

    @Autowired
    private TaskApi taskApi;
    @Autowired
    private AccountDao accountDao;
    @Autowired
    private TaskExecutionQueue taskExecutionQueue;

    public void start() {
        print("Start listening to user input. Type 'help' for command list");
        new Thread(() -> {
            InputStreamReader converter = new InputStreamReader(System.in);
            BufferedReader in = new BufferedReader(converter);
            String curLine = "";

            while (!("quit".equals(curLine))) {
                try {
                    curLine = in.readLine();
                    processInput(curLine);
                } catch (Exception e) {
                    log.error("failed to process input {}", curLine, e);
                }
            }
            taskExecutionQueue.shutdown();
            Ignition.stop(true);
            System.exit(0);
        }).start();
    }

    private void processInput(String input) {
        if (input == null || input.isEmpty()) {
            return;
        }
        String[] trimmedInput = input.split("\\s+");
        ConsoleCommand command = ConsoleCommand.byName(trimmedInput[0]);
        if (command == null) {
            print("unsupported command, type 'help' for command list");
            return;
        }
        log.trace("Received command {}", command);
        // по-хорошему стоило бы добавить валидацию входных параметров
        switch (command) {
            case PUT_ACCOUNT:
                accountDao.put(new Account(trimmedInput[1], new BigDecimal(trimmedInput[2]), trimmedInput[3]));
                break;
            case GET_ACCOUNT:
                print(accountDao.getAccount(trimmedInput[1]).toString());
                break;
            case GETALL:
                accountDao.getAll().stream().forEach(this::print);
                break;
            case HELP:
                for (ConsoleCommand cmd : ConsoleCommand.values()) {
                    print(cmd);
                }
                break;
            case SEND:
                taskApi.createTransfer(new PaymentDocument(trimmedInput[1], trimmedInput[2], new BigDecimal(trimmedInput[3])), false);
                break;
            case CURRENT_BANK:
                print(Application.bankName);
                break;
        }
    }

    private void print(Object s) {
        System.out.println(s);
    }
}
