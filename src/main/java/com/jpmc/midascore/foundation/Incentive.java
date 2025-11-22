package com.jpmc.midascore.foundation;

public class Incentive {

    // must match JSON: { "amount": ... }
    private float amount;

    public Incentive() {
    }

    public Incentive(float amount) {
        this.amount = amount;
    }

    public float getAmount() {
        return amount;
    }

    public void setAmount(float amount) {
        this.amount = amount;
    }
}
