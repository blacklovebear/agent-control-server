package com.citic.control;

import com.google.common.util.concurrent.ThreadFactoryBuilder;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DataXJobMonitor {

    private ScheduledExecutorService executorService;
    private static final Logger LOGGER = LoggerFactory.getLogger(DataXJobMonitor.class);
    private static final int INTERVAL = 5;


    /**
     * Start.
     */
    public void start() {
        executorService = Executors.newSingleThreadScheduledExecutor(
            new ThreadFactoryBuilder().setNameFormat("dataX-job-monitor-%d")
                .build());
        // 分两个线程单独监控
        executorService
            .scheduleAtFixedRate(new JobRunnable(), 0, INTERVAL, TimeUnit.SECONDS);
    }

    /**
     * Stop.
     */
    public void stop() {
        executorService.shutdown();
        try {
            while (!executorService.awaitTermination(500, TimeUnit.MILLISECONDS)) {
                LOGGER.debug("Waiting for dataX job monitor to terminate");
            }
        } catch (InterruptedException e) {
            LOGGER.debug("Interrupted while waiting for dataX job monitor to terminate");
            Thread.currentThread().interrupt();
        }
    }
    
    private static class JobRunnable implements Runnable {

        @Override
        public void run() {
            DataXJobController.checkJobStateAndSendResponse();
        }
    }

}
