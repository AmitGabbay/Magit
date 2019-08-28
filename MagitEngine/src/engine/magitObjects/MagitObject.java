package engine.magitObjects;

import java.io.Serializable;

public interface MagitObject extends Serializable {
    String calcSha1();

    //todo move that all to inheritance
    String getPath();
    void setPath(String path);

    String getLastModifier();
    void setLastModifier(String lastModifier);

    String getLastModifiedTime();
    void setLastModifiedTime(String lastModifiedTime);

    void setHelperFields(String path, String lastModifier, String lastModifiedTime);
}
