package com.marcel.utils.CryptoStuff;

import org.apache.commons.codec.binary.Hex;
import org.apache.commons.codec.digest.DigestUtils;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;

public class HashStuff {
    public static byte[] HashInput(String data) {
        return HashInput(data.getBytes(StandardCharsets.UTF_8));
    }

    public static byte[] HashInput(byte[] data) {
        return DigestUtils.sha256(data);
    }

    public static String HashInputToStr(byte[] data) {
        return ByteArrToHexStr(HashInput(data));
    }

    public static String HashInputToStr(String data) {
        return ByteArrToHexStr(HashInput(data));
    }

    public static String ByteArrToHexStr(byte[] data) {
        return Hex.encodeHexString(data);
    }

    public static String RandomHashStr() {
        return HashInputToStr(Pbkdf2Stuff.CreateRandomSalt());
    }

    private static final String inputStr = "lol crazy Hash input String!!!1!";
    private static final byte[] inputArr = new byte[] {108, 111, 108, 32, 99, 114, 97, 122, 121, 32, 72, 97, 115, 104, 32, 105, 110, 112, 117, 116, 32, 83, 116, 114, 105, 110, 103, 33, 33, 33, 49, 33};
    private static final byte[] hashArr = new byte[] {123, 62, 78, 122, -4, -9, 93, -97, 93, 55, -44, 8, -15, 112, -36, -117, 72, 6, 16, -73, 96, 112, -57, -70, 17, 1, -50, 28, -23, 89, -86, 106};
    private static final String hashStr = "7b3e4e7afcf75d9f5d37d408f170dc8b480610b76070c7ba1101ce1ce959aa6a";
    public static void TestHashStuff() {
        System.out.println("> Testing Hash Stuff");
        System.out.println(" > Input String: " + inputStr);

        byte[] resHash = HashInput(inputStr);
        System.out.println(" > Hash from Str: " + Arrays.toString(resHash));
        if (!Arrays.equals(resHash, hashArr))
            throw new RuntimeException("Hashing from Str failed! mismatch");
        System.out.println("  > Hash Str bytearr matches!");

        resHash = HashInput(inputArr);
        System.out.println(" > Hash from Arr: " + Arrays.toString(resHash));
        if (!Arrays.equals(resHash, hashArr))
            throw new RuntimeException("Hashing from Arr failed! mismatch");
        System.out.println("  > Hash Arr bytearr matches!");

        String resStr = ByteArrToHexStr(resHash);
        System.out.println(" > Hex Str from Arr: " + resStr);
        if (!resStr.equals(hashStr))
            throw new RuntimeException("Hash to Hex String failed! mismatch");
        System.out.println("  > Hex String matches!");


        resStr = HashInputToStr(inputStr);
        System.out.println(" > Hex Str from Input: " + resStr);
        if (!resStr.equals(hashStr))
            throw new RuntimeException("Str to Hex String failed! mismatch");
        System.out.println("  > Hex String matches!");

        System.out.println(" > Hash Test successful!\n\n");
    }
}
