package org.kezoo.bankapp.task.service.impl;

import org.kezoo.bankapp.model.PaymentDocument;
import org.kezoo.bankapp.task.model.Task;
import org.kezoo.bankapp.task.model.TransferTask;
import org.kezoo.bankapp.task.service.TaskApi;
import org.kezoo.bankapp.task.service.TaskExecutionQueue;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class TaskApiImpl implements TaskApi {

    private static final Logger log = LoggerFactory.getLogger(TaskApiImpl.class);

    @Autowired
    private TaskExecutionQueue executionQueue;

    public void createTransfer(PaymentDocument paymentDocument, boolean isExternalSource) {
        log.info("createTransfer : creating transfer for payment {} externalSource= {}", paymentDocument, isExternalSource);
        putTask(new TransferTask(paymentDocument, isExternalSource));
    }

    private void putTask(Task task) {
        executionQueue.put(task);
    }
}