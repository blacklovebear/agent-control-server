package com.citic.control;

import static com.citic.AppConstants.AGENT_IP;
import static com.citic.AppConstants.CANAL_MONITOR_CMD;
import static com.citic.AppConstants.CANAL_PROCESS_NAME;
import static com.citic.AppConstants.CURRENT_TIME;
import static com.citic.AppConstants.PROCESS_MONITOR_INTERVAL;
import static com.citic.AppConstants.STATE_ALIVE;
import static com.citic.AppConstants.STATE_DEAD;
import static com.citic.AppConstants.SUPPORT_TIME_FORMAT;
import static com.citic.AppConstants.TAGENT_MONITOR_CMD;
import static com.citic.AppConstants.TAGENT_PROCESS_NAME;

import com.citic.AppConf;
import com.citic.AppConstants;
import com.citic.helper.SchemaCache;
import com.citic.helper.ShellExecutor;
import com.citic.helper.SimpleKafkaProducer;
import com.citic.helper.Utility;
import com.google.common.collect.Lists;
import com.google.common.util.concurrent.ThreadFactoryBuilder;
import java.nio.charset.Charset;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import org.apache.avro.Schema;
import org.apache.avro.generic.GenericData;
import org.apache.avro.generic.GenericRecord;
import org.apache.commons.lang.SystemUtils;
import org.json.simple.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ProcessMonitor {

    private static final Logger LOGGER = LoggerFactory.getLogger(ProcessMonitor.class);

    private static final String CANAL_STATE = "canal_state";
    private static final String TAGENT_STATE = "tAgent_state";

    private static final String AVRO_PROCESS_MONITOR_TOPIC = "avro_process_monitor";
    private static final String JSON_PROCESS_MONITOR_TOPIC = "json_process_monitor";

    private static final List<String> ATTR_LIST = Lists.newArrayList(CANAL_STATE, TAGENT_STATE,
        CURRENT_TIME, AGENT_IP);
    private final SimpleKafkaProducer<Object, Object> producer;
    private final boolean useAvro;
    private ScheduledExecutorService executorService;

    public ProcessMonitor(SimpleKafkaProducer<Object, Object> producer, boolean useAvro) {
        this.producer = producer;
        this.useAvro = useAvro;
    }

    public void start() {
        executorService = Executors.newSingleThreadScheduledExecutor(
            new ThreadFactoryBuilder().setNameFormat("process-monitor-%d")
                .build());
        // 进程检查时间间隔
        int interval = Integer.parseInt(AppConf.getConfig(PROCESS_MONITOR_INTERVAL));
        // 分两个线程单独监控
        executorService
            .scheduleAtFixedRate(new ProcessWatchRunnable(useAvro), 0, interval, TimeUnit.SECONDS);
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
        String schemaString = Utility.getTableFieldSchema(ATTR_LIST, AVRO_PROCESS_MONITOR_TOPIC);

        Schema schema = SchemaCache.getSchema(schemaString);
        GenericRecord avroRecord = new GenericData.Record(schema);
        avroRecord.put(CANAL_STATE, canalState);
        avroRecord.put(TAGENT_STATE, tAgentState);

        avroRecord.put(CURRENT_TIME, new SimpleDateFormat(SUPPORT_TIME_FORMAT).format(new Date()));
        avroRecord
            .put(AGENT_IP, Utility.getLocalIP(AppConf.getConfig(AppConstants.AGENT_IP_INTERFACE)));
        return avroRecord;
    }

    private byte[] buildJsonRecord(String canalState, String tAgentState) {
        JSONObject jsonRecord = new JSONObject();
        jsonRecord.put(CANAL_STATE, canalState);
        jsonRecord.put(TAGENT_STATE, tAgentState);
        jsonRecord.put(CURRENT_TIME, new SimpleDateFormat(SUPPORT_TIME_FORMAT).format(new Date()));
        jsonRecord
            .put(AGENT_IP, Utility.getLocalIP(AppConf.getConfig(AppConstants.AGENT_IP_INTERFACE)));
        return jsonRecord.toJSONString().getBytes(Charset.forName("UTF-8"));
    }

    /*
     * 进程监控执行线程
     * */
    private class ProcessWatchRunnable implements Runnable {

        private final ShellExecutor executor = new ShellExecutor();
        private final String CanalCmd = AppConf.getConfig(CANAL_MONITOR_CMD);
        private final String tAgentCmd = AppConf.getConfig(TAGENT_MONITOR_CMD);
        private final boolean useAvro;

        private ProcessWatchRunnable(boolean useAvro) {
            this.useAvro = useAvro;
        }

        private String monitorCanal() {
            if (SystemUtils.IS_OS_LINUX) {
                try {
                    String state = executor.monitorProcess(this.CanalCmd, CANAL_PROCESS_NAME);
                    if (state.contains("running")) {
                        ExecuteCmd.INSTANCE.setCanalState(STATE_ALIVE);
                    } else if (state.contains("dead")) {
                        ExecuteCmd.INSTANCE.setCanalState(STATE_DEAD);
                    }
                    return state;
                } catch (Exception e) {
                    LOGGER.error(e.getMessage(), e);
                }
            } else {
                LOGGER.debug("not linux platform monitor process");
            }
            return null;
        }

        private String monitorTAgent() {
            if (SystemUtils.IS_OS_LINUX) {
                try {
                    String state = executor.monitorProcess(this.tAgentCmd, TAGENT_PROCESS_NAME);
                    if (state.contains("running")) {
                        ExecuteCmd.INSTANCE.setTAgentState(STATE_ALIVE);
                    } else if (state.contains("dead")) {
                        ExecuteCmd.INSTANCE.setTAgentState(STATE_DEAD);
                    }
                    return state;
                } catch (Exception e) {
                    LOGGER.error(e.getMessage(), e);
                }
            } else {
                LOGGER.debug("not linux platform monitor process");
            }
            return null;
        }

        @Override
        public void run() {

            String canalState = monitorCanal();
            String tAgentState = monitorTAgent();

            try {
                if (canalState != null && tAgentState != null) {
                    if (useAvro) {
                        GenericRecord avroRecord = buildAvroRecord(canalState, tAgentState);
                        producer.send(AVRO_PROCESS_MONITOR_TOPIC, avroRecord);
                    } else {
                        byte[] jsonRecord = buildJsonRecord(canalState, tAgentState);
                        producer.send(JSON_PROCESS_MONITOR_TOPIC, jsonRecord);
                    }
                }
            } catch (Exception e) {
                LOGGER.error(e.getMessage(), e);
            }
        }
    }
}
