package com.marcel.utils;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.marcel.utils.CryptoStuff.HashStuff;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.BasicFileAttributeView;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.*;
import java.util.regex.Matcher;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class DirectoryTraversal {
    public static class TraversalEntry {
        @JsonProperty("pp")
        public String parentPath;
        @JsonProperty("p")
        public String path;
        @JsonProperty("n")
        public long newestTime;
        @JsonProperty("f")
        public boolean isFile;

        public TraversalEntry() {

        }

        public TraversalEntry(String parentPath, String path, long newestTime, boolean isFile) {
            this.parentPath = parentPath;
            this.path = path;
            this.newestTime = newestTime;
            this.isFile = isFile;
        }

        @Override
        public String toString() {
            return "TraversalEntry{" +
                    "parentPath='" + parentPath + '\'' +
                    ", path='" + path + '\'' +
                    ", newestTime=" + newestTime +
                    ", isFile=" + isFile +
                    ", Hash='" + EntryToHash() + "'" +
                    ", HashPath='" + FsStuff.JoinPath(GetParentPathHashFolder(parentPath), GetHashPath()) + "'" +
                    '}';
        }

        public String EntryToHash() {
            return HashStuff.HashInputToStr(parentPath + "_:@/STR/@:_" + path + "_:@/STR/@:_" + isFile + " LOL " + newestTime).substring(0, 32);
        }

        public String GetHashPath() {
            return String.join(PathDel(), Arrays.stream(path.split(PathDelRegex())).map((str) -> HashStuff.HashInputToStr("_:@/STR/@:_" + str + "_:@/STR/@:_").substring(0, 16)).collect(Collectors.toList()));
        }
    }

    public static String PathDel() {
        return System.getProperty("file.separator");
    }

    public static String PathDelRegex() {
        return Matcher.quoteReplacement(System.getProperty("file.separator"));
    }

    public static String GetParentPathHashFolder(String parentPath) {
        return Paths.get(parentPath).getFileName().toString() + " _" + HashStuff.HashInputToStr(parentPath + "_:@/STR/@:_" + parentPath + "_:@/STR/@:_").substring(0, 16) + "_";
    }

    public static long getNewestTime(Path path) {
        BasicFileAttributes view;
        try {
            view = Files.getFileAttributeView(path, BasicFileAttributeView.class).readAttributes();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return Arrays.stream(new long[]{
                view.creationTime().toMillis(),
                // view.lastAccessTime().toMillis(),
                view.lastModifiedTime().toMillis(),
        }).max().getAsLong();
    }

    public List<TraversalEntry> Entries = new ArrayList<>();
    public DirectoryTraversal() {

    }

    public DirectoryTraversal(List<String> parentPaths) {
        Entries = new ArrayList<>();
        System.out.println("  > Creating Root Directory Traversal for: " + String.join(", ", parentPaths));
        for (String parentPathStr : parentPaths) {
            Path parentPath = Paths.get(parentPathStr);
            System.out.println("   > Creating Root Directory Traversal for: " + parentPath);
            try (Stream<Path> paths = Files.walk(parentPath)) {
                paths.forEach((path) -> {
                    Path offset = parentPath.relativize(path);
                    // System.out.println("  > Path \"" + path + "\" = \"" + parentPath + "\" / \"" + offset + "\"");
                    if (Files.isDirectory(path)) {
                        // System.out.println("  > Found Folder: " + path);
                        Entries.add(new TraversalEntry(parentPathStr, offset.toString(), getNewestTime(path), false));
                    } else if (Files.isRegularFile(path)) {
                        //  System.out.println("  > Found File: " + path);
                        Entries.add(new TraversalEntry(parentPathStr, offset.toString(), getNewestTime(path), true));
                    } else {
                        System.out.println("    > Found Unknown: " + path);
                        System.err.println("    > Found Unknown: " + path + " in Traversal for: " + parentPathStr);
                    }
                });
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }

        System.out.println("  > Traversal complete!\n");

//        System.out.println("> Entries:");
//        for (TraversalEntry entry : Entries)
//            System.out.println(" > " + entry);
//        System.out.println("> Entry count: " + Entries.size());

        //JsonUtils.CreateJSONFile("./test.json", Entries, false);
    }

    public HashMap<String, TraversalEntry> ConvertToHashMap() {
        HashMap<String, TraversalEntry> res = new HashMap<>();

        for (TraversalEntry entry : Entries)
            res.put(entry.EntryToHash(), entry);

        return res;
    }

    @Override
    public String toString() {
//        System.out.println("> Entries:");
//        for (TraversalEntry entry : Entries)
//            System.out.println(" > " + entry);
//        System.out.println("> Entry count: " + Entries.size());

        return "DirectoryTraversal{" +
                "Entries (" + Entries.size() + ") =" + Entries +
                '}';
    }
}
