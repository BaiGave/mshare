/*
 * Decompiled with CFR 0.152.
 */
package org.apache.logging.log4j.core.appender.mom.kafka;

import java.nio.charset.StandardCharsets;
import java.util.Objects;
import java.util.Properties;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.serialization.ByteArraySerializer;
import org.apache.logging.log4j.core.LoggerContext;
import org.apache.logging.log4j.core.appender.AbstractManager;
import org.apache.logging.log4j.core.appender.ManagerFactory;
import org.apache.logging.log4j.core.appender.mom.kafka.DefaultKafkaProducerFactory;
import org.apache.logging.log4j.core.appender.mom.kafka.KafkaProducerFactory;
import org.apache.logging.log4j.core.config.Property;
import org.apache.logging.log4j.core.util.Integers;
import org.apache.logging.log4j.core.util.Log4jThread;

public class KafkaManager
extends AbstractManager {
    public static final String DEFAULT_TIMEOUT_MILLIS = "30000";
    static KafkaProducerFactory producerFactory = new DefaultKafkaProducerFactory();
    private final Properties config = new Properties();
    private Producer<byte[], byte[]> producer;
    private final int timeoutMillis;
    private final String topic;
    private final String key;
    private final boolean syncSend;
    private final boolean sendTimestamp;
    private static final KafkaManagerFactory factory = new KafkaManagerFactory();

    public KafkaManager(LoggerContext loggerContext, String name, String topic, boolean syncSend, Property[] properties, String key) {
        this(loggerContext, name, topic, syncSend, false, properties, key);
    }

    private KafkaManager(LoggerContext loggerContext, String name, String topic, boolean syncSend, boolean sendTimestamp, Property[] properties, String key) {
        super(loggerContext, name);
        this.topic = Objects.requireNonNull(topic, "topic");
        this.syncSend = syncSend;
        this.sendTimestamp = sendTimestamp;
        this.config.put("key.serializer", ByteArraySerializer.class);
        this.config.put("value.serializer", ByteArraySerializer.class);
        this.config.put("batch.size", (Object)0);
        for (Property property : properties) {
            this.config.setProperty(property.getName(), property.getValue());
        }
        this.key = key;
        String timeoutMillis = this.config.getProperty("timeout.ms");
        if (timeoutMillis == null) {
            timeoutMillis = this.config.getProperty("request.timeout.ms", DEFAULT_TIMEOUT_MILLIS);
        }
        this.timeoutMillis = Integers.parseInt(timeoutMillis);
    }

    @Override
    public boolean releaseSub(long timeout, TimeUnit timeUnit) {
        if (timeout > 0L) {
            this.closeProducer(timeout, timeUnit);
        } else {
            this.closeProducer(this.timeoutMillis, TimeUnit.MILLISECONDS);
        }
        return true;
    }

    private void closeProducer(long timeout, TimeUnit timeUnit) {
        if (this.producer != null) {
            Log4jThread closeThread = new Log4jThread(() -> {
                if (this.producer != null) {
                    this.producer.close();
                }
            }, "KafkaManager-CloseThread");
            closeThread.setDaemon(true);
            closeThread.start();
            try {
                closeThread.join(timeUnit.toMillis(timeout));
            }
            catch (InterruptedException ignore) {
                Thread.currentThread().interrupt();
            }
        }
    }

    @Deprecated
    public void send(byte[] msg) throws ExecutionException, InterruptedException, TimeoutException {
        this.send(msg, null);
    }

    public void send(byte[] msg, Long eventTimestamp) throws ExecutionException, InterruptedException, TimeoutException {
        if (this.producer != null) {
            byte[] newKey = null;
            if (this.key != null && this.key.contains("${")) {
                newKey = this.getLoggerContext().getConfiguration().getStrSubstitutor().replace(this.key).getBytes(StandardCharsets.UTF_8);
            } else if (this.key != null) {
                newKey = this.key.getBytes(StandardCharsets.UTF_8);
            }
            Long timestamp = this.sendTimestamp ? eventTimestamp : null;
            ProducerRecord newRecord = new ProducerRecord(this.topic, null, timestamp, (Object)newKey, (Object)msg);
            if (this.syncSend) {
                Future response = this.producer.send(newRecord);
                response.get(this.timeoutMillis, TimeUnit.MILLISECONDS);
            } else {
                this.producer.send(newRecord, (metadata, e) -> {
                    if (e != null) {
                        LOGGER.error("Unable to write to Kafka in appender [" + this.getName() + "]", (Throwable)e);
                    }
                });
            }
        }
    }

    public void startup() {
        if (this.producer == null) {
            this.producer = producerFactory.newKafkaProducer(this.config);
        }
    }

    public String getTopic() {
        return this.topic;
    }

    @Deprecated
    public static KafkaManager getManager(LoggerContext loggerContext, String name, String topic, boolean syncSend, Property[] properties, String key) {
        return KafkaManager.getManager(loggerContext, name, topic, syncSend, false, properties, key);
    }

    static KafkaManager getManager(LoggerContext loggerContext, String name, String topic, boolean syncSend, boolean sendTimestamp, Property[] properties, String key) {
        StringBuilder sb = new StringBuilder(name);
        sb.append(" ").append(topic).append(" ").append(syncSend).append(" ").append(sendTimestamp);
        for (Property prop : properties) {
            sb.append(" ").append(prop.getName()).append("=").append(prop.getValue());
        }
        return KafkaManager.getManager(sb.toString(), factory, new FactoryData(loggerContext, topic, syncSend, sendTimestamp, properties, key));
    }

    private static class KafkaManagerFactory
    implements ManagerFactory<KafkaManager, FactoryData> {
        private KafkaManagerFactory() {
        }

        @Override
        public KafkaManager createManager(String name, FactoryData data) {
            return new KafkaManager(data.loggerContext, name, data.topic, data.syncSend, data.sendTimestamp, data.properties, data.key);
        }
    }

    private static class FactoryData {
        private final LoggerContext loggerContext;
        private final String topic;
        private final boolean syncSend;
        private final boolean sendTimestamp;
        private final Property[] properties;
        private final String key;

        public FactoryData(LoggerContext loggerContext, String topic, boolean syncSend, boolean sendTimestamp, Property[] properties, String key) {
            this.loggerContext = loggerContext;
            this.topic = topic;
            this.syncSend = syncSend;
            this.sendTimestamp = sendTimestamp;
            this.properties = properties;
            this.key = key;
        }
    }
}

