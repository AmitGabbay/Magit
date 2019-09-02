package engine.repo;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class WC_PendingChangesData {

    List<Path> newFiles;
    Map<Path, String> changedFiles;
    List<Path> deletedFiles;

    public WC_PendingChangesData() {
        this.newFiles = new ArrayList<>();
        this.changedFiles = new HashMap<>();
        this.deletedFiles = new ArrayList<>();
    }

    public void clearData()
    {
        newFiles.clear();
        changedFiles.clear();
        deletedFiles.clear();
    }

    public void addNewFile(Path newFilePath){
        newFiles.add(newFilePath);
    }
    public void addChangedFile(Path changedFilePath, String sha1) {
        changedFiles.put(changedFilePath, sha1);
    }
    public void addDeletedFile(Path deletedFilePath){
        deletedFiles.add(deletedFilePath);
    }

}
