package engine.fileMangers;

import engine.magitObjects.MagitObjMetadata;
import engine.magitObjects.Blob;
import engine.magitObjects.Commit;
import engine.magitObjects.MagitFolder;
import engine.magitObjects.MagitObject;
import engine.repo.Branch;
import engine.repo.RepoSettings;
import engine.repo.Repository;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.text.SimpleDateFormat;

public class MagitFileUtils {


    /**
     * Checks path validity (parent path exist and there is no folder with new repo name in it).
     * Exceptions will be thrown and propagated to the calling method if any problems found
     *
     * @param requestedParentPath
     * @param newRepoName
     * @throws InvalidPathException
     * @throws FileAlreadyExistsException
     */
    public static void CreateNewRepoOnDisk_PathValidation(String requestedParentPath, String newRepoName) throws InvalidPathException, FileAlreadyExistsException {

        Path parentPath = Paths.get(requestedParentPath);
        Path newRepoPath = parentPath.resolve(newRepoName);

        //check parent path validity (must exist in the file system!)
        if (Files.notExists(parentPath))
            throw new InvalidPathException(parentPath.toString(), "Path doesn't exist");

            //verifies there is no folder with the new repo name
        else if (Files.exists(newRepoPath)) {
            throw new FileAlreadyExistsException(newRepoPath.getFileName().toString(), null,
                    "Folder with this name already exist in the path " + parentPath.toString());
        } else
            System.out.println("good"); //test
    }

    /**
     * Should Be used only after path validation using the CreateNewRepoOnDisk_PathValidation() method!
     *
     * @param newRepo
     * @throws IOException
     */
    public static void createNewRepoOnDisk(RepoSettings newRepo) throws IOException {

        Path newRepoPath = Paths.get(newRepo.getPath());
        //Path parentPath = newRepoPath.getParent();
        Path magitPath = newRepoPath.resolve(".magit");

        //create the new repository directory and the internal magit folders structure
        Files.createDirectory(newRepoPath);
        Files.createDirectory(magitPath);
        System.out.println(magitPath.toString()); //test
        Files.createDirectory(magitPath.resolve("objects"));
        Files.createDirectory(magitPath.resolve("branches"));

        //write the repo setting file the filesystem (at /<repoName>/.magit)
        Path repoSettingsFile = magitPath.resolve("RepoSettings");
        try (ObjectOutputStream out =
                     new ObjectOutputStream(
                             new FileOutputStream(repoSettingsFile.toString()))) {
            out.writeObject(newRepo);
            out.flush();
        }
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
        repoRoot.setHelperFields("<RepoRoot>", repo.getActiveUser(), currentTime);

        getFirstCommitFromWC_Rec(repoDir, repo, repoRoot, currentTime);
        repo.addObject(repoRoot);
        Commit firstCommit = new Commit(repoRoot.calcSha1(), null, newCommitDescription,
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
    private static void getFirstCommitFromWC_Rec(File currentObject, Repository repo, MagitFolder parent, String newCommitTime) {
        try {
            File[] files = currentObject.listFiles();
            if (files == null)
                return;

            for (File file : files) {

                //todo catch I/O ERROR OUTSIDE THE METHOD

                if (file.isDirectory() && file.getName().equals(".magit"))  // TODO CREATE ANOTHER METHOD WITHOUT THIS?
                    continue;

                if (file.isDirectory()) {
                    //ignore empty folders (doesn't work on folder contains an empty folder...)
                    if (!(file.list().length > 0))
                        return;

                    System.out.println("directory:" + file.getCanonicalPath());
                    MagitFolder currentFolder = new MagitFolder();
                    currentFolder.setHelperFields(parent.getPath()+"/"+file.getName(), repo.getActiveUser(), newCommitTime);
                    getFirstCommitFromWC_Rec(file, repo, currentFolder, newCommitTime);
                    repo.addObject(currentFolder);
                    MagitObjMetadata folderData = new MagitObjMetadata(file, currentFolder.calcSha1(), repo.getActiveUser(), newCommitTime);
                    parent.addObjectData(folderData);
                } else {   //is file
                    System.out.println("file:" + file.getCanonicalPath());
                    Blob fileContent = new Blob(file);
                    fileContent.setHelperFields(parent.getPath()+"/"+file.getName(), repo.getActiveUser(), newCommitTime);
                    repo.addObject(fileContent);
                    MagitObjMetadata fileData = new MagitObjMetadata(file, fileContent.calcSha1(), repo.getActiveUser(), newCommitTime);
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
            for (MagitObjMetadata objectData : ((MagitFolder) object).getObjectsValues()) {
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


    /**
     * test only!!!
     *
     * @param branchToWrite
     * @param BranchesFolder
     * @throws IOException
     */
    public static void writeBranchToDisk2(Branch branchToWrite, Path BranchesFolder) throws IOException {

        final String fileToWritePath = BranchesFolder.toString() + "/" + branchToWrite.getName();
        try (Writer out = new BufferedWriter(
                new OutputStreamWriter(
                        new FileOutputStream(fileToWritePath), StandardCharsets.UTF_8))) {

            out.write("ama");
        }
    }
}