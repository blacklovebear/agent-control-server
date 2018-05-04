package com.citic.control;

import com.citic.AppConf;
import com.citic.helper.LogFileTailer;
import com.citic.helper.ShellExecutor;
import com.citic.helper.SimpleKafkaProducer;
import com.citic.helper.Utility;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.concurrent.*;

import static com.citic.AppConstants.TAGENT_HOME_DIR;
import static com.citic.AppConstants.TAGENT_LOG_FILE_PATH;

public class ErrorLogMonitor {
    private static final Logger LOGGER = LoggerFactory.getLogger(ErrorLogMonitor.class);

    private final ExecutorService executorService;
//    private final SimpleKafkaProducer<Object, Object> producer;
//    private final boolean useAvro;


    public ErrorLogMonitor(SimpleKafkaProducer<Object, Object> producer, boolean useAvro) {
//        this.producer = producer;
//        this.useAvro = useAvro;
        this.executorService = Executors.newCachedThreadPool();
    }

    public ErrorLogMonitor() {
        this.executorService = Executors.newCachedThreadPool();
    }

    public void start() {
        String tAgentLogPath =  AppConf.getConfig(TAGENT_HOME_DIR) + File.separator +
                AppConf.getConfig(TAGENT_LOG_FILE_PATH);

        LogFileTailer tail_tailF = new LogFileTailer(tAgentLogPath, 2000, System.out::println);
        executorService.submit(tail_tailF);
    }

    public void stop() {
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

