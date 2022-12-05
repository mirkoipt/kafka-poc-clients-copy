package ch.ipt.kafka.producer;


import ch.ipt.kafka.protobuf.SimpleTestMessageOuterClass;
import io.confluent.kafka.serializers.AbstractKafkaSchemaSerDeConfig;
import io.confluent.kafka.serializers.protobuf.KafkaProtobufSerializer;
import org.apache.kafka.clients.admin.NewTopic;
import org.apache.kafka.clients.producer.*;
import org.apache.kafka.common.serialization.StringSerializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.kafka.config.TopicBuilder;
import org.springframework.scheduling.annotation.Scheduled;
import java.time.LocalDateTime;
import java.util.Properties;
import java.util.UUID;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

//@Component
public class SimpleMessageProtobufProducer {

    private static final Logger LOGGER = LoggerFactory.getLogger(SimpleMessageProtobufProducer.class);

    @Value("${source-topic-simple-message}")
    private String sourceTopic;


    Producer<String, SimpleTestMessageOuterClass.SimpleTestMessage> producer;


    public SimpleMessageProtobufProducer(@Value("${spring.kafka.properties.schema.registry.url}") String schemaRegistryUrl) {
        Properties properties = new Properties();
        properties.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:9092");
        properties.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        properties.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, KafkaProtobufSerializer.class);
        properties.put(AbstractKafkaSchemaSerDeConfig.SCHEMA_REGISTRY_URL_CONFIG, schemaRegistryUrl);

        producer = new KafkaProducer<>(properties);
    }

    @Bean
    public NewTopic topicExampleSimpleTestMessageProtobuf() {
        return TopicBuilder.name(sourceTopic)
                .partitions(3)
                .replicas(3)
                .build();
    }

    @Scheduled(fixedRate = 5000)
    private void run() {
        SimpleTestMessageOuterClass.SimpleTestMessage simpleMessage = generateSimpleTestMessageProtobuf();
        sendSimpleTestMessage(simpleMessage);
    }


    private SimpleTestMessageOuterClass.SimpleTestMessage generateSimpleTestMessageProtobuf() {

        SimpleTestMessageOuterClass.SimpleTestMessage.Builder simpleMessageBuilder = SimpleTestMessageOuterClass.SimpleTestMessage.newBuilder();
        simpleMessageBuilder.setContent("Test message");
        simpleMessageBuilder.setDateTime(LocalDateTime.now().toString());
        return simpleMessageBuilder.build();
    }

    private void sendSimpleTestMessage(SimpleTestMessageOuterClass.SimpleTestMessage message) {
        String key = UUID.randomUUID().toString();
        ProducerRecord<String, SimpleTestMessageOuterClass.SimpleTestMessage> prodRecord
                = new ProducerRecord<>(sourceTopic, key, message);
        Future<RecordMetadata> future = producer.send(prodRecord);
        try {
            future.get();
            if (future.isDone()) {
                LOGGER.info("Sent protobuf message: {} {}", key, message);
            }
        } catch (InterruptedException | ExecutionException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException(e);
        }
    }


}
