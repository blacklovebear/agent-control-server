package com.citic.control;

import java.util.Properties;
import java.util.UUID;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import com.citic.AppConf;
import com.citic.helper.ShellExecutor;
import com.citic.helper.SimpleKafkaProducer;
import com.google.common.util.concurrent.ThreadFactoryBuilder;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static com.citic.AppConstants.*;

public class ProcessMonitor {
    private static final Logger LOGGER = LoggerFactory.getLogger(ProcessMonitor.class);

    private ScheduledExecutorService executorService;
    private AppConf conf;
    private String monitorTopic;
    private SimpleKafkaProducer<String, String> producer;

    public ProcessMonitor(SimpleKafkaProducer<String, String> producer) {
        conf = AppConf.getInstance();
        monitorTopic = conf.getConfig(KAFKA_MONITOR_TOPIC);

        this.producer = producer;
    }

    public void start() {
        // 进程检查时间间隔
        int interval = Integer.parseInt(conf.getConfig(PROCESS_MONITOR_INTERVAL));

        executorService = Executors.newSingleThreadScheduledExecutor(
                new ThreadFactoryBuilder().setNameFormat("process-monitor-%d")
                        .build());
        // 分两个线程单独监控
        processWatchRunnable canalWatch = new processWatchRunnable(
                conf.getConfig(CANAL_MONITOR_CMD), CANAL_PROCESS_NAME);
        processWatchRunnable tAgentWatch = new processWatchRunnable(
                conf.getConfig(TAGENT_MONITOR_CMD), TAGENT_PROCESS_NAME);

        executorService.scheduleWithFixedDelay(canalWatch, 0, interval, TimeUnit.SECONDS);
        executorService.scheduleWithFixedDelay(tAgentWatch, 0, interval, TimeUnit.SECONDS);
    }

    public void stop() {
        executorService.shutdown();

        try {
            while (!executorService.awaitTermination(500, TimeUnit.MILLISECONDS)) {
                LOGGER.debug("Waiting for process monitor to terminate");
            }
        } catch (InterruptedException e) {
            LOGGER.debug("Interrupted while waiting for process monitor to terminate");
            Thread.currentThread().interrupt();
        }
    }

    /*
    * 发送消息到kafka
    * */
    private void sendStageToKafka(String stateMessage) {
        producer.send(monitorTopic, UUID.randomUUID().toString(), stateMessage);
    }

    /*
    * 进程监控执行线程
    * */
    private class processWatchRunnable implements Runnable {
        private ShellExecutor executor;
        private String cmd;
        private String processName;
        private processWatchRunnable(String cmd, String processName) {
            executor = new ShellExecutor();
            this.cmd = cmd;
            this.processName = processName;
        }
        @Override
        public void run() {
            try {
                String state = executor.monitorProcess(this.cmd, this.processName);
                sendStageToKafka(state);
            } catch (Exception e) {
                LOGGER.error(e.getMessage(), e);
            }

        }
    }
}
