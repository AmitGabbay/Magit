package engine.magitMemoryObjects;

import org.apache.commons.codec.digest.DigestUtils;

import java.util.Map;
import java.util.Objects;
import java.util.TreeMap;

public class MagitFolder implements MagitObject, MagitParentObject {

    private Map<String, MagitObjMetadata> objects;
    private StringBuilder contentAsText;

    public MagitFolder() {
        objects = new TreeMap<>();
        contentAsText = new StringBuilder();
    }

    public void addObject(MagitObjMetadata objData) {
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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        MagitFolder that = (MagitFolder) o;
        return objects.equals(that.objects) &&
                contentAsText.equals(that.contentAsText);
    }

    @Override
    public int hashCode() {
        return Objects.hash(objects, contentAsText);
    }

    @Override
    public String toString() {
        return getTextContent();
    }
}

