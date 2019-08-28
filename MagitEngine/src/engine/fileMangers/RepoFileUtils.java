package engine.fileMangers;

import engine.magitObjects.MagitObjMetadata;
import engine.repo.Branch;
import engine.repo.Repository;
import engine.magitObjects.Blob;
import engine.magitObjects.Commit;
import engine.magitObjects.MagitFolder;
import engine.magitObjects.MagitObject;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.Map;

public class RepoFileUtils {

    private final Path repoPath;
    private final Path magitPath;
    private final Path objectsPath;
    private final Path branchesPath;
    private String newCommitTime;

    public RepoFileUtils(String repoStringPath) {
        this.repoPath = Paths.get(repoStringPath);
        this.magitPath = repoPath.resolve(".magit");
        this.objectsPath = magitPath.resolve("objects");
        this.branchesPath = magitPath.resolve("branches");
    }

    public void updateNewCommitTime() {
        SimpleDateFormat sdf = new SimpleDateFormat(Repository.DATE_FORMAT);
        this.newCommitTime = sdf.format(System.currentTimeMillis());
    }

    public Path getMagitPath() {
        return magitPath;
    }

    public Path getRepoPath() {
        return repoPath;
    }

    public Path getObjectsPath() {
        return objectsPath;
    }

    public Path getBranchesPath() {
        return branchesPath;
    }

    public String getNewCommitTime() {
        return newCommitTime;
    }

    public Map<String, MagitObject> createWC_ObjectsMap(Map<String, MagitObject> currentCommitObjects){
        updateNewCommitTime();
        Map<String, MagitObject> WC_Objects= new HashMap<>();


        File repoDir = new File(repoPath.toString());
        MagitFolder repoRoot = new MagitFolder();

        //getFirstCommitFromWC_Rec(repoDir, repo, repoRoot, currentTime);


        //repoRoot.setHelperFields("<RepoRoot>", repo.getActiveUser(), currentTime); //compare to the repo root in currentCommit

        WC_Objects.put(repoRoot.calcSha1(), repoRoot);

        return WC_Objects;

    }




}