package engine.magitMemoryObjects;

import java.io.File;

public class MagitObjMetadata {

    private String name;
    private String sha1;
    private MagitObjectType objectType;
    private String lastModifier;
    private String lastModifiedTime;

    public MagitObjMetadata(File file, String sha1, MagitObjectType objectType, String lastModifier) {
        this.name = file.getName();
        this.sha1 = sha1;
        this.objectType = objectType;
        this.lastModifier = lastModifier;


        this.lastModifiedTime = "now"; //todo use SimpleDateFormat with the const format in magit object
        //todo and omit objectType parameter (determain by the file parameter)
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
