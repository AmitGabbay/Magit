package engine.magitObjects;

import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.Objects;

public class Blob implements  MagitObject {

   private String content;

   private transient String path;
   private transient String lastModifier;
   private transient String lastModifiedTime;




   public Blob(File file) throws IOException {
       content = FileUtils.readFileToString(file, "UTF-8");
       System.out.println(content); //test
   }

    @Override
    public String calcSha1() {
        return DigestUtils.sha1Hex(this.content);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Blob blob = (Blob) o;
        return content.equals(blob.content);
    }

    @Override
    public int hashCode() {
        return Objects.hash(content);
    }

    @Override
    public String toString() {
        return this.content;// + " " + path;
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
