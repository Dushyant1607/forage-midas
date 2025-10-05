package com.jpmc.midascore;

import com.jpmc.midascore.foundation.Transaction;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class KafkaTestConsumer {

    private final List<Transaction> consumedTransactions = new ArrayList<>();

    @KafkaListener(topics = "${general.kafka-topic}", groupId = "midas")
    public void consume(Transaction transaction) {
        consumedTransactions.add(transaction);
        System.out.println("✅ Consumed transaction: " + transaction);
    }

    public List<Transaction> getConsumedTransactions() {
        return consumedTransactions;
    }
}

