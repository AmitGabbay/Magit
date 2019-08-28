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


    public static void writeBranchToDisk(Branch branchToWrite, Path BranchesFolder) throws IOException {

        final String fileToWritePath = BranchesFolder.toString() + "/" + branchToWrite.getName();
        try (Writer out = new BufferedWriter(
                new OutputStreamWriter(
                        new FileOutputStream(fileToWritePath), StandardCharsets.UTF_8))) {

            out.write(branchToWrite.getPointedCommit());
        }
    }


    public static Commit getFirstCommitFromWC(Repository repo, String newCommitDescription) {

        SimpleDateFormat sdf = new SimpleDateFormat(Repository.DATE_FORMAT);
        String currentTime = sdf.format(System.currentTimeMillis());

        File repoDir = new File(repo.getStringPath());
        MagitFolder repoRoot = new MagitFolder();
        getFirstCommitFromWC_Rec(repoDir, repo, repoRoot, currentTime);
        repo.addObject(repoRoot);
        Commit firstCommit = new Commit(repoRoot.calcSha1(), null, newCommitDescription, //todo ask user for the repo name
                repo.getActiveUser(), currentTime);

        repo.addCommit(firstCommit);

        //TEMPORARY!!!!
        try {
            repo.createMasterBranch_TESTINT_ONLY();
        } catch (IOException e) {
            System.out.println("fuck");
            e.printStackTrace();
        }
        ///////

        repo.getActiveBranch().setPointedCommit(firstCommit.calcSha1());
        System.out.println(firstCommit);
        return firstCommit;
    }

    //todo send instead only the objects map?
    private static void getFirstCommitFromWC_Rec(File currentObject, Repository repo, MagitFolder parent, String currentTime) {
        try {
            File[] files = currentObject.listFiles();
            for (File file : files) {

                //todo catch I/O ERROR OUTSIDE THE METHOD

                if (file.isDirectory() && file.getName().equals(".magit"))  // TODO CREATE ANOTHER METHOD WITHOUT THIS?
                    continue;

                if (file.isDirectory()) {
                    System.out.println("directory:" + file.getCanonicalPath());
                    MagitFolder currentFolder = new MagitFolder();
                    getFirstCommitFromWC_Rec(file, repo, currentFolder, currentTime);
                    repo.addObject(currentFolder);
                    MagitObjMetadata folderData = new MagitObjMetadata(file, currentFolder.calcSha1(), repo.getActiveUser(), currentTime);
                    parent.addObjectData(folderData);
                } else {   //is file
                    System.out.println("file:" + file.getCanonicalPath());
                    Blob fileContent = new Blob(file);
                    repo.addObject(fileContent);
                    MagitObjMetadata fileData = new MagitObjMetadata(file, fileContent.calcSha1(), repo.getActiveUser(), currentTime);
                    parent.addObjectData(fileData);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    public static void writeFirstCommitToMagitFolder(Repository repo, Commit commit) throws IOException {
        Path magitFolderPath = repo.getMagitPath();
        //verify .magit folder exist
        if (Files.notExists(magitFolderPath))
            throw new FileNotFoundException(".magit folder doesn't exist!");

        //create "commit tree"
        MagitFolder repoRoot = (MagitFolder) repo.getObject(commit.getRootFolderSha1());
        traverseCommit_Rec(repo, repoRoot);

        Path newCommitPath = repo.getObjectsPath().resolve(commit.calcSha1());
        writeObjectToMagit(newCommitPath, commit);
        Branch activeBranch = repo.getActiveBranch();
        writeBranchToDisk(activeBranch, repo.getBranchesPath());
    }

    private static void traverseCommit_Rec(Repository repo, MagitObject object) throws IOException {
        Path magitObjectsPath = repo.getObjectsPath();
        Path objectPath = magitObjectsPath.resolve(object.calcSha1());
        writeObjectToMagit(objectPath, object);

        if (object instanceof MagitFolder) {
            for (MagitObjMetadata objectData : ((MagitFolder) object).getObjectsValues() ) {
                MagitObject currentObject = repo.getObject(objectData.getSha1());
                traverseCommit_Rec(repo, currentObject);
            }
        }
    }

    private static void writeObjectToMagit(Path objectPath, MagitObject object) throws IOException {
        try (ObjectOutputStream out =
                     new ObjectOutputStream(
                             new FileOutputStream(objectPath.toString()))) {
            out.writeObject(object);
            out.flush();
        }
    }









}