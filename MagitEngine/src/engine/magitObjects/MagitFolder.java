package engine.magitObjects;

import org.apache.commons.codec.digest.DigestUtils;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.util.*;

public class MagitFolder extends MagitObject {

    private final Map<String, MagitObjMetadata> objects;
    private transient StringBuilder contentAsText;

    public MagitFolder() {
        objects = new TreeMap<>();
        contentAsText = new StringBuilder();
    }

    public void addObjectData(MagitObjMetadata objData) {
        objects.put(objData.getName(), objData);
    }

    public String getTextContent() {
        updateTextContent();
        return contentAsText.toString();
    }

    @Override
    public String calcSha1() {
        updateTextContent();
        return DigestUtils.sha1Hex(contentAsText.toString());
    }

    private void updateTextContent() {
        contentAsText.setLength(0);
        for (MagitObjMetadata objData : objects.values()) {
            contentAsText.append(objData.toString() + "\n");
        }
    }

    public Collection<MagitObjMetadata> getObjectsValues() {
        return objects.values();
    }

    public MagitObjMetadata getObjMetadataByName(String objName) {
        return objects.get(objName);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        MagitFolder that = (MagitFolder) o;
        return Objects.equals(objects, that.objects) &&
                Objects.equals(contentAsText, that.contentAsText);
    }

    @Override
    public int hashCode() {
        return Objects.hash(objects, contentAsText);
    }

    @Override
    public String toString() {
        return getTextContent();
    }

    /**
     * Overrides default readObject method in the serialization mechanism to restore a MagitFolder correctly
     */
    private void readObject(ObjectInputStream ois) throws ClassNotFoundException, IOException {
        ois.defaultReadObject();
        this.contentAsText = new StringBuilder();
    }

}

