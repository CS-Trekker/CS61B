package gitlet;

import java.io.File;
import java.io.IOException;
import java.util.*;

import static gitlet.Utils.*;

/** Represents a gitlet repository.
 *  Main负责读入命令名和参数，Repository用来执行处理
 *  @author CS-Trekker
 */
public class Repository {
    /**
     * TODO: add instance variables here.
     *
     * List all instance variables of the Repository class here with a useful comment above them describing what that variable represents and how that variable is used. We've provided two examples for you.
     */

    /** The current working directory. */
    public static final File CWD = new File(System.getProperty("user.dir"));
    public static final File GITLET_DIR = join(CWD, ".gitlet");
    public static final File COMMIT_DIR = join(GITLET_DIR, "commits");
    public static final File BLOB_DIR = join(GITLET_DIR, "blobs");
    public static final File TREE_DIR = join(GITLET_DIR, "trees");
    public static final File STAGE_FILE = join(GITLET_DIR, "stage");
    public static final File BRANCH_DIR = join(GITLET_DIR, "branches");
    public static File HEAD_File = join(GITLET_DIR, "HEAD");

    public static void initCommand() {
        if (GITLET_DIR.exists()) {
            System.out.println("A Gitlet version-control system already exists in the current directory.");
            return;
        }

        setPersistence();

        Tree emptyTree = new Tree();
        emptyTree.saveTree();
        Commit InitialCommit = new Commit("initial commit", null, emptyTree);
        InitialCommit.saveCommit();
        Branch master = new Branch("master", InitialCommit.getHash());
        master.saveBranch();

        switchHEAD("master");
        updateHEADBranch(InitialCommit);
    }

    public static void addCommand(String arg) {
        checkIfGitletExists();

        // arg is actually the relative path of the file to be added relative to CWD
        File fToBeAdded = join(CWD, arg);
        if (!fToBeAdded.exists()) {
            System.out.println("File does not exist.");
            return;
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

    /*
    1、已被HEADCommit跟踪，工作目录中存在，删除工作目录中的文件
    2、未被HEADCommit跟踪，工作目录中存在，保留工作目录中的文件
    已被跟踪，则放入stageForRemoval区（不管工作区中存不存在）
    如果在stageForAddition区存在，则删去
     */
    public static void rmCommand(String arg) {
        checkIfGitletExists();
        File fToBeRemoved = join(CWD, arg);
        String hashOfFileToBeRemoved = null;
        if (fToBeRemoved.exists()) {
            hashOfFileToBeRemoved = sha1(readContents(fToBeRemoved));
        }
        Tree HEAD_Tree = getHEADTree();
        String hashOfFileInHEAD = HEAD_Tree.getHashOfFile(arg);
        Stage stage = Stage.loadStageArea();

        boolean ifTracked = hashOfFileInHEAD != null;
        boolean ifStaged = stage.getStagedForAddition().containsKey(arg);


        // The file does not exist in the add area, nor does it exist in the HEADCommit
        if (!ifStaged && !ifTracked) {
            System.out.println("No reason to remove the file.");
        }
        if (ifStaged) {
            stage.getStagedForAddition().remove(arg);
        }
        if (ifTracked) {
            stage.stageForRemoval(arg, hashOfFileToBeRemoved);
            if (fToBeRemoved.exists()) {
                restrictedDelete(fToBeRemoved);
            }
        }
        stage.saveStageArea();
    }

    public static void commitCommand(String arg) {
        checkIfGitletExists();

        if (arg.equals("")) {
            System.out.println("Please enter a commit message.");
            return;
        }

        Stage stage = Stage.loadStageArea();
        Map<String, String> stageForAddition = stage.getStagedForAddition();
        Map<String, String> stageForRemoval = stage.getStagedForRemoval();

        if (stageForAddition.isEmpty() && stageForRemoval.isEmpty()) {
            System.out.println("No changes added to the commit.");
            return;
        }

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

        updateHEADBranch(newCommit);

        // After committing, the Stage area needs to be cleared
        new Stage().saveStageArea();
    }

    public static void logCommand() {
        checkIfGitletExists();

        Commit curCommit = getHEADCommit();
        while (curCommit.getParent() != null) {
            System.out.println("===");
            printInfoOfCommit(curCommit);
            System.out.println();
            curCommit = curCommit.getParent();
        }
        System.out.println("===");
        printInfoOfCommit(curCommit);
    }

    public static void globallogCommand() {
        checkIfGitletExists();

        List<String> HashOfCommits =  plainFilenamesIn(COMMIT_DIR);
        for (String hash : HashOfCommits) {
            Commit c = readObject(join(COMMIT_DIR, hash), Commit.class);
            System.out.println("===");
            printInfoOfCommit(c);
            System.out.println();
        }
    }

    public static void findCommand(String arg) {
        checkIfGitletExists();

        List<String> HashOfCommits =  plainFilenamesIn(COMMIT_DIR);
        boolean flag = false;
        for (String hash : HashOfCommits) {
            Commit c = readObject(join(COMMIT_DIR, hash), Commit.class);
            if (c.getMessage().equals(arg)) {
                System.out.println(c.getHash());
                flag = true;
            }
        }
        if (!flag) {
            System.out.println("Found no commit with that message.");
        }
    }

    public static void statusCommand() {
        checkIfGitletExists();

        System.out.println("=== Branches ===");
        List<String> branchNames = plainFilenamesIn(BRANCH_DIR);
        String headBranchName = getHEADBranchName();
        branchNames.sort(null);
        for (String branchName : branchNames) {
            if (branchName.equals(headBranchName)) {
                System.out.print("*");
            }
            System.out.println(branchName);
        }
        System.out.println();

        System.out.println("=== Staged Files ===");
        Stage stage = Stage.loadStageArea();
        Map<String, String> stageForAddition = stage.getStagedForAddition();
        List<String> filesStagedForAddition = new ArrayList<>(stageForAddition.keySet());
        filesStagedForAddition.sort(null);
        for (String filePath : filesStagedForAddition) {
            System.out.println(filePath);
        }
        System.out.println();

        System.out.println("=== Removed Files ===");
        Map<String, String> stageForRemoval = stage.getStagedForRemoval();
        List<String> filesStagedForRemoval = new ArrayList<>(stageForRemoval.keySet());
        filesStagedForAddition.sort(null);
        for (String filePath : filesStagedForRemoval) {
            System.out.println(filePath);
        }
        System.out.println();

        System.out.println("=== Modifications Not Staged For Commit ===");
        Commit headCommit = getHEADCommit();
        Map<String, String> trackedFiles = headCommit.getTree().getAllFilesInTree();
        List<String> cwdFilesList = getAllFilePathsInCWD(CWD);
        Set<String> cwdFilesSet = new HashSet<>(cwdFilesList);
        SortedSet<String> modifications = new TreeSet<>();

        Set<String> allFilesToCheck = new HashSet<>(trackedFiles.keySet());
        allFilesToCheck.addAll(stageForAddition.keySet());

        for (String filePath : allFilesToCheck) {
            String trackedHash = trackedFiles.get(filePath);
            String stagedAddHash = stageForAddition.get(filePath);
            boolean existsInCwd = cwdFilesSet.contains(filePath);

            if (!existsInCwd) {
                // Case 1: The file was deleted, but it was not temporarily saved and deleted
                if (!stageForRemoval.containsKey(filePath)) {
                    modifications.add(filePath + " (deleted)");
                }
            } else {
                // Case 2: The file exists. Check if the content has been modified
                String cwdHash = sha1(readContents(join(CWD, filePath)));

                // Subcase 2.1: The file was temporarily stored, but it was modified after being temporarily stored
                if (stagedAddHash != null && !stagedAddHash.equals(cwdHash)) {
                    modifications.add(filePath + " (modified)");
                }
                // Subcase 2.2: The file is being tracked but not temporarily stored, and its content is inconsistent with that in the HEAD commit
                else if (trackedHash != null && stagedAddHash == null && !trackedHash.equals(cwdHash)) {
                    modifications.add(filePath + " (modified)");
                }
            }
        }
        for (String mod : modifications) {
            System.out.println(mod);
        }
        System.out.println();

        System.out.println("=== Untracked Files ===");
        // 1. Obtain a collection of tracked files and temporarily stored files for quick search
        Set<String> trackedFilesSet = trackedFiles.keySet();
        Set<String> stagedForAdditionSet = stageForAddition.keySet();

        // 2. Search for the set: It exists in the workspace but is neither tracked nor in the staging area
        List<String> untrackedFiles = new ArrayList<>();
        for (String filePath : cwdFilesList) {
            if (!trackedFilesSet.contains(filePath) && !stagedForAdditionSet.contains(filePath)) {
                untrackedFiles.add(filePath);
            }
        }

        // 3. sort and print
        untrackedFiles.sort(null);
        for (String untrackedFile : untrackedFiles) {
            System.out.println(untrackedFile);
        }
        System.out.println();
    }

    public static void checkoutCommand(String[] args) {
        checkIfGitletExists();

        if (args.length == 2) {
            // java gitlet.Main checkout <Branch-name>
            File branchFile = join(BRANCH_DIR, args[1]);
            if (!branchFile.exists()) {
                System.out.println("No such branch exists.");
                return;
            }
            String targetCommitHash = readContentsAsString(branchFile);
            String targetBranchName = args[1];
            Commit targetCommit = readObject(join(COMMIT_DIR, targetCommitHash), Commit.class);

            if (!join(BRANCH_DIR, targetBranchName).exists()) {
                System.out.println("No such branch exists.");
                return;
            }
            if (targetBranchName.equals(getHEADBranchName())) {
                System.out.println("No need to checkout the current branch.");
                return;
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
                        System.out.println("There is an untracked file in the way; delete it, or add and commit it first.");
                        return;
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

            switchHEAD(args[1]);
        } else if (args.length == 3) {
            // java gitlet.Main checkout -- <File-name>
            if (!args[1].equals("--")) {
                System.out.println("Incorrect operands.");
            }

            String fileName = args[2];
            Tree HEAD_Tree = getHEADTree();
            String blobHash = HEAD_Tree.getHashOfFile(fileName);
            if (blobHash == null) {
                System.out.println("File does not exist in that commit.");
                return;
            } else {
                Blob blobToWrite = readObject(join(BLOB_DIR, blobHash), Blob.class);
                File fileToWrite = join(CWD, fileName);
                writeContents(fileToWrite, blobToWrite.getContent());
            }
        } else if (args.length == 4) {
            // java gitlet.Main checkout dj2kj3 -- <File-name>
            if (!args[2].equals("--")) {
                System.out.println("Incorrect operands.");
            }

            String commitHash = args[1];
            String fileName = args[3];

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
                System.out.println("No commit with that id exists.");
                return;
            }

            Commit targetCommit = readObject(join(COMMIT_DIR, fullCommitHash), Commit.class);
            Tree targetTree = targetCommit.getTree();

            String blobHash = targetTree.getHashOfFile(fileName);

            if (blobHash == null) {
                System.out.println("File does not exist in that commit.");
                return;
            }

            Blob blobToWrite = readObject(join(BLOB_DIR, blobHash), Blob.class);
            File fileToWrite = join(CWD, fileName);
            writeContents(fileToWrite, blobToWrite.getContent());
        }
    }

    public static void branchCommand(String arg) {
        checkIfGitletExists();

        if (join(BRANCH_DIR, arg).exists()) {
            throw new GitletException("A branch with that name already exists.");
        }

        Branch newBranch = new Branch(arg, getHEADCommit().getHash());

        newBranch.saveBranch();
    }

    public static void rmbranchCommand(String arg) {
        checkIfGitletExists();

        File branchFile = join(BRANCH_DIR, arg);

        if (!branchFile.exists()) {
            System.out.println("A branch with that name does not exist.");
            return;
        }
        if (arg.equals(getHEADBranchName())) {
            System.out.println("Cannot remove the current branch.");
            return;
        }

        branchFile.delete();
    }

    public static void resetCommand(String arg) {
        checkIfGitletExists();

    }

    public static void mergeCommand(String arg) {
        checkIfGitletExists();


    }

    public static void setPersistence() {
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

        if (!BRANCH_DIR.exists()) {
            BRANCH_DIR.mkdir();
        }

        if (!HEAD_File.exists()) {
            try {
                HEAD_File.createNewFile();
            }
            catch (IOException ignore) {
            }
        }
    }

    public static void checkIfGitletExists() {
        if (!GITLET_DIR.exists()) {
            throw new RuntimeException("Not in an initialized Gitlet directory.");
        }
    }

    public static String getHEADBranchName() {
        return readContentsAsString(HEAD_File);
    }

    public static Commit getHEADCommit() {
        String headBranchName = getHEADBranchName();
        String headCommitHash = readContentsAsString(join(BRANCH_DIR, headBranchName));
        return readObject(join(COMMIT_DIR, headCommitHash), Commit.class);
    }

    public static Tree getHEADTree() {
        Commit headCommit = getHEADCommit();
        return headCommit.getTree();
    }

    // commit
    public static void updateHEADBranch(Commit c) {
        String headBranchName = readContentsAsString(HEAD_File);
        writeContents(join(BRANCH_DIR, headBranchName), c.getHash());
    }

    // checkout
    public static void switchHEAD(String branchName) {
        writeContents(HEAD_File, branchName);
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

    private static void printInfoOfCommit(Commit c) {
        System.out.println("commit " + c.getHash());
        System.out.println("Date: " + c.getTimeStamp());
        System.out.println(c.getMessage());
    }
}
