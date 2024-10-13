package com.cloudstorage.secure.truenas;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class TrueNasController {

    @Autowired
    private TrueNasService trueNasService;



    // Call to sign in the user
    // Example: http://localhost:8080/ping?username=root&password=root
    @GetMapping("/ping")
    public void ping(@RequestParam String username, @RequestParam String password) {
        trueNasService.ping(username, password);
    } // No return value

    @GetMapping("/get/api_token")
    public String getApiToken(@RequestParam String username, @RequestParam String password) {
        return trueNasService.getApiToken(username, password);
    } // Returns a string, Example: fsddsfsdfds75f6das56f7sad5fd8saf5dsa86f56dsf568sadf586sdf8sad68f6865da



}
