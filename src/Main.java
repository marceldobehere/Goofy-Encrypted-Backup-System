import utils.Base32Stuff;
import utils.CryptoStuff.Pbkdf2Stuff;
import utils.DirectoryTraversal;
import utils.FsStuff;
import utils.MainConfig;


public class Main {
    public static void main(String[] args) {
        Base32Stuff.TestBase32Stuff();
        FsStuff.InitFsStuff();
        Pbkdf2Stuff.InitPbkdf2Stuff(MainConfig.glob.randomSalt);

        DirectoryTraversal traversal = new DirectoryTraversal(MainConfig.glob.inputPaths);

        System.out.println("> Done");
    }
}