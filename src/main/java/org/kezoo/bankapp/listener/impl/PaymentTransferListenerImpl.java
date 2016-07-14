package org.kezoo.bankapp.listener.impl;

import org.apache.ignite.Ignition;
import org.kezoo.bankapp.Application;
import org.kezoo.bankapp.listener.PaymentTransferListener;
import org.kezoo.bankapp.model.PaymentDocument;
import org.kezoo.bankapp.task.service.TaskApi;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class PaymentTransferListenerImpl implements PaymentTransferListener {

    private static final Logger log = LoggerFactory.getLogger(PaymentTransferListenerImpl.class);

    @Autowired
    private TaskApi taskApi;

    @Override
    public void startListening() {
        Ignition.ignite().message().localListen(Application.bankName, (nodeId, object) -> {
            log.debug("Received message [msg={}, from={}", object, nodeId);
            if (object instanceof PaymentDocument) {
                taskApi.createTransfer((PaymentDocument) object, true);
            }
            return true;
        });
    }
}
