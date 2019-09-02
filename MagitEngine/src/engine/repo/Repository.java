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

    private Path repoPath;
    private Path magitPath;
    private Path objectsPath;
    private Path branchesPath;
    private String activeUser = "Administrator";

    private Map<String, MagitObject> currentCommitObjects; //index is sha1
    private List<String> currentCommitPaths;

    private RepoFileUtils fileUtils;
    private WC_PendingChangesData wcPendingChanges;


    public Repository(String name, String path) {
        this.basicSettings = new RepoSettings(name, path);
        this.branches = new HashMap<>();
        this.objects = new HashMap<>();
        this.commits = new LinkedHashMap<>();
        this.currentCommitObjects = new HashMap<>();
        this.currentCommitPaths = new ArrayList<>();
        this.initializePaths();
        this.fileUtils = new RepoFileUtils(path);
        this.wcPendingChanges = new WC_PendingChangesData();
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


    public void updateCurrentCommitDatabases(){
        updateCurrentCommitObjects();
        updateCurrentCommitObjectsPaths();
    }

    private void updateCurrentCommitObjects() {

        this.currentCommitObjects.clear();
        String repoRootSha1 = getCurrentCommit().getRootFolderSha1();
        MagitFolder repoRoot = (MagitFolder) objects.get(repoRootSha1);

        updateCurrentCommitObjects_Rec(repoRoot);

        this.currentCommitObjects.put(repoRoot.calcSha1(), repoRoot); //put the root folder
    }

    private void updateCurrentCommitObjects_Rec(MagitObject object) {

        if (object instanceof MagitFolder) {
            for (MagitObjMetadata objectData : ((MagitFolder) object).getObjectsValues()) {
                MagitObject currentObject = this.getObject(objectData.getSha1());
                updateCurrentCommitObjects_Rec(currentObject);
            }
        }
        this.currentCommitObjects.put(object.calcSha1(), object);
    }

    private void updateCurrentCommitObjectsPaths(){

        currentCommitPaths = currentCommitObjects.values().stream()
                .filter(object -> object instanceof Blob)
                .map(object -> object.getPath())
                .collect(Collectors.toList());

        System.out.println(currentCommitPaths); //test!!!
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

        public Map<String, MagitObject> createWC_ObjectsMap() {
            updateNewCommitTime();
            Map<String, MagitObject> WC_Objects = new HashMap<>();

            File repoDir = new File(repoPath.toString());
            MagitFolder repoRoot = new MagitFolder();

            //getFirstCommitFromWC_Rec(repoDir, repo, repoRoot, currentTime);

            String repoRootSha1 = repoRoot.calcSha1();
            if (currentCommitObjects.containsKey(repoRootSha1)) //nothing changed at all
                repoRoot = (MagitFolder) currentCommitObjects.get(repoRootSha1); //assign the repoRoot from the current commit
            else
                repoRoot.setHelperFields("<RepoRoot>", getActiveUser(), getNewCommitTime()); //put updated values for this repoRoot

            WC_Objects.put(repoRoot.calcSha1(), repoRoot); //put the repoRoot folder in WC objects

            return WC_Objects;
        }

        public void createWC_ObjectsMap_Rec(Map<String, MagitObject> wcObjects, File currentObject, MagitFolder parentFolder) {

            try {
                File[] files = currentObject.listFiles();
                if (files == null)
                    return;

                for (File file : files) {
                    //todo catch I/O ERROR OUTSIDE THE METHOD
                    if (file.isDirectory() && file.getName().equals(".magit"))  // TODO CREATE ANOTHER METHOD WITHOUT THIS?
                        continue;

                    if (file.isDirectory()) {

                        if (!(file.list().length > 0))  //ignore empty folders (doesn't work on folder contains just an empty folder...)
                            return;
                        //System.out.println("directory:" + file.getCanonicalPath());

                        // Create the new folder and fill its contents (recursively)
                        MagitFolder currentFolder = new MagitFolder();
                        createWC_ObjectsMap_Rec(wcObjects, file, currentFolder);

                        /* If the folder exist "as it" in the current commit, use that object.
                           Else, add the folder metadata of a new object */
                        String currentFolderSha1 = currentFolder.calcSha1();
                        if (currentCommitObjects.containsKey(currentFolderSha1))
                            currentFolder = (MagitFolder) currentCommitObjects.get(currentFolderSha1);
                        else
                            currentFolder.setHelperFields(parentFolder.getPath()+"/"+file.getName(), getActiveUser(), getNewCommitTime());

                        // Add the folder to the WC objects Map and add it's metadata to it's parent folder
                        wcObjects.put(currentFolder.calcSha1(), currentFolder);
                        MagitObjMetadata folderData = new MagitObjMetadata(file, currentFolder); //todo change to this c'tor in the create first commit
                        parentFolder.addObjectData(folderData);
                    }

                    else {   //is file
                        System.out.println("file:" + file.getCanonicalPath());
                        Blob fileContent = new Blob(file);
                     //   fileContent.setHelperFields(parentFolder.getPath()+"/"+file.getName(), repo.getActiveUser(), newCommitTime);
                     //   repo.addObject(fileContent);
                    //    MagitObjMetadata fileData = new MagitObjMetadata(file, fileContent.calcSha1(), repo.getActiveUser(), newCommitTime);
                     //   parentFolder.addObjectData(fileData);
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }

        }

    }
}
