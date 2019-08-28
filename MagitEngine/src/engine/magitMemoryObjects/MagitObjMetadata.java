package engine.magitMemoryObjects;

import engine.Repository;

import java.io.File;
import java.io.Serializable;

public class MagitObjMetadata implements Serializable {

    private String name;
    private String sha1;
    private MagitObjectType objectType;
    private String lastModifier;
    private String lastModifiedTime;

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
}
