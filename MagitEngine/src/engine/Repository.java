package engine;

import engine.fileMangers.MagitFileUtils;
import engine.magitMemoryObjects.Commit;
import engine.magitMemoryObjects.MagitObject;

import java.io.IOException;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

public class Repository {

    public final static String DATE_FORMAT = "dd.MM.yyyy-HH:mm:ss:SSS";
    private RepoSettings basicSettings;
    private Set<Branch> branches;
    private Map<String, MagitObject> objects;
    private Map<String, Commit> commits;
    private Path repoPath;
    private Path magitPath;
    private Path objectsPath;
    private Path branchesPath;
    private String activeUser = "Administrator";

    public String getActiveUser() {
        return activeUser;
    }

    public void setActiveUser(String newActiveUser) {
        this.activeUser = newActiveUser;
    }

    public Repository(String name, String path) {
        this.basicSettings = new RepoSettings(name, path);
        this.branches = new HashSet<>();
        this.objects = new HashMap<>();
        this.commits = new LinkedHashMap<>();
        this.initializePaths();
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
        newRepo.branches.add(master);
        MagitFileUtils.writeBranchToDisk(master, newRepo.branchesPath);
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

    public void addCommit(Commit commit){
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

    public void traverseWC(){
       MagitFileUtils.getFirstCommitFromWC(this);
    }

    public Map<String, MagitObject> getObjectsAsMap() {
        return objects;
    }

    public Collection<MagitObject> getObjectsTxtAsCollection(){
        return objects.values();
    }

    public Set<Map.Entry<String, MagitObject>> getObjectsAsEntrySet() {
        return objects.entrySet();
    }

}
