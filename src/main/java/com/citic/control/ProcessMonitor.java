package com.citic.control;

import java.util.UUID;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import com.citic.AppConf;
import com.citic.helper.ShellExecutor;
import com.citic.helper.SimpleKafkaProducer;
import com.google.common.util.concurrent.ThreadFactoryBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static com.citic.AppConstants.*;

public class ProcessMonitor {
    private static final Logger LOGGER = LoggerFactory.getLogger(ProcessMonitor.class);

    private ScheduledExecutorService executorService;
    private String monitorTopic;
    private SimpleKafkaProducer<String, String> producer;

    public ProcessMonitor(SimpleKafkaProducer<String, String> producer) {
        monitorTopic = AppConf.getConfig(KAFKA_MONITOR_TOPIC);

        this.producer = producer;
    }

    public void start() {
        // 进程检查时间间隔
        int interval = Integer.parseInt(AppConf.getConfig(PROCESS_MONITOR_INTERVAL));

        executorService = Executors.newSingleThreadScheduledExecutor(
                new ThreadFactoryBuilder().setNameFormat("process-monitor-%d")
                        .build());

        // 分两个线程单独监控
        executorService.scheduleWithFixedDelay(new CanalWatchRunnable(), 0, interval, TimeUnit.SECONDS);
        executorService.scheduleWithFixedDelay(new TAgentWatchRunnable(), 0, interval, TimeUnit.SECONDS);
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
    private class CanalWatchRunnable implements Runnable {
        private final ShellExecutor executor = new ShellExecutor();
        private final String cmd = AppConf.getConfig(CANAL_MONITOR_CMD);
        @Override
        public void run() {
            if (IS_WINDOWS) {
                LOGGER.debug("windows platform monitor process");
            } else {
                try {
                    String state = executor.monitorProcess(this.cmd, CANAL_PROCESS_NAME);
                    if (state.contains("running")
                            && ExecuteCmd.getInstance().getCanalState() == STATE_DEAD) {
                        ExecuteCmd.getInstance().setCanalState(STATE_ALIVE);
                    } else if (state.contains("dead")) {
                        ExecuteCmd.getInstance().setCanalState(STATE_DEAD);
                    }

                    sendStageToKafka(state);
                } catch (Exception e) {
                    LOGGER.error(e.getMessage(), e);
                }
            }
        }
    }

    private class TAgentWatchRunnable implements Runnable {
        private final ShellExecutor executor = new ShellExecutor();
        private final String cmd = AppConf.getConfig(TAGENT_MONITOR_CMD);
        @Override
        public void run() {
            if (IS_WINDOWS) {
                LOGGER.debug("windows platform monitor process");
            } else {
                try {
                    String state = executor.monitorProcess(this.cmd, TAGENT_PROCESS_NAME);
                    if (ExecuteCmd.getInstance().getTAgentState() == STATE_DEAD &&
                            state.contains("running")) {
                        ExecuteCmd.getInstance().setTAgentState(STATE_ALIVE);
                    } else if (state.contains("dead")) {
                        ExecuteCmd.getInstance().setTAgentState(STATE_DEAD);
                    }

                    sendStageToKafka(state);
                } catch (Exception e) {
                    LOGGER.error(e.getMessage(), e);
                }
            }
        }
    }
}
