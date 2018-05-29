package com.citic.control;

import com.google.common.collect.Maps;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import org.zeroturnaround.exec.ProcessResult;

public class DataXJobMonitor {
    private final ExecutorService executorService;

    DataXJobMonitor() {
        executorService = Executors.newCachedThreadPool();
    }


    private static class JobRunnable implements Runnable {

        @Override
        public void run() {

        }
    }

}
