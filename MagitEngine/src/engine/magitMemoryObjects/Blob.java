package engine.magitMemoryObjects;

import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;
import java.util.Objects;

public class Blob implements  MagitObject {

   private String content;

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
}
