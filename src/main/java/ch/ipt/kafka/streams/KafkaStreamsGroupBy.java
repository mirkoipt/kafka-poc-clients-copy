package ch.ipt.kafka.streams;

import ch.ipt.kafka.avro.Payment;
import org.apache.kafka.clients.admin.NewTopic;
import org.apache.kafka.streams.KeyValue;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.kstream.KStream;
import org.apache.kafka.streams.kstream.Materialized;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.kafka.config.TopicBuilder;


//@Component
public class KafkaStreamsGroupBy {

    @Value("${source-topic-transactions}")
    private String sourceTopic;
    String sinkTopic = "grouped-transactions";

    @Bean
    public NewTopic topicExampleGrouped() {
        return TopicBuilder.name(sinkTopic)
                .partitions(3)
                .replicas(3)
                .build();
    }

    private static final Logger LOGGER = LoggerFactory.getLogger(KafkaStreamsGroupBy.class);

    @Autowired
    void buildPipeline(StreamsBuilder streamsBuilder) {

        //counts the number of payments grouped by the cardtype (e.g. "Debit": 12, "Credit": 27)

        KStream<String, Payment> stream = streamsBuilder.stream(sourceTopic);

        stream.map((key, value) -> new KeyValue<>(
                        value.getCardType().toString(), value
                ))
                .groupByKey()
                .count(Materialized.as("grouped-transactions-count"))
                .toStream()
                .peek((key, value) -> LOGGER.info("Grouped Transactions: key={}, value={}", key, value))
                .to(sinkTopic);

        stream.to(sinkTopic);

        // ksql
        // SELECT cardType, COUNT (*) as amount FROM TRANSACTIONS GROUP BY cardType EMIT CHANGES;


        LOGGER.info(String.valueOf(streamsBuilder.build().describe()));
    }

}
