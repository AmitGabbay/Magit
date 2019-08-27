package engine.magitMemoryObjects;

import java.io.Serializable;

public interface MagitObject extends Serializable {
    String calcSha1();
}
