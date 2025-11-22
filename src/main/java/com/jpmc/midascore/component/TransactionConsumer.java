package com.jpmc.midascore.component;

import com.jpmc.midascore.foundation.Incentive;
import com.jpmc.midascore.foundation.Transaction;
import com.jpmc.midascore.entity.TransactionRecord;
import com.jpmc.midascore.entity.UserRecord;
import com.jpmc.midascore.repository.TransactionRecordRepository;
import com.jpmc.midascore.repository.UserRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

@Component
public class TransactionConsumer {

    private final UserRepository userRepository;
    private final TransactionRecordRepository transactionRecordRepository;
    private final RestTemplate restTemplate;
    private final String incentiveUrl;

    public TransactionConsumer(UserRepository userRepository,
                               TransactionRecordRepository transactionRecordRepository,
                               RestTemplate restTemplate,
                               @Value("${general.incentive-url}") String incentiveUrl) {
        this.userRepository = userRepository;
        this.transactionRecordRepository = transactionRecordRepository;
        this.restTemplate = restTemplate;
        this.incentiveUrl = incentiveUrl;
    }

    @KafkaListener(topics = "${general.kafka-topic}", groupId = "midas-task4")
    @Transactional
    public void handleTransaction(Transaction transaction) {

        Long senderId = transaction.getSenderId();
        Long recipientId = transaction.getRecipientId();
        float amount = transaction.getAmount();

        var senderOpt = userRepository.findById(senderId);
        var recipientOpt = userRepository.findById(recipientId);

        // 1. Validate users exist
        if (senderOpt.isEmpty() || recipientOpt.isEmpty()) {
            return; // discard
        }

        UserRecord sender = senderOpt.get();
        UserRecord recipient = recipientOpt.get();

        // 2. Validate sender balance
        if (sender.getBalance() < amount) {
            return; // discard
        }

        // 3. Call Incentive API
        Incentive incentiveResponse =
                restTemplate.postForObject(incentiveUrl, transaction, Incentive.class);

        float incentiveAmount = 0f;
        if (incentiveResponse != null) {
            incentiveAmount = incentiveResponse.getAmount();
        }

        // 4. Update balances
        sender.setBalance(sender.getBalance() - amount);
        recipient.setBalance(recipient.getBalance() + amount + incentiveAmount);

        userRepository.save(sender);
        userRepository.save(recipient);

        // 5. Persist transaction + incentive
        TransactionRecord record =
                new TransactionRecord(sender, recipient, amount, incentiveAmount);

        transactionRecordRepository.save(record);
    }
}
