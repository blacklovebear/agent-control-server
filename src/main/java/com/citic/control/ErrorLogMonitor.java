package com.citic.control;

import static com.citic.AppConstants.AGENT_IP;
import static com.citic.AppConstants.CANAL_HOME_DIR;
import static com.citic.AppConstants.CANAL_LOGS_DIR;
import static com.citic.AppConstants.CURRENT_TIME;
import static com.citic.AppConstants.SUPPORT_TIME_FORMAT;
import static com.citic.AppConstants.TAGENT_HOME_DIR;
import static com.citic.AppConstants.TAGENT_LOG_FILE_PATH;

import com.citic.AppConf;
import com.citic.AppConstants;
import com.citic.helper.LogFileTailer;
import com.citic.helper.SchemaCache;
import com.citic.helper.SimpleKafkaProducer;
import com.citic.helper.Utility;
import com.google.common.collect.Lists;
import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.apache.avro.Schema;
import org.apache.avro.generic.GenericData;
import org.apache.avro.generic.GenericRecord;
import org.json.simple.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The type Error log monitor.
 */
public class ErrorLogMonitor {

    private static final Logger LOGGER = LoggerFactory.getLogger(ErrorLogMonitor.class);
    private static final String LOG_PATH = "log_path";
    private static final String ERROR_LOG = "error_log";
    private static final Pattern ERROR_PATTERN = Pattern.compile("ERROR|Error|error");

    private static final String AVRO_ERROR_LOG_TOPIC = "avro_error_log";
    private static final String JSON_ERROR_LOG_TOPIC = "json_error_log";

    private static final List<String> ATTR_LIST = Lists.newArrayList(LOG_PATH, ERROR_LOG,
        CURRENT_TIME, AGENT_IP);
    private final SimpleKafkaProducer<Object, Object> producer;
    private final boolean useAvro;
    private ExecutorService executorService;


    /**
     * Instantiates a new Error log monitor.
     *
     * @param producer the producer
     * @param useAvro the use avro
     */
    public ErrorLogMonitor(SimpleKafkaProducer<Object, Object> producer, boolean useAvro) {
        this.producer = producer;
        this.useAvro = useAvro;
        this.executorService = Executors.newCachedThreadPool();
    }

    private void sendErrorLog(String logLine, String logPath) {
        Matcher m = ERROR_PATTERN.matcher(logLine);
        if (m.find()) {
            if (this.useAvro) {
                sendAvroErrorLog(logLine, logPath);
            } else {
                sendJsonErrorLog(logLine, logPath);
            }
        }
    }

    private void sendAvroErrorLog(String logLine, String logPath) {
        Schema schema = SchemaCache.getSchema(ATTR_LIST, AVRO_ERROR_LOG_TOPIC);
        GenericRecord avroRecord = new GenericData.Record(schema);
        avroRecord.put(LOG_PATH, logPath);
        avroRecord.put(ERROR_LOG, logLine);

        avroRecord.put(CURRENT_TIME, new SimpleDateFormat(SUPPORT_TIME_FORMAT).format(new Date()));
        avroRecord
            .put(AGENT_IP, Utility.getLocalIp(AppConf.getConfig(AppConstants.AGENT_IP_INTERFACE)));

        producer.send(AVRO_ERROR_LOG_TOPIC, avroRecord);
    }

    private void sendJsonErrorLog(String logLine, String logPath) {
        JSONObject jsonRecord = new JSONObject();
        jsonRecord.put(LOG_PATH, logPath);
        jsonRecord.put(ERROR_LOG, logLine);

        jsonRecord.put(CURRENT_TIME, new SimpleDateFormat(SUPPORT_TIME_FORMAT).format(new Date()));
        jsonRecord
            .put(AGENT_IP, Utility.getLocalIp(AppConf.getConfig(AppConstants.AGENT_IP_INTERFACE)));

        producer.send(JSON_ERROR_LOG_TOPIC, jsonRecord);
    }

    private void startLogFile(String logFilePath) {
        LogFileTailer logFileTailer = new LogFileTailer(logFilePath, 2000, this::sendErrorLog);
        executorService.submit(logFileTailer);
    }

    private void startLogFile(File logFilePath) {
        LogFileTailer logFileTailer = new LogFileTailer(logFilePath, 2000, this::sendErrorLog);
        executorService.submit(logFileTailer);
    }

    private void startTAgent() {
        String tagentLogPath = AppConf.getConfig(TAGENT_HOME_DIR) + File.separator
            + AppConf.getConfig(TAGENT_LOG_FILE_PATH);
        startLogFile(tagentLogPath);
    }

    private void startCanal() {
        String canalLogsDir = AppConf.getConfig(CANAL_HOME_DIR) + File.separator
            + AppConf.getConfig(CANAL_LOGS_DIR);

        File logsDir = new File(canalLogsDir);
        File [] listFiles = logsDir.listFiles();
        if (listFiles == null) {
            return;
        }
        for (File instanceDir : Objects.requireNonNull(listFiles)) {
            File[] childFiles = instanceDir.listFiles();
            if (childFiles == null) {
                continue;
            }
            for (File logFile : Objects.requireNonNull(childFiles)) {
                if (logFile.isFile() && logFile.getName().endsWith(".log")) {
                    startLogFile(logFile);
                }
            }

        }
    }

    /**
     * Start.
     */
    public synchronized void start() {
        if (this.executorService != null) {
            this.stop();
        }

        this.executorService = Executors.newCachedThreadPool();
        startCanal();
        startTAgent();
    }

    /**
     * Start.
     *
     * @param instanceList the instance list
     */
    public synchronized void start(List<String> instanceList) {
        if (this.executorService != null) {
            this.stop();
        }

        this.executorService = Executors.newCachedThreadPool();
        startCanalInstances(instanceList);
        startTAgent();
    }


    private void startCanalInstances(List<String> instanceList) {
        String canalLogsDir = AppConf.getConfig(CANAL_HOME_DIR) + File.separator
            + AppConf.getConfig(CANAL_LOGS_DIR);

        instanceList.forEach(instanceName -> {
            String logFile = canalLogsDir + File.separator
                + instanceName + File.separator
                + instanceName + ".log";
            startLogFile(logFile);
        });
    }

    /**
     * Stop.
     */
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

