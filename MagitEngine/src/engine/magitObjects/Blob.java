package engine.magitObjects;

import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;
import java.util.Objects;
import java.util.Random;

public class Blob extends MagitObject {

    private String content;

    //from xml
    public Blob(String content) {
        this.content = content;
    }

    //from a file
    public Blob(File file) throws IOException {
        content = FileUtils.readFileToString(file, "UTF-8");
    }

    public String getContent() {
        return content;
    }



//    public Blob(File file) {
//
//        try {
//            content = FileUtils.readFileToString(file, "UTF-8");
//            System.out.println(content); //test
//        } catch (IOException e) {
//            e.printStackTrace(); // Notify in another way if not working in consoleUI
//            Random intGenerator = new Random();
//            content = "Error! couldn't get this file content!\n" +
//                    "Generated random ID: " + intGenerator.nextInt();
//            System.out.println(content); //test
//        }
//
//    }


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
