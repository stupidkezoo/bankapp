package org.kezoo.bankapp.task.service;

import org.kezoo.bankapp.model.PaymentDocument;

public interface TaskApi {
    void createTransfer(PaymentDocument paymentDocument, boolean isExternalSource);
}
