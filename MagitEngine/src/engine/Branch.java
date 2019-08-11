package engine;

import com.sun.istack.internal.NotNull;

import java.util.Objects;

/**
 *
 */
public class Branch {




    @NotNull private String name;

    private String pointedCommit;

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


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Branch branch = (Branch) o;
        return name.equals(branch.name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name);
    }

    public String getName() {
        return name;
    }

    public String getPointedCommit() {
        return pointedCommit;
    }

}

