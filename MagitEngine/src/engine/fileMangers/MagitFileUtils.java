package engine.fileMangers;

import engine.magitObjects.*;
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

//    public static void traverseCommit(Repository repo, Commit commit) throws IOException {
//
//        //create "commit tree"
//        MagitFolder repoRoot = (MagitFolder) repo.getRepoObject(commit.getRootFolderSha1());
//        traverseCommit_Rec(repo, repoRoot);
//
//    }
//
//    private static void traverseCommit_Rec(Repository repo, MagitObject object) throws IOException {
//        Path magitObjectsPath = repo.getObjectsPath();
//        Path objectPath = magitObjectsPath.resolve(object.calcSha1());
//        writeObjectToMagit(objectPath, object);
//
//        if (object instanceof MagitFolder) {
//            for (MagitObjMetadata objectData : ((MagitFolder) object).getObjectsValues()) {
//                MagitObject currentObject = repo.getRepoObject(objectData.getSha1());
//                traverseCommit_Rec(repo, currentObject);
//            }
//        }
//    }



    /*
     ********************* test only section!!!*********************************
     */


} //class end