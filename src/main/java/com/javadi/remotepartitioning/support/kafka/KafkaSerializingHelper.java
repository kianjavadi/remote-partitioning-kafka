package com.javadi.remotepartitioning.support.kafka;

import java.util.Set;

public class KafkaSerializingHelper {

    private KafkaSerializingHelper() {}

    public static final Set<String> BINARY_MESSAGES_IN_KAFKA_SERIALIZER_CLASSES_STARTS_WITH = Set.of(
            "org.springframework.batch"
    );

    public static final Set<String> BINARY_MESSAGES_IN_KAFKA_SERIALIZER_CLASSES_EXACT_MATCH = Set.of(
            "[B"
    );

    public static final Set<String> BINARY_MESSAGES_IN_KAFKA_DESERIALIZER_CLASSES_STARTS_WITH = Set.of(
            "org.springframework.batch"
    );

    public static final Set<String> BINARY_MESSAGES_IN_KAFKA_DESERIALIZER_CLASSES_EXACT_MATCH = Set.of(
            "[B"
    );

    public static boolean needsBinarySerializer(Object data) {
        if (data instanceof byte[] || data instanceof Byte[] || data instanceof Byte)
            return true;
        if (data != null && data.getClass() != null) {
            return isInBinarySerializerClasses(data.getClass().getName());
        }
        return false;
    }

    public static boolean needsBinaryDeserializer(String className) {
        if (className != null && !className.isBlank()) {
            return isInBinaryDeserializerClasses(className);
        }
        return false;
    }

    private static boolean isInBinarySerializerClasses(String className) {
        if (isInStartWithSerializerList(className))
            return true;
        return isInExactMatchSerializerList(className);
    }

    private static boolean isInBinaryDeserializerClasses(String className) {
        if (isInStartWithDeserializerList(className))
            return true;
        return isInExactMatchDeserializerList(className);
    }

    private static boolean isInStartWithSerializerList(String className) {
        return KafkaSerializingHelper.BINARY_MESSAGES_IN_KAFKA_SERIALIZER_CLASSES_STARTS_WITH.stream()
                .anyMatch(className::startsWith);
    }

    private static boolean isInExactMatchSerializerList(String className) {
        return KafkaSerializingHelper.BINARY_MESSAGES_IN_KAFKA_SERIALIZER_CLASSES_EXACT_MATCH.contains(className);
    }

    private static boolean isInStartWithDeserializerList(String className) {
        return KafkaSerializingHelper.BINARY_MESSAGES_IN_KAFKA_DESERIALIZER_CLASSES_STARTS_WITH.stream()
                .anyMatch(className::startsWith);
    }

    private static boolean isInExactMatchDeserializerList(String className) {
        return KafkaSerializingHelper.BINARY_MESSAGES_IN_KAFKA_DESERIALIZER_CLASSES_EXACT_MATCH.contains(className);
    }

}
