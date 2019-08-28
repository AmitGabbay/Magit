package engine.magitObjects;

import java.io.Serializable;

public interface MagitObject extends Serializable {
    String calcSha1();
}
