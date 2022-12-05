package ch.ipt.kafka.streams;

import ch.ipt.kafka.avro.Account;
import ch.ipt.kafka.avro.AccountPayment;
import ch.ipt.kafka.avro.Payment;
import org.apache.kafka.clients.admin.NewTopic;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.kstream.KStream;
import org.apache.kafka.streams.kstream.KTable;
import org.apache.kafka.streams.kstream.Materialized;
import org.apache.kafka.streams.kstream.TableJoined;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.kafka.config.TopicBuilder;
import org.springframework.stereotype.Component;


@Component
public class KafkaStreamsJoin {

    @Value("${source-topic-transactions}")
    private String sourceTransactions;
    @Value("${source-topic-accounts}")
    private String sourceAccounts;
    String sinkTopic = "filtered-join";


    @Bean
    public NewTopic topicExampleFilteredJoin() {
        return TopicBuilder.name(sinkTopic)
                .partitions(3)
                .replicas(3)
                .build();
    }

    private static final Logger LOGGER = LoggerFactory.getLogger(KafkaStreamsJoin.class);

    @Autowired
    void buildPipeline(StreamsBuilder streamsBuilder) {

        // filters all Payments for the customers with last name "Fischer"

        KStream<String, Payment> transactionStream = streamsBuilder.stream(sourceTransactions);
        KStream<String, Account> accountStream = streamsBuilder.stream(sourceAccounts);

        KTable<String, Account> accountTable = accountStream.toTable(Materialized.as("filtered-join-account-table"));
        transactionStream.toTable(Materialized.as("filtered-join-payment-table"))
                .join(accountTable, t -> t.getAccountId().toString(),
                        this::joinAccountTransaction,
                        TableJoined.as("filtered-join-join"),
                        Materialized.as("filtered-join-mat"))
                .toStream()
                .filter((k, v) -> "Fischer".equals(v.getLastname().toString()))
                .peek((key, value) -> LOGGER.info("Message of filtered join: key={}, value={}", key, value))
                .to(sinkTopic);

        // ksql
//        CREATE STREAM TRANSACTIONSREKEYED
//        WITH (PARTITIONS=1) AS
//        SELECT *
//                FROM TRANSACTIONS
//        PARTITION BY accountId;

//        CREATE STREAM FISCHERPAYMENTS AS
//        SELECT t.*
//        FROM TRANSACTIONSREKEYED t LEFT OUTER JOIN ACCOUNTS a
//        WITHIN 7 DAYS
//        ON t.accountId = a.accountId
//        WHERE a.lastname = 'Fischer'
//        EMIT CHANGES;

        LOGGER.info(String.valueOf(streamsBuilder.build().describe()));
    }

    private AccountPayment joinAccountTransaction(Payment payment, Account account) {
        return new AccountPayment(account.getAccountId(),
                account.getSurname(),
                account.getLastname(),
                account.getStreet(),
                account.getCity(),
                payment.getId(),
                payment.getCardNumber(),
                payment.getCardType(),
                payment.getAmount());
    }

}