package engine.magitObjects;

public enum MagitObjectType {
    FILE("File"), FOLDER("Folder");

    private final String nameAsTxt;

    MagitObjectType(String name) {
        this.nameAsTxt = name;
    }

    @Override
    public String toString() {
        return nameAsTxt;
    }

}
