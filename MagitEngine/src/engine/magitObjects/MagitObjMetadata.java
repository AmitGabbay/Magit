package engine.magitObjects;

import engine.xml.generated.MagitBlob;
import engine.xml.generated.MagitSingleFolder;

import java.io.File;
import java.io.Serializable;

public class MagitObjMetadata implements Serializable {

    private final String name;
    private final String sha1;
    private final MagitObjectType objectType;
    private final String lastModifier;
    private final String lastModifiedTime;

    public MagitObjMetadata(File file, String sha1, String lastModifier, String lastModifiedTime) {

        this.name = file.getName();
        this.sha1 = sha1;

        if (file.isDirectory())
            this.objectType = MagitObjectType.FOLDER;
        else // is File
            this.objectType = MagitObjectType.FILE;

        this.lastModifier = lastModifier;
        this.lastModifiedTime = lastModifiedTime;
    }

    public MagitObjMetadata(MagitBlob blobData, String sha1) {
        this.name = blobData.getName();
        this.sha1 = sha1;
        this.objectType = MagitObjectType.FILE;
        this.lastModifier = blobData.getLastUpdater();
        this.lastModifiedTime = blobData.getLastUpdateDate();
    }

    public MagitObjMetadata(MagitSingleFolder folderData, String sha1) {
        this.name = folderData.getName();
        this.sha1 = sha1;
        this.objectType = MagitObjectType.FOLDER;
        this.lastModifier = folderData.getLastUpdater();
        this.lastModifiedTime = folderData.getLastUpdateDate();
    }



    @Override
    public String toString() {
        return name + ", " +
                sha1 + ", " +
                objectType + ", " +
                lastModifier + ", " +
                lastModifiedTime;
    }


    public String getName() {
        return name;
    }

    public String getSha1() {
        return sha1;
    }

    public MagitObjectType getObjectType() {
        return objectType;
    }

    public String getLastModifier() {
        return lastModifier;
    }

    public String getLastModifiedTime() {
        return lastModifiedTime;
    }

    public String getInfoForUI(){
        return String.format("%s, Sha1: %s, Last modifier: %s, Last updated: %s\n", objectType, sha1,lastModifier, lastModifiedTime);
    }

}
