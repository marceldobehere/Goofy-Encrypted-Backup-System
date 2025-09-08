import utils.DirectoryTraversal;
import utils.FsStuff;
import utils.JsonUtils;
import utils.MainConfig;

import java.util.HashMap;
import java.util.Set;

public class BackupStuff {
    public static final String remoteConf = MainConfig.glob.outputPath + "/main.bin";
    public static final String remoteConfExtra = "RemoteConfig Yes!";

    public static void DoBackup() {
        System.out.println("> Doing Backup");

        // Remote Stuff
        System.out.println(" > Attempting to load existing remote traversal data");
        DirectoryTraversal remoteTraversal = new DirectoryTraversal();
        if (FsStuff.DoesFileExist(remoteConf))
            remoteTraversal = JsonUtils.ParseJSON(FsStuff.ReadEncryptedFile(remoteConf, remoteConfExtra), DirectoryTraversal.class);
        System.out.println(" > Remote Traversal: ");
        System.out.println(remoteTraversal);
        System.out.println();
        HashMap<String, DirectoryTraversal.TraversalEntry> remoteSet = remoteTraversal.ConvertToHashMap();

        // Local Stuff
        System.out.println(" > Creating local traversal data");
        DirectoryTraversal traversal = new DirectoryTraversal(MainConfig.glob.inputPaths);
        System.out.println(" > Local Traversal: ");
        System.out.println(traversal);
        HashMap<String, DirectoryTraversal.TraversalEntry> localSet = traversal.ConvertToHashMap();

        // Write new Stuff
        System.out.println(" > Writing new files");
        localSet.forEach((key, val) -> {
            if (!remoteSet.containsKey(key)) {
                try {
                    System.out.println("  > New File/Folder: " + val);
                    String inPath = val.parentPath + val.path;
                    String resPath = MainConfig.glob.outputPath + "/data/" +
                            DirectoryTraversal.GetParentPathHashFolder(val.parentPath) + "/" +
                            val.GetHashPath();
                    System.out.println("   > In Path:  " + inPath);
                    System.out.println("   > Res Path: " + resPath);

                    if (val.isFile) {
                        byte[] data = FsStuff.ReadEntireFileBytes(inPath);
                        FsStuff.WriteCompressedEncryptedBytesFile(resPath + ".bin", data, val.EntryToHash());
                    } else {
                        FsStuff.CreateFolderIfNotExist(resPath);
                    }
                } catch (Exception e) {
                    System.err.println("> ERROR: Failed to write file!");
                    System.err.println(val);
                    System.err.println(e.getMessage());
                    e.printStackTrace();
                }
            }
        });
        System.out.println();

        // Maybe delete old Stuff


        // Save new list
        //FsStuff.WriteEncryptedFile(remoteConf, JsonUtils.CreateJSON(traversal, false), remoteConfExtra);

        System.out.println("> Backup complete");
    }
}
