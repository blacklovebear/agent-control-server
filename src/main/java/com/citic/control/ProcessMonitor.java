package com.citic.control;

import java.io.IOException;
import java.util.UUID;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import com.citic.AppConf;
import com.citic.helper.ShellExecutor;
import com.citic.helper.SimpleKafkaProducer;
import com.citic.helper.Utility;
import com.google.common.base.Strings;
import com.google.common.util.concurrent.ThreadFactoryBuilder;
import org.apache.commons.lang.SystemUtils;
import org.apache.http.HttpResponse;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.BasicResponseHandler;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static com.citic.AppConstants.*;

public class ProcessMonitor {
    private static final Logger LOGGER = LoggerFactory.getLogger(ProcessMonitor.class);
    private static final String CHECK_TIME = "CheckTime";
    private static final String AGENT_IP = "AgentIP";
    private static final String MONITOR_TOPIC = AppConf.getConfig(KAFKA_MONITOR_TOPIC);
    private static final String METRICS_TOPIC = AppConf.getConfig(TAGENT_METRICS_TOPIC);

    private ScheduledExecutorService executorService;
    private SimpleKafkaProducer<String, String> producer;

    public ProcessMonitor(SimpleKafkaProducer<String, String> producer) {
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

    /*
    * 从 TAgent 获取监控数据
    * */
    public String getMetricsInfo() {
        String result;
        DefaultHttpClient httpClient = new DefaultHttpClient();
        HttpGet getRequest = new HttpGet(AppConf.getConfig(TAGENT_METRICS_URL));

        String content;
        HttpResponse response;
        try {
            response = httpClient.execute(getRequest);
            if (response.getStatusLine().getStatusCode() != 200) {
                LOGGER.error("Failed : HTTP error code : {}", response.getStatusLine().getStatusCode());
                return null;
            }
            ResponseHandler<String> handler = new BasicResponseHandler();
            content = handler.handleResponse(response);
        } catch (IOException e) {
            LOGGER.error("Failed access to TAgent metrics Listener. info:{}", e.getMessage());
            return null;
        }

        try {
            JSONParser parser = new JSONParser();
            Object obj = parser.parse( content );
            JSONObject jsonObject = (JSONObject) obj;
            jsonObject.put(CHECK_TIME, String.valueOf(System.currentTimeMillis()));
            jsonObject.put(AGENT_IP, Utility.getLocalIP(AppConf.getConfig(AGENT_IP_INTERFACE)));
            result = jsonObject.toJSONString();

            LOGGER.debug(result);
        } catch (ParseException e) {
            LOGGER.error(e.getMessage(), e);
            return null;
        }
        return result;
    }

    /*
    * 发送消息到kafka
    * */
    private void sendStageToKafka(String topic, String stateMessage) {
        producer.send(topic, UUID.randomUUID().toString(), stateMessage);
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
                        // 如果TAgent 存活，则获取监控信息并存入 kafka 中
                        String info = getMetricsInfo();
                        if (!Strings.isNullOrEmpty(info)) {
                            sendStageToKafka(METRICS_TOPIC, info);
                        }
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
            JSONObject jsonObject = new JSONObject();
            String canalState = monitorCanal();
            String tAgentState = monitorTAgent();
            if (!Strings.isNullOrEmpty(canalState))
                jsonObject.put("canal", canalState);
            if (!Strings.isNullOrEmpty(tAgentState))
                jsonObject.put("tagent", tAgentState);

            sendStageToKafka(MONITOR_TOPIC, jsonObject.toJSONString());
        }
    }
}
