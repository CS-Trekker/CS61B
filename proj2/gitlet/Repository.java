package gitlet;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

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

    /** The .gitlet/blobs directory. */
    public static final File BLOB_DIR = join(GITLET_DIR, "blobs");

    /** The .gitlet/trees directory. */
    public static final File TREE_DIR = join(GITLET_DIR, "trees");

    /** The .gitlet/stage FILE. */
    public static final File STAGE_FILE = join(GITLET_DIR, "stage");

    /** The .gitlet/branch directory. */
    public static final File BRANCH_DIR = join(GITLET_DIR, "branch");

    // The HEAD_File is used to store the hash value of the Commit pointed to by the HEAD pointer
    public static File HEAD_File = join(GITLET_DIR, "HEAD");


    /** TODO:Generate a.gitlet folder under CWD
     *  TODO: create a Commit, and the commit message is "initial commit".The timestamp is "Thu Jan 01 08:00:00 1970 +0800".
     *  TODO: Where is it saved after the Commit is created? How to store it?
     *  TODO：What is a UID?
     */
    public static void initCommand() {
        setPersistence();

        Commit InitialCommit = new Commit("initial commit", null, null);
        InitialCommit.saveCommit();

        writeContents(HEAD_File, InitialCommit.getHash());

        Branch master = new Branch("master", InitialCommit);
        master.saveBranch();
    }

    public static void addCommand(String arg) {
        checkIfGitletExists();

        // arg is actually the relative path of the file to be added relative to CWD
        File fToBeAdded = join(CWD, arg);

        if (!fToBeAdded.exists()) {
            throw new GitletException("File does not exist.");
        }
        String hashOfFileToBeAdded = sha1(readContents(fToBeAdded));

        // In addition to saving the hash value of the file to be added into the staging area, the content of the file should also be saved under.gitlet/blobs
        Blob blobOfFileToAdd = new Blob(fToBeAdded);
        blobOfFileToAdd.saveBlob();

        Tree HEAD_Tree = getHEADTree();

        String hashOfFileInHEAD = HEAD_Tree.getHashOfFile(arg);

        Stage stage = Stage.loadStageArea();
        if (hashOfFileInHEAD == null || !hashOfFileInHEAD.equals(hashOfFileToBeAdded)) {
            stage.stageForAddition(arg, hashOfFileToBeAdded);
            stage.saveStageArea();
        }
        if (stage.getStagedForRemoval().containsKey(arg)) {
            stage.getStagedForRemoval().remove(arg);
            stage.saveStageArea();
        }
    }

    // Delete the file simultaneously from the add area and the workspace of the staging area
    public static void rmCommand(String arg) {
        checkIfGitletExists();

        File fToBeRemoved = join(CWD, arg);

        String hashOfFileToBeRemoved = sha1(readContents(fToBeRemoved));

        Tree HEAD_Tree = getHEADTree();

        String hashOfFileInHEAD = HEAD_Tree.getHashOfFile(arg);

        Stage stage = Stage.loadStageArea();
        if (stage.getStagedForAddition().containsKey(arg)) {
            stage.getStagedForAddition().remove(arg);
        }

        if (!stage.getStagedForAddition().containsKey(arg) && hashOfFileInHEAD == null) {
            throw new GitletException("No reason to remove the file.");
        }

        stage.getStagedForRemoval().put(arg, hashOfFileToBeRemoved);
    }

    public static void commitCommand(String arg) {
        Tree HEAD_Tree = getHEADTree();

        Stage stage = Stage.loadStageArea();

        Map<String, String> stageForAddition = stage.getStagedForAddition();
        Map<String, String> stageForRemoval = stage.getStagedForRemoval();

        Map<String, String> newBlobs = new HashMap<>(HEAD_Tree.getBlobs());

        for (Map.Entry<String, String> entry : stageForAddition.entrySet()) {
            String fileName = entry.getKey();
            String blobHash = entry.getValue();
            // Put the file and its blob hash into the blobs hash table of the new Tree
            // If the file already exists, this will update its hash value; If it doesn't exist, it will be added.
            newBlobs.put(fileName, blobHash);
        }
        for (String fileName : stageForRemoval.keySet()) {
            // Remove this file from the blobs hash table of the new Tree
            newBlobs.remove(fileName);
        }

        Tree newTree = null;

        Commit newCommit = new Commit(arg, getHEADCommit(), newTree);
    }

    public static void logCommand() {

    }

    public static void globallogCommand() {

    }

    public static void findCommand(String arg) {

    }

    public static void statusCommand() {

    }

    public static void checkoutCommand(String arg) {

    }

    public static void branchCommand(String arg) {

    }

    public static void rmbranchCommand(String arg) {

    }

    public static void resetCommand(String arg) {

    }

    public static void mergeCommand(String arg) {

    }

    public static void setPersistence() {
        if (GITLET_DIR.exists()) {
            throw new GitletException("A Gitlet version-control system already exists in the current directory.");
        }
        GITLET_DIR.mkdir();

        if (!COMMIT_DIR.exists()) {
            COMMIT_DIR.mkdir();
        }

        if (!BLOB_DIR.exists()) {
            BLOB_DIR.mkdir();
        }

        if (!TREE_DIR.exists()) {
            TREE_DIR.mkdir();
        }

        if (!STAGE_FILE.exists()) {
            try {
                STAGE_FILE.createNewFile();
            }
            catch (IOException ignore) {
            }
        }

        if (!HEAD_File.exists()) {
            try {
                HEAD_File.createNewFile();
            }
            catch (IOException ignore) {
            }
        }

        if (!BRANCH_DIR.exists()) {
            BRANCH_DIR.mkdir();
        }
    }

    public static Commit getHEADCommit() {
        return readObject(join(COMMIT_DIR, readContentsAsString(HEAD_File)), Commit.class);
    }

    public static Tree getHEADTree() {
        Commit HEAD = getHEADCommit();
        return HEAD.getTree();
    }

    public static void checkIfGitletExists() {
        if (!GITLET_DIR.exists()) {
            throw new RuntimeException("Not in an initialized Gitlet directory.");
        }
    }
}
