package engine.magitMemoryObjects;

import org.apache.commons.codec.digest.DigestUtils;

import java.util.Map;
import java.util.TreeMap;

public class MagitFolder implements MagitObject, MagitParentObject {

    private Map<String, MagitObjMetadata> objects;
    private StringBuilder content;

    public MagitFolder(){
        objects = new TreeMap<>();
        content = new StringBuilder();
    }

    public void addObject(MagitObjMetadata objData)
    {
        objects.put(objData.getName(), objData);

    }

    @Override
    public String calcSha1() {
        content.setLength(0);
        for (MagitObjMetadata objData : objects.values()){
            content.append(objData.toString() + "\n");
        }
        return DigestUtils.sha1Hex(this.content.toString());
    }
}
