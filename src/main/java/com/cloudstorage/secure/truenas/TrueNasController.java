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
    }



}
