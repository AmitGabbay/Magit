package engine.magitObjects;

import engine.MagitParentObject;
import org.apache.commons.codec.digest.DigestUtils;

import java.nio.file.Path;
import java.util.*;

public class MagitFolder implements MagitObject, MagitParentObject {

    private final Map<String, MagitObjMetadata> objects;
    private final transient StringBuilder contentAsText;

    private transient String path;
    private transient String lastModifier;
    private transient String lastModifiedTime;

    public MagitFolder() {
        objects = new TreeMap<>();
        contentAsText = new StringBuilder();
    }

    public void addObjectData(MagitObjMetadata objData) {
        objects.put(objData.getName(), objData);
    }

    public String getTextContent(){
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
              //  + " " + path;
    }


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

