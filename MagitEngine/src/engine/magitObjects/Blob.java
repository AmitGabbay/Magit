package engine.magitObjects;

import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;
import java.util.Objects;

public class Blob extends MagitObject {

    private String content;

    //from xml or txt file
    public Blob(String content) {
        this.content = content;
    }

    //from a blob file
    public Blob(File file) throws IOException {
        content = FileUtils.readFileToString(file, "UTF-8");
    }

    public String getContent() {
        return content;
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

}
