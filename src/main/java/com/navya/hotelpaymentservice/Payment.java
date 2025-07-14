package com.navya.hotelpaymentservice;

import org.springframework.data.annotation.Id;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDate;

@Document(collection = "payments")
@Getter
@Setter

public class Payment {

    @Id
    String paymentId;

    String bookingId;

    Integer totalPrice;

    LocalDate dateofPayment;

    String paymentStatus; // PENDING, COMPLETED, FAILED

    @Override
    public String toString() {
        return "Payment [paymentId=" + getPaymentId() + ", bookingId=" + bookingId + ", dateofPayment=" + dateofPayment + "]";
    }
}
