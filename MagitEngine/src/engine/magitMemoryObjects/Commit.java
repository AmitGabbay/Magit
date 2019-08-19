package engine.magitMemoryObjects;

public class Commit implements MagitObject, MagitParentObject {

    public Commit(String rootFolderSha1) {
        this.rootFolderSha1 = rootFolderSha1;
    }

    private String rootFolderSha1;

    @Override
    public String calcSha1() {
        return null;
    }

    public String getRootFolderSha1() {
        return rootFolderSha1;
    }
}
