package com.citic.control;

import com.citic.AppConf;
import com.citic.helper.LogFileTailer;
import com.citic.helper.SimpleKafkaProducer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.*;

import static com.citic.AppConstants.*;

public class ErrorLogMonitor {
    private static final Logger LOGGER = LoggerFactory.getLogger(ErrorLogMonitor.class);

    private  ExecutorService executorService;
//    private final SimpleKafkaProducer<Object, Object> producer;
//    private final boolean useAvro;


    public ErrorLogMonitor(SimpleKafkaProducer<Object, Object> producer, boolean useAvro) {
//        this.producer = producer;
//        this.useAvro = useAvro;
//        this.executorService = Executors.newCachedThreadPool();
    }

    public ErrorLogMonitor() {
    }

    private void startLogFile(String logFilePath) {
        LogFileTailer tail_tailF = new LogFileTailer(logFilePath, 2000, System.out::println);
        executorService.submit(tail_tailF);
    }

    private void startLogFile(File logFilePath) {
        LogFileTailer tail_tailF = new LogFileTailer(logFilePath, 2000, System.out::println);
        executorService.submit(tail_tailF);
    }

    private void startTAgent() {
        String tAgentLogPath =  AppConf.getConfig(TAGENT_HOME_DIR) + File.separator +
                AppConf.getConfig(TAGENT_LOG_FILE_PATH);

        startLogFile(tAgentLogPath);
    }

    private void startCanal() {
        String canalLogsDir =  AppConf.getConfig(CANAL_HOME_DIR) + File.separator +
                AppConf.getConfig(CANAL_LOGS_DIR);

        File logsDir = new File(canalLogsDir);
        if (logsDir.listFiles() == null)
            return;
        for (File instanceDir : Objects.requireNonNull(logsDir.listFiles())) {
            if (instanceDir.listFiles() == null)
                continue;
            for (File logFile: Objects.requireNonNull(instanceDir.listFiles())) {
                if (logFile.isFile() && logFile.getName().contains(".log")) {
                    startLogFile(logFile);
                }
            }

        }
    }

    public synchronized void start() {
        if (this.executorService != null)
            this.stop();

        this.executorService = Executors.newCachedThreadPool();
        startCanal();
        startTAgent();
    }

    public synchronized void start(List<String> instanceList) {
        if (this.executorService != null)
            this.stop();

        this.executorService = Executors.newCachedThreadPool();
        startCanal(instanceList);
        startTAgent();
    }


    private void startCanal(List<String> instanceList) {
        String canalLogsDir =  AppConf.getConfig(CANAL_HOME_DIR) + File.separator +
                AppConf.getConfig(CANAL_LOGS_DIR);

        instanceList.forEach(instanceName -> {
            String logFile = canalLogsDir + File.separator
                    + instanceName + File.separator
                    + instanceName + ".log";
            startLogFile(logFile);
        });
    }

    public synchronized void stop() {
        executorService.shutdownNow();
        try {
            while (!executorService.awaitTermination(500, TimeUnit.MILLISECONDS)) {
                LOGGER.debug("Waiting for ErrorLog monitor to terminate");
            }
        } catch (InterruptedException e) {
            LOGGER.debug("Interrupted while waiting for ErrorLog monitor to terminate");
            Thread.currentThread().interrupt();
        }
    }

}

