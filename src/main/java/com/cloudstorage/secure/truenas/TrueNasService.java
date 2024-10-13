package com.cloudstorage.secure.truenas;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
public class TrueNasService {

    @Autowired
    private RestTemplateBuilder restTemplateBuilder;

    public void ping(String username, String password) {


        RestTemplate restTemplate = restTemplateBuilder
                .basicAuthentication(username, password)
                .build();

        // Make call to ping server and print it: https://10.0.0.202/api/v2.0/system/ping
        String url = "https://10.0.0.202/api/v2.0/core/ping";
        try {
            String response = restTemplate.getForObject(url, String.class);
            System.out.println("Ping response: " + response);
        } catch (Exception e) {
            System.err.println("Error during ping request: " + e.getMessage());
        }







    }

    public String getApiToken(String username, String password) {
        RestTemplate restTemplate = restTemplateBuilder
                .basicAuthentication(username, password)
                .build();

        // Make post call to check account: https://10.0.0.202/api/v2.0/auth/check_password
        String url = "https://10.0.0.202/api/v2.0/auth/check_password";
        String body = "{\"username\": \"" + username + "\", \"password\": \"" + password + "\"}";

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(org.springframework.http.MediaType.APPLICATION_JSON);

        HttpEntity<String> entity = new HttpEntity<>(body, headers);
        try {
            Boolean response = restTemplate.postForObject(url, entity, Boolean.class); // ToDo: Understand why this works when the pw is set in the template builder
            System.out.println("Api auth response: " + response);
            if (Boolean.TRUE.equals(response)) {
                // Make call to get api token: https://10.0.0.202/api/v2.0/auth/generate_token
                url = "https://10.0.0.202/api/v2.0/auth/generate_token";
                body = "{\"ttl\": 600,\"attrs\": {\"additionalProp1\": {}}}";
                headers.setContentType(org.springframework.http.MediaType.APPLICATION_JSON);
                HttpEntity<String> entityTwo = new HttpEntity<>(body, headers);
                System.out.println("EntityTwo body: " + entityTwo.getBody());
                String token = restTemplate.postForObject(url, entityTwo, String.class);
                System.out.println("Api token: " + token);
                return token;
            }
            return null;
        } catch (Exception e) {
            System.err.println("Error during api token request: " + e.getMessage());
            return null;
        }



    }


    // REST api calls to the server: https://www.truenas.com/docs/api/core_rest_api.html



    //
}
