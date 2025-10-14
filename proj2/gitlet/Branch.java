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

    public void saveBranch() {
        writeContents(Utils.join(Repository.BRANCH_DIR, getName()), commitHash);
    }
}
