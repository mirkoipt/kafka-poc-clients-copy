package ch.ipt.kafka.streams;

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


//@Component
public class KafkaStreamsFilter {

    @Value("${source-topic-transactions}")
    private String sourceTopic;

    @Bean
    public NewTopic topicExampleFiltered() {
        return TopicBuilder.name("filtered-transactions")
                .partitions(3)
                .replicas(3)
                .build();
    }

    private static final Logger LOGGER = LoggerFactory.getLogger(KafkaStreamsFilter.class);
    private static final double LIMIT = 500.00;

    @Autowired
    void buildPipeline(StreamsBuilder streamsBuilder) {
        String sinkTopic = "filtered-transactions";

        //filter: only sends payments over 500.- to a sink topic

        KStream<String, Payment> stream = streamsBuilder.stream(sourceTopic);

        stream
                .filter((key, payment) -> payment.getAmount() > LIMIT)
                .peek((key, payment) -> LOGGER.info("Message: key={}, value={}", key, payment));
        stream.to(sinkTopic);

        // ksql
        // CREATE STREAM TRANSACTIONS WITH (KAFKA_TOPIC='transactions', VALUE_FORMAT='avro', partitions=6);
        // CREATE STREAM BIGTRANSACTIONS AS SELECT * FROM TRANSACTIONS WHERE amount>500 EMIT CHANGES;

        LOGGER.info(String.valueOf(streamsBuilder.build().describe()));
    }

}