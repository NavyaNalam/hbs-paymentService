package com.navya.hotelpaymentservice;

import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;

@Getter
@Setter
public class BookingEvent implements Serializable {
    private Long bookingId;
    private String userId;
    private Double totalFare;

    @Override
    public String toString() {
        return "{" +
                "bookingId: " + bookingId +
                ", totalFare:" + totalFare +
                '}';
    }

}
