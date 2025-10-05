package com.jpmc.midascore.component;

import com.jpmc.midascore.entity.TransactionRecord;
import com.jpmc.midascore.entity.UserRecord;
import com.jpmc.midascore.foundation.Transaction;
import com.jpmc.midascore.repository.TransactionRecordRepository;
import com.jpmc.midascore.repository.UserRepository;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Component
public class TransactionConsumer {

    private final UserRepository userRepository;
    private final TransactionRecordRepository transactionRecordRepository;

    public TransactionConsumer(UserRepository userRepository,
                               TransactionRecordRepository transactionRecordRepository) {
        this.userRepository = userRepository;
        this.transactionRecordRepository = transactionRecordRepository;
    }

    @KafkaListener(topics = "${general.kafka-topic}", groupId = "midas-task3")
    @Transactional
    public void handleTransaction(Transaction transaction) {

        Long senderId = transaction.getSenderId();
        Long recipientId = transaction.getRecipientId();
        float amount = transaction.getAmount();

        Optional<UserRecord> senderOpt = userRepository.findById(senderId);
        Optional<UserRecord> recipientOpt = userRepository.findById(recipientId);

        // 1) senderId & recipientId must be valid
        if (senderOpt.isEmpty() || recipientOpt.isEmpty()) {
            return; // discard invalid
        }

        UserRecord sender = senderOpt.get();
        UserRecord recipient = recipientOpt.get();

        // 2) sender must have enough balance
        if (sender.getBalance() < amount) {
            return; // discard
        }

        // 3) update balances
        sender.setBalance(sender.getBalance() - amount);
        recipient.setBalance(recipient.getBalance() + amount);

        userRepository.save(sender);
        userRepository.save(recipient);

        // 4) store transaction record
        TransactionRecord record = new TransactionRecord(sender, recipient, amount);
        transactionRecordRepository.save(record);
    }
}
