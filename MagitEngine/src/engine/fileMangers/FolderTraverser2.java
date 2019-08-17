package engine.fileMangers;

import engine.Blob;
import engine.MagitObject;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public class FolderTraverser2 {

    public static List<MagitObject> test = new ArrayList<>();

    public static void main(String[] args) {
//        File currentDir = new File("C:/Magit/test"); // current directory
//        traverseWC(currentDir);
    }

    public static void traverseWC(Path repoPath){
        File repoDir = new File(repoPath.toString());
        traverseWC_Rec(repoDir);
    }

    private static void traverseWC_Rec(File dir) {
        try {
            File[] files = dir.listFiles();
            for (File file : files) {

                //todo catch I/O ERROR OUTSIDE THE METHOD
                // TODO CREATE ANOTHER METHOD WITHOUT THIS?
                if (file.isDirectory() && file.getName().equals(".magit"))
                    continue;

                if (file.isDirectory()) {
                    System.out.println("directory:" + file.getCanonicalPath());
                    traverseWC_Rec(file);
                } else {
                    System.out.println("file:" + file.getCanonicalPath());
                    test.add(new Blob(file));
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
