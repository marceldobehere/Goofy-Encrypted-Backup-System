import utils.CryptoStuff.HashStuff;
import utils.DirectoryTraversal;
import utils.FsStuff;
import utils.JsonUtils;
import utils.MainConfig;

public class RestoreStuff {
    public static void DoRestore(String _restorePath) {
        System.out.println("> Doing Restore");
        final String remoteConf = MainConfig.glob.outputPath + "/main.bin";
        final String remoteConfExtra = "RemoteConfig Yes!";

        String aRestorePath = _restorePath + "/restore _" + HashStuff.RandomHashStr().substring(0,8) + "_";
        String restorePath = aRestorePath + "/data";
        FsStuff.CreateFolderIfNotExist(restorePath);
        System.out.println(" > Restoring to: " + restorePath);

        System.out.println(" > Attempting to load existing remote traversal data");
        if (!FsStuff.DoesFileExist(remoteConf))
            throw new RuntimeException("Remote Traversal Data not found!");

        // Load & Save Traversal Data
        DirectoryTraversal remoteTraversal = JsonUtils.ParseJSON(FsStuff.ReadEncryptedFile(remoteConf, remoteConfExtra), DirectoryTraversal.class);
        System.out.println(" > Remote Traversal: ");
        if (MainConfig.glob.logs)
            System.out.println(remoteTraversal);
        System.out.println();

        System.out.println(" > Attempting to save existing traversal data");
        JsonUtils.CreateJSONFile(aRestorePath + "/main.json", remoteTraversal, false);


        // Decrypt & Save Files
        System.out.println(" > Attempting to restore all files");
        remoteTraversal.Entries.forEach((entry) -> {
            try {
                if (MainConfig.glob.logs)
                    System.out.println("  > Saving File/Folder: " + entry);
                String inPath = MainConfig.glob.outputPath + "/data/" +
                        DirectoryTraversal.GetParentPathHashFolder(entry.parentPath) + "/" +
                        entry.GetHashPath();
                String resPath = restorePath + "/" +
                        DirectoryTraversal.GetParentPathHashFolder(entry.parentPath) + "/" +
                        entry.path;
                // System.out.println("   > In Path:  " + inPath);
                // System.out.println("   > Res Path: " + resPath);

                if (entry.isFile) {
                    byte[] data = FsStuff.ReadCompressedEncryptedBytesFile(inPath + ".bin", entry.EntryToHash());
                    FsStuff.WriteEntireFileBytes(resPath, data);
                } else {
                    FsStuff.CreateFolderIfNotExist(resPath);
                }
            } catch (Exception e) {
                System.err.println("> ERROR: Failed to write file!");
                System.err.println(entry);
                System.err.println(e.getMessage());
                e.printStackTrace();
            }
        });

        System.out.println("> Restore complete");
    }
}
