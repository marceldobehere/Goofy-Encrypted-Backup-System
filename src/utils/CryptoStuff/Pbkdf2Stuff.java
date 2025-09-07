package utils.CryptoStuff;

import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.KeySpec;
import java.util.Arrays;
import java.util.List;

public class Pbkdf2Stuff {
    private static byte[] globalPwSalt;

    public static void InitPbkdf2Stuff(List<Byte> pwSalt) {
        InitPbkdf2Stuff(ArrConv.ByteCListToBytePArr(pwSalt));
    }

    public static void InitPbkdf2Stuff(byte[] pwSalt) {
        System.out.println("> Initialising Pbkdf2 Stuff");
        globalPwSalt = pwSalt;

        try {
            TestPbkdf2();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }



    private static final String testStr = "Test123";
    private static final byte[] testSalt = new byte[] {90, -108, -73, 43, 51, -51, -114, 3, 118, 80, 6, -47, 32, -12, -41, 127, 22, 71, -111, 42, 100, -99, 42, -108, 10, -57, -106, -39, 107, 70, -51, 55};
    private static final byte[] testStrRes = new byte[] {-65, 79, 76, 16, 7, 120, 36, -124, -119, 90, 49, 69, -103, -3, -18, -37, -45, -50, 117, 101, -48, -50, -112, 88, -51, 87, -30, -52, -58, 102, -14, 87};
    public static void TestPbkdf2() throws Exception {
        System.out.println("> Running Pbkdf2 Test");
        byte[] res = getRandomFromPassword(testStr, testSalt);
        System.out.println(" > Result: " + Arrays.toString(res));

        if (Arrays.equals(res, testStrRes)) {
            System.out.println(" > Results match");
        } else {
            System.err.println(" > Results do NOT match! (" + Arrays.toString(res) + " != " + Arrays.toString(testStrRes) + ")");
            throw new Exception("> PBKDF2 Init Test failed! Mismatch!!!");
        }

        System.out.println(" > Pbkdf2 Test successful!");
    }


    public static byte[] CreateRandomSalt() {
        byte[] secure = new byte[32];
        try {
            SecureRandom.getInstanceStrong().nextBytes(secure);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
        System.out.println("> Created secure random salt: " + Arrays.toString(secure));
        return secure;
    }


    private static final int iterations = 1000;
    private static final int derivedKeyLength = 32;
    public static byte[] getRandomFromPassword(
            String password,
            byte[] salt
    ) throws NoSuchAlgorithmException, InvalidKeySpecException {
        KeySpec spec = new PBEKeySpec(
                password.toCharArray(),
                salt,
                iterations,
                derivedKeyLength * 8
        );

        SecretKeyFactory f = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256");

        return f.generateSecret(spec).getEncoded();
    }
}
