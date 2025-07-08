package com.navya.hotelpaymentservice;

import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;

@Getter
@Setter
public class BookingEvent implements Serializable {
    private String bookingId;
    private String userId;
    private Integer totalFare;

    @Override
    public String toString() {
        return "{" +
                "bookingId: " + bookingId +
                ", totalFare:" + totalFare +
                '}';
    }

}
