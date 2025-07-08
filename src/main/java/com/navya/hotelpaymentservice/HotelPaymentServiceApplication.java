package com.navya.hotelpaymentservice;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
@ComponentScan
public class HotelPaymentServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(HotelPaymentServiceApplication.class, args);
    }

}
