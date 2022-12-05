package ch.ipt.kafka.config;

import ch.ipt.kafka.avro.Payment;
import org.apache.kafka.clients.admin.NewTopic;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.kstream.KStream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.kafka.config.TopicBuilder;
import org.springframework.stereotype.Component;


@Component
/**
 * This class is only here to have a basic topology so the project is runnable without an active streams application
 */
public class KafkaStreamsDefaultTopology {

    @Value("${source-topic-transactions}")
    private String sourceTopic;

    private static final Logger LOGGER = LoggerFactory.getLogger(KafkaStreamsDefaultTopology.class);

    @Bean
    public NewTopic topicExampleTransactions() {
        return TopicBuilder.name(sourceTopic)
                .partitions(3)
                .replicas(3)
                .build();
    }

    @Autowired
    void buildPipeline(StreamsBuilder streamsBuilder) {
        //default topology which peeks at every transaction
        KStream<String, Payment> messageStream = streamsBuilder.stream(sourceTopic);
        messageStream.peek((key, payment) -> LOGGER.trace("Message: key={}, value={}", key, payment));
        LOGGER.info("{}", streamsBuilder.build().describe());
    }
}