package com.navya.hotelpaymentservice;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientException;
import org.springframework.web.reactive.function.client.WebClientResponseException;

@Service
public class TokenService
{
    private static final Logger logger = LoggerFactory.getLogger(TokenService.class);


    @Autowired
    @Qualifier("authValidateWebClient")
    WebClient authValidateWebClient;

    public String validateToken(String token) throws WebClientResponseException
    {
        logger.info("TokenService.validateToken() called with token: " + token);
        return authValidateWebClient.get()
                .header("Authorization", token)
                .retrieve()
                .bodyToMono(String.class)
                .block(); // Assuming the token is valid for demonstration purposes
    }


/*
    // Instead of using WebClient..I have used Kafka  to send the message to the Booking Service
    @Autowired
    @Qualifier("confirmBookingWebClient")
    WebClient confirmBookingWebClient;

    public String confirmBooking(Long bookingId, String token) throws WebClientResponseException
    {
        logger.info("Confirm Booking called for Booking ID: " + bookingId);
        return String.valueOf(confirmBookingWebClient.post()
                .header("Authorization", token)
                .retrieve()
                .bodyToMono(String.class)
                .doOnError(error -> logger.error("Error while forwarding message to project service: " + error.getMessage()))); // Assuming the token is valid for demonstration purposes
    }
*/

}