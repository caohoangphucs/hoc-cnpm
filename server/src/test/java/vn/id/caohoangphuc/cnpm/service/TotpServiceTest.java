package vn.id.caohoangphuc.cnpm.service;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class TotpServiceTest {
    private final TotpService totp = new TotpService("test-encryption-key-that-is-long-enough");

    @Test
    void validatesTheRfcTotpExample() {
        assertTrue(totp.matches("GEZDGNBVGY3TQOJQGEZDGNBVGY3TQOJQ", "287082", 59));
        assertFalse(totp.matches("GEZDGNBVGY3TQOJQGEZDGNBVGY3TQOJQ", "287083", 59));
    }

    @Test
    void generatesAnAuthenticatorUriAndProtectsStoredSecrets() {
        var secret = totp.newSecret();
        var uri = totp.provisioningUri("KTX Smart Hub", "admin", secret);

        assertEquals(32, secret.length());
        assertTrue(uri.startsWith("otpauth://totp/KTX%20Smart%20Hub%3Aadmin?secret=" + secret));
        var encrypted = totp.encrypt(secret);
        assertNotEquals(secret, encrypted);
        assertEquals(secret, totp.decrypt(encrypted));
    }
}
