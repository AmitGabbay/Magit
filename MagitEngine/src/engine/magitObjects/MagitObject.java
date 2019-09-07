package engine.magitObjects;

import java.io.Serializable;

public abstract class MagitObject implements Sha1Able, Serializable {

    public abstract String calcSha1();


    private transient MagitFolder parentFolder; //Used only on currentCommit and WC objects Map. Generate updated value before any use!!!

    public MagitFolder getParentFolder() {
        return parentFolder;
    }

    public void setParentFolder(MagitFolder parentFolder) {
        this.parentFolder = parentFolder;
    }

}
