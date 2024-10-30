package com.cloudstorage.secure.truenas;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
public class TrueNasService {

    @Autowired
    private RestTemplateBuilder restTemplateBuilder;

    @Value("${truenas.api.key}")
    private String apiKey;

    @Value("${truenas.api.url.base}")
    private String urlBase;

    public int createUser(String nickname, String refreshToken) {
        String url = urlBase + "/user";
        // Make call to mysql database for uid and increment it
        int userId = 1001;

        String body = "{" +
                "\"uid\": " + userId + ", " +
                "\"username\": \"" + nickname + "\", " +
                "\"group: \"" + 46 + "\", " +
                "\"group_create\":" + false + ", " +
                "\"home\": \"/mnt/jnpj/" + nickname + "\", " +
                "\"home_mode\": \"0777\", " +
                "\"shell\": \"/bin/bash\", " +
                "\"full_name\": \"" + nickname + "\", " +
                "\"password\": \"" + refreshToken + "\", " +
                "\"password_disabled\":" + false + "\", " +
                "\"locked:\"" + false + "\", " +
                "\"microsoft_account:\"" + false + "\", " +
                "\"smb:\"" + true + "\", " +
                "\"sudo:\"" + false + "\", " +
                "}";


        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(org.springframework.http.MediaType.APPLICATION_JSON);
        headers.setBearerAuth(apiKey); // Set the Bearer token
        HttpEntity<String> entity = new HttpEntity<>(body, headers);

        RestTemplate restTemplate = restTemplateBuilder
                .build();

        try {

            ResponseEntity<Boolean> responseEntity = restTemplate.postForEntity(url, entity, Boolean.class);
            if (responseEntity.getStatusCode() == HttpStatus.OK) {
                System.out.println("User created" );
                return userId;
            } else {
                System.err.println("Error during user creation");
                return -1;
            }
        } catch (Exception e) {
            System.err.println("Error during user creation");
            return -1;
        }
    }


    // put
    public boolean resetPassword(String uid, String refreshToken) {
        String url = urlBase + "/user";
        String body = "{" +
                "\"uid\": " + uid + ", " +
                "\"password\": \"" + refreshToken + "\"," +
                "}";

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(org.springframework.http.MediaType.APPLICATION_JSON);
        headers.setBearerAuth(apiKey); // Set the Bearer token
        HttpEntity<String> entity = new HttpEntity<>(body, headers);

        RestTemplate restTemplate = restTemplateBuilder
                .build();

        try {
            ResponseEntity<String> responseEntity = restTemplate.exchange(url, HttpMethod.PUT, entity, String.class);
            if (responseEntity.getStatusCode() == HttpStatus.OK) {
                System.out.println("Password reset" );
                return true;
            } else {
                System.err.println("Error during password reset");
                return false;
            }
        } catch (Exception e) {
            System.err.println("Error during password reset");
            return false;
        }
    }

    // Sets the volume size off a dataset
    public boolean setVolSize(String nickname) {
        //TODO: call this in the user creation

        String body = "{" +
                "\"Volsize:" + 5368709120 + "," +
                "}";


        return false;

    }


    // TODO: Call this in the setVolSize
    public boolean createShare(String nickname) {
        String path = "/mnt/jnpj/" + nickname;



    }


    public boolean changeNickname(int userid, String nickname, String oldNickname) {
        // Update user


        // Update path
        String path = "/mnt/jnpj/" + oldNickname;
    }
}
