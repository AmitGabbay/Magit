package engine.repo;

import engine.magitObjects.Blob;

import java.nio.file.Path;
import java.nio.file.Paths;
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

//    public void clearData() {
//        newFiles.clear();
//        changedFiles.clear();
//        deletedFiles.clear();
//    }

    public void addNewFile(Path newFilePath) {
        newFiles.add(newFilePath);
    }

    public void addChangedFile(Path changedFilePath, String oldFileSha1) {
        changedFiles.put(changedFilePath, oldFileSha1);
    }

    public void addDeletedFile(Path deletedFilePath) {
        deletedFiles.add(deletedFilePath);
    }

    public void addBlobToPendingChanges(Blob blob, Map<String, String> currentCommitFilesPaths) {
        String blobPath = blob.getPath();
        if (currentCommitFilesPaths.containsKey(blobPath)) // Changed file
        {
            String oldFileSha1 = currentCommitFilesPaths.get(blobPath);
            addChangedFile(Paths.get(blobPath), oldFileSha1);
        } else // New file
            addNewFile(Paths.get(blobPath));
    }


}
