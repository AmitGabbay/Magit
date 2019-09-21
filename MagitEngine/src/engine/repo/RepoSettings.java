package engine.repo;

import java.io.Serializable;

public class RepoSettings implements Serializable {

    private final String name;
    private final String stringPath; // ==location
    private RemoteRepo remoteRepo;
    private String headBranch;


    public RepoSettings(String name, String stringPath, String headBranch) {
        this.name = name;
        this.stringPath = stringPath;
        this.headBranch = headBranch;
    }

    public String getName() {
        return name;
    }

    public String getStringPath() {
        return stringPath;
    }


    public RemoteRepo getRemoteRepo() {
        return remoteRepo;
    }

    public void setRemoteRepo(RemoteRepo remoteRepo) {
        this.remoteRepo = remoteRepo;
    }

    public String getHeadBranch() {
        return headBranch;
    }

    public void setHeadBranch(String headBranch) {
        this.headBranch = headBranch;
    }
}
