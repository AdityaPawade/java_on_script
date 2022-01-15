package com.adtsw.jcommons.utils;

import org.junit.Assert;
import org.junit.Test;

public class EncryptionUtilTest {

    private final static char[] sessionEncryptionPassword = "encryption_password".toCharArray();

    @Test
    public void testEncryption() throws EncryptionUtil.CryptoException {

        String plaintext = "{hello there}";
        String encryptedString = EncryptionUtil.encryptString(sessionEncryptionPassword, plaintext);
        String decryptedString = EncryptionUtil.decryptString(sessionEncryptionPassword, encryptedString);
        
        Assert.assertEquals(plaintext, decryptedString);
    }
}
