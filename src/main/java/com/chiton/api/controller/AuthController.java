package com.chiton.api.controller;

import com.chiton.api.dto.ReqRes;
import com.chiton.api.service.AuthService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/auth")
public class AuthController {

    @Autowired
    private AuthService authService;

    @PostMapping("")
    public ResponseEntity<?> login(@RequestBody ReqRes reqRes){
        return ResponseEntity.ok(authService.signIn(reqRes));
    }

    @PostMapping("/refresh")
    public ResponseEntity<?> refreshToken(@RequestBody ReqRes refreshTokenRequest){
        return ResponseEntity.ok(authService.refreshToken(refreshTokenRequest));
    }
}
