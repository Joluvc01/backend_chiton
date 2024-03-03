package com.chiton.api.service;

import com.chiton.api.entity.Role;
import com.chiton.api.entity.User;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Date;
import java.util.HashMap;
import java.util.function.Function;


@Component
public class JWTUtils {

    private SecretKey Key;
    private static final long EXPIRATION_TIME_TOKEN = 10; //14400000;// 4 horas
    private static final long EXPIRATION_TIME_REFRESH =  28800000;// 8 horas


    public JWTUtils(){
        String secreteString = "586E3272357538782F413F4428472B4B6250655368566B597033733676397924";
        byte[] keyBytes = Base64.getDecoder().decode(secreteString.getBytes(StandardCharsets.UTF_8));
        this.Key = new SecretKeySpec(keyBytes, "HmacSHA256");
    }

    public String generateToken(User user, UserDetails userDetails){
        String role = String.valueOf(user.getRole());
        String firstname = user.getFirstname();
        String lastname = user.getLastname();

        return Jwts.builder()
                .subject((userDetails.getUsername()))
                .claim("role:", role)
                .claim("firstname:", firstname)
                .claim("lastname:", lastname)
                .issuedAt(new Date((System.currentTimeMillis())))
                .expiration(new Date(System.currentTimeMillis()+EXPIRATION_TIME_TOKEN))
                .signWith(Key)
                .compact();
    }

    public String generateRefreshToken(HashMap<String, Object> claims, UserDetails userDetails){
        return Jwts.builder()
                .claims(claims)
                .subject((userDetails.getUsername()))
                .issuedAt(new Date((System.currentTimeMillis())))
                .expiration(new Date(System.currentTimeMillis()+ EXPIRATION_TIME_REFRESH))
                .signWith(Key)
                .compact();
    }

    public String extractUsername(String token){
        return  extractClaims(token, Claims::getSubject);
    }

    private <T> T extractClaims(String token, Function<Claims, T> claimsTFunction){
        return claimsTFunction.apply(Jwts.parser().verifyWith(Key).build().parseSignedClaims(token).getPayload());
    }

    public boolean isTokenValid(String token, UserDetails userDetails){
        final String username = extractUsername(token);
        return (username.equals(userDetails.getUsername())&&!isTokenExpired(token));
    }

    public boolean isTokenExpired(String token){
        return extractClaims(token, Claims::getExpiration).before(new Date());
    }
}
