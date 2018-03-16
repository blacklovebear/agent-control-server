package com.citic;

import com.citic.control.ProcessMonitor;
import com.citic.service.ConfigurationService;
import com.citic.service.ExecutorService;
import io.netty.channel.Channel;
import org.glassfish.jersey.netty.httpserver.NettyHttpContainerProvider;
import org.glassfish.jersey.server.ResourceConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URI;

/**
 * Main class.
 *
 */
public class Main {
    // private static final URI BASE_URI = URI.create("http://localhost:8080/");
    private static final Logger LOGGER = LoggerFactory.getLogger(Main.class);
    private static final String AGENT_BASE_URI = "agent.base.uri";

    public static void main(String[] args) {
        ApplicationConf conf = ApplicationConf.getInstance();
        String baseUri = conf.getConfig(AGENT_BASE_URI);

        URI BASE_URI = URI.create(baseUri);

        ResourceConfig resourceConfig = new ResourceConfig(
                ConfigurationService.class,
                ExecutorService.class
                );
        final Channel server = NettyHttpContainerProvider.createHttp2Server(BASE_URI, resourceConfig, null);

        LOGGER.info("Jersey App on Netty Server starting: {}", BASE_URI.toString());

        Runtime.getRuntime().addShutdownHook(new Thread(new Runnable() {
            @Override
            public void run() {
                server.close();
            }
        }));

        ProcessMonitor monitor = new ProcessMonitor();
        monitor.start();

        try {
            Thread.currentThread().join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

    }
}

