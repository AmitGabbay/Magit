package engine.repo;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public class WC_PendingChangesData {

    List<Path> newFiles;
    List<Path> changedFiles;
    List<Path> deletedFiles;

    public WC_PendingChangesData() {
        this.newFiles = new ArrayList<>();
        this.changedFiles = new ArrayList<>();
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
    public void addChangedFile(Path changedFilePath) {
        changedFiles.add(changedFilePath);
    }
    public void addDeletedFile(Path deletedFilePath){
        deletedFiles.add(deletedFilePath);
    }

}
