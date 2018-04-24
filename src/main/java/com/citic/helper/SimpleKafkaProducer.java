package com.citic.helper;

import com.citic.AppConf;
import org.apache.kafka.clients.producer.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.util.Properties;
import java.util.concurrent.Future;

import static com.citic.AppConstants.*;

/*
* 发送消息到 kafka
* */
public class SimpleKafkaProducer<K, V> {
    private static final Logger LOGGER = LoggerFactory.getLogger(SimpleKafkaProducer.class);

    private KafkaProducer<Object, Object> producer;
    private boolean syncSend;
    private volatile boolean shutDown = false;

    public SimpleKafkaProducer( boolean syncSend) {
        Properties producerConfig = new Properties();

        producerConfig.put("bootstrap.servers", AppConf.getConfig(KAFKA_BOOTSTRAP_SERVERS));
        producerConfig.put("client.id", AppConf.getConfig(KAFKA_CLIENT_ID));
        producerConfig.put("acks", AppConf.getConfig(KAFKA_ACKS));
        producerConfig.put("retries", AppConf.getConfig(KAFKA_RETRIES));

        producerConfig.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, KAFKA_AVRO_SERIALIZER);
        producerConfig.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, KAFKA_AVRO_SERIALIZER);
        producerConfig.put(SCHEMA_REGISTRY_URL_NAME, AppConf.getConfig(KAFKA_REGISTRY_URL));

        this.syncSend = syncSend;
        this.producer = new KafkaProducer<>(producerConfig);
        LOGGER.info("Started Producer.  sync  : {}", syncSend);
    }

    public void send(String topic, V v) {
        send(topic, -1, null, v, new DummyCallback());
    }

    public void send(String topic, K k, V v) {
        send(topic, -1, k, v, new DummyCallback());
    }

    public void send(String topic, int partition, V v) {
        send(topic, partition, null, v, new DummyCallback());
    }

    public void send(String topic, int partition, K k, V v) {
        send(topic, partition, k, v, new DummyCallback());
    }

    public void send(String topic, int partition, K key, V value, Callback callback) {
        if (shutDown) {
            throw new RuntimeException("Producer is closed.");
        }

        try {
            ProducerRecord record;
            if(partition < 0)
                record = new ProducerRecord<Object, Object>(topic, key, value);
            else
                record = new ProducerRecord<Object, Object>(topic, partition, key, value);

            Future<RecordMetadata> future = producer.send(record, callback);
            if (!syncSend) return;
            future.get();
        } catch (Exception e) {
            LOGGER.error("Error while producing event for topic : {}", topic, e);
        }
    }

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
                LOGGER.error("Error while producing message to topic : {}", recordMetadata.topic(), exception);
            }

            if (recordMetadata != null) {
                LOGGER.debug("sent message to topic:{} partition:{}  offset:{}",
                        recordMetadata.topic(), recordMetadata.partition(), recordMetadata.offset());
            }
        }
    }
}