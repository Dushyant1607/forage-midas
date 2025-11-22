package com.jpmc.midascore.entity;

import jakarta.persistence.*;

@Entity
public class TransactionRecord {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private float amount;

    // NEW: incentive amount from incentive API
    private float incentive;

    @ManyToOne(optional = false)
    @JoinColumn(name = "sender_id")
    private UserRecord sender;

    @ManyToOne(optional = false)
    @JoinColumn(name = "recipient_id")
    private UserRecord recipient;

    public TransactionRecord() {
    }

    public TransactionRecord(UserRecord sender, UserRecord recipient, float amount, float incentive) {
        this.sender = sender;
        this.recipient = recipient;
        this.amount = amount;
        this.incentive = incentive;
    }

    public Long getId() {
        return id;
    }

    public float getAmount() {
        return amount;
    }

    public float getIncentive() {
        return incentive;
    }

    public UserRecord getSender() {
        return sender;
    }

    public UserRecord getRecipient() {
        return recipient;
    }
}
