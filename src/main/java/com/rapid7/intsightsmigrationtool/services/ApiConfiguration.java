package com.rapid7.intsightsmigrationtool.services;

import lombok.Data;
import org.springframework.stereotype.Component;

@Data
@Component
public class ApiConfiguration {

    public static final String R7_CORRELATION_ID = "R7-Correlation-Id";
    public static final String R7_CONSUMER = "R7-Consumer";
    public static final String X_API_KEY = "X-Api-Key";
    public static final String X_INTERNAL_SERVICE_KEY = "R7-Internal-Service-Key";

    String apiHost;
    String apiKey;
    String rbacApiKey;
    String consumerHeader;
}
