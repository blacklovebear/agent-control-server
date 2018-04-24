package com.citic.control;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import com.citic.AppConf;
import com.citic.AppConstants;
import com.citic.helper.SchemaCache;
import com.citic.helper.ShellExecutor;
import com.citic.helper.SimpleKafkaProducer;
import com.citic.helper.Utility;
import com.google.common.collect.Lists;
import com.google.common.util.concurrent.ThreadFactoryBuilder;
import org.apache.avro.Schema;
import org.apache.avro.generic.GenericData;
import org.apache.avro.generic.GenericRecord;
import org.apache.commons.lang.SystemUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static com.citic.AppConstants.*;

public class ProcessMonitor {
    private static final Logger LOGGER = LoggerFactory.getLogger(ProcessMonitor.class);
    private static final String SUPPORT_TIME_FORMAT = "yyyy-MM-dd HH:mm:ss";

    private static final String CANAL_STATE = "canal_state";
    private static final String TAGENT_STATE = "tAgent_state";
    private static final String CURRENT_TIME = "ctime";
    private static final String AGENT_IP = "agent";

    private static final String MONITOR_TOPIC = AppConf.getConfig(KAFKA_MONITOR_TOPIC);

    private static final List<String> ATTR_LIST = Lists.newArrayList(CANAL_STATE, TAGENT_STATE,
            CURRENT_TIME, AGENT_IP);

    private ScheduledExecutorService executorService;
    private SimpleKafkaProducer<Object, Object> producer;

    public ProcessMonitor(SimpleKafkaProducer<Object, Object> producer) {
        this.producer = producer;
    }

    public void start() {
        // 进程检查时间间隔
        int interval = Integer.parseInt(AppConf.getConfig(PROCESS_MONITOR_INTERVAL));
        executorService = Executors.newSingleThreadScheduledExecutor(
                new ThreadFactoryBuilder().setNameFormat("process-monitor-%d")
                        .build());
        // 分两个线程单独监控
        executorService.scheduleWithFixedDelay(new ProcessWatchRunnable(), 0, interval, TimeUnit.SECONDS);
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


    private GenericRecord buildAvroRecord(String canalState, String tAgentState) {
        String schemaString = Utility.getTableFieldSchema(ATTR_LIST, MONITOR_TOPIC);

        Schema schema = SchemaCache.getSchema(schemaString);
        GenericRecord avroRecord = new GenericData.Record(schema);
        avroRecord.put(CANAL_STATE, canalState);
        avroRecord.put(TAGENT_STATE, tAgentState);

        avroRecord.put(CURRENT_TIME, new SimpleDateFormat(SUPPORT_TIME_FORMAT).format(new Date()));
        avroRecord.put(AGENT_IP, Utility.getLocalIP(AppConf.getConfig(AppConstants.AGENT_IP_INTERFACE)));
        return avroRecord;
    }

    /*
    * 进程监控执行线程
    * */
    private class ProcessWatchRunnable implements Runnable {
        private final ShellExecutor executor = new ShellExecutor();
        private final String CanalCmd = AppConf.getConfig(CANAL_MONITOR_CMD);
        private final String tAgentCmd = AppConf.getConfig(TAGENT_MONITOR_CMD);

        private String monitorCanal() {
            if (SystemUtils.IS_OS_LINUX) {
                try {
                    String state = executor.monitorProcess(this.CanalCmd, CANAL_PROCESS_NAME);
                    if (state.contains("running")) {
                        ExecuteCmd.getInstance().setCanalState(STATE_ALIVE);
                    } else if (state.contains("dead")) {
                        ExecuteCmd.getInstance().setCanalState(STATE_DEAD);
                    }
                    return state;
                } catch (Exception e) {
                    LOGGER.error(e.getMessage(), e);
                }
            } else {
                LOGGER.debug("windows platform monitor process");
            }
            return null;
        }

        private String monitorTAgent() {
            if (SystemUtils.IS_OS_LINUX) {
                try {
                    String state = executor.monitorProcess(this.tAgentCmd, TAGENT_PROCESS_NAME);
                    if (state.contains("running")) {
                        ExecuteCmd.getInstance().setTAgentState(STATE_ALIVE);
                    } else if (state.contains("dead")) {
                        ExecuteCmd.getInstance().setTAgentState(STATE_DEAD);
                    }
                    return state;
                } catch (Exception e) {
                    LOGGER.error(e.getMessage(), e);
                }
            } else {
                LOGGER.debug("windows platform monitor process");
            }
            return null;
        }

        @Override
        public void run() {
            String canalState = monitorCanal();
            String tAgentState = monitorTAgent();

            if (canalState != null && tAgentState != null) {
                GenericRecord avroRecord = buildAvroRecord(canalState, tAgentState);
                producer.send(MONITOR_TOPIC, avroRecord);
            }

        }
    }
}
