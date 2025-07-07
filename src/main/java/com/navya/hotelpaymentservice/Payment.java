package com.navya.hotelpaymentservice;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Entity
@Getter
@Setter
public class Payment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long paymentId;

    private Long bookingId;

    private LocalDate dateofPayment;

    @Override
    public String toString() {
        return "Payment [paymentId=" + paymentId + ", bookingId=" + bookingId + ", dateofPayment=" + dateofPayment + "]";
    }
}
