package engine;

import engine.fileMangers.MagitFileUtils;

import java.io.IOException;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class Repository {

    private RepoSettings basicSettings;
    private Set<Branch> branches;
    private Map<String, MagitObject> objects;//Todo add objects map
    private Path repoPath;
    private Path magitPath;
    private Path objectsPath;
    private Path branchesPath;


    public Repository(String name, String path) {
        this.basicSettings = new RepoSettings(name, path);
        this.branches = new HashSet<>();
        this.objects = new HashMap<>();
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

    public Map<String, MagitObject> getObjects() {
        return objects;
    }
}
