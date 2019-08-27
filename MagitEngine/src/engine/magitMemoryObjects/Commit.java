package engine.magitMemoryObjects;

import org.apache.commons.codec.digest.DigestUtils;

public class Commit implements MagitObject, MagitParentObject {

    private String rootFolderSha1;
    private String parentCommitSha1;
    private String anotherParentCommitSha1 = null;
    private String description;
    private String creationTime;
    private String author;

    private String content;

    public Commit(String rootFolderSha1, String parentCommitSha1, String description, String author, String creationTime) {

        this.rootFolderSha1 = rootFolderSha1;
        this.parentCommitSha1 = parentCommitSha1;
        this.description = description;
        this.creationTime = creationTime;
        this.author = author;

        this.content = calcContent();
    }

    private String calcContent(){
        return new StringBuilder(this.rootFolderSha1 + this.parentCommitSha1 +this.anotherParentCommitSha1+ this.description + this.creationTime + this.author).toString();
    }

    @Override
    public String calcSha1() {

        //calcContent();
        return DigestUtils.sha1Hex(this.content);
    }

    public String getRootFolderSha1() {
        return rootFolderSha1;
    }
}
