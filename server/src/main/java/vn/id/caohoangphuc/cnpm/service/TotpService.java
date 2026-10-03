package vn.id.caohoangphuc.cnpm.service;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.Base64;
import javax.crypto.Cipher;
import javax.crypto.Mac;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

/** Creates, protects, and checks standard time-based codes used by authenticator apps. */
@Service
public class TotpService {
    private static final String BASE32 = "ABCDEFGHIJKLMNOPQRSTUVWXYZ234567";
    private static final int PERIOD_SECONDS = 30;
    private static final SecureRandom RANDOM = new SecureRandom();
    private final SecretKeySpec encryptionKey;

    public TotpService(@Value("${security.totp-encryption-key:local-development-only-change-me}") String key) {
        if (key.length() < 32) {
            throw new IllegalArgumentException("TOTP_ENCRYPTION_KEY must contain at least 32 characters");
        }
        try {
            byte[] digest = MessageDigest.getInstance("SHA-256").digest(key.getBytes(StandardCharsets.UTF_8));
            encryptionKey = new SecretKeySpec(digest, "AES");
        } catch (GeneralSecurityException exception) {
            throw new IllegalStateException("Unable to initialize TOTP encryption", exception);
        }
    }

    /** Creates a random Base32 key accepted by Google Authenticator and similar apps. */
    public String newSecret() {
        byte[] bytes = new byte[20];
        RANDOM.nextBytes(bytes);
        return encodeBase32(bytes);
    }

    /** Produces the provisioning URI that authenticator apps can scan or open. */
    public String provisioningUri(String issuer, String account, String secret) {
        String label = urlEncode(issuer + ":" + account);
        return "otpauth://totp/" + label + "?secret=" + secret + "&issuer=" + urlEncode(issuer)
                + "&algorithm=SHA1&digits=6&period=" + PERIOD_SECONDS;
    }

    /** Checks the current code and the adjacent time step to allow small clock differences. */
    public boolean matches(String secret, String code, long epochSeconds) {
        if (code == null || !code.matches("[0-9]{6}")) return false;
        long counter = epochSeconds / PERIOD_SECONDS;
        for (long offset = -1; offset <= 1; offset++) {
            if (code.equals(codeFor(secret, counter + offset))) return true;
        }
        return false;
    }

    /** Encrypts the authenticator key before it is stored in the database. */
    public String encrypt(String secret) {
        try {
            byte[] nonce = new byte[12];
            RANDOM.nextBytes(nonce);
            Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
            cipher.init(Cipher.ENCRYPT_MODE, encryptionKey, new GCMParameterSpec(128, nonce));
            byte[] encrypted = cipher.doFinal(secret.getBytes(StandardCharsets.UTF_8));
            return Base64.getEncoder().encodeToString(ByteBuffer.allocate(nonce.length + encrypted.length)
                    .put(nonce).put(encrypted).array());
        } catch (GeneralSecurityException exception) {
            throw new IllegalStateException("Unable to protect TOTP secret", exception);
        }
    }

    /** Decrypts a secret already protected by this application's encryption key. */
    public String decrypt(String encryptedSecret) {
        try {
            byte[] data = Base64.getDecoder().decode(encryptedSecret);
            ByteBuffer buffer = ByteBuffer.wrap(data);
            byte[] nonce = new byte[12];
            buffer.get(nonce);
            byte[] encrypted = new byte[buffer.remaining()];
            buffer.get(encrypted);
            Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
            cipher.init(Cipher.DECRYPT_MODE, encryptionKey, new GCMParameterSpec(128, nonce));
            return new String(cipher.doFinal(encrypted), StandardCharsets.UTF_8);
        } catch (GeneralSecurityException | IllegalArgumentException exception) {
            throw new IllegalStateException("Unable to read stored TOTP secret", exception);
        }
    }

    private static String codeFor(String secret, long counter) {
        try {
            Mac mac = Mac.getInstance("HmacSHA1");
            mac.init(new SecretKeySpec(decodeBase32(secret), "HmacSHA1"));
            byte[] hash = mac.doFinal(ByteBuffer.allocate(Long.BYTES).putLong(counter).array());
            int offset = hash[hash.length - 1] & 0x0f;
            int binary = ((hash[offset] & 0x7f) << 24) | ((hash[offset + 1] & 0xff) << 16)
                    | ((hash[offset + 2] & 0xff) << 8) | (hash[offset + 3] & 0xff);
            return String.format("%06d", binary % 1_000_000);
        } catch (GeneralSecurityException exception) {
            throw new IllegalStateException("Unable to check TOTP code", exception);
        }
    }

    private static String encodeBase32(byte[] bytes) {
        StringBuilder result = new StringBuilder();
        int buffer = 0;
        int bits = 0;
        for (byte value : bytes) {
            buffer = (buffer << 8) | (value & 0xff);
            bits += 8;
            while (bits >= 5) {
                result.append(BASE32.charAt((buffer >> (bits -= 5)) & 31));
            }
        }
        if (bits > 0) result.append(BASE32.charAt((buffer << (5 - bits)) & 31));
        return result.toString();
    }

    private static byte[] decodeBase32(String value) {
        byte[] result = new byte[value.length() * 5 / 8];
        int buffer = 0;
        int bits = 0;
        int index = 0;
        for (char character : value.toUpperCase().toCharArray()) {
            int digit = BASE32.indexOf(character);
            if (digit < 0) throw new IllegalArgumentException("Invalid Base32 secret");
            buffer = (buffer << 5) | digit;
            bits += 5;
            if (bits >= 8) result[index++] = (byte) (buffer >> (bits -= 8));
        }
        return result;
    }

    private static String urlEncode(String value) {
        return java.net.URLEncoder.encode(value, StandardCharsets.UTF_8).replace("+", "%20");
    }
}
