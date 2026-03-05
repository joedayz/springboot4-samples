package com.bcp.training.jwt;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import io.jsonwebtoken.Jwts;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.KeyFactory;
import java.security.PrivateKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.util.Base64;

/**
 * Start version: tokens do not include audience nor groups.
 * Complete the lab to add audience and groups so role-based security works.
 */
@Component
public class JwtGenerator {

    private final String issuer;
    private final PrivateKey privateKey;

    public JwtGenerator(
            @Value("${jwt.issuer}") String issuer,
            @Value("${jwt.private-key-location}") String privateKeyLocation) throws IOException {
        this.issuer = issuer;
        String path = privateKeyLocation.replace("file:", "").trim();
        String keyPem = Files.readString(Path.of(path))
                .replace("-----BEGIN PRIVATE KEY-----", "")
                .replace("-----END PRIVATE KEY-----", "")
                .replaceAll("\\s", "");
        byte[] encoded = Base64.getDecoder().decode(keyPem);
        PKCS8EncodedKeySpec spec = new PKCS8EncodedKeySpec(encoded);
        try {
            this.privateKey = KeyFactory.getInstance("RSA").generatePrivate(spec);
        } catch (Exception e) {
            throw new IOException("Failed to load private key", e);
        }
    }

    public String generateJwtForRegularUser(String username) {
        return Jwts.builder()
                .issuer(issuer)
                .claim("upn", username + "@example.com")
                .signWith(privateKey)
                .compact();
    }

    public String generateJwtForAdmin(String username) {
        return Jwts.builder()
                .issuer(issuer)
                .subject(username)
                .claim("upn", username + "@example.com")
                .claim("locale", "en_US")
                .signWith(privateKey)
                .compact();
    }
}
