package org.kezoo.bankapp.task.service.impl;

import org.kezoo.bankapp.task.enumeration.TaskType;
import org.kezoo.bankapp.task.processor.TaskProcessor;
import org.kezoo.bankapp.task.service.TaskProcessorManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.EnumMap;

@Service
public class TaskProcessorManagerImpl implements TaskProcessorManager {

    // небольшой оверкилл, но зато позволяет легко ввести новые задачи, задав только новый тип и написав обработчик

    private static final Logger log = LoggerFactory.getLogger(TaskProcessorManagerImpl.class);

    private EnumMap<TaskType, TaskProcessor> map = new EnumMap<>(TaskType.class);

    @Override
    public void register(TaskProcessor processor) {
        map.put(processor.getTaskType(), processor);
        log.debug("Registered processor for task type {}", processor.getTaskType());
    }

    @Override
    public TaskProcessor get(TaskType type) {
        return map.get(type);
    }

}
