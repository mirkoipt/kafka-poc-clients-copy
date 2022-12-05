package ch.ipt.kafka.streams;

import ch.ipt.kafka.avro.Payment;
import io.confluent.kafka.streams.serdes.avro.SpecificAvroSerde;
import org.apache.kafka.clients.admin.NewTopic;
import org.apache.kafka.common.serialization.Serde;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.streams.KeyValue;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.kstream.Consumed;
import org.apache.kafka.streams.kstream.KStream;
import org.apache.kafka.streams.kstream.Materialized;
import org.apache.kafka.streams.kstream.Produced;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.kafka.config.TopicBuilder;


//@Component
public class KafkaStreamsAggregate {

    @Value("${source-topic-transactions}")
    private String sourceTopic;
    String sinkTopic = "total-of-transactions";

    private static final Serde<String> STRING_SERDE = Serdes.String();
    private static final Serde<Double> DOUBLE_SERDE = Serdes.Double();
    private static final Serde<Payment> PAYMENT_SERDE = new SpecificAvroSerde<>();

    @Bean
    public NewTopic topicExampleTotal() {
        return TopicBuilder.name(sinkTopic)
                .partitions(3)
                .replicas(3)
                .build();
    }

    private static final Logger LOGGER = LoggerFactory.getLogger(KafkaStreamsAggregate.class);

    @Autowired
    void buildPipeline(StreamsBuilder streamsBuilder) {

        //computes the total of all transactions per account (e.g. account x : 1632.45, account y: 256.00, ...)

        KStream<String, Double> groupedStream = streamsBuilder.stream(sourceTopic, Consumed.with(STRING_SERDE, PAYMENT_SERDE))
                .map((key, value) -> new KeyValue<>(
                        value.getCardType().toString(), value
                ))
                .groupByKey()
                .aggregate(
                        () -> 0.0,
                        (key, payment, total) -> total + payment.getAmount(), Materialized.with(Serdes.String(), Serdes.Double())
                )
                .toStream()
                .peek((key, value) -> LOGGER.info("Outgoing record - key " +key +" value " + value));

        groupedStream.to(sinkTopic, Produced.with(STRING_SERDE, DOUBLE_SERDE));

        // ksql
//        CREATE stream ACCOUNTS WITH (KAFKA_TOPIC='accounts', VALUE_FORMAT='avro', partitions=1);
//
//        CREATE STREAM TRANSACTIONSREKEYED
//        WITH (PARTITIONS=1) AS
//        SELECT *
//                FROM TRANSACTIONS
//        PARTITION BY accountId;
//
//        CREATE TABLE TOTALTRANSACTIONS AS
//        SELECT a.accountId, SUM(t.amount) AS sum_all_transactions
//        FROM TRANSACTIONSREKEYED t LEFT OUTER JOIN ACCOUNTS a
//        WITHIN 7 DAYS
//        ON t.accountId = a.accountId
//        GROUP BY a.accountId
//        EMIT CHANGES;

        LOGGER.info(String.valueOf(streamsBuilder.build().describe()));
    }

}