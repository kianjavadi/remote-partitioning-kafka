package com.javadi.remotepartitioning.support.kafka;

import org.apache.kafka.common.serialization.StringSerializer;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.core.serializer.DefaultSerializer;
import org.springframework.core.serializer.Serializer;
import org.springframework.kafka.support.serializer.JsonSerializer;
import org.springframework.messaging.converter.MessageConversionException;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

public class KafkaStringOrByteSerializer<T> extends JsonSerializer<T> {

    private static final Logger LOGGER = LogManager.getLogger(KafkaStringOrByteSerializer.class);

    private final Serializer<Object> byteSerializer = new DefaultSerializer();
    private final org.apache.kafka.common.serialization.Serializer<String> stringSerializer = new StringSerializer();

    @Override
    public byte[] serialize(String topic, T data) {
        if (KafkaSerializingHelper.needsBinarySerializer(data)) {
            return this.serializeBinary(data);
        } else {
            return stringSerializer.serialize(topic, (String) data);
        }
    }

    private byte[] serializeBinary(Object data) {
        try (ByteArrayOutputStream output = new ByteArrayOutputStream()) {
            byteSerializer.serialize(data, output);
            return output.toByteArray();
        } catch (IOException e) {
            LOGGER.error("error while serialize message", e);
            throw new MessageConversionException("Cannot convert object to bytes", e);
        }
    }

}
