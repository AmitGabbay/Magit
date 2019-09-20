package engine.xml;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import engine.fileMangers.MagitFileUtils;
import engine.magitObjects.*;
import engine.repo.RepoSettings;
import engine.repo.Repository;
import engine.xml.generated.Item;
import engine.xml.generated.MagitBlob;
import engine.xml.generated.MagitRepository;
import engine.xml.generated.MagitSingleFolder;
import org.apache.commons.io.FileUtils;

public class XmlRepoImporter {

    private final static String JAXB_XML_MAGIT_PACKAGE_NAME = "engine.xml.generated";
    private MagitRepository repoData;
    private Repository repo;
    private Map<String, MagitObjMetadata> blobsData; //<ImportedBlobID, createdMeteData>
    private Map<String, MagitObjMetadata> foldersData; //<ImportedFolderID, createdMeteData>


    public XmlRepoImporter(File xmlFile) throws IOException {

        InputStream inputStream = FileUtils.openInputStream(xmlFile);
        try {
            this.repoData = deserializeFrom(inputStream);
        } catch (JAXBException e) { //todo remove later or specify error
            e.printStackTrace();
        }
        blobsData = new HashMap<>();
        foldersData = new HashMap<>();
    }

    private static MagitRepository deserializeFrom(InputStream in) throws JAXBException {
        JAXBContext jc = JAXBContext.newInstance(JAXB_XML_MAGIT_PACKAGE_NAME);
        Unmarshaller u = jc.createUnmarshaller();
        return (MagitRepository) u.unmarshal(in);
    }

//    boolean isRepoAlreadyExistsOnLocation(){
//        return MagitFileUtils.isExistingRepoPath(generatedRepo.getLocation());
//    }

    public String getDesignatedPath() {
        return repoData.getLocation();
    }


    public Repository createRepoFromXml() throws Exception {

        String headBranch = repoData.getMagitBranches().getHead();
        RepoSettings settings = new RepoSettings(repoData.getName(), repoData.getLocation(), headBranch);
        this.repo = new Repository(settings);

        Path designatedPath = Paths.get(repoData.getLocation());
        // If the repoFolder doesn't exist, create it. Else, clean its content and create the .magit folders
        MagitFileUtils.createRepoFoldersOnDisk(repo.getSettings(), Files.exists(designatedPath));

        loadBlobs();

        //write all to the disk
        //after all - set active branch - to create the necessary files
        return repo;
    }

    private void loadBlobs() throws Exception {
        List<MagitBlob> blobsList = repoData.getMagitBlobs().getMagitBlob();
        for (MagitBlob blobData : blobsList) {

            //create blob and add it to the repo
            Blob blob = new Blob(blobData.getContent());
            repo.addMagitObjectToRepo(blob);

            //create it's metadata and add to an internal Importer map
            if (this.blobsData.containsKey(blobData.getId()))
                throw new Exception("Duplicate ID for two different blobs found!");
            MagitObjMetadata blobMetadata = new MagitObjMetadata(blobData, blob.calcSha1());
            this.blobsData.put(blobData.getId(), blobMetadata);
        }
    }


    private void loadFolders() throws Exception {
        List<MagitSingleFolder> foldersList = repoData.getMagitFolders().getMagitSingleFolder();
        for (MagitSingleFolder folderData : foldersList) {

            if (this.foldersData.containsKey(folderData.getId())) //The folder was created already
                continue;

            //create blob and add it to the repo
            MagitFolder rootFolder = new MagitFolder();

            //todo recursive

            repo.addMagitObjectToRepo(rootFolder);
            //create it's metadata and add to an internal Importer map
            if (this.foldersData.containsKey(folderData.getId()))
                throw new Exception("folder #" + folderData.getId() + " has itself as an item!");
            MagitObjMetadata folderMetadata = new MagitObjMetadata(folderData, rootFolder.calcSha1());
            this.foldersData.put(folderData.getId(), folderMetadata);
        }
    }

    private void FillFolderContent_Rec(MagitFolder parentFolder, MagitSingleFolder parentFolderData) throws Exception {

        List<Item> parentFolderItems = parentFolderData.getItems().getItem();
        for (Item item : parentFolderItems) {
            String itemID = item.getId();

            if (item.getType().equalsIgnoreCase("folder")) {

                MagitObjMetadata currentFolderMetadata;

                if (foldersData.containsKey(itemID))
                    currentFolderMetadata = foldersData.get(itemID);

                else {
                    MagitFolder currentFolder = new MagitFolder();
                    MagitSingleFolder currentFolderData;//=todo write locator for the folder obj
                    //todo recursive
                    repo.addMagitObjectToRepo(currentFolder);
                    //create it's metadata and add to an internal Importer map
                    if (this.foldersData.containsKey(itemID))
                        throw new Exception("folder #" + itemID + " has itself as an item!");
                    currentFolderMetadata = new MagitObjMetadata(currentFolderData, currentFolder.calcSha1());
                    this.foldersData.put(itemID, currentFolderMetadata);
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


    //    public  void loadXML(File xmlFile) throws IOException {
//
//        InputStream inputStream = FileUtils.openInputStream(xmlFile);
//        try {
//           this.generatedRepo = deserializeFrom(inputStream);
//            //System.out.println("aaa");
//        } catch (JAXBException e) { //todo remove later or specify error
//            e.printStackTrace();
//        }
//    }

}
