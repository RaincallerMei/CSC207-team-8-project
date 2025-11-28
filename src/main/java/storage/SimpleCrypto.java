package storage;

import javax.crypto.*;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;
import java.security.SecureRandom;
import java.security.spec.KeySpec;
import java.util.Base64;

public class SimpleCrypto {

    public static class Encrypted {
        public final String ciphertextB64;
        public final String saltB64;
        public final String ivB64;
        public Encrypted(String ct, String salt, String iv) {
            this.ciphertextB64 = ct; this.saltB64 = salt; this.ivB64 = iv;
        }
    }

    private static final int SALT_LEN = 16;         // bytes
    private static final int IV_LEN = 12;           // bytes for GCM
    private static final int KEY_LEN = 256;         // bits
    private static final int TAG_LEN = 128;         // bits
    private static final int PBKDF2_ITERS = 100_000;

    private static final SecureRandom RNG = new SecureRandom();

    public static Encrypted encrypt(String plaintext, char[] password) throws Exception {
        byte[] salt = new byte[SALT_LEN];
        RNG.nextBytes(salt);
        SecretKey key = derive(password, salt);

        byte[] iv = new byte[IV_LEN];
        RNG.nextBytes(iv);

        Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
        cipher.init(Cipher.ENCRYPT_MODE, key, new GCMParameterSpec(TAG_LEN, iv));
        byte[] ct = cipher.doFinal(plaintext.getBytes(java.nio.charset.StandardCharsets.UTF_8));

        return new Encrypted(
                Base64.getEncoder().encodeToString(ct),
                Base64.getEncoder().encodeToString(salt),
                Base64.getEncoder().encodeToString(iv)
        );
    }

    public static String decrypt(Encrypted enc, char[] password) throws Exception {
        byte[] salt = Base64.getDecoder().decode(enc.saltB64);
        byte[] iv = Base64.getDecoder().decode(enc.ivB64);
        byte[] ct = Base64.getDecoder().decode(enc.ciphertextB64);

        SecretKey key = derive(password, salt);

        Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
        cipher.init(Cipher.DECRYPT_MODE, key, new GCMParameterSpec(TAG_LEN, iv));
        byte[] pt = cipher.doFinal(ct);
        return new String(pt, java.nio.charset.StandardCharsets.UTF_8);
    }

    private static SecretKey derive(char[] password, byte[] salt) throws Exception {
        KeySpec spec = new PBEKeySpec(password, salt, PBKDF2_ITERS, KEY_LEN);
        SecretKeyFactory f = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256");
        byte[] k = f.generateSecret(spec).getEncoded();
        return new SecretKeySpec(k, "AES");
    }
}