package com.navya.hotelpaymentservice;

import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;

@Getter
@Setter
public class PaymentEvent {
    private String paymentId;
    private String bookingId;
    private Integer totalFare;
    private String status; // Added status field to track payment status

    @Override
    public String toString() {
        return "{" +
                "bookingId: " + bookingId +
                ", totalFare:" + totalFare +
                ", status: " + status + // Include status in the string representation
                '}';
    }
}