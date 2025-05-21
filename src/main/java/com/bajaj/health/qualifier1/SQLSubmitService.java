package com.bajaj.health.qualifier1;

import jakarta.annotation.PostConstruct;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import org.springframework.http.HttpHeaders;
import java.util.HashMap;
import java.util.Map;

@Service
public class SQLSubmitService {

    @PostConstruct
    public void runOnStartup() {
        RestTemplate restTemplate = new RestTemplate();

        // Step 1: Call the first POST to generate webhook
        String url = "https://bfhldevapigw.healthrx.co.in/hiring/generateWebhook/JAVA";

        Map<String, String> requestBody = new HashMap<>();
        requestBody.put("name", "John Doe");
        requestBody.put("regNo", "REG12347");
        requestBody.put("email", "john@example.com");

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        HttpEntity<Map<String, String>> entity = new HttpEntity<>(requestBody, headers);

        try {
            ResponseEntity<WebhookResponse> response = restTemplate.postForEntity(url, entity, WebhookResponse.class);

            if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
                String webhook = response.getBody().getWebhook();
                String token = response.getBody().getAccessToken();

                // SQL Query for Question 1
                String finalQuery = "SELECT p.amount AS SALARY, CONCAT(e.first_name, ' ', e.last_name) AS NAME, TIMESTAMPDIFF(YEAR, e.dob, CURDATE()) AS AGE, d.department_name AS DEPARTMENT_NAME FROM payments p JOIN employee e ON p.emp_id = e.emp_id JOIN department d ON e.department = d.department_id WHERE DAY(p.payment_time) != 1 ORDER BY p.amount DESC LIMIT 1";

                submitSQLAnswer(restTemplate, webhook, token, finalQuery);
            } else {
                System.err.println("Failed to generate webhook. Status: " + response.getStatusCode());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void submitSQLAnswer(RestTemplate restTemplate, String webhook, String token, String finalQuery) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("Authorization", token);
        Map<String, String> body = new HashMap<>();
        body.put("finalQuery", finalQuery);

        HttpEntity<Map<String, String>> entity = new HttpEntity<>(body, headers);

        try {
            ResponseEntity<String> response = restTemplate.postForEntity(webhook, entity, String.class);
            System.out.println("Submission Response: " + response.getStatusCode() + " - " + response.getBody());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
