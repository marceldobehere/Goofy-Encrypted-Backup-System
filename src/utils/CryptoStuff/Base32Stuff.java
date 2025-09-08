package utils.CryptoStuff;

import org.apache.commons.codec.binary.Base32;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;

public class Base32Stuff {
    public static String ByteArrToB32(byte[] arr) {
        Base32 base32 = new Base32();
        return base32.encodeAsString(arr);
    }

    public static byte[] B32ToByteArr(String b32) {
        Base32 base32 = new Base32();
        return base32.decode(b32);
    }

    public static String B32ToFs(String b32) {
        return b32.replace('=', '_');
    }

    public static String FsToB32(String fs) {
        return fs.replace('_', '=');
    }

    public static String StringToB32Fs(String path) {
        return B32ToFs(ByteArrToB32(path.getBytes(StandardCharsets.UTF_8)));
    }

    public static String B32FsToString(String b32Fs) {
        return new String(B32ToByteArr(FsToB32(b32Fs)), StandardCharsets.UTF_8);
    }



    private static final String testStrInput = "test! abc YES 12345 === \" lol";
    private static final byte[] testStrInputArr = new byte[] {116, 101, 115, 116, 33, 32, 97, 98, 99, 32, 89, 69, 83, 32, 49, 50, 51, 52, 53, 32, 61, 61, 61, 32, 34, 32, 108, 111, 108};
    private static final String testStrOutputB32 = "ORSXG5BBEBQWEYZALFCVGIBRGIZTINJAHU6T2IBCEBWG63A=";
    private static final String testStrOutputFS = "ORSXG5BBEBQWEYZALFCVGIBRGIZTINJAHU6T2IBCEBWG63A_";
    public static void TestBase32Stuff() {
        System.out.println("> Testing Base32 Stuff");

        System.out.println(" > Initial String: " + testStrInput);
        byte[] arr = testStrInput.getBytes(StandardCharsets.UTF_8);
        System.out.println(" > UTF-8 Arr: " + Arrays.toString(arr));
        if (!Arrays.equals(arr, testStrInputArr))
            throw new RuntimeException("String to Byte arr failed! mismatch");
        System.out.println("  > Arrays match!");

        String resB32 = ByteArrToB32(arr);
        System.out.println(" > B32: " + resB32);
        if (!resB32.equals(testStrOutputB32))
            throw new RuntimeException("Byte arr to B32 failed! mismatch");
        System.out.println("  > B32 str matches!");

        String resFs = B32ToFs(resB32);
        System.out.println(" > FS: " + resFs);
        if (!resFs.equals(testStrOutputFS))
            throw new RuntimeException("B32 to FS failed! mismatch");
        System.out.println("  > FS str matches!");

        String resFs2 = StringToB32Fs(testStrInput);
        System.out.println(" > String -> B32FS: " + resFs);
        if (!resFs2.equals(resFs))
            throw new RuntimeException("String to B32FS failed! mismatch");
        System.out.println("  > B32FS str matches!");

        String resStr = B32FsToString(resFs2);
        System.out.println(" > B32FS -> String: " + resStr);
        if (!resStr.equals(testStrInput))
            throw new RuntimeException("B32FS to String failed! mismatch");
        System.out.println("  > Initial str matches!");

        System.out.println(" > Base32 Test successful!\n\n");
    }
}
