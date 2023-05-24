package utils;

import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import java.security.SecureRandom;
import java.security.spec.KeySpec;

public class HashPasswordHelper {
    /**
     * Hashes a password using PBKDF2WithHmacSHA1
     * @param password password to hash
     * @return byte[][]
     */
    public byte[][] hashPassword(String password) {
        SecureRandom random = new SecureRandom();
        byte[] salt = new byte[16];
        random.nextBytes(salt);

        try {
            KeySpec spec = new PBEKeySpec(password.toCharArray(), salt, 65536, 128);
            SecretKeyFactory factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA1");
            return new byte[][]{factory.generateSecret(spec).getEncoded(), salt};
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Hashes a password using PBKDF2WithHmacSHA1
     * Used for checking if a password is correct
     * @param password password to hash
     * @param salt salt to use
     * @return byte[]
     */
    public byte[] hashPassword(String password, byte[] salt) {
        try {
            KeySpec spec = new PBEKeySpec(password.toCharArray(), salt, 65536, 128);
            SecretKeyFactory factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA1");
            return factory.generateSecret(spec).getEncoded();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}
