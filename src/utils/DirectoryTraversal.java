package utils;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.BasicFileAttributeView;
import java.nio.file.attribute.BasicFileAttributes;
import java.nio.file.attribute.FileTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Stream;

public class DirectoryTraversal {
    public class TraversalEntry {
        @JsonProperty("pp")
        public String parentPath;
        @JsonProperty("p")
        public String path;
        @JsonProperty("n")
        public long newestTime;
        @JsonProperty("f")
        public boolean isFile;

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
                    '}';
        }
    }

    public List<TraversalEntry> Entries = new ArrayList<>();

    public static long getNewestTime(Path path) {
        BasicFileAttributes view;
        try {
            view = Files.getFileAttributeView(path, BasicFileAttributeView.class).readAttributes();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return Arrays.stream(new long[] {
                view.creationTime().toMillis(),
                view.lastAccessTime().toMillis(),
                view.lastModifiedTime().toMillis(),
        }).max().getAsLong();
    }

    public DirectoryTraversal(List<String> parentPaths) {
        Entries = new ArrayList<>();
        System.out.println("> Creating Root Directory Traversal for: " + String.join(", ", parentPaths));
        for (String parentPathStr : parentPaths) {
            Path parentPath = Paths.get(parentPathStr);
            System.out.println(" > Creating Root Directory Traversal for: " + parentPath);
            try (Stream<Path> paths = Files.walk(parentPath)) {
                paths.forEach((path) -> {
                    Path offset = parentPath.relativize(path);
                    // System.out.println("  > Path \"" + path + "\" = \"" + parentPath + "\" / \"" + offset + "\"");
                    if (Files.isDirectory(path)) {
                        System.out.println("  > Found Folder: " + path);
                        Entries.add(new TraversalEntry(parentPathStr, offset.toString(), getNewestTime(path), false));
                    } else if (Files.isRegularFile(path)) {
                        System.out.println("  > Found File: " + path);
                        Entries.add(new TraversalEntry(parentPathStr, offset.toString(), getNewestTime(path), true));
                    } else {
                        System.out.println("  > Found Unknown: " + path);
                    }
                });
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }

        System.out.println("> Traversal complete!");
        for (TraversalEntry entry : Entries)
            System.out.println(" > " + entry);
        System.out.println("> Entry count: " + Entries.size());

        JsonUtils.CreateJSONFile("./test.json", Entries, false);
    }

//    public void SubTraversal(String parentPath, String subPath) {
//        Path mainPath = Paths.get(parentPath + "/" + subPath);
//        System.out.println("  > Creating Sub Directory Traversal for: " + mainPath);
//        File folder = new File(mainPath.to);
//
//    }


//    public static TraversalEntry FromPath(String parentPath, String path, boolean isFile) {
//        FileTime time = null;
//        boolean isFile = false;
//        return new TraversalEntry(parentPath, path, time.toMillis(), isFile);
//    }

    @Override
    public String toString() {
        return "DirectoryTraversal{" +
                "Entries=" + Entries +
                '}';
    }
}
