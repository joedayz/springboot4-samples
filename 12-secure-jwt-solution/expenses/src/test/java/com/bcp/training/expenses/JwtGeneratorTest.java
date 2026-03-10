package com.bcp.training.expenses;

import com.bcp.training.jwt.JwtGenerator;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.nio.file.Files;
import java.nio.file.Path;
import java.security.KeyFactory;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
@ActiveProfiles("test")
class JwtGeneratorTest {

    @Autowired
    JwtGenerator jwtGenerator;

    @Test
    void userJwtBelongsToUserGroup() {
        String token = jwtGenerator.generateJwtForRegularUser("testUser");
        Claims claims = parseToken(token);

        @SuppressWarnings("unchecked")
        List<String> groups = (List<String>) claims.get("groups");
        assertNotNull(groups, "JWT groups claim missing");
        assertTrue(groups.contains("USER"), "JWT groups for regular user do not contain USER");
    }

    @Test
    void userJwtContainsSubjectClaim() {
        String token = jwtGenerator.generateJwtForRegularUser("testUser");
        Claims claims = parseToken(token);
        assertEquals("testUser", claims.getSubject(), "JWT 'sub' claim not set as expected");
    }

    @Test
    void userJwtContainsUpnClaim() {
        String token = jwtGenerator.generateJwtForRegularUser("testUser");
        Claims claims = parseToken(token);
        assertEquals("testUser@example.com", claims.get("upn"), "JWT 'upn' claim not set as expected");
    }

    @Test
    void userJwtContainsIssuerClaim() {
        String token = jwtGenerator.generateJwtForRegularUser("testUser");
        Claims claims = parseToken(token);
        assertEquals("https://example.com/bcptraining", claims.getIssuer(), "JWT 'iss' claim not set as expected");
    }

    @Test
    void userJwtContainsLocaleClaim() {
        String token = jwtGenerator.generateJwtForRegularUser("testUser");
        Claims claims = parseToken(token);
        assertNotNull(claims.get("locale"), "JWT 'locale' claim not set as expected");
        assertEquals("en_US", claims.get("locale"), "JWT 'locale' value not as expected");
    }

    @Test
    void userJwtContainsAudienceClaim() {
        String token = jwtGenerator.generateJwtForRegularUser("testUser");
        Claims claims = parseToken(token);
        assertNotNull(claims.get("aud"), "JWT 'aud' claim not set as expected");
    }

    @Test
    void adminJwtBelongsToUserGroup() {
        String token = jwtGenerator.generateJwtForAdmin("testUser");
        Claims claims = parseToken(token);

        @SuppressWarnings("unchecked")
        List<String> groups = (List<String>) claims.get("groups");
        assertNotNull(groups, "JWT groups claim missing");
        assertTrue(groups.contains("USER"), "JWT groups for admin do not contain USER");
    }

    @Test
    void adminJwtBelongsToAdminGroup() {
        String token = jwtGenerator.generateJwtForAdmin("testUser");
        Claims claims = parseToken(token);

        @SuppressWarnings("unchecked")
        List<String> groups = (List<String>) claims.get("groups");
        assertNotNull(groups, "JWT groups claim missing");
        assertTrue(groups.contains("ADMIN"), "JWT groups for admin do not contain ADMIN");
    }

    @Test
    void adminJwtContainsLocaleClaim() {
        String token = jwtGenerator.generateJwtForAdmin("testUser");
        Claims claims = parseToken(token);
        assertNotNull(claims.get("locale"), "JWT 'locale' claim not set as expected");
        assertEquals("en_US", claims.get("locale"), "JWT 'locale' value not as expected");
    }

    private Claims parseToken(String token) {
        try {
            Path path = Path.of(getClass().getResource("/keys/publicKey.pem").toURI());
            String keyPem = Files.readString(path)
                    .replace("-----BEGIN PUBLIC KEY-----", "")
                    .replace("-----END PUBLIC KEY-----", "")
                    .replaceAll("\\s", "");
            byte[] encoded = Base64.getDecoder().decode(keyPem);
            RSAPublicKey publicKey = (RSAPublicKey) KeyFactory.getInstance("RSA")
                    .generatePublic(new X509EncodedKeySpec(encoded));

            return Jwts.parser()
                    .verifyWith(publicKey)
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();
        } catch (Exception e) {
            throw new RuntimeException("Failed to parse JWT", e);
        }
    }
}
