package com.cloudstorage.secure.truenas;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
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
    // Example: http://localhost:8080/ping?token=fsddsfsdfds75f6das56f7sad5fd8saf5dsa86f56dsf568sadf586sdf8sad68f6865da
    @GetMapping("/ping")
    public void ping(@RequestParam String token) {
        trueNasService.ping(token);
    } // No return value



    //http://localhost:8080/ping?username=root&password=root
    @GetMapping("/auth")
    public String getApiToken(@RequestParam String username, @RequestParam String password) {
        return trueNasService.getApiKey(username, password);
    } // Returns a string, Example: fsddsfsdfds75f6das56f7sad5fd8saf5dsa86f56dsf568sadf586sdf8sad68f6865da



}
