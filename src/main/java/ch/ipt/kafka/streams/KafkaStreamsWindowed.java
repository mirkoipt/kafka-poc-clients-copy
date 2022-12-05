package ch.ipt.kafka.streams;

import ch.ipt.kafka.avro.Payment;
import org.apache.kafka.clients.admin.NewTopic;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.kstream.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.kafka.config.TopicBuilder;

import java.time.Duration;


//@Component
public class KafkaStreamsWindowed {

    @Value("${source-topic-transactions}")
    private String sourceTopic;
    String sinkTopic = "transactions-last-minute";


    @Bean
    public NewTopic topicExampleLastMinute() {
        return TopicBuilder.name(sinkTopic)
                .partitions(3)
                .replicas(3)
                .build();
    }
    private static final Logger LOGGER = LoggerFactory.getLogger(KafkaStreamsWindowed.class);

    @Autowired
    void buildPipeline(StreamsBuilder streamsBuilder) {

        //computes the number of transactions per card type within the last minute
        KStream<String, Payment> stream = streamsBuilder.stream(sourceTopic);

        TimeWindows window = TimeWindows.ofSizeWithNoGrace(Duration.ofMinutes(1));
        stream
                .groupBy((k, v) -> v.getCardType().toString())
                .windowedBy(window)
                .count(Materialized.as("transactions-last-minute-count"))
                .toStream()
                .peek((key, value) -> LOGGER.info("Total of transactions in the last minute: key={}, value={}", key, value))
                .to(sinkTopic, Produced.keySerde(WindowedSerdes.timeWindowedSerdeFrom(String.class, window.sizeMs)));


        // ksql
//        SELECT cardType, COUNT (*) as amount
//        FROM TRANSACTIONS
//        WHERE ROWTIME>UNIX_TIMESTAMP()-60000
//        GROUP BY cardType EMIT CHANGES;

        LOGGER.info(String.valueOf(streamsBuilder.build().describe()));
    }

}