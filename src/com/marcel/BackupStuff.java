package com.marcel;

import com.marcel.utils.*;
import org.apache.commons.io.FileUtils;
import com.marcel.utils.CryptoStuff.AesStuff;

import java.io.File;
import java.nio.file.CopyOption;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

public class BackupStuff {
    public static void DoBackup(boolean fullBackup) {
        System.out.println("> Doing Backup");
        final String remoteConf = FsStuff.JoinPath(MainConfig.glob.outputPath, "main.bin");
        final String remoteConfBak = remoteConf + ".bak";
        final String remoteConfExtra = "RemoteConfig Yes!";

        // Check Total Space
        long spaceTotal = new File(MainConfig.glob.outputPath).getTotalSpace() / (1024 * 1024 * 1024);
        System.out.println(" > Remote Space Total GB: " + spaceTotal);
        if (spaceTotal < MainConfig.glob.neededGb) {
            System.err.println("> NOT ENOUGH TOTAL SPACE!!! NEEDED: " + MainConfig.glob.neededGb + "GB BUT GOT " + spaceTotal + "GB INSTEAD");
            System.exit(-1);
        }

        // Check Free Space
        long spaceFree = new File(MainConfig.glob.outputPath).getFreeSpace() / (1024 * 1024 * 1024);
        final long spaceFreeMin = 20;
        System.out.println(" > Remote Space Free GB: " + spaceFree);
        if (spaceFree < spaceFreeMin) {
            System.err.println("> NOT ENOUGH FREE SPACE!!! NEEDED: " + spaceFreeMin + "GB BUT GOT " + spaceFree + "GB INSTEAD");
            System.exit(-1);
        }

        // Remote Stuff
        System.out.println(" > Attempting to load existing remote traversal data");
        DirectoryTraversal remoteTraversal = new DirectoryTraversal();
        if (FsStuff.DoesFileExist(remoteConf)) {
            try {
                remoteTraversal = JsonUtils.ParseJSON(FsStuff.ReadEncryptedFile(remoteConf, remoteConfExtra), DirectoryTraversal.class);
            } catch (Exception e) {
                System.err.println("> REMOTE ERROR 1: " + e.getMessage());
                e.printStackTrace();

                if (FsStuff.DoesFileExist(remoteConfBak)) {
                    try {
                        remoteTraversal = JsonUtils.ParseJSON(FsStuff.ReadEncryptedFile(remoteConfBak, remoteConfExtra), DirectoryTraversal.class);
                    } catch (Exception e2) {
                        System.err.println("> REMOTE ERROR 2: " + e2.getMessage());
                        e2.printStackTrace();
                    }
                }
            }
        }
        System.out.println(" > Remote Traversal: " +  remoteTraversal.Entries.size() + " Entries");
        if (MainConfig.glob.logs)
            System.out.println(remoteTraversal);
        System.out.println();

        if (fullBackup) {
            System.out.println(" > Wiping backup remains for new clean backup");
            remoteTraversal.Entries.forEach((val) -> {
                try {
                    if (MainConfig.glob.logs)
                        System.out.println("  > Deleting File/Folder: " + val);
                    String resPath = FsStuff.JoinPath(MainConfig.glob.outputPath, "data",
                            DirectoryTraversal.GetParentPathHashFolder(val.parentPath),
                            val.GetHashPath());

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
        System.out.println(" > Local Traversal: " +  traversal.Entries.size() + " Entries");
        if (MainConfig.glob.logs)
            System.out.println(traversal);
        HashMap<String, DirectoryTraversal.TraversalEntry> localSet = traversal.ConvertToHashMap();

        Set<String> addedFiles = new HashSet<>();

        // Write new Stuff
        DirectoryTraversal tempTraversal = new DirectoryTraversal();
        System.out.println(" > Writing new Files / Folders");
        List<DirectoryTraversal.TraversalEntry> toWrite = new ArrayList<>();
        localSet.forEach((key, val) -> {
                    if (!remoteSet.containsKey(key))
                        toWrite.add(val);
                    else
                        tempTraversal.Entries.add(val);
                });

        AtomicInteger lastPercent = new AtomicInteger();
        Long startTime = System.currentTimeMillis();
        AtomicLong totalSize = new AtomicLong();
        toWrite.stream()
                .parallel()
                .forEach((val) -> {
            try {
                if (MainConfig.glob.logs)
                    System.out.println("  > New File/Folder: " + val);
                String inPath = FsStuff.JoinPath(val.parentPath, val.path);
                String resPath = FsStuff.JoinPath(MainConfig.glob.outputPath, "data",
                        DirectoryTraversal.GetParentPathHashFolder(val.parentPath),
                        val.GetHashPath());
                // System.out.println("   > In Path:  " + inPath);
                // System.out.println("   > Res Path: " + resPath);

                if (val.isFile) {
                    GoofyStream stream = FsStuff.CreateGoofyStream(inPath, resPath + ".bin");
                    stream.connectOs(CompressionStuff::CompressStream);
                    stream.connectOs(AesStuff.EncryptStreamRandomIV(val.EntryToHash()));
                    stream.complete();
                } else {
                    FsStuff.CreateFolderIfNotExist(resPath);
                }

                synchronized (addedFiles) {
                    long fSize = FsStuff.GetFileSize(inPath);
                    long tempSize = totalSize.addAndGet(fSize);
                    double tempSizeGb = Math.round((tempSize) / (1024 * 1024 * 1024.0 / 100.0)) / 100.0;
                    // System.out.println("> FSize: " + fSize + ", Temp: " + tempSize + ", Temp GB: " + tempSizeGb);
                    tempTraversal.Entries.add(val);
                    addedFiles.add(FsStuff.JoinPath(val.parentPath, val.path));
                    int percent = (addedFiles.size() * 100) / toWrite.size();
                    if (percent != lastPercent.get()) {
                        lastPercent.set(percent);

                        if (percent % 5 == 0) {
                            System.out.println("   > Doing Temp Traversal Backup");
                            // Backup old list
                            if (FsStuff.DoesFileExist(remoteConf))
                                Files.copy(Paths.get(remoteConf), Paths.get(remoteConfBak), StandardCopyOption.REPLACE_EXISTING);
                            // Save new partial list
                            FsStuff.WriteEncryptedFile(remoteConf, JsonUtils.CreateJSON(tempTraversal, false), remoteConfExtra);
                            System.out.println("   > Done");
                        }

                        Long cTime = System.currentTimeMillis();
                        double elapsed = ((cTime - startTime) / 1000.0);
                        double eta = elapsed / (((double)addedFiles.size() / toWrite.size()));
                        double remaining = eta - elapsed;
                        System.out.println("  > Main Backup progress: " + percent + "%, Elapsed: " + formatTime(elapsed) + ", Remaining: " + formatTime(remaining) + ", Processed Data Size: " + tempSizeGb + " GB");
                    }
                }
            } catch (Exception e) {
                System.err.println("> ERROR: Failed to write file!");
                System.err.println(val);
                System.err.println(e.getMessage());
                e.printStackTrace();
            }
        });
        System.out.println();

        // Maybe delete old Stuff
        if (MainConfig.glob.syncDeletions) {
            System.out.println(" > Syncing deletions");
            remoteSet.forEach((key, val) -> {
                if (!localSet.containsKey(key) && !addedFiles.contains(FsStuff.JoinPath(val.parentPath, val.path))) {
                    try {
                        if (MainConfig.glob.logs)
                            System.out.println("  > Old File/Folder: " + val);
                        String resPath = FsStuff.JoinPath(MainConfig.glob.outputPath, "data",
                                DirectoryTraversal.GetParentPathHashFolder(val.parentPath),
                                val.GetHashPath());

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

    public static String formatTime(double seconds) {
        int s = (int)seconds % 60;
        int m = (int)(seconds / 60) % 60;
        int h = (int)(seconds / (60 * 60));

        StringBuilder str = new StringBuilder();

        if (h > 0)
            str.append(h + "h ");
        if (m > 0)
            str.append(m + "m ");
        str.append(s + "s");

        return str.toString();
    }

    public static void AutoBackup() {
        System.out.println("> Starting Auto Backup");
        LocalTime timeNow = LocalTime.now(); // LocalTime.of(10, 0, 0);
        LocalTime timeWanted = LocalTime.of(MainConfig.glob.autoBackupStartHour, MainConfig.glob.autoBackupStartMinute, 0); // LocalTime.of(1, 18, 0);
        System.out.println(" > Current Time: " + timeNow);
        System.out.println(" > Starting at: " + timeWanted);

        int diffM = (timeWanted.toSecondOfDay() - timeNow.toSecondOfDay()) / 60;
        if (diffM < 0)
            diffM += 60 * 24;
        System.out.println(" > Starting Diff: " + (diffM / 60) + "h " + (diffM % 60) + "m");
        System.out.println();

        TimerTask task = new TimerTask() {
            @Override
            public void run() {
                try {
                    System.out.println("> Starting Auto Backup at " + LocalTime.now() + ", Date: " + LocalDate.now() + "\n");

                    DoBackup(false);

                    System.out.println("> Auto Backup Complete!");
                } catch (Exception e) {
                    System.err.println("> TIMER TASK ERROR: " + e.getMessage());
                    e.printStackTrace();
                }
            }
        };

        final Timer timer = new Timer();
        timer.scheduleAtFixedRate(task, (long)diffM * 60 * 1000, 24 * 60 * 60 * 1000);
        while (true) {
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
    }
}
