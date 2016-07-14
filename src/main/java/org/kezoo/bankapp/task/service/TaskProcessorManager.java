package org.kezoo.bankapp.task.service;

import org.kezoo.bankapp.task.enumeration.TaskType;
import org.kezoo.bankapp.task.processor.TaskProcessor;

public interface TaskProcessorManager {
    void register(TaskProcessor processor);
    TaskProcessor get(TaskType type);
}
