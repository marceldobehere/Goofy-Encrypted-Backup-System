import utils.CryptoStuff.HashStuff;
import utils.FsStuff;
import utils.MainConfig;

public class RestoreStuff {
    public static void DoRestore(String _restorePath) {
        String aRestorePath = _restorePath + "/restore _" + HashStuff.RandomHashStr().substring(0,8) + "_";
        FsStuff.CreateFolderIfNotExist(aRestorePath);
        System.out.println("> Restoring to: " + aRestorePath);


    }
}
