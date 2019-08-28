package engine.fileMangers;

import java.io.File;
import java.io.IOException;
import java.util.List;
import org.apache.commons.io.*;
import org.apache.commons.io.filefilter.TrueFileFilter;




public class FolderTraverser {

        public static void main(String[] args) throws IOException {

            File dir = new File("C:\\Magit");

            System.out.println("Getting all files in " + dir.getCanonicalPath() + " including those in subdirectories");
            List<File> files = (List<File>) FileUtils.listFilesAndDirs(dir, TrueFileFilter.INSTANCE, TrueFileFilter.INSTANCE);
            for (File file : files) {
                System.out.println("file: " + file.getCanonicalPath());
            }

        }

    }

