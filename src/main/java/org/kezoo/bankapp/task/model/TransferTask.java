package org.kezoo.bankapp.task.model;

import org.kezoo.bankapp.model.PaymentDocument;
import org.kezoo.bankapp.task.enumeration.TaskType;

public class TransferTask implements Task{

    @Override
    public TaskType getType() {
        return TaskType.TRANSFER;
    }

    private PaymentDocument paymentDocument;
    private boolean isExternalSource;

    public TransferTask(PaymentDocument paymentDocument, boolean isExternalSource) {
        this.paymentDocument = paymentDocument;
        this.isExternalSource = isExternalSource;
    }

    public PaymentDocument getPaymentDocument() {
        return paymentDocument;
    }

    public void setPaymentDocument(PaymentDocument paymentDocument) {
        this.paymentDocument = paymentDocument;
    }

    public boolean isExternalSource() {
        return isExternalSource;
    }

    public void setExternalSource(boolean externalSource) {
        isExternalSource = externalSource;
    }
}
