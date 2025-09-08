import utils.DirectoryTraversal;
import utils.FsStuff;
import utils.JsonUtils;
import utils.MainConfig;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

public class BackupStuff {
    public static void DoBackup(boolean fullBackup) {
        System.out.println("> Doing Backup");
        final String remoteConf = MainConfig.glob.outputPath + "/main.bin";
        final String remoteConfExtra = "RemoteConfig Yes!";

        // Remote Stuff
        System.out.println(" > Attempting to load existing remote traversal data");
        DirectoryTraversal remoteTraversal = new DirectoryTraversal();
        if (FsStuff.DoesFileExist(remoteConf))
            remoteTraversal = JsonUtils.ParseJSON(FsStuff.ReadEncryptedFile(remoteConf, remoteConfExtra), DirectoryTraversal.class);
        System.out.println(" > Remote Traversal: ");
        if (MainConfig.glob.logs)
            System.out.println(remoteTraversal);
        System.out.println();

        if (fullBackup) {
            System.out.println(" > Wiping backup remains for new clean backup");
            remoteTraversal.Entries.forEach((val) -> {
                try {
                    if (MainConfig.glob.logs)
                        System.out.println("  > Deleting File/Folder: " + val);
                    String inPath = val.parentPath + "/" + val.path;
                    String resPath = MainConfig.glob.outputPath + "/data/" +
                            DirectoryTraversal.GetParentPathHashFolder(val.parentPath) + "/" +
                            val.GetHashPath();

                    if (val.isFile) {
                        FileUtils.forceDelete(new File(resPath + ".bin"));
                    } else {
                        FileUtils.forceDelete(new File(resPath));
                    }
                } catch (Exception e) {
                    System.err.println("> WARN/ERROR: Failed to delete file! " + e.getMessage());
                    System.err.println(val);
                }
            });

            remoteTraversal = new DirectoryTraversal();
        }


        HashMap<String, DirectoryTraversal.TraversalEntry> remoteSet = remoteTraversal.ConvertToHashMap();

        // Local Stuff
        System.out.println(" > Creating local traversal data");
        DirectoryTraversal traversal = new DirectoryTraversal(MainConfig.glob.inputPaths);
        System.out.println(" > Local Traversal: ");
        if (MainConfig.glob.logs)
            System.out.println(traversal);
        HashMap<String, DirectoryTraversal.TraversalEntry> localSet = traversal.ConvertToHashMap();

        Set<String> addedFiles = new HashSet<>();

        // Write new Stuff
        System.out.println(" > Writing new Files / Folders");
        localSet.forEach((key, val) -> {
            if (!remoteSet.containsKey(key)) {
                try {
                    if (MainConfig.glob.logs)
                        System.out.println("  > New File/Folder: " + val);
                    String inPath = val.parentPath + "/" + val.path;
                    String resPath = MainConfig.glob.outputPath + "/data/" +
                            DirectoryTraversal.GetParentPathHashFolder(val.parentPath) + "/" +
                            val.GetHashPath();
                    // System.out.println("   > In Path:  " + inPath);
                    // System.out.println("   > Res Path: " + resPath);

                    if (val.isFile) {
                        byte[] data = FsStuff.ReadEntireFileBytes(inPath);
                        FsStuff.WriteCompressedEncryptedBytesFile(resPath + ".bin", data, val.EntryToHash());
                    } else {
                        FsStuff.CreateFolderIfNotExist(resPath);
                    }
                    addedFiles.add(val.parentPath + "/" + val.path);
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
        if (MainConfig.glob.syncDeletions) {
            System.out.println(" > Syncing deletions");
            remoteSet.forEach((key, val) -> {
                if (!localSet.containsKey(key) && !addedFiles.contains(val.parentPath + "/" + val.path)) {
                    try {
                        if (MainConfig.glob.logs)
                            System.out.println("  > Old File/Folder: " + val);
                        String inPath = val.parentPath + "/" + val.path;
                        String resPath = MainConfig.glob.outputPath + "/data/" +
                                DirectoryTraversal.GetParentPathHashFolder(val.parentPath) + "/" +
                                val.GetHashPath();

                        if (val.isFile) {
                            FileUtils.forceDelete(new File(resPath + ".bin"));
                        } else {
                            FileUtils.forceDelete(new File(resPath));
                        }
                    } catch (Exception e) {
                        System.err.println("> WARN/ERROR: Failed to delete file!");
                        System.err.println(val);
                        System.err.println(e.getMessage());
                        //e.printStackTrace();
                    }
                }
            });
        }

        // Save new list
        FsStuff.WriteEncryptedFile(remoteConf, JsonUtils.CreateJSON(traversal, false), remoteConfExtra);

        System.out.println("> Backup complete");
    }
}
