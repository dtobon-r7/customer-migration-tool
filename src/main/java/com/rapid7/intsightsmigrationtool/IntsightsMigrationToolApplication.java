package com.rapid7.intsightsmigrationtool;

import com.rapid7.intsightsmigrationtool.gui.GUI;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.retry.annotation.EnableRetry;
import org.springframework.web.client.RestTemplate;

import javax.swing.*;


@RequiredArgsConstructor
@SpringBootApplication
@EnableRetry
public class IntsightsMigrationToolApplication {

    private static GUI gui;

    public static void main(String[] args) {
        System.setProperty("java.awt.headless", "false");
        SpringApplication.run(IntsightsMigrationToolApplication.class, args);
        SwingUtilities.invokeLater(() -> gui.setVisible(true));
    }

    @Bean
    public RestTemplate restTemplate(RestTemplateBuilder builder) {

        RestTemplate restTemplate = builder.build();
        HttpComponentsClientHttpRequestFactory requestFactory = new HttpComponentsClientHttpRequestFactory();
        requestFactory.setConnectTimeout(60000);
        requestFactory.setReadTimeout(60000);
        restTemplate.setRequestFactory(requestFactory);

        return restTemplate;
    }
}
