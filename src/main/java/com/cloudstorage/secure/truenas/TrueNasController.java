package com.cloudstorage.secure.truenas;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class TrueNasController {

    @Autowired
    private TrueNasService trueNasService;


    @GetMapping("/create-user")
    @PreAuthorize("hasAuthority('read:messages')")
    public ResponseEntity<Integer> createUser(@RequestParam String nickname, @RequestParam String refreshToken) {
        int userId = trueNasService.createUser(nickname, refreshToken);
        if (userId > 0) {
            return new ResponseEntity<>(userId, HttpStatus.CREATED);
        } else {
            return new ResponseEntity<>(userId, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping("/reset-password")
    @PreAuthorize("hasAuthority('read:messages')")
    public boolean resetPassword(@RequestParam String uid, @RequestParam String refreshToken) {
        return trueNasService.resetPassword(uid, refreshToken);
    }

}

