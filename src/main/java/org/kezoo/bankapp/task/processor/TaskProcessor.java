package org.kezoo.bankapp.task.processor;

import org.kezoo.bankapp.task.enumeration.TaskType;
import org.kezoo.bankapp.task.model.Task;
import org.kezoo.bankapp.task.service.TaskProcessorManager;
import org.springframework.beans.factory.annotation.Autowired;

import javax.annotation.PostConstruct;

public abstract class TaskProcessor {

    @Autowired
    private TaskProcessorManager processorManager;

    protected TaskType taskType;

    public TaskProcessor(TaskType taskType) {
        this.taskType = taskType;
    }

    public TaskType getTaskType() {
        return taskType;
    }

    public abstract void execute(Task task);

    @PostConstruct
    private void postConstruct() {
        processorManager.register(this);
    }

}
