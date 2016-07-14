package org.kezoo.bankapp.task.service;

import org.kezoo.bankapp.task.model.Task;

public interface TaskExecutionQueue {
    void put(Task task);
    void shutdown();
}
