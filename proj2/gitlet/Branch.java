package gitlet;

import java.io.Serializable;
import static gitlet.Utils.*;

public class Branch implements Serializable {
    private String name;
    private Commit commit;

    public Branch(String n, Commit c) {
        name = n;
        commit = c;
    }

    public String getName() {
        return name;
    }

    public String getCommitId() {
        return commit.getHash();
    }

    public void saveBranch() {
        writeObject(Utils.join(Repository.BRANCH_DIR, getName()),this);
    }
}
