package engine.repo;

import com.sun.istack.internal.NotNull;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;
import java.util.Objects;

/**
 *
 */
public class Branch {

    @NotNull private final String name;
    @NotNull private String pointedCommit;

    private boolean isRemote;
    private boolean tracking;
    private String trackingAfter;

    
    public Branch(String name, String pointedCommit) throws Exception {

        if (name.equalsIgnoreCase("HEAD"))
            throw new Exception("HEAD file overriding is not allowed!!!");

        this.name = name;
        this.pointedCommit = pointedCommit;
        this.isRemote = false;
        this.tracking = false;
    }

    public Branch(File branchFile) throws IOException {
        this.name=branchFile.getName();
        this.pointedCommit= FileUtils.readFileToString(branchFile, "UTF-8");
        this.isRemote = false;
        this.tracking = false;
    }
    
    /**
     private ct'or to create initial master branch
     */
    private Branch() {
        this.name = "master";
        this.pointedCommit="";
        this.isRemote = false;
        this.tracking = false;
    }

    public static Branch createBlankMasterBranch(){
        return new Branch();
    }


    public String getName() {
        return name;
    }

    public String getPointedCommitSha1() {
        return pointedCommit;
    }

    public void setPointedCommit(String pointedCommit) {
        this.pointedCommit = pointedCommit;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Branch branch = (Branch) o;
        return isRemote == branch.isRemote &&
                tracking == branch.tracking &&
                name.equals(branch.name) &&
                pointedCommit.equals(branch.pointedCommit) &&
                Objects.equals(trackingAfter, branch.trackingAfter);
    }

    @Override
    public String toString() {
        return "Branch name: " + name +
                " \tPointed Commit: " + pointedCommit;
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, pointedCommit, isRemote, tracking, trackingAfter);
    }
}

