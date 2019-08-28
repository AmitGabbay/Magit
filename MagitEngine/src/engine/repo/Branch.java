package engine.repo;

import com.sun.istack.internal.NotNull;

import java.util.Objects;

/**
 *
 */
public class Branch {

    @NotNull private String name;

    @NotNull private String pointedCommit;

    private boolean isRemote;

    private boolean tracking;
    private String trackingAfter;
    /**
     private ct'or to create initial master branch
     */
    private Branch() {
        this.name = "master";
        this.pointedCommit="";
        this.isRemote = false;
        this.tracking=false;
    }

    public static Branch createMasterBranch(){
        return new Branch();
    }


    public String getName() {
        return name;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Branch branch = (Branch) o;
        return name.equals(branch.name) &&
                pointedCommit.equals(branch.pointedCommit) &&
                Objects.equals(trackingAfter, branch.trackingAfter);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, pointedCommit, trackingAfter);
    }

    public String getPointedCommit() {
        return pointedCommit;
    }

    public void setPointedCommit(String pointedCommit) {
        this.pointedCommit = pointedCommit;
    }

}

