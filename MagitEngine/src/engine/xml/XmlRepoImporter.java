package engine.xml;

import engine.fileMangers.MagitFileUtils;
import engine.magitObjects.*;
import engine.repo.Branch;
import engine.repo.RepoSettings;
import engine.repo.Repository;
import engine.xml.generated.*;
import org.apache.commons.io.FileUtils;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

public class XmlRepoImporter {

    private final static String JAXB_XML_MAGIT_PACKAGE_NAME = "engine.xml.generated";
    private MagitRepository repoData;
    private Repository repo;
    private Map<String, MagitObjMetadata> blobsData; //<ImportedBlobID, createdMeteData>
    private Map<String, MagitObjMetadata> foldersData; //<ImportedFolderID, createdMeteData>
    private Map<String, String> rootFoldersIDtoSha1; //<rootFolderID, folderSha1>
    private Map<String, String> commitsIDtoSha1; //<commitID, commitSha1>


    public XmlRepoImporter(File xmlFile) throws IOException, JAXBException {

        InputStream inputStream = FileUtils.openInputStream(xmlFile);
        this.repoData = deserializeFrom(inputStream);

        blobsData = new HashMap<>();
        foldersData = new HashMap<>();
        rootFoldersIDtoSha1 = new HashMap<>();
        commitsIDtoSha1 = new HashMap<>();
    }

    private static MagitRepository deserializeFrom(InputStream in) throws JAXBException {
        JAXBContext jc = JAXBContext.newInstance(JAXB_XML_MAGIT_PACKAGE_NAME);
        Unmarshaller u = jc.createUnmarshaller();
        return (MagitRepository) u.unmarshal(in);
    }

    public String getDesignatedPath() {
        return repoData.getLocation();
    }

    public Repository createRepoFromXml() throws Exception {

        verifyFoldersAndCommitsIntegrity();
        String headBranch = repoData.getMagitBranches().getHead();
        RepoSettings settings = new RepoSettings(repoData.getName(), repoData.getLocation(), headBranch);
        this.repo = new Repository(settings);

        Path designatedPath = Paths.get(repoData.getLocation());
        // If the repoFolder doesn't exist, create it. Else, clean its content and create the .magit folders
        MagitFileUtils.createRepoFoldersOnDisk(repo.getSettings(), Files.exists(designatedPath));

        loadBlobs();
        loadFolders();
        //Write blobs and folders to disk
        for (MagitObject object : repo.getObjectsAsCollection())
            repo.useFileUtils().writeObjectToDisk(object);

        loadCommits();
        loadBranches();
        repo.useFileUtils().checkoutOnDisk(repo.getActiveBranch());
        repo.createCurrentCommitDatabases();
        return repo;
    }

    private void verifyFoldersAndCommitsIntegrity() throws Exception {
        Set<String> foldersIDs= new HashSet<>();
        Set<String> commitsIDs= new HashSet<>();

        for (MagitSingleFolder folder : repoData.getMagitFolders().getMagitSingleFolder()) {
            if (foldersIDs.contains(folder.getId()))
                throw new Exception("Duplicate ID for two folders found!");
            foldersIDs.add(folder.getId());
        }

        for (MagitSingleCommit commit : repoData.getMagitCommits().getMagitSingleCommit()) {
            if (commitsIDs.contains(commit.getId()))
                throw new Exception("Duplicate ID for two commits found!");
            commitsIDs.add(commit.getId());
        }
    }


    private void loadBlobs() throws Exception {
        List<MagitBlob> blobsList = repoData.getMagitBlobs().getMagitBlob();
        for (MagitBlob blobData : blobsList) {

            //create blob and add it to the repo
            Blob blob = new Blob(blobData.getContent());
            repo.addMagitObjectToRepo(blob);

            //create it's metadata and add to an internal Importer map
            if (this.blobsData.containsKey(blobData.getId()))
                throw new Exception("Duplicate ID for two blobs found!");
            MagitObjMetadata blobMetadata = new MagitObjMetadata(blobData, blob.calcSha1());
            this.blobsData.put(blobData.getId(), blobMetadata);
        }
    }

    private void loadFolders() throws Exception {
        List<MagitSingleFolder> foldersList = repoData.getMagitFolders().getMagitSingleFolder();
        for (MagitSingleFolder folderData : foldersList) {

            if (this.foldersData.containsKey(folderData.getId())) //The folder was created already
                continue;

            //create magitFolder, fill it's contents and add it to the repo
            MagitFolder folder = new MagitFolder();
            fillFolderContent_Rec(folder, folderData);
            repo.addMagitObjectToRepo(folder);

            //create it's metadata and add to an internal Importer map
            createMetadataAndPutToImporterMaps(folderData, folder.calcSha1());
        }
    }

    private void fillFolderContent_Rec(MagitFolder parentFolder, MagitSingleFolder parentFolderData) throws Exception {

        List<Item> parentFolderItems = parentFolderData.getItems().getItem();
        for (Item item : parentFolderItems) {
            String itemID = item.getId();

            if (item.getType().equalsIgnoreCase("folder")) {

                MagitObjMetadata currentFolderMetadata;

                if (foldersData.containsKey(itemID))
                    currentFolderMetadata = foldersData.get(itemID);

                else {
                    //create magitFolder, fill it's contents and add it to the repo
                    MagitFolder currentFolder = new MagitFolder();
                    MagitSingleFolder currentFolderData = findFolderDataByID(itemID);
                    fillFolderContent_Rec(currentFolder, currentFolderData);
                    repo.addMagitObjectToRepo(currentFolder);
                    //create it's metadata and add to an internal Importer map
                    currentFolderMetadata = createMetadataAndPutToImporterMaps(currentFolderData, currentFolder.calcSha1());
                }

                parentFolder.addObjectData(currentFolderMetadata);

            } else { //item is blob

                if (blobsData.containsKey(itemID)) {
                    MagitObjMetadata currentBlobMetadata = blobsData.get(itemID);
                    parentFolder.addObjectData(currentBlobMetadata);
                } else
                    throw new Exception("Cannot find blob id #" + itemID + "!");
            }
        }
    }

    private MagitSingleFolder findFolderDataByID(String folderID) throws Exception {
        List<MagitSingleFolder> foldersList = repoData.getMagitFolders().getMagitSingleFolder();
        MagitSingleFolder folderData = foldersList.stream()
                .filter(f -> f.getId().equals(folderID))
                .findFirst().orElseThrow(() -> new Exception("Cannot find folder id #" + folderID + "!"));

        return folderData;
    }

    private MagitSingleCommit findCommitDataByID(String commitID) throws Exception {
        List<MagitSingleCommit> commitsList = repoData.getMagitCommits().getMagitSingleCommit();
        MagitSingleCommit commitData = commitsList.stream()
                .filter(c -> c.getId().equals(commitID))
                .findFirst().orElseThrow(() -> new Exception("Cannot find commit id #" + commitID + "!"));

        return commitData;
    }

    private MagitObjMetadata createMetadataAndPutToImporterMaps(MagitSingleFolder folderData, String folderSha1) throws Exception {
        String folderID = folderData.getId();
        if (this.foldersData.containsKey(folderID))
            throw new Exception("folder #" + folderID + " has itself as an item!");
        MagitObjMetadata folderMetadata = new MagitObjMetadata(folderData, folderSha1);
        this.foldersData.put(folderID, folderMetadata);

        if (folderData.isIsRoot())
            rootFoldersIDtoSha1.put(folderID, folderSha1);

        return folderMetadata;
    }

    private void loadCommits() throws Exception {

        List<MagitSingleCommit> commitsList = repoData.getMagitCommits().getMagitSingleCommit();
        for (MagitSingleCommit commitData : commitsList) {

            if (this.commitsIDtoSha1.containsKey(commitData.getId())) //The commit was already already
                continue;

            loadCommits_Rec(commitData);
        }
    }

    private void loadCommits_Rec(MagitSingleCommit commitData) throws Exception {

        if (commitData.getPrecedingCommits() != null) {
            //verify all preceding commit already loaded (we need to have their sha1). if not, load them first.
            final List<PrecedingCommits.PrecedingCommit> precedingCommits = commitData.getPrecedingCommits().getPrecedingCommit();
            for (PrecedingCommits.PrecedingCommit precedingCommit : precedingCommits) {
                //prevent infinite loop
                if (precedingCommit.getId().equals(commitData.getId()))
                    throw new Exception("Commit ID #" + commitData.getId() + " Points to itself!");

                if (!this.commitsIDtoSha1.containsKey(precedingCommit.getId())) {
                    MagitSingleCommit precedingCommitData = findCommitDataByID(precedingCommit.getId());
                    loadCommits_Rec(precedingCommitData);
                }
            }
        }
        createNewCommit(commitData);
    }

    private void createNewCommit(MagitSingleCommit commitData) throws Exception {

        String creationTime = commitData.getDateOfCreation();
        String author = commitData.getAuthor();
        String message = commitData.getMessage();

        String parentCommitSha1 = getParentCommitSha1(commitData);
        //String anotherParentCommitID = commitData.getPrecedingCommits().getPrecedingCommit().get(1).getId();
        //String anotherParentCommitSha1 = commitsIDtoSha1.get(anotherParentCommitID);
        String rootFolderSha1 = getCommitRootFolderSha1(commitData);

        Commit commit = new Commit(rootFolderSha1, parentCommitSha1, message, creationTime, author);
        this.repo.addNewCommitToRepo(commit); //This method includes writing the commit to disk
        this.commitsIDtoSha1.put(commitData.getId(), commit.calcSha1());
    }

    private String getParentCommitSha1(MagitSingleCommit commitData) {
        //parentCommitSha1 should be exist because of the recursive commits loading, or null if the commit has no parents
        String parentCommitSha1 = null;
        if (commitData.getPrecedingCommits() != null) {
            final List<PrecedingCommits.PrecedingCommit> precedingCommits = commitData.getPrecedingCommits().getPrecedingCommit();
            if (!precedingCommits.isEmpty()) {
                String parentCommitID = precedingCommits.get(0).getId();
                parentCommitSha1 = commitsIDtoSha1.get(parentCommitID);
            }
        }
        return parentCommitSha1;
    }

    private String getCommitRootFolderSha1(MagitSingleCommit commitData) throws Exception {

        String rootFolderID = commitData.getRootFolder().getId();
        String rootFolderSha1 = rootFoldersIDtoSha1.get(rootFolderID);
        if (rootFolderSha1 == null) {
            String exceptionMsg;
            if (foldersData.containsKey(rootFolderID)) {
                exceptionMsg = "Commit ID #" + commitData.getId() + " is pointing to non-root folder #"
                        + rootFolderID + "!";
            } else {
                exceptionMsg = "Commit ID #" + commitData.getId() + " is pointing to invalid folder #"
                        + rootFolderID + "!";
            }
            throw new Exception(exceptionMsg);
        }
        return rootFolderSha1;
    }

    private void loadBranches() throws Exception {
        List<MagitSingleBranch> branchesList = repoData.getMagitBranches().getMagitSingleBranch();
        for (MagitSingleBranch branchData : branchesList) {

            String pointedCommitID = branchData.getPointedCommit().getId();
            String pointedCommitSha1 = commitsIDtoSha1.get(pointedCommitID);
            if (pointedCommitSha1 == null)
                throw new Exception("Branch " + branchData.getName() + " points to invalid commit id #"
                        + pointedCommitID + "!");

            Branch branch = new Branch(branchData.getName(), pointedCommitSha1);
            repo.addNewBranchToRepo(branch);
        }

        if (repo.getActiveBranch() == null)
            throw new Exception("Head branch " + repoData.getMagitBranches().getHead() + " doesn't exist!");

        repo.useFileUtils().updateHeadBranchOnDisk(repo.getActiveBranch()); //to create HEAD and RepoSettings files on disk
    }


} //class end
