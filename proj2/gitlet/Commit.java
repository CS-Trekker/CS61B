package gitlet;

// TODO: any imports you need here

import java.io.File;
import static gitlet.Utils.*;
import java.io.Serializable;
import java.util.Date; // TODO: You'll likely use this in this class
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Locale;
import java.util.ArrayList;

/** Represents a gitlet commit object.
 *  A Commit encapsulates a snapshot of the repository state at a particular point in time,
 *  including the commit message, timestamp, parent commit(s), and the tree representing the file structure.
 *
 *  @author CS-Trekker
 */
public class Commit implements Serializable {
    /**
     * Instance variables for the Commit class.
     * Each variable is described below with its purpose and usage.
     */

    /** The message of this Commit. */
    private String message;

    /** The timestamp when this commit was created. */
    private String timeStamp;

    /** The parent commit. For the initial commit, this is null. */
    private Commit parent;

    /** The second parent commit, used for merge commits. For regular commits, this is null. */
    private Commit secondParent = null;

    /** The tree object representing the file structure at this commit. */
    private Tree tree;

    /**
     * Cached hash value of this commit.
     * Once computed at construction time, this value never changes.
     * This ensures that the commit's identity is stable and immutable.
     */
    private String cachedHash;

    public Commit(String m, Commit p, Tree t) {
        message = m;
        parent = p;
        tree = t;

        if (parent == null) {
            timeStamp = "Thu Jan 01 08:00:00 1970 +0800";
            message = "initial commit";
        } else {
            // Get current time once and reuse it
            ZonedDateTime now = ZonedDateTime.now();
            // define the format, which is similar to the timestamp in git
            DateTimeFormatter gitFormatter = DateTimeFormatter.ofPattern("EEE MMM dd HH:mm:ss yyyy Z", Locale.ENGLISH);
            // Format as a string
            String gitTime = now.format(gitFormatter);

            timeStamp = gitTime;
        }

        // Fix: Calculate and cache the hash immediately in the constructor
        // This ensures:
        // 1. hash is calculated only once, which results in better performance
        // 2. hash will never change, even if the tree object is modified later
        // 3. The deserialized commit also has the same hash
        this.cachedHash = sha1(serialize(this));
    }

    public String getMessage() {
        return message;
    }

    public String getTimeStamp() {
        return timeStamp;
    }

    public Commit getParent() {
        return parent;
    }

    public Commit getSecondParent() {
        return secondParent;
    }

    public Tree getTree() {
        return tree;
    }

    /**
     * Fix: Return the cached hash instead of recalculating each time
     * This is a crucial fix!
     */
    public String getHash() {
        return cachedHash;
    }

    /**
     * First serialize the Commit object, then calculate the hash, and finally save it in ".gitlet/commits/ hash value ".
     */
    public void saveCommit() {
        writeObject(Utils.join(Repository.COMMIT_DIR, this.getHash()), this);
    }
}