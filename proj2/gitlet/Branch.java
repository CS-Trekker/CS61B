package gitlet;

import java.io.Serializable;
import static gitlet.Utils.*;
import gitlet.Utils.*;

// Branch names the file with its own name, and the hash value of the Commit it points to is saved in the file
public class Branch implements Serializable {
    private String name;
    private String commitHash;

    public Branch(String n, String c) {
        name = n;
        commitHash = c;
    }

    public String getName() {
        return name;
    }

    public String getCommitHash() {
        return commitHash;
    }

    public String getHash() {
        return sha1(serialize(this));
    }

    public void saveBranch() {
        writeContents(Utils.join(Repository.BRANCH_DIR, getName()), commitHash);
    }

    public void updateBranch(Commit newCommit) {
        commitHash = newCommit.getHash();
    }
}
