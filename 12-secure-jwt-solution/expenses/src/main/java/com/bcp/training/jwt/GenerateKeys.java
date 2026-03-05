package com.bcp.training.jwt;

import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.util.Base64;

/**
 * Standalone utility to generate RSA key pair for JWT signing.
 * Run: mvn exec:java -Dexec.mainClass="com.bcp.training.jwt.GenerateKeys"
 * Or run main() from your IDE. Keys are written to ${user.home}/DO378/secure-jwt/
 */
public class GenerateKeys {

    public static void main(String[] args) {
        System.out.println("Generando claves RSA para JWT usando Java...");
        try {
            String homeDir = System.getProperty("user.home");
            String keyDir = homeDir + "/DO378/secure-jwt";
            Path keyPath = Paths.get(keyDir);
            if (!Files.exists(keyPath)) {
                Files.createDirectories(keyPath);
                System.out.println("Directorio creado: " + keyDir);
            }
            KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance("RSA");
            keyPairGenerator.initialize(2048);
            KeyPair keyPair = keyPairGenerator.generateKeyPair();
            PrivateKey privateKey = keyPair.getPrivate();
            PublicKey publicKey = keyPair.getPublic();
            String privateKeyPath = keyDir + "/privateKey.pem";
            savePrivateKey(privateKey, privateKeyPath);
            System.out.println("Clave privada generada: " + privateKeyPath);
            String publicKeyPath = keyDir + "/publicKey.pem";
            savePublicKey(publicKey, publicKeyPath);
            System.out.println("Clave pública generada: " + publicKeyPath);
            System.out.println("\n¡Claves generadas exitosamente!");
        } catch (Exception e) {
            System.err.println("Error generando claves: " + e.getMessage());
            e.printStackTrace();
            System.exit(1);
        }
    }

    private static void savePrivateKey(PrivateKey privateKey, String filePath) throws IOException {
        try (FileWriter writer = new FileWriter(filePath)) {
            writer.write("-----BEGIN PRIVATE KEY-----\n");
            writer.write(Base64.getEncoder().encodeToString(privateKey.getEncoded()));
            writer.write("\n-----END PRIVATE KEY-----\n");
        }
    }

    private static void savePublicKey(PublicKey publicKey, String filePath) throws IOException {
        try (FileWriter writer = new FileWriter(filePath)) {
            writer.write("-----BEGIN PUBLIC KEY-----\n");
            writer.write(Base64.getEncoder().encodeToString(publicKey.getEncoded()));
            writer.write("\n-----END PUBLIC KEY-----\n");
        }
    }
}
