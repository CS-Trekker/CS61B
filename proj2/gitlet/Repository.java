package gitlet;

import java.io.File;
import static gitlet.Utils.*;

// TODO: any imports you need here

/** Represents a gitlet repository.
 *  TODO: It's a good idea to give a description here of what else this Class does at a high level.
 *
 *  @author CS-Trekker
 */
public class Repository {
    /**
     * TODO: add instance variables here.
     *
     * List all instance variables of the Repository class here with a useful comment above them describing what that variable represents and how that variable is used. We've provided two examples for you.
     */

    /** the directory structure of .gitlet:
     * .gitlet/
     *      - commits/ - The following are all serialized files of commit objects named with hash values (assumed)
     *      - blobs
     *      */

    /** The current working directory. */
    public static final File CWD = new File(System.getProperty("user.dir"));
    /** The .gitlet directory. */
    public static final File GITLET_DIR = join(CWD, ".gitlet");

    /** The .gitlet/commits directory. */
    public static final File COMMIT_DIR = join(GITLET_DIR, "commits");

    /** TODO:Generate a.gitlet folder under CWD
     *  TODO: create a Commit, and the commit message is "initial commit".The timestamp is "Thu Jan 01 08:00:00 1970 +0800".
     *  TODO: Where is it saved after the Commit is created? How to store it?
     *  TODO：What is a UID?
     */
    public static void initCommand() {
        if (GITLET_DIR.exists()) {
            throw new GitletException("A Gitlet version-control system already exists in the current directory.");
        }
        GITLET_DIR.mkdir();

        if (!Commit.COMMIT_DIR.exists()) {
            Commit.COMMIT_DIR.mkdir();
        }
    }

    /* TODO: fill in the rest of this class. */
}
