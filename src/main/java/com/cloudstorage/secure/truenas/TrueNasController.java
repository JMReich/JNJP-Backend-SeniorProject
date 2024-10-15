package com.cloudstorage.secure.truenas;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class TrueNasController {

    @Autowired
    private TrueNasService trueNasService;


    // Call to get sessions
    @GetMapping("/get/sessions")
    public void getSessions() {
        trueNasService.getAuthSessions();
    } // No return value


    // Call to sign in the user
    // Example: http://localhost:9090/ping?token=fsddsfsdfds75f6das56f7sad5fd8saf5dsa86f56dsf568sadf586sdf8sad68f6865da
    @GetMapping("/ping")
    public void ping(@RequestParam String token) {
        trueNasService.ping(token);
    } // No return value



    // http://localhost:9090/auth?username=root&password=root
    @GetMapping("/auth")
    public ResponseEntity<String> getApiKey(@RequestParam String username, @RequestParam String password) {
        Boolean response = trueNasService.isAdmin(username, password);
        String apiKey = trueNasService.getApiKey(username, password);
        if (apiKey != null) {
            return new ResponseEntity<>(new ApiKeyResponse(apiKey, response).toString(), HttpStatus.OK);
        } else {
            return new ResponseEntity<>("Authentication failed", HttpStatus.UNAUTHORIZED);
        }
    } // Returns a ResponseEntity with the API key or an error message



}

class ApiKeyResponse {
    private String apiKey;
    private boolean isAdmin;


    public ApiKeyResponse(String apiKey, boolean isAdmin) {
        this.apiKey = apiKey;
        this.isAdmin = isAdmin;
    }



    public String getApiKey() {
        return apiKey;
    }

    public void setApiKey(String apiKey) {
        this.apiKey = apiKey;
    }

    public boolean isAdmin() {
        return isAdmin;
    }

    public void setAdmin(boolean isAdmin) {
        this.isAdmin = isAdmin;
    }

    @Override
    public String toString() {
        return "ApiKeyResponse{" +
                "apiKey='" + apiKey + '\'' +
                ", isAdmin=" + isAdmin +
                '}';
    }
}
