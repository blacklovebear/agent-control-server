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

    private static final String CANAL_PROCESS_NAME = "Canal Server";
    private static final String TAGENT_PROCESS_NAME = "TAgent";

    private ScheduledExecutorService executorService;
    private ApplicationConf conf;

    private SimpleKafkaProducer<String, String> producer;
    public ProcessMonitor() {
         conf = ApplicationConf.getInstance();

        Properties producerConfig = new Properties();

        String brokerList = "192.168.2.25:9092";
        producerConfig.put("bootstrap.servers", brokerList);
        producerConfig.put("client.id", "basic-producer");
        producerConfig.put("acks", "all");
        producerConfig.put("retries", "3");
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

        processWatchRunnable processWatchRunnable = new processWatchRunnable();

        executorService.scheduleWithFixedDelay(processWatchRunnable, 0, interval,
                TimeUnit.SECONDS);
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

    private void sendStageToKafka(String stateMessage) {
        String topic = "process_monitor";
        producer.send(topic, UUID.randomUUID().toString(), stateMessage);
    }

    /*
    * 进程监控执行线程
    * */
    private class processWatchRunnable implements Runnable {
        private ShellExecutor executor;

        private processWatchRunnable() {
            executor = new ShellExecutor();
        }

        @Override
        public void run() {
            try {
                String canalState = executor.monitorProcess(conf.getConfig(CANAL_MONITOR_CMD), CANAL_PROCESS_NAME);
                String tAgentState = executor.monitorProcess(conf.getConfig(TAGENT_MONITOR_CMD), TAGENT_PROCESS_NAME);

                sendStageToKafka(canalState);
                sendStageToKafka(tAgentState);
            } catch (Exception e) {
                LOGGER.error(e.getMessage(), e);
            }

        }
    }
}
