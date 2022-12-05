package ch.ipt.kafka.producer;

import ch.ipt.kafka.avro.Payment;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.util.concurrent.ListenableFuture;
import org.springframework.util.concurrent.ListenableFutureCallback;

import java.util.UUID;

@Configuration
public class PaymentAvroProducer {
    private static final Logger LOGGER = LoggerFactory.getLogger(PaymentAvroProducer.class);

    @Value("${source-topic-transactions}")
    private String sourceTopic;

    @Autowired
    KafkaTemplate<String, Payment> kafkaTemplatePayment;


    @Scheduled(fixedRate = 2000)
    private void scheduleFixedRateTask() {
        Payment payment = generatePayment();
        sendPayment(payment);
    }

    private Payment generatePayment() {
        double amount = generateDouble();
        String id = UUID.randomUUID().toString();
        AccountDataEnum account = AccountDataEnum.getRandomEnum();
        Payment payment = new Payment(id, account.getAccountId(), account.getPan(), account.getCardType(), amount);
        LOGGER.debug("Payment created with values: {}, {}", payment.getId(), payment.getAmount());
        return payment;
    }

    private Double generateDouble(){
        Double min = 0.0;
        Double max = 1000.0;
        double x = (Math.random() * ((max - min) + 1)) + min;   // This Will Create A Random Number in between Min And Max.
        return Math.round(x * 100.0) / 100.0;
    }

    private void sendPayment(Payment message) {
        ListenableFuture<SendResult<String, Payment>> future =
                kafkaTemplatePayment.send(sourceTopic, message.getId().toString() , message);

        future.addCallback(new ListenableFutureCallback<>() {

            @Override
            public void onSuccess(SendResult<String, Payment> result) {
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
