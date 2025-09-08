import utils.*;
import utils.CryptoStuff.AesStuff;
import utils.CryptoStuff.Base32Stuff;
import utils.CryptoStuff.HashStuff;
import utils.CryptoStuff.Pbkdf2Stuff;

import java.io.Console;


public class Main {
    public static void main(String[] args) {
        Base32Stuff.TestBase32Stuff();
        HashStuff.TestHashStuff();
        CompressionStuff.TestCompressionStuff();
        FsStuff.InitFsStuff();
        Pbkdf2Stuff.InitPbkdf2Stuff(MainConfig.glob.randomSalt);
        AesStuff.TestAesStuff();

        System.out.println("> Started with args: " + String.join(",", args));
        String option = "";
        Console console = System.console();
        while (!option.equals("backup") && !option.equals("restore")) {
            System.out.println("> Please choose between \"backup\" and \"restore\".");
            option = console.readLine("> ").trim();
            System.out.println();
        }

        if (option.equals("backup"))
            BackupStuff.DoBackup();
        else if (option.equals("restore"))
            RestoreStuff.DoRestore("./restore");
        else
            System.err.println("> Unknown option: " + option);

        System.out.println("> Done");
    }
}