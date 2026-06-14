package dev.javarush.youtube.auth_server.security;

import com.nimbusds.jose.jwk.JWKSet;
import com.nimbusds.jose.jwk.RSAKey;
import com.nimbusds.jose.jwk.source.ImmutableJWKSet;
import com.nimbusds.jose.jwk.source.JWKSource;
import com.nimbusds.jose.proc.SecurityContext;
import org.jspecify.annotations.NonNull;
import org.springframework.context.annotation.Bean;

import java.security.*;
import java.security.interfaces.RSAPrivateCrtKey;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.RSAPublicKeySpec;
import java.util.Base64;
import java.util.UUID;

public class RSAKeyConfig {
    @Bean
    JWKSource<SecurityContext> jwkSource() {
        KeyPair keyPair = generateRsaKey();
        RSAPrivateKey privateKey = (RSAPrivateKey) keyPair.getPrivate();
        RSAPublicKey publicKey = (RSAPublicKey) keyPair.getPublic();
        RSAKey rsaKey = new RSAKey.Builder(publicKey)
                .privateKey(privateKey)
                .keyID(UUID.randomUUID().toString())
                .build();
        JWKSet jwkSet = new JWKSet(rsaKey);
        return new ImmutableJWKSet<>(jwkSet);
    }

    private static KeyPair generateRsaKey() {
        // // 1. Read and clean the private key string from the environment
        String privateKeyContent = getPrivateKeyContent();

        try {
            KeyFactory keyFactory = KeyFactory.getInstance("RSA");

            // 2. Decode and generate the Private Key
            PrivateKey privateKey = getPrivateKey(privateKeyContent, keyFactory);

            // 3. Derive the Public Key from the Private Key
            // Standard Java RSA private keys implement the RSAPrivateCrtKey interface
            PublicKey publicKey = getPublicKey(privateKey, keyFactory);

            return new KeyPair(publicKey, privateKey);
        } catch (NoSuchAlgorithmException | InvalidKeySpecException e) {
            throw new RuntimeException(e);
        }
    }

    private static PublicKey getPublicKey(PrivateKey privateKey, KeyFactory keyFactory) throws InvalidKeySpecException {
        RSAPrivateCrtKey rsaPrivateCrtKey = (RSAPrivateCrtKey) privateKey;

        RSAPublicKeySpec publicKeySpec = new RSAPublicKeySpec(
                rsaPrivateCrtKey.getModulus(),
                rsaPrivateCrtKey.getPublicExponent()
        );

        return keyFactory.generatePublic(publicKeySpec);
    }

    private static PrivateKey getPrivateKey(String privateKeyContent, KeyFactory keyFactory) throws InvalidKeySpecException {
        byte[] decodedKeyBytes = Base64.getDecoder().decode(privateKeyContent);
        PKCS8EncodedKeySpec keySpec = new PKCS8EncodedKeySpec(decodedKeyBytes);
        return keyFactory.generatePrivate(keySpec);
    }

    private static @NonNull String getPrivateKeyContent() {
        String pemKeyString = System.getenv("RSA_PRIVATE_KEY");
        if (pemKeyString == null || pemKeyString.isBlank()) {
            throw new IllegalStateException("Missing or empty environment variable: RSA_PRIVATE_KEY");
        }
        return pemKeyString
                .trim()
                .replace("-----BEGIN PRIVATE KEY-----", "")
                .replace("-----END PRIVATE KEY-----", "")
                .replaceAll("\\s+", "");
    }
}
