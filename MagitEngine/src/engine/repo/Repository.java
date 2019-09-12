package engine.repo;

import engine.fileMangers.MagitFileUtils;
import engine.magitObjects.*;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.MapUtils;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

public class Repository {

    public final static String DATE_FORMAT = "dd.MM.yyyy-HH:mm:ss:SSS";

    private final RepoSettings basicSettings;
    private final Map<String, Branch> branches; //index is name
    private final Map<String, MagitObject> repoObjects; //index is sha1
    private final Map<String, Commit> commits; //index is sha1

    private Map<String, MagitObject> currentCommitObjects; //index is sha1
    private Map<String, MagitObject> wcObjects; //index is sha1
    private Map<Path, String> currentCommitFilesPaths; //<File Path, File Sha1>, current commit files path-sha1 table
    private Map<Path, String> wcFilesPaths; //<File Path, File Sha1>, WC files path-sha1 table
    private WC_PendingChangesData wcPendingChanges;
    private boolean pendingChangesWaiting; /* an "OK switch" for doing a new commit.
                                             Set "true" only by checkForWcPendingChanges method */

    private String activeUser = "Administrator";
    private RepoFileUtils fileUtils;


    public Repository(RepoSettings settings) {
        this.basicSettings = settings;
        this.branches = new HashMap<>();
        this.repoObjects = new HashMap<>();
        this.commits = new HashMap<>();
        this.fileUtils = new RepoFileUtils(settings.getStringPath());
        this.pendingChangesWaiting = false;
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
    public static Repository newRepoFromScratchCreator(String newRepoName, String requestedPath) throws Exception {

        RepoSettings settings = new RepoSettings(newRepoName, requestedPath);
        Repository newRepo = new Repository(settings);
        MagitFileUtils.createNewRepoOnDisk(newRepo.getBasicSettings());

        Branch master = Branch.createBlankMasterBranch();
        newRepo.addNewBranchToRepo(master);
        newRepo.setActiveBranch(master);
        return newRepo;
    }

    public static Repository openRepoFromFolder(String stringRepoPath) throws Exception {

        Path repoMagitPath = Paths.get(stringRepoPath).resolve(".magit");
        RepoSettings repoSettings = MagitFileUtils.readRepoSettingsFromDisk(repoMagitPath);
        Repository repo = new Repository(repoSettings);
        repo.fileUtils.loadBranchesFromDisk();
        repo.fileUtils.loadMagitObjectsAndCommitsFromDisk();
        repo.createCurrentCommitDatabases();

        return repo;
    }


    public WC_PendingChangesData getWcPendingChanges() {
        return wcPendingChanges;
    }

    public boolean isPendingChangesWaiting() {
        return pendingChangesWaiting;
    }

    public boolean isNoCommits() {
        return commits.isEmpty();
    }

    public String getName() {
        return basicSettings.getName();
    }

    public String getStringPath() {
        return basicSettings.getStringPath();
    }

    public RepoSettings getBasicSettings() {
        return basicSettings;
    }

    public Map<String, MagitObject> getObjectsAsMap() {
        return repoObjects;
    }

    public Collection<MagitObject> getObjectsTxtAsCollection() {
        return repoObjects.values();
    }

    public Set<Map.Entry<String, MagitObject>> getObjectsAsEntrySet() {
        return repoObjects.entrySet();
    }

    public MagitObject getRepoObject(String sha1) {
        return repoObjects.get(sha1);
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

    /**
     * Note: This is note a full checkout method, just HEAD branch changer!
     *
     * @param branch Branch to set as active (the HEAD branch)
     */
    private void setActiveBranch(Branch branch) throws IOException {
        basicSettings.setHeadBranch(branch.getName());
        fileUtils.updateHeadFileOnDisk(branch);
    }

    public void createMasterBranch_TESTINT_ONLY() throws Exception {
        Branch master = Branch.createBlankMasterBranch();
        addNewBranchToRepo(master);
        setActiveBranch(master);
    }

    public Commit getCurrentCommit() {
        if (isNoCommits())
            return null;

        Branch activeBranch = getActiveBranch();
        return commits.get(activeBranch.getPointedCommit());
    }

    public boolean checkForWcPendingChanges() throws IOException {

        boolean anyChanges = false;

        if (isNoCommits()) {
            if (!fileUtils.isWcEmpty()) {
                fileUtils.createWcDatabases_FirstCommit();
                anyChanges = true;
            }
        }

        //for Repo with existing commits
        else // todo verify to build currentCommitDatabases on repo loading from xml
            anyChanges = fileUtils.createWcDatabases();

        // If any changes waiting, set the "OK Switch" for new commit
        if (anyChanges)
            this.pendingChangesWaiting = true;

        return anyChanges;
    }

    /**
     * This method needs the checkForWcPendingChanges methods to work before it and activate
     * it's "OK switch" - the "pendingChangesWaiting" field. Else, an exception is thrown.
     *
     * @param commitDescription The new commit description from the user
     * @throws Exception "Ok switch" is false or another general error form inner method call
     */
    public void newCommit(String commitDescription) throws Exception {
        if (!this.pendingChangesWaiting)
            throw new Exception("Cannot commit! Please check for pending changes again!");

        //Create the new commit and set it as the active one
        Commit newCommit = createNewCommit(commitDescription);

        addNewObjectsToRepo(); //add the new wcObjects to  the repo objects and than write them to disk
        addNewCommitToRepo(newCommit); //add the new commit the repo's commits map and write it to the disk
        updateActiveBranch(newCommit.calcSha1()); //set active branch to point the current commit and write the branch to disk
        System.out.println(newCommit); //test

        //Update Current commit databases to the new one and disable the "Pending changes switch"
        createCurrentCommitDatabases();
        this.pendingChangesWaiting = false;

    }

    private Commit createNewCommit(String commitDescription) {
        //Gather the new commit metadata
        String rootFolderSha1 = fileUtils.getNewCommitRootFolderSha1();
        String creationTime = fileUtils.getNewCommitTime();
        String author = getActiveUser();
        String parentCommitSha1; //Depends whether it is the first commit on the repo
        if (isNoCommits())
            parentCommitSha1 = null;
        else
            parentCommitSha1 = getCurrentCommit().calcSha1();

        return new Commit(rootFolderSha1, parentCommitSha1, commitDescription,
                creationTime, author);
    }

    private void addNewObjectsToRepo() throws IOException {

        //Get a map contains only the new objects that aren't already exist in the repo
        Map<String, MagitObject> newObjects = wcObjects.entrySet().stream()
                .filter(e -> !(repoObjects.containsKey(e.getKey())))
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));

        //add newObjects to the repo main objects map
        repoObjects.putAll(newObjects);

        //write newObjects to disk
        for (MagitObject object : newObjects.values())
            fileUtils.writeObjectToDisk(object);
    }

    private void updateActiveBranch(String newPointedCommitSha1) throws IOException {
        Branch activeBranch = getActiveBranch();
        activeBranch.setPointedCommit(newPointedCommitSha1);
        fileUtils.writeBranchToDisk(activeBranch);
    }

    private void addNewCommitToRepo(Commit newCommit) throws IOException {
        commits.put(newCommit.calcSha1(), newCommit); //add to the repo commit's map
        fileUtils.writeObjectToDisk(newCommit); //write the commit on disk
    }

    public boolean isBranchNameExists(String name){
        return branches.containsKey(name);
    }

    public void addNewBranchToRepo(Branch newBranch) throws Exception {
        //UI should check that the new branch name doesn't already exists
        if (isBranchNameExists(newBranch.getName())) //Additional check to prevent erroneous use
            throw new Exception("Already got a branch with that name. Overriding is not allowed!");

        branches.put(newBranch.getName(), newBranch);
        fileUtils.writeBranchToDisk(newBranch);
    }

    public void createCurrentCommitDatabases() {

        currentCommitObjects = new HashMap<>();
        currentCommitFilesPaths = new HashMap<>();

        String repoRootSha1 = getCurrentCommit().getRootFolderSha1();
        MagitFolder repoRoot = (MagitFolder) repoObjects.get(repoRootSha1);

        String repoRootPath = fileUtils.getRepoPath().toString();
        createCurrentCommitDatabases_Rec(repoRoot, repoRootPath);

        this.currentCommitObjects.put(repoRoot.calcSha1(), repoRoot); //put the root folder
        //System.out.println(currentCommitFilesPaths); //test
    }

    private void createCurrentCommitDatabases_Rec(MagitObject object, String objectPath) {

        if (object instanceof MagitFolder) {
            Collection<MagitObjMetadata> magitFolderContents = ((MagitFolder) object).getObjectsValues();
            for (MagitObjMetadata currentObjectData : magitFolderContents) {
                String currentObjectPath = objectPath.concat("\\" + currentObjectData.getName());
                MagitObject currentObject = repoObjects.get(currentObjectData.getSha1()); //get object from the repo objects map
                currentObject.setParentFolder((MagitFolder) object); //object is the parent MagitFolder
                createCurrentCommitDatabases_Rec(currentObject, currentObjectPath);
            }
        } else // If (object instanceof Blob), add the file path to current commit files path-sha1 table
            currentCommitFilesPaths.put(Paths.get(objectPath), object.calcSha1());


        this.currentCommitObjects.put(object.calcSha1(), object); //add object to currentCommit table
    }

    public String getCurrentCommitObjectsData() throws Exception {

        StringBuilder commitObjectsData = new StringBuilder();
        String repoRootSha1 = getCurrentCommit().getRootFolderSha1();
        MagitFolder repoRoot = (MagitFolder) currentCommitObjects.get(repoRootSha1);
        String repoRootPath = fileUtils.getRepoPath().toString();

        getCurrentCommitObjectsData_Rec(commitObjectsData, repoRoot, repoRootPath, 0);

        return commitObjectsData.toString();
    }

    private void getCurrentCommitObjectsData_Rec(StringBuilder commitObjectsData, MagitFolder parentFolder,
                                                 String path, int treeLevel) {

        Collection<MagitObjMetadata> magitFolderContents = parentFolder.getObjectsValues();
        for (MagitObjMetadata currentObjectData : magitFolderContents) {

            String currentObjectPath = path.concat("\\" + currentObjectData.getName());
            for (int i = 0; i <= treeLevel; i++)
                commitObjectsData.append("\t");
            commitObjectsData.append(currentObjectPath + ": ");
            commitObjectsData.append(currentObjectData.getInfoForUI());

            if (currentObjectData.getObjectType() == MagitObjectType.FOLDER) {
                String currentFolderSha1 = currentObjectData.getSha1();
                MagitFolder currentFolder = (MagitFolder) currentCommitObjects.get(currentFolderSha1);
                getCurrentCommitObjectsData_Rec(commitObjectsData, currentFolder, currentObjectPath, treeLevel+1);
                //commitObjectsData.append("TEST\n");
            }
        }
    }


    //******************* Test/Archive zone! ********************************************************************

    public void test123() {
        System.out.println(repoObjects);
        System.out.println(currentCommitObjects);
        System.out.println(currentCommitFilesPaths);
        System.out.println(wcObjects);
        System.out.println(wcPendingChanges);
    }

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


    private class RepoFileUtils {

        private final Path repoPath;
        private final Path magitPath;
        private final Path objectsPath;
        private final Path branchesPath;
        private String newCommitTime;

        private String newCommitRootFolderSha1;

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

        public String getNewCommitRootFolderSha1() {
            return newCommitRootFolderSha1;
        }
//--------------------------------------------------------------------------------------------------------------

        public void createWcDatabases_FirstCommit() throws IOException {

            updateNewCommitTime();
            wcObjects = new HashMap<>();
            wcFilesPaths = new HashMap<>();

            File repoDir = new File(repoPath.toString());
            MagitFolder wcRootFolder = new MagitFolder();
            createWcDatabases_FirstCommit_REC(repoDir, wcRootFolder); //traverse WC and get Objects map and files paths

            wcObjects.put(wcRootFolder.calcSha1(), wcRootFolder); //put the wcRootFolder to WC objects
            newCommitRootFolderSha1 = wcRootFolder.calcSha1();

            //System.out.println(wcFilesPaths); //test
            currentCommitFilesPaths = new HashMap<>(); //use an empty list for finding changes (There is no commit to compare to)
            wcPendingChanges = new WC_PendingChangesData(); //Create WC files change lists
        }

        //Note: This method DOESN'T use/set the parentFolder field in MagitObject
        private void createWcDatabases_FirstCommit_REC(File parentFolderFile, MagitFolder parentFolder) throws IOException {
            File[] filesList = parentFolderFile.listFiles();
            if (filesList == null) //a fallback
                return;

            for (File objectFile : filesList) {

                MagitObject object;

                if (objectFile.isDirectory()) {
                    //todo replace !(objectFile.list().length > 0) with !(filesList.length>0)

                    /*  - Ignore Magit system folder
                        - Ignore empty folders (Doesn't work on folder contains just an empty folder...) */
                    if (objectFile.list().length == 0 || objectFile.getName().equals(".magit"))
                        continue;

                    MagitFolder currentFolder = new MagitFolder();
                    createWcDatabases_FirstCommit_REC(objectFile, currentFolder); // Fill the new folder contents (recursively)
                    object = currentFolder;

                } else {   //object is file
                    Blob currentBlob = new Blob(objectFile);
                    wcFilesPaths.put(Paths.get(objectFile.getPath()), currentBlob.calcSha1()); //Add file to WC path-sha1 map
                    object = currentBlob;
                }

                //Create the object's metadata and add it to the parent folder
                String objectSha1 = object.calcSha1();
                String lastModifier = Repository.this.getActiveUser();
                String lastModifiedTime = getNewCommitTime();
                MagitObjMetadata objMetadata = new MagitObjMetadata(objectFile, objectSha1, lastModifier, lastModifiedTime);
                parentFolder.addObjectData(objMetadata);

                wcObjects.put(objectSha1, object);
            }
        }


        /**
         * This method assumes the integrity of the repo path!
         *
         * @return True if WC is empty (only .magit folder exists), False otherwise.
         */
        public boolean isWcEmpty() {
            File repoDir = new File(repoPath.toString());
            if (repoDir.list().length == 1)
                return true;

            else
                return false;
        }

        //returns True if any pending changes found, else otherwise.
        public boolean createWcDatabases() throws IOException {

            updateNewCommitTime();
            wcObjects = new HashMap<>();
            wcFilesPaths = new HashMap<>();

            File repoDir = new File(repoPath.toString());
            MagitFolder wcRootFolder = new MagitFolder();
            createWcDatabases_REC(repoDir, wcRootFolder); //traverse WC and get Objects map and files paths

            String wcRootFolderSha1 = wcRootFolder.calcSha1();
            wcObjects.put(wcRootFolderSha1, wcRootFolder); //put the wcRootFolder to WC objects
            newCommitRootFolderSha1 = wcRootFolderSha1;

            //Check if there are any changes between current commit and the Wc
            String currentCommitRootFolderSha1 = getCurrentCommit().getRootFolderSha1();
            if (wcRootFolderSha1.equals(currentCommitRootFolderSha1))
                return false;

            //System.out.println(wcFilesPaths); //test
            wcPendingChanges = new WC_PendingChangesData(); //Create WC files change lists
            return true; //there are changes
        }

        private void createWcDatabases_REC(File parentFolderFile, MagitFolder parentFolder) throws IOException {

            File[] filesList = parentFolderFile.listFiles();
            if (filesList == null) //a fallback
                return;

            for (File objectFile : filesList) {

                MagitObject object;

                if (objectFile.isDirectory()) {
                    //todo replace !(objectFile.list().length > 0) with !(filesList.length>0)

                    /*  - Ignore Magit system folder
                        - Ignore empty folders (Doesn't work on folder contains just an empty folder...) */
                    if (objectFile.list().length == 0 || objectFile.getName().equals(".magit"))
                        continue;

                    MagitFolder currentFolder = new MagitFolder();
                    createWcDatabases_REC(objectFile, currentFolder); // Fill the new folder contents (recursively)
                    object = currentFolder;

                } else {   //object is file
                    Blob currentBlob = new Blob(objectFile);
                    wcFilesPaths.put(Paths.get(objectFile.getPath()), currentBlob.calcSha1()); //Add file to WC path-sha1 map
                    object = currentBlob;
                }

                object.setParentFolder(parentFolder);
                createWcObjMetadataAndPutInParent(object, objectFile);
                wcObjects.put(object.calcSha1(), object);
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

        private void writeObjectToDisk(Sha1Able object) throws IOException {

            Path pathToWrite = objectsPath.resolve(object.calcSha1());

            try (FileOutputStream fos = new FileOutputStream(pathToWrite.toString());
                 GZIPOutputStream zos = new GZIPOutputStream(fos);
                 ObjectOutputStream out = new ObjectOutputStream(zos)) {

                out.writeObject(object);
                out.flush();

            }
        }

        //Create a new branch file on disk, or override an existing one
        private void writeBranchToDisk(Branch branch) throws IOException {

            final Path pathToWrite = branchesPath.resolve(branch.getName());
            try (Writer out = new BufferedWriter(
                    new OutputStreamWriter(
                            new FileOutputStream(pathToWrite.toString()), StandardCharsets.UTF_8))) {

                out.write(branch.getPointedCommit());
            }
        }

        //Create a new HEAD file on disk, or override an existing one
        private void updateHeadFileOnDisk(Branch newActiveBranch) throws IOException {

            final Path HeadFilePath = branchesPath.resolve("HEAD"); //todo verify that cannot be overwritten by a new branch
            try (Writer out = new BufferedWriter(
                    new OutputStreamWriter(
                            new FileOutputStream(HeadFilePath.toString()), StandardCharsets.UTF_8))) {

                out.write(newActiveBranch.getName());
            }
        }

        private void loadBranchesFromDisk() throws Exception {

            File branchesFolder = new File(branchesPath.toString());
            File[] branchFilesList = branchesFolder.listFiles();
            if (branchFilesList == null)
                throw new IOException("No files found on the repo's branches path");

            for (File branchFile : branchFilesList) {
                if (!branchFile.getName().equals("HEAD")) {
                    Branch b = new Branch(branchFile);
                    Repository.this.branches.put(b.getName(), b);
                }
            }

            if (Repository.this.branches.isEmpty())
                throw new Exception("Cannot find any branch on this repo");
            //System.out.println(branches); //test
        }

        private void loadMagitObjectsAndCommitsFromDisk() throws Exception {

            File objectsFolder = new File(objectsPath.toString());
            File[] objectsFilesList = objectsFolder.listFiles();
            if (objectsFilesList == null)
                throw new IOException("No files found on the repo's objects path");

            for (File objectFile : objectsFilesList) {

                Sha1Able obj;
                try (FileInputStream fis = new FileInputStream(objectFile);
                     GZIPInputStream zis = new GZIPInputStream(fis);
                     ObjectInputStream in = new ObjectInputStream(zis)) {

                    obj = (Sha1Able) in.readObject();
                }

                if (obj instanceof MagitObject)
                    Repository.this.repoObjects.put(obj.calcSha1(), (MagitObject) obj);
                else if (obj instanceof Commit)
                    Repository.this.commits.put(obj.calcSha1(), (Commit) obj);
                else
                    throw new IOException("Unknown file: " + objectFile.getName());
            }

            if (Repository.this.commits.isEmpty())
                throw new Exception("Cannot find any commits on this repo");
            MapUtils.verbosePrint(System.out, null, commits); //test
            // MapUtils.verbosePrint(System.out, null, repoObjects); //test
        }


    } //RepoFileUtils Class end

    private class WC_PendingChangesData {

        Collection<Path> newFiles;
        Collection<Path> changedFiles;
        Collection<Path> deletedFiles;

        public WC_PendingChangesData() {
            this.newFiles = CollectionUtils.subtract(wcFilesPaths.keySet(), currentCommitFilesPaths.keySet());
            this.deletedFiles = CollectionUtils.subtract(currentCommitFilesPaths.keySet(), wcFilesPaths.keySet());
            this.changedFiles = createChangedFilesPathList();
        }

        private Collection<Path> createChangedFilesPathList() {
            Collection<Path> commonPaths = CollectionUtils.intersection(currentCommitFilesPaths.keySet(), wcFilesPaths.keySet());
            //find only paths with modified sha1
            Collection<Path> changeList = commonPaths.stream()
                    .filter(k -> !currentCommitFilesPaths.get(k).equals(wcFilesPaths.get(k)))
                    .collect(Collectors.toList());

            return changeList;
        }

        @Override
        public String toString() {
            return "Pending changes to commit: " + "\n" +
                    "New Files: " + newFiles + "\n" +
                    "Changed Files: " + changedFiles + "\n" +
                    "Deleted Files: " + deletedFiles + "\n";
        }

        //    public void clearData() {
        //        newFiles.clear();
        //        changedFiles.clear();
        //        deletedFiles.clear();
        //    }

    } // WC_PendingChangesData class end

} //Repository class end
