package engine.repo;

import engine.fileMangers.MagitFileUtils;
import engine.magitObjects.*;

import java.io.File;
import java.io.IOException;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import java.nio.file.Paths;
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
    private Map<String, String> currentCommitFilesPaths; //<File Path, File Sha1>
    private Map<String, MagitObject> wcObjects; //index is sha1
    private WC_PendingChangesData wcPendingChanges;



    public Repository(String name, String path) {
        this.basicSettings = new RepoSettings(name, path);
        this.branches = new HashMap<>();
        this.objects = new HashMap<>();
        this.commits = new LinkedHashMap<>();
        this.fileUtils = new RepoFileUtils(path);
        this.initializePaths(); //todo get rid of it
        //this.currentCommitObjects = new HashMap<>();
        //this.currentCommitPaths = new ArrayList<>();
        //this.wcObjects = new HashMap<>();
        //this.wcPendingChanges = new WC_PendingChangesData();
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


    public void updateCurrentCommitDatabases() {
        updateCurrentCommitObjects();
        updateCurrentCommitFilesPaths();
    }

    public void TEST_updateWcDatabase()
    {
        fileUtils.createWcObjectsMapAndPendingChanges();
    }

    private void updateCurrentCommitObjects() {

        this.currentCommitObjects = new HashMap<>();
        String repoRootSha1 = getCurrentCommit().getRootFolderSha1();
        MagitFolder repoRoot = (MagitFolder) objects.get(repoRootSha1);

        updateCurrentCommitObjects_Rec(repoRoot);

        this.currentCommitObjects.put(repoRoot.calcSha1(), repoRoot); //put the root folder
    }

    private void updateCurrentCommitObjects_Rec(MagitObject object) {

        if (object instanceof MagitFolder) {
            Collection<MagitObjMetadata> magitFolderContents = ((MagitFolder) object).getObjectsValues();
            for (MagitObjMetadata objectData : magitFolderContents) {
                MagitObject currentObject = this.getObject(objectData.getSha1()); //get object from the repo objects map
                updateCurrentCommitObjects_Rec(currentObject);
            }
        }
        this.currentCommitObjects.put(object.calcSha1(), object);
    }

    private void updateCurrentCommitFilesPaths() {

        currentCommitFilesPaths = currentCommitObjects.values().stream()
                .filter(object -> object instanceof Blob)
                .collect(Collectors.toMap(MagitObject::getPath, MagitObject::calcSha1));

        System.out.println(currentCommitFilesPaths); //test!!!

//        currentCommitPaths = currentCommitObjects.values().stream()
//                .filter(object -> object instanceof Blob)
//                .map(object -> object.getPath())
//                .collect(Collectors.toList());

    }


    public void test123(){
        System.out.println(objects);
        System.out.println(currentCommitObjects);
        System.out.println(currentCommitFilesPaths);
        System.out.println(wcObjects);
        System.out.println(wcPendingChanges);
    }

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


        public void createWcObjectsMapAndPendingChanges() {

            updateNewCommitTime();
            wcObjects = new HashMap<>();
            wcPendingChanges = new WC_PendingChangesData();

            File repoDir = new File(repoPath.toString());
            MagitFolder repoRootFolder = new MagitFolder();
            repoRootFolder.setPath("<RepoRoot>");
            createWcObjectsMapAndPendingChanges_REC(repoDir, repoRootFolder); //traverse WC and get Objects map and pending changes

            String repoRootSha1 = repoRootFolder.calcSha1();
            //if nothing changed at all, use the existing repoRootFolder from the current commit
            if (currentCommitObjects.containsKey(repoRootSha1))
                repoRootFolder = (MagitFolder) currentCommitObjects.get(repoRootSha1);
                // If there are changes, add appropriate metadata
            else
                repoRootFolder.setHelperFields("<RepoRoot>", getActiveUser(), getNewCommitTime());

            wcObjects.put(repoRootFolder.calcSha1(), repoRootFolder); //put the repoRootFolder to WC objects
        }

        private void createWcObjectsMapAndPendingChanges_REC(File currentFolderFile, MagitFolder parentFolder) {

            File[] filesList = currentFolderFile.listFiles();
            if (filesList == null) //a fallback
                return;

            for (File objectFile : filesList) {

                if (objectFile.isDirectory()) {
                    //todo check that works (also on empty folder - verify an array with 0 entries returned
                    //todo replace objectFile.list().length with filesList.length
                    if (!(objectFile.list().length > 0))  //ignore empty folders (doesn't work on folder contains just an empty folder...)
                        return;
                    if (objectFile.getName().equals(".magit")) //ignore Magit system folder (Continue to next file/folder)
                        continue;

                    //System.out.println("directory:" + file.getCanonicalPath());
                    MagitFolder currentFolder = new MagitFolder();
                    createWcObjectsMapAndPendingChanges_REC(objectFile, currentFolder); // Fill the new folder contents (recursively)
                    finalizeWcObjectCreation(currentFolder, objectFile, parentFolder);
                } else {   //is file
                    //System.out.println("file:" + file.getCanonicalPath());
                    Blob currentBlob = new Blob(objectFile);
                    finalizeWcObjectCreation(currentBlob, objectFile, parentFolder);
                }
            }
        }

        private void finalizeWcObjectCreation(MagitObject object, File objectFile, MagitFolder parentFolder) {

            String objectSha1 = object.calcSha1();

            // If the object exist "as it" in the current commit, use the existing one.
            if (currentCommitObjects.containsKey(objectSha1))
                object = currentCommitObjects.get(objectSha1);

                // If the object is new/modified, add appropriate metadata
            else {
                String objectPath = parentFolder.getPath() + "/" + objectFile.getName();
                object.setHelperFields(objectPath, getActiveUser(), getNewCommitTime());
                // for new/modified files - add their path to the relevant WC Changes list
                if (object instanceof Blob)
                    wcPendingChanges.addBlobToPendingChanges((Blob) object, currentCommitFilesPaths);
            }

            // add the object to the wcObject Map and it's metadata to the parentFolder
            wcObjects.put(object.calcSha1(), object);
            MagitObjMetadata objectData = new MagitObjMetadata(objectFile, object);
            parentFolder.addObjectData(objectData);
        }

//        /**
//         * Check if the given MagitObject already exists in the current commit (same sha1). If it is, point it the
//         * existing one. If not, add the object updated metadata.
//         * @param object to check if already exists (and to return)
//         * @param objectFile
//         * @param parentFolderPath
//         * @return The final object, which is the existing one on in the current commit or a new one with updated helper fields
//         */
//        private MagitObject wcObjectDataChecker(MagitObject object, File objectFile, String parentFolderPath){
//            String currentObjectSha1 = object.calcSha1();
//            if (currentCommitObjects.containsKey(currentObjectSha1))
//                object = currentCommitObjects.get(currentObjectSha1);
//            else
//                object.setHelperFields(parentFolderPath+"/"+objectFile.getName(), getActiveUser(), getNewCommitTime());
//            return object;

//        }

    }
}
