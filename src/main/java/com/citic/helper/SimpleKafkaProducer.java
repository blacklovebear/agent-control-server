package com.citic.helper;

import static com.citic.AppConstants.DEFAULT_KEY_SERIALIZER;
import static com.citic.AppConstants.DEFAULT_VALUE_SERIAIZER;
import static com.citic.AppConstants.KAFKA_ACKS;
import static com.citic.AppConstants.KAFKA_AVRO_SERIALIZER;
import static com.citic.AppConstants.KAFKA_BOOTSTRAP_SERVERS;
import static com.citic.AppConstants.KAFKA_CLIENT_ID;
import static com.citic.AppConstants.KAFKA_REGISTRY_URL;
import static com.citic.AppConstants.KAFKA_RETRIES;
import static com.citic.AppConstants.SCHEMA_REGISTRY_URL_NAME;

import com.citic.AppConf;
import java.util.Properties;
import java.util.concurrent.Future;
import org.apache.kafka.clients.producer.Callback;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.clients.producer.RecordMetadata;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The type Simple kafka producer.
 *
 * @param <K> the type parameter
 * @param <V> the type parameter
 */
/*
 * 发送消息到 kafka
 * */
public class SimpleKafkaProducer<K, V> {

    private static final Logger LOGGER = LoggerFactory.getLogger(SimpleKafkaProducer.class);

    private KafkaProducer<Object, Object> producer;
    private boolean syncSend;
    private volatile boolean shutDown = false;

    /**
     * Instantiates a new Simple kafka producer.
     *
     * @param syncSend the sync send
     * @param useAvro the use avro
     */
    public SimpleKafkaProducer(boolean syncSend, boolean useAvro) {
        Properties producerConfig = new Properties();

        producerConfig.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG,
            AppConf.getConfig(KAFKA_BOOTSTRAP_SERVERS));
        producerConfig.put(ProducerConfig.CLIENT_ID_CONFIG, AppConf.getConfig(KAFKA_CLIENT_ID));
        producerConfig.put(ProducerConfig.ACKS_CONFIG, AppConf.getConfig(KAFKA_ACKS));
        producerConfig.put(ProducerConfig.RETRIES_CONFIG, AppConf.getConfig(KAFKA_RETRIES));

        if (useAvro) {
            producerConfig.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, KAFKA_AVRO_SERIALIZER);
            producerConfig.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, KAFKA_AVRO_SERIALIZER);
            producerConfig.put(SCHEMA_REGISTRY_URL_NAME, AppConf.getConfig(KAFKA_REGISTRY_URL));
        } else {
            producerConfig.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, DEFAULT_KEY_SERIALIZER);
            producerConfig
                .put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, DEFAULT_VALUE_SERIAIZER);
        }

        this.syncSend = syncSend;
        this.producer = new KafkaProducer<>(producerConfig);
        LOGGER.info("Started Producer.  sync  : {}", syncSend);
    }

    /**
     * Send.
     *
     * @param topic the topic
     * @param v the v
     */
    public void send(String topic, V v) {
        send(topic, -1, null, v, new DummyCallback());
    }

    /**
     * Send.
     *
     * @param topic the topic
     * @param k the k
     * @param v the v
     */
    public void send(String topic, K k, V v) {
        send(topic, -1, k, v, new DummyCallback());
    }

    /**
     * Send.
     *
     * @param topic the topic
     * @param partition the partition
     * @param v the v
     */
    public void send(String topic, int partition, V v) {
        send(topic, partition, null, v, new DummyCallback());
    }

    /**
     * Send.
     *
     * @param topic the topic
     * @param partition the partition
     * @param k the k
     * @param v the v
     */
    public void send(String topic, int partition, K k, V v) {
        send(topic, partition, k, v, new DummyCallback());
    }

    /**
     * Send.
     *
     * @param topic the topic
     * @param partition the partition
     * @param key the key
     * @param value the value
     * @param callback the callback
     */
    public void send(String topic, int partition, K key, V value, Callback callback) {
        if (shutDown) {
            throw new RuntimeException("Producer is closed.");
        }

        try {
            ProducerRecord record;
            if (partition < 0) {
                record = new ProducerRecord<Object, Object>(topic, key, value);
            } else {
                record = new ProducerRecord<Object, Object>(topic, partition, key, value);
            }

            Future<RecordMetadata> future = producer.send(record, callback);
            if (!syncSend) {
                return;
            }
            future.get();
        } catch (Exception e) {
            LOGGER.error("Error while producing event for topic : {}", topic, e);
        }
    }

    /**
     * Close.
     */
    public void close() {
        shutDown = true;
        try {
            producer.close();
        } catch (Exception e) {
            LOGGER.error("Exception occurred while stopping the producer", e);
        }
    }

    private class DummyCallback implements Callback {

        @Override
        public void onCompletion(RecordMetadata recordMetadata, Exception exception) {
            if (exception != null) {
                LOGGER.error("Error while producing message {}", exception.getMessage(), exception);
            }

            if (recordMetadata != null) {
                LOGGER.debug("sent message to topic:{} partition:{}  offset:{}",
                    recordMetadata.topic(), recordMetadata.partition(), recordMetadata.offset());
            }
        }
    }
}