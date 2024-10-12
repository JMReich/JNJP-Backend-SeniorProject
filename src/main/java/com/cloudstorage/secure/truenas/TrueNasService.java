package com.cloudstorage.secure.truenas;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.web.client.RestTemplateBuilder;
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


    // REST api calls to the server: https://www.truenas.com/docs/api/core_rest_api.html



    //
}
