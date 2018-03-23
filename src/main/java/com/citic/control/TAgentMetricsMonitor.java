package com.citic.control;

import com.citic.AppConf;
import com.citic.helper.ShellExecutor;
import com.citic.helper.SimpleKafkaProducer;
import com.google.common.util.concurrent.ThreadFactoryBuilder;
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

import java.io.IOException;
import java.util.UUID;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import static com.citic.AppConstants.*;

public class TAgentMetricsMonitor {
    private static final Logger LOGGER = LoggerFactory.getLogger(TAgentMetricsMonitor.class);
    private static final String CHECK_TIME = "CheckTime";

    private ScheduledExecutorService executorService;
    private AppConf conf;
    private String metricsTopic;
    private SimpleKafkaProducer<String, String> producer;

    public TAgentMetricsMonitor(SimpleKafkaProducer<String, String> producer) {
        conf = AppConf.getInstance();
        metricsTopic = conf.getConfig(TAGENT_METRICS_TOPIC);

        this.producer = producer;
    }

    public String getMetricsInfo() {
        String result = null;
        DefaultHttpClient httpClient = new DefaultHttpClient();
        HttpGet getRequest = new HttpGet(conf.getConfig(TAGENT_METRICS_URL));

        HttpResponse response;
        try {
            response = httpClient.execute(getRequest);
        } catch (IOException e) {
            LOGGER.error(e.getMessage(), e);
            return result;
        }

        if (response.getStatusLine().getStatusCode() != 200) {
            LOGGER.error("Failed : HTTP error code : {}",
                    response.getStatusLine().getStatusCode());
            return result;
        }

        String content;
        try {
            ResponseHandler<String> handler = new BasicResponseHandler();
            content = handler.handleResponse(response);
        } catch (IOException e) {
            LOGGER.error(e.getMessage(), e);
            return result;
        }

        try {
            JSONParser parser = new JSONParser();
            Object obj = parser.parse( content );
            JSONObject jsonObject = (JSONObject) obj;
            jsonObject.put(CHECK_TIME, String.valueOf(System.currentTimeMillis()));
            result = jsonObject.toJSONString();

            LOGGER.debug(result);
        } catch (ParseException e) {
            LOGGER.error(e.getMessage(), e);
        }
        return result;
    }

    public void start() {
        // 进程检查时间间隔
        int interval = Integer.parseInt(conf.getConfig(TAGENT_METRICS_CHECK_INTERVAL));
        executorService = Executors.newSingleThreadScheduledExecutor(
                new ThreadFactoryBuilder().setNameFormat("tAgent-metrics-%d")
                        .build());

        GetMetricsRunnable tAgentMetrics = new GetMetricsRunnable();
        executorService.scheduleWithFixedDelay(tAgentMetrics, 0, interval, TimeUnit.SECONDS);
    }

    public void stop() {
        executorService.shutdown();
        try {
            while (!executorService.awaitTermination(500, TimeUnit.MILLISECONDS)) {
                LOGGER.debug("Waiting for tAgent metrics monitor to terminate");
            }
        } catch (InterruptedException e) {
            LOGGER.debug("Interrupted while waiting for tAgent metrics monitor to terminate");
            Thread.currentThread().interrupt();
        }
    }

    /*
    * 发送消息到kafka
    * */
    private void sendStageToKafka(String stateMessage) {
        producer.send(metricsTopic, UUID.randomUUID().toString(), stateMessage);
    }

    private class GetMetricsRunnable implements Runnable {
        private GetMetricsRunnable() { }
        @Override
        public void run() {
            try {
                String info = getMetricsInfo();
                if (info != null) {
                    sendStageToKafka(info);
                }
            } catch (Exception e) {
                LOGGER.error(e.getMessage(), e);
            }
        }
    }
}
