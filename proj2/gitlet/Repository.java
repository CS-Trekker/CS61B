package gitlet;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
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
    public static final File BRANCH_DIR = join(GITLET_DIR, "branches");

    // The HEAD_File is used to store the hash value of the Commit pointed to by the HEAD pointer
    public static File HEAD_File = join(GITLET_DIR, "HEAD");

    // the current branch
    public static File CUR_BRANCH = join(GITLET_DIR, "CUR_BRANCH");

    /** TODO:Generate a.gitlet folder under CWD
     *  TODO: create a Commit, and the commit message is "initial commit".The timestamp is "Thu Jan 01 08:00:00 1970 +0800".
     *  TODO: Where is it saved after the Commit is created? How to store it?
     *  TODO：What is a UID?
     */
    public static void initCommand() {
        setPersistence();

        Tree emptyTree = new Tree();
        emptyTree.saveTree();

        Commit InitialCommit = new Commit("initial commit", null, emptyTree);
        InitialCommit.saveCommit();

        updateHEAD(InitialCommit);

        Branch master = new Branch("master", InitialCommit.getHash());
        master.saveBranch();

        updateCurBRANCH(master);
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
        }
        if (stage.getStagedForRemoval().containsKey(arg)) {
            stage.getStagedForRemoval().remove(arg);
        }
        stage.saveStageArea();
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

        if (hashOfFileInHEAD != null) {
            restrictedDelete(fToBeRemoved);
        }

        if (!stage.getStagedForAddition().containsKey(arg) && hashOfFileInHEAD == null) {
            throw new GitletException("No reason to remove the file.");
        }

        stage.stageForRemoval(arg, hashOfFileToBeRemoved);

        stage.saveStageArea();
    }

    public static void commitCommand(String arg) {
        checkIfGitletExists();

        Stage stage = Stage.loadStageArea();
        Map<String, String> stageForAddition = stage.getStagedForAddition();
        Map<String, String> stageForRemoval = stage.getStagedForRemoval();

        Commit parentCommit = getHEADCommit();
        Tree newTree = parentCommit.getTree();

        // 1. Traverse all the files to be added in the staging area and update the Tree structure one by one
        for (Map.Entry<String, String> entry : stageForAddition.entrySet()) {
            String filePath = entry.getKey();
            String blobHash = entry.getValue();
            newTree = Tree.update(newTree, filePath, blobHash);
        }

        // 2. Traverse all the files to be deleted in the temporary storage area and update the Tree structure one by one
        for (String filePath : stageForRemoval.keySet()) {
            newTree = Tree.update(newTree, filePath, null);
        }

        // After a series of updates, we obtained the final state of the newTree. Save it
        newTree.saveTree();

        Commit newCommit = new Commit(arg, parentCommit, newTree);
        newCommit.saveCommit();

        writeContents(HEAD_File, newCommit.getHash());

        // After committing, the Stage area needs to be cleared
        new Stage().saveStageArea();

        Branch curBranch = getCurBranch();
        File curBranchFile = join(BRANCH_DIR, curBranch.getName());
        writeContents(curBranchFile, newCommit.getHash());
    }

    public static void logCommand() {

    }

    public static void globallogCommand() {

    }

    public static void findCommand(String arg) {

    }

    public static void statusCommand() {

    }

    public static void checkoutCommand(String[] args) {
        checkIfGitletExists();

        if (args.length == 2) {
            // java gitlet.Main checkout <Branch-name>
            File branchFile = join(BRANCH_DIR, args[1]);
            if (!branchFile.exists()) {
                throw new GitletException("No such branch exists.");
            }
            String targetCommitHash = readContentsAsString(branchFile);
            Branch targetBranch = new Branch(args[1], targetCommitHash);
            Commit targetCommit = readObject(join(COMMIT_DIR, targetCommitHash), Commit.class);

            if (targetBranch == null) {
                throw new GitletException("No such branch exists.");
            }
            if (targetBranch == getCurBranch()) {
                throw new GitletException("No need to checkout the current branch.");
            }

            /** Modification of the workspace + inspection of the temporary storage area */
            Commit currentCommit = getHEADCommit();

            Tree currentTree = currentCommit.getTree();
            Tree targetTree = targetCommit.getTree();
            Map<String, String> currentFiles = currentTree.getAllFilesInTree();
            Map<String, String> targetFiles = targetTree.getAllFilesInTree();

            // (CRITICAL) Check for untracked files that would be overwritten.
            List<String> cwdFilePaths = getAllFilePathsInCWD(CWD);
            for (String filePath : cwdFilePaths) {
                // An untracked file is one that exists in CWD but is not tracked by the current commit and is not staged for addition.
                boolean isTracked = currentFiles.containsKey(filePath);
                boolean isStaged = Stage.loadStageArea().getStagedForAddition().containsKey(filePath);
                if (!isTracked && !isStaged) {
                    // If this untracked file exists in the target branch, checking out would overwrite it.
                    if (targetFiles.containsKey(filePath)) {
                        throw new GitletException("There is an untracked file in the way; delete it, or add and commit it first.");
                    }
                }
            }

            /** modify the working area. */
            // 1. Traverse the files committed by the target and restore all of them to the workspace
            for (Map.Entry<String, String> entry : targetFiles.entrySet()) {
                String filePath = entry.getKey();
                String blobHash = entry.getValue();
                File file = join(CWD, filePath);

                // Make sure the parent directory exists
                if (file.getParentFile() != null) {
                    file.getParentFile().mkdirs();
                }

                Blob blob = readObject(join(BLOB_DIR, blobHash), Blob.class);
                writeContents(file, blob.getContent());
            }

            // 2. Traverse the currently committed file. If it is not in the target commit, delete it
            for (String filePath : currentFiles.keySet()) {
                if (!targetFiles.containsKey(filePath)) {
                    restrictedDelete(join(CWD, filePath));
                }
            }

            // Clear the staging area.
            new Stage().saveStageArea();

            updateCurBRANCH(targetBranch);
            updateHEAD(targetCommit);
        } else if (args.length == 3 && args[1].equals("--")) {
            // java gitlet.Main checkout -- <File-name>
            String fileName = args[2];
            Tree HEAD_Tree = getHEADTree();
            String blobHash = HEAD_Tree.getHashOfFile(fileName);
            if (blobHash == null) {
                throw new GitletException("File does not exist in that commit.");
            } else {
                Blob blobToWrite = readObject(join(BLOB_DIR, blobHash), Blob.class);
                File fileToWrite = join(CWD, fileName);
                writeContents(fileToWrite, blobToWrite.getContent());
            }
        } else if (args.length == 4 && args[2].equals("--")) {
            // java gitlet.Main checkout dj2kj3 -- a.txt
            String commitHash = args[1];
            String fileName = args[3];
            File commitFile = join(COMMIT_DIR, commitHash);
            if (!commitFile.exists()) {
                throw new GitletException("No commit with that id exists.");
            }

            String fullCommitHash = null;
            List<String> allCommitHashes = plainFilenamesIn(COMMIT_DIR);
            if (allCommitHashes != null) {
                for (String hash : allCommitHashes) {
                    if (hash.startsWith(commitHash)) {
                        fullCommitHash = hash;
                        break;
                    }
                }
            }

            if (fullCommitHash == null) {
                throw new GitletException("No commit with that id exists.");
            }

            Commit targetCommit = readObject(join(COMMIT_DIR, fullCommitHash), Commit.class);
            Tree targetTree = targetCommit.getTree();

            String blobHash = targetTree.getHashOfFile(fileName);

            if (blobHash == null) {
                throw new GitletException("File does not exist in that commit.");
            }

            Blob blobToWrite = readObject(join(BLOB_DIR, blobHash), Blob.class);
            File fileToWrite = join(CWD, fileName);
            writeContents(fileToWrite, blobToWrite.getContent());
        }
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

    public static void checkIfGitletExists() {
        if (!GITLET_DIR.exists()) {
            throw new RuntimeException("Not in an initialized Gitlet directory.");
        }
    }

    public static Commit getHEADCommit() {
        return readObject(join(COMMIT_DIR, readContentsAsString(HEAD_File)), Commit.class);
    }

    public static Tree getHEADTree() {
        Commit HEAD = getHEADCommit();
        return HEAD.getTree();
    }

    public static Branch getCurBranch() {
        String curBranchName = readContentsAsString(CUR_BRANCH);
        File branchFile = join(BRANCH_DIR, curBranchName);
        String commitHash = readContentsAsString(branchFile);
        return new Branch(curBranchName, commitHash);
    }

    public static void updateHEAD(Commit c) {
        writeContents(HEAD_File, c.getHash());
    }

    public static void updateCurBRANCH(Branch b) {
        writeContents(CUR_BRANCH, b.getName());
    }

    /**
     * Recursively get all file paths in the working directory relative to CWD.
     * @param dir The directory to start from (usually CWD).
     * @return A list of relative file paths, using '/' as the separator.
     */
    private static List<String> getAllFilePathsInCWD(File dir) {
        List<String> filePaths = new ArrayList<>();
        if (dir == null || !dir.isDirectory()) {
            return filePaths;
        }
        // The absolute path of CWD, used to calculate the relative path.
        String cwdPath = CWD.getAbsolutePath();
        listFilesRecursive(dir, cwdPath, filePaths);
        return filePaths;
    }

    private static void listFilesRecursive(File currentDir, String basePath, List<String> filePaths) {
        File[] files = currentDir.listFiles();
        if (files == null) {
            return;
        }

        for (File file : files) {
            // Ignore the .gitlet directory itself.
            if (file.getName().equals(".gitlet")) {
                continue;
            }

            if (file.isDirectory()) {
                listFilesRecursive(file, basePath, filePaths);
            } else {
                String absolutePath = file.getAbsolutePath();
                // Add 1 to remove the leading path separator.
                String relativePath = absolutePath.substring(basePath.length() + 1);
                // Standardize to use '/' as the separator for cross-platform compatibility.
                filePaths.add(relativePath.replace('\\', '/'));
            }
        }
    }
}
