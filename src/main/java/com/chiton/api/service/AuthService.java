package com.chiton.api.service;

import com.chiton.api.dto.ReqRes;
import com.chiton.api.entity.User;
import com.chiton.api.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.stereotype.Service;
import java.util.HashMap;

@Service
public class AuthService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private JWTUtils jwtUtils;

    @Autowired
    private AuthenticationManager authenticationManager;

    public ReqRes signIn(ReqRes singInRequest){
        ReqRes response = new ReqRes();
        try {
            authenticationManager.authenticate((new UsernamePasswordAuthenticationToken(singInRequest.getUsername(), singInRequest.getPassword())));
            var user = userRepository.findByUsername(singInRequest.getUsername()).orElseThrow();
            var jwt = jwtUtils.generateToken(user, user);
            response.setStatusCode(200);
            response.setToken(jwt);
            response.setMessage("Exito al ingresar");
        } catch (Exception e){
            response.setStatusCode(401);
            response.setError(e.getMessage());
        }
        return response;
    }
}
