package ch.ipt.kafka.streams;

import ch.ipt.kafka.avro.Payment;
import org.apache.kafka.clients.admin.NewTopic;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.kstream.Branched;
import org.apache.kafka.streams.kstream.KStream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.kafka.config.TopicBuilder;


//@Component
public class KafkaStreamsSplitter {

    @Value("${source-topic-transactions}")
    private String sourceTopic;

    String creditSinkTopic = "credit-transactions";
    String debitSinkTopic = "debit-transactions";
    String undefinedSinkTopic = "undefined-transactions";

    private static final Logger LOGGER = LoggerFactory.getLogger(KafkaStreamsSplitter.class);

    @Bean
    public NewTopic topicExampleCredit() {
        return TopicBuilder.name(creditSinkTopic)
                .partitions(3)
                .replicas(3)
                .build();
    }

    @Bean
    public NewTopic topicExampleDebit() {
        return TopicBuilder.name(debitSinkTopic)
                .partitions(3)
                .replicas(3)
                .build();
    }

    @Bean
    public NewTopic topicExampleUndefined() {
        return TopicBuilder.name(undefinedSinkTopic)
                .partitions(3)
                .replicas(3)
                .build();
    }
    @Autowired
    void buildPipeline(StreamsBuilder streamsBuilder) {

        //splits the topic in two different sink topics: one for debit payments (into debit-transactions) and one for credit transactions (credit-transactions).

        KStream<String, Payment> stream = streamsBuilder
                .stream("transactions");

        stream.split()
                .branch(
                        (key, value) -> value.getCardType().toString().equals("Debit"),
                        Branched.withConsumer(s -> s
                                .peek((key, payment) -> LOGGER.info("Debit Message: key={}, value={}", key, payment))
                                .to(debitSinkTopic))
                )
                .branch(
                        (key, value) -> value.getCardType().toString().equals("Credit"),
                        Branched.withConsumer(s -> s
                                .peek((key, payment) -> LOGGER.info("Credit Message: key={}, value={}", key, payment))
                                .to(creditSinkTopic))
                )
                .branch(
                        (key, value) -> true, //catch unknown events
                        Branched.withConsumer(s -> s
                                .peek((key, payment) -> LOGGER.info("Unknow Message: key={}, value={}", key, payment))
                                .to(undefinedSinkTopic))
                );

        //ksql
        // CREATE STREAM DEBIT AS
        // SELECT * FROM TRANSACTIONS WHERE cardType = 'Debit' EMIT CHANGES;
        //
        // CREATE STREAM CREDIT AS
        // SELECT * FROM TRANSACTIONS WHERE cardType = 'Credit' EMIT CHANGES;

        LOGGER.info(String.valueOf(streamsBuilder.build().describe()));
    }

}