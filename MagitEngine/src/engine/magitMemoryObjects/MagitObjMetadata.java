package engine.magitMemoryObjects;

public class MagitObjMetadata {

    private String name;
    private String sha1;
    private MagitObjectType objectType;
    private String lastModifier;
    private String lastModifiedTime;

    public MagitObjMetadata(String name, String sha1, MagitObjectType objectType) {
        this.name = name;
        this.sha1 = sha1;
        this.objectType = objectType;
        this.lastModifier = "amit";
        this.lastModifiedTime = "now";
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
