package engine.repo;

import engine.fileMangers.MagitFileUtils;
import engine.magitObjects.*;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.io.FileUtils;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

public class Repository {

    public final static String DATE_FORMAT = "dd.MM.yyyy-HH:mm:ss:SSS";

    private final RepoSettings settings;
    private final Map<String, Branch> branches; //index is name
    private final Map<String, MagitObject> repoObjects; //index is sha1
    private final Map<String, Commit> commits; //index is sha1
    private final RepoFileUtils fileUtils;
    private Map<String, MagitObject> currentCommitObjects; //index is sha1
    private Map<String, MagitObject> wcObjects; //index is sha1
    private Map<Path, String> currentCommitFilesPaths; //<File Path, File Sha1>, current commit files path-sha1 table
    private Map<Path, String> wcFilesPaths; //<File Path, File Sha1>, WC files path-sha1 table
    private WC_PendingChangesData wcPendingChanges;
    private boolean pendingChangesWaiting; /* an "OK switch" for doing a new commit.
                                             Set "true" only by checkForWcPendingChanges method */
    private String activeUser = "Administrator";

    public Repository(RepoSettings settings) {
        this.settings = settings;
        this.branches = new HashMap<>();
        this.repoObjects = new HashMap<>();
        this.commits = new HashMap<>();
        this.fileUtils = new RepoFileUtils(settings.getStringPath());
        this.pendingChangesWaiting = false;
    }

    /**
     * Use only after validation of the requested path by the class method checkNewRepoPath!!!
     * Creates new repo (and folders on disk), and then adds an empty master branch to it (+write on disk)
     */
    public static Repository newRepoFromScratchCreator(String newRepoName, String requestedPath) throws Exception {

        RepoSettings settings = new RepoSettings(newRepoName, requestedPath, "master");
        Repository newRepo = new Repository(settings);
        MagitFileUtils.createRepoFoldersOnDisk(newRepo.getSettings(), false);

        Branch master = Branch.createBlankMasterBranch();
        newRepo.addNewBranchToRepo(master);
        newRepo.setActiveBranch(master); //HEAD and RepoSettings Files will be created on this action
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


    public RepoFileUtils useFileUtils() {
        return fileUtils;
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
        return settings.getName();
    }

    public String getStringPath() {
        return settings.getStringPath();
    }

    public RepoSettings getSettings() {
        return settings;
    }


    public Collection<MagitObject> getObjectsAsCollection() {
        return repoObjects.values();
    }

    public Set<Map.Entry<String, MagitObject>> getObjectsAsEntrySet() {
        return repoObjects.entrySet();
    }


    public String getActiveUser() {
        return activeUser;
    }

    public void setActiveUser(String newActiveUser) {
        this.activeUser = newActiveUser;
    }

    public Branch getActiveBranch() {
        return branches.get(settings.getHeadBranch().toLowerCase());
    }


    /**
     * Note: This is not a full checkout method, just HEAD branch changer!
     *
     * @param branch Branch to set as active (the HEAD branch)
     */
    private void setActiveBranch(Branch branch) throws IOException {
        settings.setHeadBranch(branch.getName());
        fileUtils.updateHeadBranchOnDisk(branch);
    }

    public String getActiveBranchName() {
        return getActiveBranch().getName();
    }

    public Commit getCurrentCommit() {
        if (isNoCommits())
            return null;

        Branch activeBranch = getActiveBranch();
        return commits.get(activeBranch.getPointedCommitSha1());
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
        else
            anyChanges = fileUtils.createWcDatabases();

        //set the "OK Switch" according to the the changes check result
        this.pendingChangesWaiting = anyChanges;

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
        //System.out.println(newCommit); //test

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

    public void addNewCommitToRepo(Commit newCommit) throws IOException {
        commits.put(newCommit.calcSha1(), newCommit); //add to the repo commit's map
        fileUtils.writeObjectToDisk(newCommit); //write the commit on disk
    }

    public boolean isBranchNameExists(String name) {
        return branches.containsKey(name.toLowerCase()); //lower case to support case insensitive
    }

    public void addMagitObjectToRepo(MagitObject object){
        repoObjects.put(object.calcSha1(), object);
    }

    public void addNewBranchToRepo(Branch newBranch) throws Exception {
        //UI should check that the new branch name doesn't already exists
        if (isBranchNameExists(newBranch.getName())) //Additional check to prevent erroneous use
            throw new Exception("Already got a branch with that name. Overriding is not allowed!");

        branches.put(newBranch.getName().toLowerCase(), newBranch); //lower case to support case insensitive
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
            Collection<MagitObjMetadata> magitFolderContents = ((MagitFolder) object).getFolderObjects();
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

    public String getCurrentCommitObjectsData() {

        StringBuilder commitObjectsData = new StringBuilder();
        String repoRootSha1 = getCurrentCommit().getRootFolderSha1();
        MagitFolder repoRoot = (MagitFolder) currentCommitObjects.get(repoRootSha1);
        String repoRootPath = fileUtils.getRepoPath().toString();

        getCurrentCommitObjectsData_Rec(commitObjectsData, repoRoot, repoRootPath, 0);

        return commitObjectsData.toString();
    }

    private void getCurrentCommitObjectsData_Rec(StringBuilder commitObjectsData, MagitFolder parentFolder,
                                                 String path, int treeLevel) {

        Collection<MagitObjMetadata> magitFolderContents = parentFolder.getFolderObjects();
        for (MagitObjMetadata currentObjectData : magitFolderContents) {

            String currentObjectPath = path.concat("\\" + currentObjectData.getName());
            for (int i = 0; i <= treeLevel; i++)
                commitObjectsData.append("\t");
            commitObjectsData.append(currentObjectPath + ": ");
            commitObjectsData.append(currentObjectData.getInfoForUI());

            if (currentObjectData.getObjectType() == MagitObjectType.FOLDER) {
                String currentFolderSha1 = currentObjectData.getSha1();
                MagitFolder currentFolder = (MagitFolder) currentCommitObjects.get(currentFolderSha1);
                getCurrentCommitObjectsData_Rec(commitObjectsData, currentFolder, currentObjectPath, treeLevel + 1);
            }
        }
    }

    public String getAllBranchesInfo() {
        StringBuilder allBranchesInfo = new StringBuilder();
        String headBranchName = getActiveBranch().getName();

        for (Branch b : branches.values()) {
            Commit pointedCommit = commits.get(b.getPointedCommitSha1());
            String commitDescription = pointedCommit.getDescription();

            allBranchesInfo.append(b);
            allBranchesInfo.append("\tDescription: " + commitDescription);
            if (b.getName().equals(headBranchName))
                allBranchesInfo.append("\t*Head branch*");

            allBranchesInfo.append("\n");
        }
        return allBranchesInfo.toString();
    }


    public void deleteBranch(Branch branchToDelete) throws IOException {

        branches.remove(branchToDelete.getName().toLowerCase());
        fileUtils.deleteBranchFromDisk(branchToDelete.getName());
    }

    public String getActiveBranchCommitsInfo() {

        StringBuilder commitsInfo = new StringBuilder();
        if (isNoCommits())
            commitsInfo.append("No commits yet.");

        Commit currentCommit = getCurrentCommit();
        while (currentCommit != null) {
            commitsInfo.append(currentCommit.getInfoForUI2());
            commitsInfo.append("\n");
            currentCommit = commits.get(currentCommit.getParentCommitSha1());
        }
        return commitsInfo.toString();
    }


    public void checkout(Branch branchToCheckout) throws IOException {

        //If the branch to change to is on the same commit, we don't need to change any WC databases or files
        if (commits.get(branchToCheckout.getPointedCommitSha1())==(getCurrentCommit()))
            setActiveBranch(branchToCheckout);

        else {
            fileUtils.checkoutOnDisk(branchToCheckout);
            setActiveBranch(branchToCheckout);
            createCurrentCommitDatabases();
        }
    }

    public Branch getBranch(String branchName) {
        return branches.get(branchName.toLowerCase());
    }

    //******************* Test/Archive zone! ********************************************************************

    public void test123() {
        System.out.println(repoObjects);
        System.out.println(currentCommitObjects);
        System.out.println(currentCommitFilesPaths);
        System.out.println(wcObjects);
        System.out.println(wcPendingChanges);
    }


    public class RepoFileUtils {

        private final Path repoPath;
        private final Path magitPath;
        private final Path objectsPath;
        private final Path branchesPath;
        private String newCommitTime;

        private String newCommitRootFolderSha1;

        private RepoFileUtils(String repoStringPath) {
            this.repoPath = Paths.get(repoStringPath);
            this.magitPath = repoPath.resolve(".magit");
            this.objectsPath = magitPath.resolve("objects");
            this.branchesPath = magitPath.resolve("branches");
        }

        private void updateNewCommitTime() {
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
            return repoDir.list().length == 1; // Only .magit folder exists
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
                //TODO Find metadata by sha1 (it's not a map index... before this fix both objects have to share the same name)?
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

        public void writeObjectToDisk(Sha1Able object) throws IOException {

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

                out.write(branch.getPointedCommitSha1());
            }
        }

        //Creates Head and RepoSettings files, or overrides them
        public void updateHeadBranchOnDisk(Branch newHeadBranch) throws IOException {

            //Write the HEAD file (a string defining the head branch name) on disk
            final Path HeadFilePath = branchesPath.resolve("HEAD");
            try (Writer out = new BufferedWriter(
                    new OutputStreamWriter(
                            new FileOutputStream(HeadFilePath.toString()), StandardCharsets.UTF_8))) {

                out.write(newHeadBranch.getName());
            }

            //write the repo settings file on the filesystem (at /<repoName>/.magit)
            Path repoSettingsFile = magitPath.resolve("RepoSettings");
            try (ObjectOutputStream out =
                         new ObjectOutputStream(
                                 new FileOutputStream(repoSettingsFile.toString()))) {
                out.writeObject(Repository.this.settings);
                out.flush();
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
                    Repository.this.branches.put(b.getName().toLowerCase(), b); //lower case to support case insensitive
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
            //MapUtils.verbosePrint(System.out, null, commits); //test
            //MapUtils.verbosePrint(System.out, null, repoObjects); //test
        }

        private void deleteBranchFromDisk(String branchToDeleteName) throws IOException {
            Path branchToDeletePath = branchesPath.resolve(branchToDeleteName);
            Files.delete(branchToDeletePath);
        }

        private void cleanWcOnDisk() throws IOException {
            File repoFolder = new File(repoPath.toString());
            File[] filesList = repoFolder.listFiles();
            if (filesList == null)
                throw new IOException("Bad repo path");

            for (File file : filesList) {
                if (!file.getName().equals(".magit"))
                    FileUtils.forceDelete(file);
            }
        }

        public void checkoutOnDisk(Branch branchToCheckout) throws IOException {

            cleanWcOnDisk();

            Commit commitToCheckout = commits.get(branchToCheckout.getPointedCommitSha1());
            String repoRootSha1 = commitToCheckout.getRootFolderSha1();
            MagitFolder repoRoot = (MagitFolder) repoObjects.get(repoRootSha1);
            Path repoRootPath = fileUtils.getRepoPath();

            checkoutOnDisk_Rec(repoRoot, repoRootPath);
        }

        private void checkoutOnDisk_Rec(MagitFolder folder, Path folderPath) throws IOException {

            Collection<MagitObjMetadata> magitFolderContents = folder.getFolderObjects();
            for (MagitObjMetadata currentObjectData : magitFolderContents) {
                Path currentObjectPath = folderPath.resolve(currentObjectData.getName());
                MagitObject currentObject = repoObjects.get(currentObjectData.getSha1()); //get object from the repo objects map

                if (currentObject instanceof MagitFolder) {
                    Files.createDirectory(currentObjectPath);
                    checkoutOnDisk_Rec((MagitFolder) currentObject, currentObjectPath);
                } else { //Object is a blob
                    File blobFile = new File(currentObjectPath.toString());
                    FileUtils.writeStringToFile(blobFile, ((Blob) currentObject).getContent(), StandardCharsets.UTF_8);
                }
            }
        }

    } //RepoFileUtils Class end

    private class WC_PendingChangesData {

        Collection<Path> newFiles;
        Collection<Path> changedFiles;
        Collection<Path> deletedFiles;

        private WC_PendingChangesData() {
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
