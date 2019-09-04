package engine.repo;

import engine.fileMangers.MagitFileUtils;
import engine.magitObjects.*;
import org.apache.commons.collections4.CollectionUtils;

import java.io.File;
import java.io.IOException;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.SQLOutput;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;

public class Repository {

    public final static String DATE_FORMAT = "dd.MM.yyyy-HH:mm:ss:SSS";

    private final RepoSettings basicSettings;
    private final Map<String, Branch> branches; //index is name
    private final Map<String, MagitObject> objects; //index is sha1
    private final Map<String, Commit> commits; //index is sha1

    private String activeUser = "Administrator";
    private RepoFileUtils fileUtils;

    private Path repoPath;
    private Path magitPath;
    private Path objectsPath;
    private Path branchesPath;

    private Map<String, MagitObject> currentCommitObjects; //index is sha1
    private Map<String, MagitObject> wcObjects; //index is sha1
    private Map<Path, String> currentCommitFilesPaths; //<File Path, File Sha1>, current commit files path-sha1 table
    private Map<Path, String> wcFilesPaths; //<File Path, File Sha1>, wc files path-sha1 table
    private WC_PendingChangesData wcPendingChanges;


    public Repository(String name, String path) {
        this.basicSettings = new RepoSettings(name, path);
        this.branches = new HashMap<>();
        this.objects = new HashMap<>();
        this.commits = new LinkedHashMap<>();
        this.fileUtils = new RepoFileUtils(path);
        this.initializePaths(); //todo get rid of it
    }

    public static void checkNewRepoPath(String requestedParentPath, String newRepoName) throws InvalidPathException, FileAlreadyExistsException {
        MagitFileUtils.CreateNewRepoOnDisk_PathValidation(requestedParentPath, newRepoName);
    }

    /**
     * Use only after validation of the requested path by the class method checkNewRepoPath!!!
     * Creates new repo (and folders on disk), and then adds an empty master branch to it (+write on disk)
     *
     * @param newRepoName
     * @param requestedPath
     * @return
     * @throws IOException
     */
    public static Repository newRepoFromScratchCreator(String newRepoName, String requestedPath) throws IOException {

        Repository newRepo = new Repository(newRepoName, requestedPath);
        MagitFileUtils.createNewRepoOnDisk(newRepo.getBasicSettings());

        Branch master = Branch.createMasterBranch();
        newRepo.branches.put("master", master);
        MagitFileUtils.writeBranchToDisk(master, newRepo.branchesPath);
        MagitFileUtils.updateHeadFileOnDisk(newRepo.branchesPath, "master");
        return newRepo;
    }

    private void initializePaths() {
        this.repoPath = Paths.get(this.getStringPath());
        this.magitPath = repoPath.resolve(".magit");
        this.objectsPath = magitPath.resolve("objects");
        this.branchesPath = magitPath.resolve("branches");
    }

    public void addObject(MagitObject object) {
        this.objects.put(object.calcSha1(), object);
    }

    public void addCommit(Commit commit) {
        this.commits.put(commit.calcSha1(), commit);
    }

    public String getName() {
        return basicSettings.getName();
    }

    public String getStringPath() {
        return basicSettings.getPath();
    }

    public RepoSettings getBasicSettings() {
        return basicSettings;
    }

    public void traverseWC(String newCommitDescription) {
        MagitFileUtils.getFirstCommitFromWC(this, newCommitDescription);
    }

    public Map<String, MagitObject> getObjectsAsMap() {
        return objects;
    }

    public Collection<MagitObject> getObjectsTxtAsCollection() {
        return objects.values();
    }

    public Set<Map.Entry<String, MagitObject>> getObjectsAsEntrySet() {
        return objects.entrySet();
    }

    public MagitObject getObject(String sha1) {
        return objects.get(sha1);
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

    public String getActiveUser() {
        return activeUser;
    }

    public void setActiveUser(String newActiveUser) {
        this.activeUser = newActiveUser;
    }

    public Branch getActiveBranch() {
        return branches.get(basicSettings.getHeadBranch());
    }

    public void createMasterBranch_TESTINT_ONLY() throws IOException {
        Branch master = Branch.createMasterBranch();
        this.branches.put("master", master);
        MagitFileUtils.writeBranchToDisk(master, this.branchesPath);
        MagitFileUtils.updateHeadFileOnDisk(this.branchesPath, "master");
    }

    public Commit getCurrentCommit() {
        Branch activeBranch = getActiveBranch();
        Commit currentCommit = commits.get(activeBranch.getPointedCommit());
        return currentCommit;
    }

    public void TEST_updateWcDatabases() {
        fileUtils.createWcDatabases();
    }


    public void createCurrentCommitDatabases() {

        currentCommitObjects = new HashMap<>();
        currentCommitFilesPaths = new HashMap<>();

        String repoRootSha1 = getCurrentCommit().getRootFolderSha1();
        MagitFolder repoRoot = (MagitFolder) objects.get(repoRootSha1);

        String repoRootPath = fileUtils.getRepoPath().toString();
        createCurrentCommitDatabases_Rec(repoRoot, repoRootPath);

        this.currentCommitObjects.put(repoRoot.calcSha1(), repoRoot); //put the root folder
        System.out.println(currentCommitFilesPaths); //test
    }

    private void createCurrentCommitDatabases_Rec(MagitObject object, String objectPath) {

        if (object instanceof MagitFolder) {
            Collection<MagitObjMetadata> magitFolderContents = ((MagitFolder) object).getObjectsValues();
            for (MagitObjMetadata currentObjectData : magitFolderContents) {
                String currentObjectPath = objectPath.concat("\\" + currentObjectData.getName());
                MagitObject currentObject = this.getObject(currentObjectData.getSha1()); //get object from the repo objects map
                currentObject.setParentFolder((MagitFolder) object); //object is the parent MagitFolder
                createCurrentCommitDatabases_Rec(currentObject, currentObjectPath);
            }
        } else // If (object instanceof Blob), add the file path to current commit files path-sha1 table
            currentCommitFilesPaths.put(Paths.get(objectPath), object.calcSha1());


        this.currentCommitObjects.put(object.calcSha1(), object); //add object to currentCommit table
    }


    public void test123() {
        System.out.println(objects);
        System.out.println(currentCommitObjects);
        System.out.println(currentCommitFilesPaths);
        System.out.println(wcObjects);
        System.out.println(wcPendingChanges);
    }

    //    @Deprecated
//    public void updateCurrentCommitDatabases() {
//        createCurrentCommitDatabases();
//        updateCurrentCommitFilesPaths();
//    }

//    @Deprecated
//    private void updateCurrentCommitFilesPaths() {
//
////        currentCommitFilesPaths = currentCommitObjects.values().stream()
////                .filter(object -> object instanceof Blob)
////                .collect(Collectors.toMap((MagitObject::getPath), MagitObject::calcSha1));
//
////        System.out.println(currentCommitFilesPaths); //test!!!
//
////        currentCommitPaths = currentCommitObjects.values().stream()
////                .filter(object -> object instanceof Blob)
////                .map(object -> object.getPath())
////                .collect(Collectors.toList());
//
//    }

    //
//    public Commit getFirstCommitFromWC(String newCommitDescription) {
//
//        fileUtils.updateNewCommitTime();
//        File repoRootDir = new File(this.getStringPath());
//        MagitFolder repoRoot = new MagitFolder();
//        getFirstCommitFromWC_Rec(repoRootDir, repoRoot);
//        addObject(repoRoot);
//        Commit firstCommit = new Commit(repoRoot.calcSha1(), null, newCommitDescription,
//                activeUser, fileUtils.getNewCommitTime());
//
//        addCommit(firstCommit);
//
//        //TEMPORARY!!!!
//        try {
//            createMasterBranch_TESTINT_ONLY();
//        } catch (IOException e) {
//            System.out.println("fuck");
//            e.printStackTrace();
//        }
//        ///////
//
//        getActiveBranch().setPointedCommit(firstCommit.calcSha1());
//        System.out.println(firstCommit);
//        return firstCommit;
//    }
//
//    private void getFirstCommitFromWC_Rec(File currentObject, MagitFolder parent) {
//        try {
//            File[] files = currentObject.listFiles();
//            for (File file : files) {
//
//                //todo catch I/O ERROR OUTSIDE THE METHOD
//
//                if (file.isDirectory() && file.getName().equals(".magit"))  // TODO CREATE ANOTHER METHOD WITHOUT THIS?
//                    continue;
//
//                if (file.isDirectory()) {
//                    System.out.println("directory:" + file.getCanonicalPath());
//                    MagitFolder currentFolder = new MagitFolder();
//                    getFirstCommitFromWC_Rec(file, currentFolder);
//                    this.addObject(currentFolder);
//                    MagitObjMetadata folderData = new MagitObjMetadata(file, currentFolder.calcSha1(), activeUser, fileUtils.getNewCommitTime());
//                    parent.addObjectData(folderData);
//                } else {   //is file
//                    System.out.println("file:" + file.getCanonicalPath());
//                    Blob fileContent = new Blob(file);
//                    this.addObject(fileContent);
//                    MagitObjMetadata fileData = new MagitObjMetadata(file, fileContent.calcSha1(), activeUser, fileUtils.getNewCommitTime());
//                    parent.addObjectData(fileData);
//                }
//            }
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//    }


    private class RepoFileUtils {

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
            SimpleDateFormat sdf = new SimpleDateFormat(DATE_FORMAT);
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


        public void createWcDatabases() {

            updateNewCommitTime();
            wcObjects = new HashMap<>();
            wcFilesPaths = new HashMap<>();

            File repoDir = new File(repoPath.toString());
            MagitFolder repoRootFolder = new MagitFolder();
            createWcDatabases_REC(repoDir, repoRootFolder); //traverse WC and get Objects map and pending changes

            wcObjects.put(repoRootFolder.calcSha1(), repoRootFolder); //put the repoRootFolder to WC objects
            System.out.println(wcFilesPaths); //test
            wcPendingChanges = new WC_PendingChangesData(); //TEST
        }

        private void createWcDatabases_REC(File folderFile, MagitFolder parentFolder) {

            //todo organize and get rid of duplicate code
            File[] filesList = folderFile.listFiles();
            if (filesList == null) //a fallback
                return;

            for (File objectFile : filesList) {

                if (objectFile.isDirectory()) {
                    //todo replace !(objectFile.list().length > 0) with !(filesList.length>0)

                    /*  - Ignore Magit system folder
                        - Ignore empty folders (Doesn't work on folder contains just an empty folder...) */
                    if (objectFile.list().length == 0 || objectFile.getName().equals(".magit"))
                        continue;

                    //System.out.println("directory:" + file.getCanonicalPath());
                    MagitFolder currentFolder = new MagitFolder();
                    currentFolder.setParentFolder(parentFolder);
                    createWcDatabases_REC(objectFile, currentFolder); // Fill the new folder contents (recursively)
                    createWcObjMetadataAndPutInParent(currentFolder, objectFile);
                    wcObjects.put(currentFolder.calcSha1(), currentFolder);
                } else {   //is file
                    //System.out.println("file:" + file.getCanonicalPath());
                    Blob currentBlob = new Blob(objectFile);
                    currentBlob.setParentFolder(parentFolder);
                    createWcObjMetadataAndPutInParent(currentBlob, objectFile);
                    wcObjects.put(currentBlob.calcSha1(), currentBlob);
                    wcFilesPaths.put(Paths.get(objectFile.getPath()), currentBlob.calcSha1());
                }
            }
        }

        private void createWcObjMetadataAndPutInParent(MagitObject object, File objectFile) {

            String objectSha1 = object.calcSha1();
            String lastModifier = Repository.this.getActiveUser();
            String lastModifiedTime = getNewCommitTime();

            // If the object exist "as it" in the current commit, get it's lastModifier and lastModifiedTime
            if (currentCommitObjects.containsKey(objectSha1)) {
                MagitObject originalObject = currentCommitObjects.get(objectSha1);
                MagitFolder originalObjParentFolder = originalObject.getParentFolder();
                //TODO Find metadata by sha1 (it's not a map index... before this fix both objects have to share the same name)
                MagitObjMetadata originalObjMetadata = originalObjParentFolder.getObjMetadataByName(objectFile.getName());
                if (originalObjMetadata != null) {
                    lastModifier = originalObjMetadata.getLastModifier();
                    lastModifiedTime = originalObjMetadata.getLastModifiedTime();
                }
            }

            MagitFolder objParentFolder = object.getParentFolder();
            MagitObjMetadata objMetadata = new MagitObjMetadata(objectFile, objectSha1, lastModifier, lastModifiedTime);
            objParentFolder.addObjectData(objMetadata);
        }

    }

    private class WC_PendingChangesData {

        Collection<Path> newFiles;
        Collection<Path> changedFiles;
        Collection<Path> deletedFiles;

        public WC_PendingChangesData() {
            this.newFiles = CollectionUtils.subtract(wcFilesPaths.keySet(), currentCommitFilesPaths.keySet());
            this.deletedFiles = CollectionUtils.subtract(currentCommitFilesPaths.keySet(), wcFilesPaths.keySet());
            this.changedFiles = createChangedFilesPathList();
            System.out.println("Added: " + newFiles);
            System.out.println("Changed: " + changedFiles);
            System.out.println("Deleted: " + deletedFiles);
        }

        private Collection<Path> createChangedFilesPathList() {
            Collection<Path> commonPaths = CollectionUtils.intersection(currentCommitFilesPaths.keySet(), wcFilesPaths.keySet());
            //find only paths with modified sha1
            Collection<Path> changeList = commonPaths.stream()
                    .filter(k -> !currentCommitFilesPaths.get(k).equals(wcFilesPaths.get(k)))
                    .collect(Collectors.toList());

            return changeList;
        }

        //    public void clearData() {
        //        newFiles.clear();
        //        changedFiles.clear();
        //        deletedFiles.clear();
        //    }

        public void addNewFile(Path newFilePath) {
            newFiles.add(newFilePath);
        }

        public void addChangedFile(Path changedFilePath) {
            changedFiles.add(changedFilePath);
        }

        public void addDeletedFile(Path deletedFilePath) {
            deletedFiles.add(deletedFilePath);
        }

        @Override
        public String toString() {
            return "WC_PendingChangesData{" + "\n" +
                    "newFiles=" + newFiles + "\n" +
                    ", changedFiles=" + changedFiles + "\n" +
                    ", deletedFiles=" + deletedFiles +
                    '}';
        }

        //    @Deprecated
        //    public void addBlobToPendingChanges(Blob blob, Map<String, String> currentCommitFilesPaths) {
        //        String blobPath = blob.getPath();
        //        if (currentCommitFilesPaths.containsKey(blobPath)) // Changed file
        //        {
        //            String oldFileSha1 = currentCommitFilesPaths.get(blobPath);
        //            addChangedFile(Paths.get(blobPath), oldFileSha1);
        //        } else // New file
        //            addNewFile(Paths.get(blobPath));
        //    }


    }
}
