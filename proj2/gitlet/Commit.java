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
 *  TODO: It's a good idea to give a description here of what else this Class does at a high level.
 *
 *  @author CS-Trekker
 */
public class Commit implements Serializable {
    /**
     * TODO: add instance variables here.
     *
     * List all instance variables of the Commit class here with a useful comment above them describing what that variable represents and how that variable is used. We've provided one example for `message`.
     */

    /** The message of this Commit. */
    private String message;

    private String timeStamp;

    private Commit parent;

    private Commit secondParent = null;

    private ArrayList<Commit> child = new ArrayList<>();

    private Tree tree;

    public Commit(String m, Commit p, Tree t) {
        message = m;
        parent = p;
        tree = t;

        if (parent == null) {
            timeStamp = "Thu Jan 01 08:00:00 1970 +0800";
            message = "initial commit";
            tree = new Tree();
        } else {
            p.addChild(this);

            // currentTime
            ZonedDateTime now = ZonedDateTime.now();
            // define the format, which is similar to the timestamp in git
            DateTimeFormatter gitFormatter = DateTimeFormatter.ofPattern("EEE MMM dd HH:mm:ss yyyy Z", Locale.ENGLISH);
            // Format as a string
            String gitTime = now.format(gitFormatter);

            timeStamp = gitTime;
        }
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

    public void addChild(Commit c) {
        child.add(c);
    }

    public boolean ifCommonAncestor() {
        if (child.size() > 1) {
            for (Commit c : child) {
                if (c.ifCommonAncestor()) {
                    return false;
                }
            }
            return true;
        }
        return false;
    }

    public String getHash() {
        return sha1(serialize(this));
    }

    // First serialize the Commit object, then calculate the hash, and finally save it in ".gitlet/commits/ hash value ".
    public void saveCommit() {
        writeObject(Utils.join(Repository.COMMIT_DIR, this.getHash()),this);
    }
}
