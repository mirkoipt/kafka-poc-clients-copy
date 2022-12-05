package ch.ipt.kafka.producer;

import ch.ipt.kafka.avro.Account;
import org.apache.kafka.clients.admin.NewTopic;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.util.concurrent.ListenableFuture;
import org.springframework.util.concurrent.ListenableFutureCallback;

import javax.annotation.PostConstruct;
import java.util.Arrays;

@Configuration
public class AccountAvroProducer {

    private static final Logger LOGGER = LoggerFactory.getLogger(AccountAvroProducer.class);

    @Value("${source-topic-accounts}")
    private String sourceTopic;
    @Autowired
    KafkaTemplate<String, Account> kafkaTemplateAccount;

    @Bean
    public NewTopic topicAccounts() {
        return TopicBuilder.name(sourceTopic)
                .partitions(3)
                .replicas(3)
                .compact()
                .build();
    }


    @PostConstruct
    public void sendAccountMessages() {
        Arrays.asList(AccountDataEnum.values())
                .forEach(
                        accountEnum ->  {
                            Account account = AccountDataEnum.getAccount(accountEnum);
                            sendAccount(account, sourceTopic);
                });
    }


    public void sendAccount(Account message, String topic) {
        ListenableFuture<SendResult<String, Account>> future =
                kafkaTemplateAccount.send(topic, message.getAccountId().toString() , message);
        future.addCallback(new ListenableFutureCallback<>() {
            @Override
            public void onSuccess(SendResult<String, Account> result) {
                LOGGER.info("Message [{}] delivered with offset {}",
                        message,
                        result.getRecordMetadata().offset());
            }
            @Override
            public void onFailure(Throwable ex) {
                LOGGER.warn("Unable to deliver message [{}]. {}",
                        message,
                        ex.getMessage());
            }
        });
    }

}
