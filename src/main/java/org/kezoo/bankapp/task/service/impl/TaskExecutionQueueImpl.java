package org.kezoo.bankapp.task.service.impl;

import org.kezoo.bankapp.task.model.Task;
import org.kezoo.bankapp.task.processor.TaskProcessor;
import org.kezoo.bankapp.task.service.TaskExecutionQueue;
import org.kezoo.bankapp.task.service.TaskProcessorManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

@Service
public class TaskExecutionQueueImpl implements TaskExecutionQueue {

    @Autowired
    private TaskProcessorManager processorManager;

    private static final Logger log = LoggerFactory.getLogger(TaskExecutionQueueImpl.class);

    private ThreadPoolExecutor executor;

    @Override
    public void put(Task task) {
        executor.execute(() -> {
            TaskProcessor processor = processorManager.get(task.getType());
            if (processor == null) {
                log.error("No processor for task type {}", task.getType());
                return;
            }
            processor.execute(task);
        });
    }

    @Override
    public void shutdown() {
        executor.shutdown();
        try {
        executor.awaitTermination(10, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            log.error("", e);
        }
    }

    @PostConstruct
    private void initPaymentQueue() {
        BlockingQueue<Runnable> blockingQueue = new ArrayBlockingQueue<>(200);

        executor = new ThreadPoolExecutor(10,
                20, 5000, TimeUnit.MILLISECONDS, blockingQueue);

        executor.setRejectedExecutionHandler((runnable, ex) -> {
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    ex.execute(runnable);
                }
        );
        executor.prestartAllCoreThreads();
    }
}
