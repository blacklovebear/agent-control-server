package com.citic;

public class AppConstants {
    public static final String CLASSPATH_URL_PREFIX = "classpath:";

    public static final String AGENT_BASE_URI = "agent.base.uri";
    
    // cmd
    public static final String CANAL_HOME_DIR = "canal.home.dir";
    public static final String CANAL_START_CMD = "canal.start.cmd";
    public static final String CANAL_STOP_CMD = "canal.stop.cmd";

    public static final String TAGENT_HOME_DIR = "tagent.home.dir";
    public static final String TAGENT_START_CMD = "tagent.start.cmd";
    public static final String TAGENT_STOP_CMD = "tagent.stop.cmd";
    
    // conf
    public static final String CANAL_SERVER_TEMPLATE = "canal.server.template";
    public static final String CANAL_SERVER_CONF = "canal.server.conf";

    public static final String CANAL_INSTANCE_TEMPLATE = "canal.instance.template";
    public static final String CANAL_INSTANCE_CONF = "canal.instance.conf";

    public static final String TAGENT_TEMPLATE = "tagent.template";
    public static final String TAGENT_CONF = "tagent.conf";

    // monitor
    public static final String PROCESS_MONITOR_INTERVAL = "process.monitor.interval";

    public static final String CANAL_MONITOR_CMD = "canal.monitor.cmd";
    public static final String TAGENT_MONITOR_CMD = "tagent.monitor.cmd";

    public static final String CANAL_PROCESS_NAME = "Canal";
    public static final String TAGENT_PROCESS_NAME = "TAgent";

    public static final String KAFKA_STRING_SERIALIZER =
            "org.apache.kafka.common.serialization.StringSerializer";

    public static final String KAFKA_MONITOR_TOPIC = "kafka.monitor.topic";
    public static final String KAFKA_BOOTSTRAP_SERVERS = "kafka.bootstrap.servers";
    public static final String KAFKA_CLIENT_ID = "kafka.client.id";
    public static final String KAFKA_ACKS = "kafka.acks";
    public static final String KAFKA_RETRIES = "kafka.retries";

    // metrics
    public static final String TAGENT_METRICS_URL = "tagent.metrics.url";
    public static final String TAGENT_METRICS_TOPIC = "tagent.metrics.topic";
    public static final String TAGENT_METRICS_CHECK_INTERVAL = "tagent.metrics.check.interval";


}
