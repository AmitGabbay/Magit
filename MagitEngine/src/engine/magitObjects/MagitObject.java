package engine.magitObjects;

import java.io.Serializable;

public abstract class MagitObject implements Sha1Able, Serializable {

    public abstract String calcSha1();

    private transient String path;
    private transient String lastModifier;
    private transient String lastModifiedTime;

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public String getLastModifier() {
        return lastModifier;
    }

    public void setLastModifier(String lastModifier) {
        this.lastModifier = lastModifier;
    }

    public String getLastModifiedTime() {
        return lastModifiedTime;
    }

    public void setLastModifiedTime(String lastModifiedTime) {
        this.lastModifiedTime = lastModifiedTime;
    }

    public void setHelperFields(String path, String lastModifier, String lastModifiedTime)
    {
        this.setPath(path);
        this.setLastModifier(lastModifier);
        this.setLastModifiedTime(lastModifiedTime);
    }

}
