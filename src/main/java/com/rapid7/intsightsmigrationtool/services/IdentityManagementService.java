package com.rapid7.intsightsmigrationtool.services;

import org.springframework.http.*;
import org.springframework.lang.Nullable;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.RestTemplate;

import java.util.UUID;

import static com.rapid7.intsightsmigrationtool.services.ApiConfiguration.*;

public interface IdentityManagementService {

    default org.springframework.http.HttpHeaders getRequestHeader(ApiConfiguration apiConfiguration, boolean isRbac) {
        String correlationId = UUID.randomUUID().toString().replace("-", "");
        final org.springframework.http.HttpHeaders httpHeaders = new org.springframework.http.HttpHeaders();
        httpHeaders.set(HttpHeaders.CONTENT_TYPE, "application/json");
        httpHeaders.set(R7_CORRELATION_ID, correlationId);
        httpHeaders.set(R7_CONSUMER, apiConfiguration.getConsumerHeader());
        if (isRbac) {
            httpHeaders.set(X_INTERNAL_SERVICE_KEY, apiConfiguration.getRbacApiKey());
        } else {
            httpHeaders.set(X_API_KEY, apiConfiguration.getApiKey());
        }

        return httpHeaders;
    }

    default void waitForBuffer() {
        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    default ResponseEntity<?> sendRequest(RestTemplate restTemplate, String url, HttpMethod method, @Nullable HttpEntity<?> requestEntity, Class<?> responseType) throws RetryableException {

        waitForBuffer();

        ResponseEntity<?> responseEntity = null;

        try {
            responseEntity = restTemplate.exchange(url, method, requestEntity, responseType);
            if (responseEntity.getStatusCode().value() >= 500 || responseEntity.getStatusCode().equals(HttpStatus.TOO_MANY_REQUESTS)) {
                System.out.println("Received exception, retrying...");
                throw new RetryableException();
            }
        } catch (HttpClientErrorException e) {
            if (e.getRawStatusCode() == HttpStatus.CONFLICT.value()) {
                responseEntity = ResponseEntity.status(HttpStatus.CONFLICT).build();
            } else {
                throw new RuntimeException("Unexpected error occurred while processing HTTP request: " + e.getMessage());
            }
        } catch (HttpServerErrorException e) {
            if (e.getRawStatusCode() == HttpStatus.INTERNAL_SERVER_ERROR.value()) {
                responseEntity = ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
            } else {
                throw new RuntimeException("Unexpected error occurred while processing HTTP request: " + e.getMessage());
            }
        } catch (Exception e) {
            throw new RuntimeException("Unexpected error occurred while processing HTTP request: " + e.getMessage());
        }

        return responseEntity;
    }
}
