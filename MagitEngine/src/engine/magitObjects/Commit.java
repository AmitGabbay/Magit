package engine.magitObjects;

import org.apache.commons.codec.digest.DigestUtils;

public class Commit implements Sha1Able {

    private final String rootFolderSha1;
    private String parentCommitSha1;
    private String anotherParentCommitSha1 = null;
    private final String description;
    private final String creationTime;
    private final String author;

    private transient String content;

    public Commit(String rootFolderSha1, String parentCommitSha1, String description, String creationTime, String author) {

        this.rootFolderSha1 = rootFolderSha1;
        this.parentCommitSha1 = parentCommitSha1;
        // this.anotherParentCommitSha1 =
        this.description = description;
        this.creationTime = creationTime;
        this.author = author;

        this.content = calcContent();
    }

    private String calcContent(){
        return (this.rootFolderSha1 + this.parentCommitSha1 + this.anotherParentCommitSha1 + this.description + this.creationTime + this.author);
    }

    public String getRootFolderSha1() {
        return rootFolderSha1;
    }

    @Override
    public String calcSha1() {

        this.content = calcContent();
        return DigestUtils.sha1Hex(this.content);
    }

    @Override
    public String toString() {
        return "Commit{" +
                "rootFolderSha1='" + rootFolderSha1 + '\'' +
                ", parentCommitSha1='" + parentCommitSha1 + '\'' +
                ", anotherParentCommitSha1='" + anotherParentCommitSha1 + '\'' +
                ", description='" + description + '\'' +
                ", creationTime='" + creationTime + '\'' +
                ", author='" + author + '\'' +
                ", content='" + content + '\'' +
                '}';
    }



}
