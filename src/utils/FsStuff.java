package utils;

import utils.CryptoStuff.AesStuff;
import utils.CryptoStuff.ArrConv;
import utils.CryptoStuff.Pbkdf2Stuff;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.stream.Collectors;

public class FsStuff {
    public static final String configPath = "./conf.json";

    public static void InitFsStuff() {
        System.out.println("> Initialising FS Stuff");

        if (!DoesFileExist(configPath)) {
            System.out.println("> ERROR: Config File does not exist!");
            System.exit(1);
        }
        MainConfig.glob = JsonUtils.ParseJSONFile(configPath, MainConfig.class);
        System.out.println("> Main Config: " + MainConfig.glob);

        if (!DoesFileExist(MainConfig.glob.secretFile)) {
            System.out.println("> ERROR: Secret File does not exist!");
            System.exit(1);
        }
        MainConfig.encPassword = ReadEntireFile(MainConfig.glob.secretFile).trim();
        // System.out.println("> Secret: \"" + MainConfig.encPassword + "\"");

        if (MainConfig.glob.randomSalt == null) {
            System.out.println("> No Random Salt found!");
            MainConfig.glob.randomSalt = ArrConv.ByteBArrToByteCList(Pbkdf2Stuff.CreateRandomSalt());
            JsonUtils.CreateJSONFile(configPath, MainConfig.glob);
        }

        for (String inputPath : MainConfig.glob.inputPaths)
            FsStuff.CreateFolderIfNotExist(inputPath);
        FsStuff.CreateFolderIfNotExist(MainConfig.glob.outputPath);
        FsStuff.CreateFolderIfNotExist(MainConfig.glob.outputPath + "/data");

        System.out.println("> FS Initialised\n\n");
    }

    public static boolean DoesFileExist(String path) {
        return Files.exists(Paths.get(path));
    }

    public static String ReadEntireFile(String path) {
        try {
            return new String(Files.readAllBytes(Paths.get(path)), StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static byte[] ReadEntireFileBytes(String path) {
        try {
            return Files.readAllBytes(Paths.get(path));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static boolean WriteEntireFile(String path, String data) {
        try {
            Files.write(Paths.get(path), data.getBytes(StandardCharsets.UTF_8));
            return true;
        } catch (IOException e) {
            System.err.println("> WARNING: Failed to write file!");
            e.printStackTrace();
            return false;
        }
    }

    public static boolean WriteListToFile(String path, List<String> data) {
        try {
            Files.write(Paths.get(path), data, Charset.defaultCharset());
            return true;
        } catch (IOException e) {
            System.err.println("> WARNING: Failed to write file!");
            e.printStackTrace();
            return false;
        }
    }

    public static List<String> ReadListFromFile(String path) {
        try {
            return Files.readAllLines(Paths.get(path));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static boolean WriteEncryptedFile(String path, String data, String randomStr) {
        try {
            Files.write(Paths.get(path), AesStuff.EncryptBytesRandomIV(data.getBytes(StandardCharsets.UTF_8), randomStr));
            return true;
        } catch (IOException e) {
            System.err.println("> WARNING: Failed to write file!");
            e.printStackTrace();
            return false;
        }
    }

    public static String ReadEncryptedFile(String path, String randomStr) {
        try {
            return new String(AesStuff.DecryptBytesRandomIV(Files.readAllBytes(Paths.get(path)), randomStr), StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static boolean WriteEncryptedBytesFile(String path, byte[] data, String randomStr) {
        try {
            Files.write(Paths.get(path), AesStuff.EncryptBytesRandomIV(data, randomStr));
            return true;
        } catch (IOException e) {
            System.err.println("> WARNING: Failed to write file!");
            e.printStackTrace();
            return false;
        }
    }

    public static byte[] ReadEncryptedBytesFile(String path, String randomStr) {
        try {
            return AesStuff.DecryptBytesRandomIV(Files.readAllBytes(Paths.get(path)), randomStr);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static boolean WriteCompressedEncryptedBytesFile(String path, byte[] data, String randomStr) {
        try {
            CreateFolderIfNotExist(Paths.get(path).getParent().toString());
            Files.write(Paths.get(path), AesStuff.EncryptBytesRandomIV(CompressionStuff.Compress(data), randomStr));
            return true;
        } catch (IOException e) {
            System.err.println("> WARNING: Failed to write file!");
            e.printStackTrace();
            return false;
        }
    }

    public static byte[] ReadCompressedEncryptedBytesFile(String path, String randomStr) {
        try {
            return CompressionStuff.Decompress(AesStuff.DecryptBytesRandomIV(Files.readAllBytes(Paths.get(path)), randomStr));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static void CreateFolderIfNotExist(String path) {
        try {
            Files.createDirectories(Paths.get(path));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
