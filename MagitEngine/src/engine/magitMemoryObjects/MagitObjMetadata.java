package engine.magitMemoryObjects;

import engine.Repository;

import java.io.File;
import java.text.SimpleDateFormat;

public class MagitObjMetadata {

    private String name;
    private String sha1;
    private MagitObjectType objectType;
    private String lastModifier;
    private String lastModifiedTime;

    public MagitObjMetadata(File file, String sha1, String lastModifier) {

        this.name = file.getName();
        this.sha1 = sha1;

        if (file.isDirectory())
            this.objectType = MagitObjectType.FOLDER;
        else // is File
            this.objectType = MagitObjectType.FILE;

        this.lastModifier = lastModifier;

        SimpleDateFormat sdf = new SimpleDateFormat(Repository.DATE_FORMAT);
        this.lastModifiedTime = sdf.format(file.lastModified());
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
}
