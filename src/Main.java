import utils.*;
import utils.CryptoStuff.AesStuff;
import utils.CryptoStuff.Base32Stuff;
import utils.CryptoStuff.HashStuff;
import utils.CryptoStuff.Pbkdf2Stuff;


public class Main {
    public static void main(String[] args) {
        Base32Stuff.TestBase32Stuff();
        HashStuff.TestHashStuff();
        CompressionStuff.TestCompressionStuff();
        FsStuff.InitFsStuff();
        Pbkdf2Stuff.InitPbkdf2Stuff(MainConfig.glob.randomSalt);
        AesStuff.TestAesStuff();

        BackupStuff.DoBackup();

        RestoreStuff.DoRestore("./restore");

        System.out.println("> Done");
    }
}