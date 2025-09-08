package utils;

import java.io.*;
import java.util.Arrays;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

public class CompressionStuff {
    private static final int BUFFER_SIZE = 1024;

    public static void gzip(InputStream is, OutputStream os) throws IOException {
        GZIPOutputStream gzipOs = new GZIPOutputStream(os);
        byte[] buffer = new byte[BUFFER_SIZE];
        int bytesRead = 0;
        while ((bytesRead = is.read(buffer)) > -1)
            gzipOs.write(buffer, 0, bytesRead);
        gzipOs.close();
    }

    public static void ungzip(InputStream is, OutputStream os) throws IOException {
        GZIPInputStream gzipIs = new GZIPInputStream(is);
        byte[] buffer = new byte[BUFFER_SIZE];
        int bytesRead = 0;
        while ((bytesRead = gzipIs.read(buffer)) > -1)
            os.write(buffer, 0, bytesRead);
        gzipIs.close();
    }

    public static byte[] Compress(byte[] data) {
        ByteArrayOutputStream os = new ByteArrayOutputStream();
        try {
            gzip(new ByteArrayInputStream(data), os);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return os.toByteArray();
    }

    public static byte[] Decompress(byte[] data) {
        ByteArrayOutputStream os = new ByteArrayOutputStream();
        try {
            ungzip(new ByteArrayInputStream(data), os);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return os.toByteArray();
    }




    private static final byte[] inputArr = new byte[] {108, 111, 108, 32, 99, 114, 97, 122, 121, 32, 72, 97, 115, 104, 32, 105, 110, 112, 117, 116, 32, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1};
    public static void TestCompressionStuff() {
        System.out.println("> Testing Compression Stuff");
        System.out.println(" > Input Arr: " + Arrays.toString(inputArr));

        byte[] compressed = Compress(inputArr);
        System.out.println(" > Compressed Arr: " + Arrays.toString(compressed));

        byte[] decompressed = Decompress(compressed);
        System.out.println(" > Decompressed Arr: " + Arrays.toString(decompressed));

        if (!Arrays.equals(inputArr, decompressed))
            throw new RuntimeException("Compression/Decompression failed! mismatch");
        System.out.println("  > Decompressed Arr matches!");

        System.out.println(" > Compression Test successful!\n\n");
    }
}
