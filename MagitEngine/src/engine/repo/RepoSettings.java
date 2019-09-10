package engine.repo;

import java.io.Serializable;

/**

 */
public class RepoSettings implements Serializable {

    private String name;
    private String stringPath; // ==location
    private RemoteRepo remoteRepo;
    private String headBranch = "master";

    public RepoSettings(String name, String path) {
        this.name = name;
        this.stringPath = path;
    }

    // TODO: 06/08/2019  c'tors for repo from xml/directory
    
    
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
