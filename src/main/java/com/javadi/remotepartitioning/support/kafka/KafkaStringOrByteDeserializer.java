package com.javadi.remotepartitioning.support.kafka;

import org.apache.kafka.common.header.Header;
import org.apache.kafka.common.header.Headers;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.core.serializer.DefaultDeserializer;
import org.springframework.core.serializer.Deserializer;
import org.springframework.kafka.support.serializer.JsonDeserializer;
import org.springframework.messaging.converter.MessageConversionException;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

public class KafkaStringOrByteDeserializer<T> extends JsonDeserializer<T> {

    private static final Logger LOGGER = LogManager.getLogger(KafkaStringOrByteDeserializer.class);

    private static final String TYPE_FIELD_NAME = "__TypeId__";

    private final Deserializer<Object> byteDeserializer = new DefaultDeserializer();

    @Override
    public T deserialize(String topic, Headers headers, byte[] data) {
        // JsonSerializer puts it in the headers
        String typeIdHeader = retrieveHeaderAsString(headers, TYPE_FIELD_NAME);

        if (KafkaSerializingHelper.needsBinaryDeserializer(typeIdHeader)) {
            return deserializeBinaryData(data);
        }

        return super.deserialize(topic, headers, data);
    }

    private String retrieveHeaderAsString(Headers headers, String headerName) {
        Header header = headers.lastHeader(headerName);
        if (header != null && header.value() != null) {
            return new String(header.value(), StandardCharsets.UTF_8);
        }
        return null;
    }

    private T deserializeBinaryData(byte[] data) {
        try (ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(data)) {
            return (T) byteDeserializer.deserialize(byteArrayInputStream);
        } catch (IOException e) {
            LOGGER.error("error while deserialize message body", e);
            throw new MessageConversionException("Could not convert message body", e);
        }
    }

}
