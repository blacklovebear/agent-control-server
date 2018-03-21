package com.citic;

import com.citic.control.ProcessMonitor;
import com.citic.control.TAgentMetricsMonitor;
import com.citic.helper.SimpleKafkaProducer;
import com.citic.service.ConfigurationService;
import com.citic.service.ExecutorService;
import io.netty.channel.Channel;
import org.glassfish.jersey.netty.httpserver.NettyHttpContainerProvider;
import org.glassfish.jersey.server.ResourceConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URI;


/**
 * AppMain class.
 */
public class AppMain {
    private static final Logger LOGGER = LoggerFactory.getLogger(AppMain.class);
    private static Channel server;
    private static SimpleKafkaProducer<String, String>  producer;
    private static ProcessMonitor processMonitor;
    private static TAgentMetricsMonitor metricsMonitor;

    public static void start() {
        AppConf conf = AppConf.getInstance();
        String baseUri = conf.getConfig(AppConstants.AGENT_BASE_URI);
        URI BASE_URI = URI.create(baseUri);

        ResourceConfig resourceConfig = new ResourceConfig(
                ConfigurationService.class,
                ExecutorService.class
        );
        server = NettyHttpContainerProvider.createHttp2Server(BASE_URI, resourceConfig, null);

        LOGGER.info("Jersey App on Netty Server starting: {}", BASE_URI.toString());

        producer = new SimpleKafkaProducer<>(false);
        // 启动对进程的监控
        processMonitor = new ProcessMonitor(producer);
        processMonitor.start();

        metricsMonitor = new TAgentMetricsMonitor(producer);
        metricsMonitor.start();

        Runtime.getRuntime().addShutdownHook(new Thread(new Runnable() {
            @Override
            public void run() {
                stop();
            }
        }));

        try {
            Thread.currentThread().join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public static void stop() {
        processMonitor.stop();
        metricsMonitor.stop();

        producer.close();
        server.close();
    }

    public static void main(String[] args) {
        start();
    }
}

