package engine.fileMangers;

import engine.repo.RepoSettings;

import java.io.*;
import java.nio.file.*;

public class MagitFileUtils {


    /**
     * Checks path validity (parent path exist and there is no folder with new repo name in it).
     * Exceptions will be thrown and propagated to the calling method if any problems found
     *
     * @param requestedPath
     * @throws InvalidPathException
     * @throws FileAlreadyExistsException
     */
    public static void newRepoOnDisk_PathValidation(String requestedPath) throws InvalidPathException, FileAlreadyExistsException {

        Path fullPath = Paths.get(requestedPath);
        Path parentPath = fullPath.getParent();

        //check parent path validity (must exist in the file system!)
        if (Files.notExists(parentPath))
            throw new InvalidPathException(parentPath.toString(), "Path doesn't exist");

            //verifies there is no folder with the new repo folder name
        else if (Files.exists(fullPath)) {
            throw new FileAlreadyExistsException(fullPath.getFileName().toString(), null,
                    "Folder with this name already exist in the path " + parentPath);
        } else
            System.out.println("Path validated."); //test
    }

    /**
     * Should Be used only after path validation using the CreateNewRepoOnDisk_PathValidation() method!
     * HEAD and RepoSettings Files won't be created in this method, but in the master branch creation.
     * @param newRepoSettings
     * @throws IOException
     */
    public static void createNewRepoFoldersOnDisk(RepoSettings newRepoSettings) throws IOException {

        Path newRepoPath = Paths.get(newRepoSettings.getStringPath());
        Path magitPath = newRepoPath.resolve(".magit");

        //create the new repository directory and the internal magit folders structure
        Files.createDirectory(newRepoPath);
        Files.createDirectory(magitPath);
        //System.out.println(magitPath.toString()); //test
        Files.createDirectory(magitPath.resolve("objects"));
        Files.createDirectory(magitPath.resolve("branches"));
    }


    public static boolean isExistingRepoPath(String path) {
        Path repoPath = Paths.get(path);
        Path magitPath = repoPath.resolve(".magit");

        return Files.exists(magitPath);
    }


    public static RepoSettings readRepoSettingsFromDisk(Path repoPath) throws IOException, ClassNotFoundException {

        Path settingsPath = repoPath.resolve("RepoSettings");
        RepoSettings settings;

        try (ObjectInputStream in = new ObjectInputStream(
                             new FileInputStream(settingsPath.toString()))) {

            settings = (RepoSettings)in.readObject();
        }

        return settings;
    }


    /*
     ********************* test only section!!!*********************************
    */

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



} //class end