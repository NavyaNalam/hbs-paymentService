package com.navya.hotelpaymentservice;

import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;

@Getter
@Setter
public class PaymentEvent {
    private Long bookingId;;
    private Long paymentId;
    private Double totalFare;
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