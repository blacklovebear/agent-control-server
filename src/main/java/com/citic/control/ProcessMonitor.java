package com.citic.control;

import java.util.Properties;
import java.util.UUID;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import com.citic.ApplicationConf;
import com.citic.helper.ShellExecutor;
import com.citic.helper.SimpleKafkaProducer;
import com.google.common.util.concurrent.ThreadFactoryBuilder;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ProcessMonitor {
    private static final Logger LOGGER = LoggerFactory.getLogger(ProcessMonitor.class);
    private static final String PROCESS_MONITOR_INTERVAL = "process.monitor.interval";

    private static final String CANAL_MONITOR_CMD = "canal.monitor.cmd";
    private static final String TAGENT_MONITOR_CMD = "tagent.monitor.cmd";

    private static final String CANAL_PROCESS_NAME = "Canal";
    private static final String TAGENT_PROCESS_NAME = "TAgent";

    private static final String KAFKA_MONITOR_TOPIC = "kafka.monitor.topic";
    private static final String KAFKA_BOOTSTRAP_SERVERS = "kafka.bootstrap.servers";
    private static final String KAFKA_CLIENT_ID = "kafka.client.id";
    private static final String KAFKA_ACKS = "kafka.acks";
    private static final String KAFKA_RETRIES = "kafka.retries";

    private ScheduledExecutorService executorService;
    private ApplicationConf conf;
    private String monitorTopic;
    private SimpleKafkaProducer<String, String> producer;

    public ProcessMonitor() {
        conf = ApplicationConf.getInstance();

        monitorTopic = conf.getConfig(KAFKA_MONITOR_TOPIC);
        Properties producerConfig = new Properties();

        producerConfig.put("bootstrap.servers", conf.getConfig(KAFKA_BOOTSTRAP_SERVERS));
        producerConfig.put("client.id", conf.getConfig(KAFKA_CLIENT_ID));
        producerConfig.put("acks", conf.getConfig(KAFKA_ACKS));
        producerConfig.put("retries", conf.getConfig(KAFKA_RETRIES));

        producerConfig.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG,
                "org.apache.kafka.common.serialization.StringSerializer");
        producerConfig.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG,
                "org.apache.kafka.common.serialization.StringSerializer");

        producer = new SimpleKafkaProducer<>(producerConfig, false);
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
        producer.close();

        try {
            while (!executorService.awaitTermination(500, TimeUnit.MILLISECONDS)) {
                LOGGER.debug("Waiting for file watcher to terminate");
            }
        } catch (InterruptedException e) {
            LOGGER.debug("Interrupted while waiting for file watcher to terminate");
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
