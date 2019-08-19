package engine.fileMangers;

import engine.*;
import engine.magitMemoryObjects.*;

import java.io.*;
import java.nio.file.*;

public class MagitFileUtils {


    /**
     * Checks path validity (parent path exist and there is no folder with new repo name in it).
     * Exceptions will be thrown and propagated to the calling method if any problems found
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
                        new FileOutputStream(fileToWritePath), "UTF-8"))) {

            out.write(branchToWrite.getPointedCommit());
        }
    }


    public static void getFirstCommitFromWC(Repository repo){
        File repoDir = new File(repo.getStringPath());
        // todo create commit and assign it the root folder sha1
        MagitFolder repoRoot = new MagitFolder();
        getFirstCommitFromWC_Rec(repoDir, repo, repoRoot);
        Commit firstCommit = new Commit(repoRoot.calcSha1());
        repo.addObject(repoRoot);
    }

    //todo send instead only the objects map?
    private static void getFirstCommitFromWC_Rec(File currentObject, Repository repo, MagitFolder parent) {
        try {
            File[] files = currentObject.listFiles();
            for (File file : files) {

                //todo catch I/O ERROR OUTSIDE THE METHOD

                if (file.isDirectory() && file.getName().equals(".magit"))  // TODO CREATE ANOTHER METHOD WITHOUT THIS?
                    continue;

                if (file.isDirectory()) {
                    System.out.println("directory:" + file.getCanonicalPath());
                    MagitFolder currentFolder = new MagitFolder();
                    getFirstCommitFromWC_Rec(file, repo, currentFolder);
                    repo.addObject(currentFolder);
                    MagitObjMetadata fileData = new MagitObjMetadata(file.getName(), currentFolder.calcSha1(), MagitObjectType.FOLDER);
                    parent.addObject(fileData);
                } else {
                    System.out.println("file:" + file.getCanonicalPath());
                    Blob fileContent = new Blob(file);
                    repo.addObject(fileContent);
                    MagitObjMetadata fileData = new MagitObjMetadata(file.getName(), fileContent.calcSha1(), MagitObjectType.FILE);
                    parent.addObject(fileData);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    /**
     * test only!!!
     * @param branchToWrite
     * @param BranchesFolder
     * @throws IOException
     */
    public static void writeBranchToDisk2(Branch branchToWrite, Path BranchesFolder) throws IOException {

        final String fileToWritePath = BranchesFolder.toString() + "/" + branchToWrite.getName();
        try (Writer out = new BufferedWriter(
                new OutputStreamWriter(
                        new FileOutputStream(fileToWritePath), "UTF-8"))) {

            out.write("ama");
        }
    }
}