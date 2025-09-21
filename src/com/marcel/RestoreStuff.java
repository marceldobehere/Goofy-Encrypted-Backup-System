package com.marcel;

import com.marcel.utils.*;
import com.marcel.utils.CryptoStuff.AesStuff;
import com.marcel.utils.CryptoStuff.HashStuff;

import java.util.concurrent.atomic.AtomicInteger;

public class RestoreStuff {
    public static void DoRestore(String _restorePath) {
        System.out.println("> Doing Restore");
        final String remoteConf = FsStuff.JoinPath(MainConfig.glob.outputPath, "main.bin");
        final String remoteConfExtra = "RemoteConfig Yes!";

        String aRestorePath = FsStuff.JoinPath(_restorePath, "restore _" + HashStuff.RandomHashStr().substring(0,8) + "_");
        String restorePath = FsStuff.JoinPath(aRestorePath, "data");
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
        JsonUtils.CreateJSONFile(FsStuff.JoinPath(aRestorePath, "main.json"), remoteTraversal, false);


        // Decrypt & Save Files
        System.out.println(" > Attempting to restore all files");
        AtomicInteger lastPercent = new AtomicInteger();
        AtomicInteger inc = new AtomicInteger(0);
        remoteTraversal.Entries.stream().parallel().forEach((entry) -> {
            try {
                if (MainConfig.glob.logs)
                    System.out.println("  > Saving File/Folder: " + entry);
                String inPath = FsStuff.JoinPath(MainConfig.glob.outputPath, "data",
                        DirectoryTraversal.GetParentPathHashFolder(entry.parentPath),
                        entry.GetHashPath());
                String resPath = FsStuff.JoinPath(restorePath,
                        DirectoryTraversal.GetParentPathHashFolder(entry.parentPath),
                        entry.path);
                // System.out.println("   > In Path:  " + inPath);
                // System.out.println("   > Res Path: " + resPath);

                if (entry.isFile) {
                    GoofyStream stream = FsStuff.CreateGoofyStream(inPath + ".bin", resPath);
                    stream.connectIs(AesStuff.DecryptStreamRandomIV(entry.EntryToHash()));
                    stream.connectIs(CompressionStuff::DecompressStream);
                    stream.complete();
                } else {
                    FsStuff.CreateFolderIfNotExist(resPath);
                }

                synchronized (inc) {
                    inc.incrementAndGet();
                    int percent = (inc.get() * 100) / remoteTraversal.Entries.size();
                    if (percent != lastPercent.get()) {
                        lastPercent.set(percent);
                        System.out.println("  > Main Restore progress: " + percent + "%");
                    }
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
