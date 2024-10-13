package com.cloudstorage.secure.truenas;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.List;

@Service
public class TrueNasService {

    @Autowired
    private RestTemplateBuilder restTemplateBuilder;

    public void ping(String token) {


        RestTemplate restTemplate = restTemplateBuilder
                .build();

        // Make call to ping server and print it: https://10.0.0.202/api/v2.0/system/ping
        String url = "https://10.0.0.202/api/v2.0/core/ping?access_token=" + token;

        try {

            ResponseEntity<String> responseEntity = restTemplate.getForEntity(url, String.class);
            if (responseEntity.getStatusCode() == HttpStatus.OK) {
                String response = responseEntity.getBody();
                System.out.println("Ping response: " + response);
            } else {
                System.err.println("Error during ping request: " + responseEntity.getStatusCode() + " - " + responseEntity.getBody());
            }
        } catch (Exception e) {
            System.err.println("Error during ping request: " + e.getMessage());
        }
    }

    public String getApiKey(String username, String password) {
        RestTemplate restTemplate = restTemplateBuilder
                .basicAuthentication("root", "root")
                // This has to be root account, it will not work with any other account
                .build();

        // Make post call to check account: https://10.0.0.202/api/v2.0/auth/check_password
        String url = "https://10.0.0.202/api/v2.0/auth/check_password";
        String body = "{\"username\": \"" + username + "\", \"password\": \"" + password + "\"}";

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(org.springframework.http.MediaType.APPLICATION_JSON);

        HttpEntity<String> entity = new HttpEntity<>(body, headers);
        try {
            Boolean response = restTemplate.postForObject(url, entity, Boolean.class);
            System.out.println("Api auth response: " + response);
            if (Boolean.TRUE.equals(response)) {

                // ToDo: Add 2FA check here
                // Non-starter: After further reading, it seems that the 2FA is only for the root/admin account


                // Check if the user already has an api key
                url = "https://10.0.0.202/api/v2.0/api_key";
                ResponseEntity<String> responseEntity = restTemplate.getForEntity(url, String.class);

                ObjectMapper objectMapper = new ObjectMapper();
                List<ApiKey> apiKeys = objectMapper.readValue(responseEntity.getBody(), new TypeReference<List<ApiKey>>() {});

                for (ApiKey apiKey : apiKeys) {
                    if (apiKey.getName().equals(username)) { // If they do, destroy it
                        System.out.println("Destroying ID: " + apiKey.getId());
                        url = "https://10.0.0.202/api/v2.0/api_key/id/" + apiKey.getId();
                        restTemplate.delete(url);
                        break;
                    }
                }

                // Make call to create api key: https://10.0.0.202/api/v2.0/
                url = "https://10.0.0.202/api/v2.0/api_key";
                body = "{\"name\": \"" + username + "\"}";
                HttpEntity<String> entity2 = new HttpEntity<>(body, headers);
                ResponseEntity<String> responseEntityTwo = restTemplate.postForEntity(url, entity2, String.class);
                ApiKey apiKey = objectMapper.readValue(responseEntityTwo.getBody(), ApiKey.class);
                return apiKey.getKey();



                /*
                Not sure why, but the api token is completely useless, and the api key needs to be used instead
                // Make call to get api token: https://10.0.0.202/api/v2.0/auth/generate_token
                url = "https://10.0.0.202/api/v2.0/auth/generate_token";
                body = "{\"ttl\": 600,\"attrs\": {\"additionalProp1\": {}}}";
                headers.setContentType(org.springframework.http.MediaType.APPLICATION_JSON);
                HttpEntity<String> entityTwo = new HttpEntity<>(body, headers);
                System.out.println("EntityTwo body: " + entityTwo.getBody());
                String token = restTemplate.postForObject(url, entityTwo, String.class);
                System.out.println("Api token: " + token);
                return token;
                 */
            }
            return null;
        } catch (Exception e) {
            System.err.println("Error during api token request: " + e.getMessage());
            return null;
        }



    }


    public void getAuthSessions() {
        RestTemplate restTemplate = restTemplateBuilder
                .basicAuthentication("root", "root")
                .build();

        // Make call to get auth sessions: https://10.0.0.202/api/v2.0/auth/sessions
        String url = "https://10.0.0.202/api/v2.0/auth/sessions";
        try {
            ResponseEntity<String> responseEntity = restTemplate.getForEntity(url, String.class);
            if (responseEntity.getStatusCode() == HttpStatus.OK) {
                String response = responseEntity.getBody();
                System.out.println("Auth sessions response: " + response);
            } else {
                System.err.println("Error during auth sessions request: " + responseEntity.getStatusCode() + " - " + responseEntity.getBody());
            }
        } catch (Exception e) {
            System.err.println("Error during auth sessions request: " + e.getMessage());
        }

    }


    // REST api calls to the server: https://www.truenas.com/docs/api/core_rest_api.html



    //
}
