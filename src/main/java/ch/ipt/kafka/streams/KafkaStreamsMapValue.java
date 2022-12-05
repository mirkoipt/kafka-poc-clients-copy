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
public class KafkaStreamsMapValue {

    @Value("${source-topic-transactions}")
    private String sourceTopic;

    private static final Logger LOGGER = LoggerFactory.getLogger(KafkaStreamsMapValue.class);

    @Bean
    public NewTopic topicExampleRounded() {
        return TopicBuilder.name("rounded-transactions")
                .partitions(3)
                .replicas(3)
                .build();
    }
    @Autowired
    void buildPipeline(StreamsBuilder streamsBuilder) {
        String sinkTopic = "rounded-transactions";

        //rounds up every amount to the next whole number (e.g. 12.20 --> 13.00)

        KStream<String, Payment> stream = streamsBuilder.stream(sourceTopic);

        stream.mapValues(value -> {
                    value.setAmount(Math.ceil(value.getAmount()));
                    return value;
                })
                .peek((key, payment) -> LOGGER.info("Message: key={}, value={}", key, payment));
        stream.to(sinkTopic);

        // ksql
        // CREATE STREAM ROUNDUP AS SELECT  ID, ACCOUNTID,  CARDNUMBER, CARDTYPE, CEIL( AMOUNT) AS AMOUNT
        // FROM TRANSACTIONS EMIT CHANGES;

        LOGGER.info(String.valueOf(streamsBuilder.build().describe()));
    }

}