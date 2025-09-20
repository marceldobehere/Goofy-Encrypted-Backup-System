import utils.*;
import utils.CryptoStuff.AesStuff;
import utils.CryptoStuff.Base32Stuff;
import utils.CryptoStuff.HashStuff;
import utils.CryptoStuff.Pbkdf2Stuff;

import java.util.Scanner;


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
        if (args.length > 0)
            option = args[0];

        while (true) {
            Scanner scanner = new Scanner(System.in);
            while (!option.equals("backup") && !option.equals("backup-clean") && !option.equals("restore") && !option.equals("exit")) {
                if (option.isEmpty())
                    System.out.println("> Unknown option: " + option);

                System.out.println("> Please choose between \"backup\", \"backup-clean\", \"restore\" and \"exit\".");
                System.out.print("Input> ");
                option = scanner.nextLine().trim();
                System.out.println();
            }

            if (option.equals("backup"))
                BackupStuff.DoBackup(false);
            else if (option.equals("backup-clean"))
                BackupStuff.DoBackup(true);
            else if (option.equals("restore"))
                RestoreStuff.DoRestore("./restore");
            else if (option.equals("exit"))
                System.exit(0);
            else
                System.err.println("> Unknown option: " + option);

            option = "";
        }
    }
}