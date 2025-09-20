package utils.CryptoStuff;

import utils.MainConfig;

import javax.crypto.Cipher;
import javax.crypto.CipherInputStream;
import javax.crypto.CipherOutputStream;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.Arrays;
import java.util.function.Function;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

public class AesStuff {

    public static byte[] EncryptBytesRandomIV(byte[] data, String randomStr) {
        try {
            return AesEncrypt(data, RandomStrToAesBytes(randomStr), RandomIvBytes());
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static byte[] DecryptBytesRandomIV(byte[] data, String randomStr) {
        try {
            return AesDecrypt(data, RandomStrToAesBytes(randomStr));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static byte[] EncryptBytesNoRandomIV(byte[] data, String randomStr) {
        try {
            return AesEncrypt(data, RandomStrToAesBytes(randomStr), RandomStrToIvBytes(randomStr));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static byte[] DecryptBytesNoRandomIV(byte[] data, String randomStr) {
        try {
            return AesDecrypt(data, RandomStrToAesBytes(randomStr));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static String EncryptStringNoRandomIv(String data, String randomStr) {
        return Base32Stuff.ByteArrToB32(EncryptBytesNoRandomIV(data.getBytes(StandardCharsets.UTF_8), randomStr));
    }

    public static String DecryptStringNoRandomIv(String data, String randomStr) {
        return new String(DecryptBytesNoRandomIV(Base32Stuff.B32ToByteArr(data), randomStr), StandardCharsets.UTF_8);
    }

    public static byte[] RandomStrToAesBytes(String randomStr) {
        return MainConfig.GetRandomBytesPwAndExtra(" _@#CRAZY BYTES AES#@_" + randomStr);
    }

    public static byte[] RandomStrToIvBytes(String randomStr) {
        byte[] data = MainConfig.GetRandomBytesPwAndExtra(" _@#CRAZY IV BYTES AES#@_" + randomStr);
        byte[] iv = new byte[IV_LENGTH_ENCRYPT];
        System.arraycopy(data, 0, iv, 0, iv.length);
        return iv;
    }

    public static byte[] RandomIvBytes() {
        byte[] iv = new byte[IV_LENGTH_ENCRYPT];
        SecureRandom secureRandom = new SecureRandom();
        secureRandom.nextBytes(iv);
        return iv;
    }



    public static final String SHA_CRYPT = "SHA-256";
    public static final String AES_ALGORITHM = "AES";
    public static final String AES_ALGORITHM_GCM = "AES/GCM/NoPadding";

    public static final Integer IV_LENGTH_ENCRYPT = 16;
    public static final Integer TAG_LENGTH_ENCRYPT = 16;

    private static SecretKeySpec generateAesKeyFromBytes(byte[] bytes) throws Exception {
        MessageDigest sha256 = MessageDigest.getInstance(SHA_CRYPT);
        byte[] keyBytes = sha256.digest(bytes);
        return new SecretKeySpec(keyBytes, AES_ALGORITHM);
    }

    public static byte[] AesEncrypt(byte[] data, byte[] random, byte[] iv) throws Exception {
        if (iv.length != IV_LENGTH_ENCRYPT)
            throw new RuntimeException("INVALID IV LEN");

        SecretKeySpec aesKey = generateAesKeyFromBytes(random);

        Cipher cipher = Cipher.getInstance(AES_ALGORITHM_GCM);
        GCMParameterSpec gcmSpec = new GCMParameterSpec(TAG_LENGTH_ENCRYPT * 8, iv);
        cipher.init(Cipher.ENCRYPT_MODE, aesKey, gcmSpec);

        byte[] encryptedBytes = cipher.doFinal(data);

        byte[] result = new byte[iv.length + encryptedBytes.length];
        System.arraycopy(iv, 0, result, 0, iv.length);
        System.arraycopy(encryptedBytes, 0, result, iv.length, encryptedBytes.length);
        return result;
    }

    public static byte[] AesDecrypt(byte[] data, byte[] random) throws Exception {
        SecretKeySpec aesKey = generateAesKeyFromBytes(random);

        byte[] iv = new byte[IV_LENGTH_ENCRYPT];
        System.arraycopy(data, 0, iv, 0, iv.length);
        byte[] encryptedText = new byte[data.length - IV_LENGTH_ENCRYPT];
        System.arraycopy(data, IV_LENGTH_ENCRYPT, encryptedText, 0, encryptedText.length);

        GCMParameterSpec gcmSpec = new GCMParameterSpec(TAG_LENGTH_ENCRYPT * 8, iv);
        Cipher cipher = Cipher.getInstance(AES_ALGORITHM_GCM);
        cipher.init(Cipher.DECRYPT_MODE, aesKey, gcmSpec);

        // Decrypt the ciphertext
        return cipher.doFinal(encryptedText);
    }

    private static final String testStrInput = "Hello AES Test!!!1!! OMG SO amazing incredible even";
    private static final byte[] testArrInput = new byte[] {1, 10, 20, 4, 100, 62, 13, 27, 14, 90};
    private static final String testExtra = "lolAbc123!§";
    public static void TestAesStuff() {
        System.out.println("> Testing AES Stuff");

        System.out.println(" > Initial String: " + testStrInput);

        String enc1 = EncryptStringNoRandomIv(testStrInput, testExtra);
        System.out.println(" > Encrypted String 1: " + enc1);
        String enc2 = EncryptStringNoRandomIv(testStrInput, testExtra);
        System.out.println(" > Encrypted String 2: " + enc2);
        if (!enc1.equals(enc2))
            throw new RuntimeException("No Random IV Encryption failed! mismatch");

        enc2 = EncryptStringNoRandomIv(testStrInput, testExtra + "a");
        System.out.println(" > Wrong Encrypted String: " + enc2);
        if (enc1.equals(enc2))
            throw new RuntimeException("No Random IV Encryption failed! identical");

        String dec1 = DecryptStringNoRandomIv(enc1, testExtra);
        System.out.println(" > Decrypted String 1: " + dec1);
        String dec2 = DecryptStringNoRandomIv(enc1, testExtra);
        System.out.println(" > Decrypted String 2: " + dec2);
        if (!dec1.equals(dec2))
            throw new RuntimeException("No Random IV Decryption failed! mismatch");

        if (!testStrInput.equals(dec1))
            throw new RuntimeException("String En/Decryption failed! mismatch");
        System.out.println("  > Strings match!\n");




        System.out.println(" > Initial Arr: " + Arrays.toString(testArrInput));

        byte[] encArr1 = EncryptBytesRandomIV(testArrInput, testExtra);
        System.out.println(" > Encrypted Arr 1: " + Arrays.toString(encArr1));
        byte[] encArr2 = EncryptBytesRandomIV(testArrInput, testExtra);
        System.out.println(" > Encrypted Arr 2: " + Arrays.toString(encArr2));
        if (Arrays.equals(encArr1, encArr2))
            throw new RuntimeException("Random IV Encryption failed! identical");


        byte[] decArr1 = DecryptBytesRandomIV(encArr1, testExtra);
        System.out.println(" > Decrypted String 1: " + Arrays.toString(decArr1));
        byte[] decArr2 = DecryptBytesRandomIV(encArr2, testExtra);
        System.out.println(" > Decrypted String 2: " + Arrays.toString(decArr2));
        if (!Arrays.equals(decArr1, decArr2))
            throw new RuntimeException("Random IV Decryption failed! mismatch");

        if (!Arrays.equals(testArrInput, decArr1))
            throw new RuntimeException("Arr En/Decryption failed! mismatch");
        System.out.println("  > Arrays match!\n");

        System.out.println(" > AES Test successful!\n\n");
    }





    public static final String AES_ALGORITHM_CBC = "AES/CBC/PKCS5Padding";



    public static Function<OutputStream, OutputStream> EncryptStreamRandomIV(String randomStr) {
        return (data) -> {
            try {
                byte[] random = RandomStrToAesBytes(randomStr);
                byte[] iv = RandomIvBytes();

                if (iv.length != IV_LENGTH_ENCRYPT)
                    throw new RuntimeException("INVALID IV LEN");

                SecretKeySpec aesKey = generateAesKeyFromBytes(random);

                Cipher cipher = Cipher.getInstance(AES_ALGORITHM_CBC);
                IvParameterSpec ivSpec = new IvParameterSpec(iv);
                cipher.init(Cipher.ENCRYPT_MODE, aesKey, ivSpec);

                data.write(iv);
                return new CipherOutputStream(data, cipher);
            } catch (Exception e) {
                e.printStackTrace();
                throw new RuntimeException(e.getMessage());
            }
        };
    }

    public static Function<InputStream, InputStream> DecryptStreamRandomIV(String randomStr) {
        return (data) -> {
            try {
                byte[] iv = new byte[IV_LENGTH_ENCRYPT];
                if (data.readNBytes(iv, 0, iv.length) != IV_LENGTH_ENCRYPT)
                    throw new RuntimeException("INVALID IV LEN READ");

                byte[] random = RandomStrToAesBytes(randomStr);
                SecretKeySpec aesKey = generateAesKeyFromBytes(random);

                IvParameterSpec ivSpec = new IvParameterSpec(iv);
                Cipher cipher = Cipher.getInstance(AES_ALGORITHM_CBC);
                cipher.init(Cipher.DECRYPT_MODE, aesKey, ivSpec);

                return new CipherInputStream(data, cipher);
            } catch (Exception e) {
                e.printStackTrace();
                throw new RuntimeException(e.getMessage());
            }
        };
    }
}
