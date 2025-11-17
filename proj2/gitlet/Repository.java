package gitlet;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.*;

import static gitlet.Utils.*;

/** Represents a gitlet repository.
 *  Main负责读入命令名和参数，Repository用来执行处理
 *  @author CS-Trekker
 */
public class Repository {
    /**
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
    private static File headFile = join(GITLET_DIR, "HEAD");

    public static void initCommand() {
        if (GITLET_DIR.exists()) {
            System.out.println("A Gitlet version-control system already exists in the current directory.");
            return;
        }

        setPersistence();

        Tree emptyTree = new Tree();
        emptyTree.saveTree();
        Commit initialCommit = new Commit("initial commit", null, emptyTree);
        initialCommit.saveCommit();
        Branch master = new Branch("master", initialCommit.getHash());
        master.saveBranch();

        switchHEAD("master");
        updateHEADBranch(initialCommit);
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
        Tree headTree = getHEADTree();
        String hashOfFileInHEAD = headTree.getHashOfFile(arg);

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

        if (arg.isEmpty()) {
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

        List<String> hashOfCommits =  plainFilenamesIn(COMMIT_DIR);
        for (String hash : hashOfCommits) {
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
        branchNames.sort(null);
        for (String branchName : branchNames) {
            if (branchName.equals(getHEADBranchName())) {
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
        // 获取当前HEAD提交所跟踪的所有文件（路径到哈希的映射）
        Commit headCommit = getHEADCommit();
        Map<String, String> trackedFiles = headCommit.getTree().getAllFilesInTree();
        // 获取工作目录中所有文件的路径列表
        List<String> cwdFilesList = getAllFilePathsInCWD(CWD);
        Set<String> cwdFilesSet = new HashSet<>(cwdFilesList);
        // 用SortedSet存储修改项，保证输出时按字典序排列
        SortedSet<String> modifications = new TreeSet<>();

        // 需要检查的文件集合：包括所有被跟踪的文件和已暂存的文件（这些文件的修改可能未被暂存）
        Set<String> allFilesToCheck = new HashSet<>(trackedFiles.keySet());
        allFilesToCheck.addAll(stageForAddition.keySet());

        for (String filePath : allFilesToCheck) {
            // 当前HEAD提交中该文件的哈希（若被跟踪）
            String trackedHash = trackedFiles.get(filePath);
            // 暂存区中该文件的哈希（若已暂存）
            String stagedAddHash = stageForAddition.get(filePath);
            // 该文件是否存在于工作目录
            boolean existsInCwd = cwdFilesSet.contains(filePath);

            if (!existsInCwd) {
                // 情况1：文件在工作目录中已被删除，但未被暂存删除（未加入stageForRemoval）
                if (!stageForRemoval.containsKey(filePath)) {
                    modifications.add(filePath + " (deleted)");
                }
            } else {
                // 情况2：文件存在于工作目录，检查内容是否有修改
                String cwdHash = sha1(readContents(join(CWD, filePath)));

                if (stagedAddHash != null && !stagedAddHash.equals(cwdHash)) {
                    // 子情况2.1：文件已暂存，但暂存后又被修改（工作目录内容与暂存内容不一致）
                    modifications.add(filePath + " (modified)");
                } else if (trackedHash != null && stagedAddHash == null && !trackedHash.equals(cwdHash)) {
                    // 子情况2.2：文件被跟踪（在HEAD中存在）但未暂存，且工作目录内容与HEAD中内容不一致
                    modifications.add(filePath + " (modified)");
                }
            }
        }
        // 打印所有未暂存的修改
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

            checkHasUntrackedFileConflicts(currentFiles, targetFiles);

            resetCWDandStage(currentFiles, targetFiles);

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

            String fullCommitHash = findFullCommitHash(commitHash);

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
            System.out.println("A branch with that name already exists.");
            return;
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

        String fullCommitHash = findFullCommitHash(arg);
        Commit targetCommit = readObject(join(COMMIT_DIR, fullCommitHash), Commit.class);
        Commit currentCommit = getHEADCommit();

        Tree currentTree = currentCommit.getTree();
        Tree targetTree = targetCommit.getTree();
        Map<String, String> currentFiles = currentTree.getAllFilesInTree();
        Map<String, String> targetFiles = targetTree.getAllFilesInTree();

        checkHasUntrackedFileConflicts(currentFiles, targetFiles);

        resetCWDandStage(currentFiles, targetFiles);

        updateHEADBranch(targetCommit);
    }


    public static void mergeCommand(String arg) {
        checkIfGitletExists();

        if (!join(BRANCH_DIR, arg).exists()) {
            System.out.println("A branch with that name does not exist.");
            return;
        }
        if (arg.equals(getHEADBranchName())) {
            System.out.println("Cannot merge a branch with itself.");
            return;
        }
        Stage stage = Stage.loadStageArea();
        if (!(stage.getStagedForAddition().isEmpty() && stage.getStagedForRemoval().isEmpty())) {
            System.out.println("You have uncommitted changes.");
            return;
        }

        Commit givenCommit = readObject(join(COMMIT_DIR, readContentsAsString(join(BRANCH_DIR, arg))), Commit.class);
        Commit headCommit = getHEADCommit();
        Commit splitPoint = findSplitPoint(headCommit, givenCommit);
        Map<String, String> givenCommitFiles = givenCommit.getTree().getAllFilesInTree();
        Map<String, String> headCommitFiles = headCommit.getTree().getAllFilesInTree();
        Map<String, String> splitPointFiles = splitPoint.getTree().getAllFilesInTree();

        checkHasUntrackedFileConflicts(headCommitFiles, givenCommitFiles);

        if (splitPoint.getHash().equals(givenCommit.getHash())) {
            System.out.println("Given branch is an ancestor of the current branch.");
            return;
        }
        if (splitPoint.getHash().equals(headCommit.getHash())) {
            resetCommand(givenCommit.getHash());
            System.out.println("Current branch fast-forwarded.");
            return;
        }

        boolean ifConflict = false;
        Set<String> fileSet = new HashSet<>();
        fileSet.addAll(headCommitFiles.keySet());
        fileSet.addAll(givenCommitFiles.keySet());
        fileSet.addAll(splitPointFiles.keySet());

        Tree mergedTree = new Tree(headCommit.getTree().getBlobs(), headCommit.getTree().getSubtrees());

        for (String filePath : fileSet) {
            String hashInHEAD = headCommitFiles.get(filePath);
            String hashInGiven = givenCommitFiles.get(filePath);
            String hashInSplit = splitPointFiles.get(filePath);

            boolean modifiedInHEAD = !Objects.equals(hashInSplit, hashInHEAD);
            boolean modifiedInGiven = !Objects.equals(hashInSplit, hashInGiven);
            boolean fileEqual = Objects.equals(hashInHEAD, hashInGiven);

            if (!modifiedInHEAD && modifiedInGiven) {
                mergedTree = Tree.update(mergedTree, filePath, hashInGiven);
            } else if (modifiedInHEAD && modifiedInGiven && !fileEqual) {
                ifConflict = true;
                byte[] headContent = (hashInHEAD == null) ? new byte[0]
                        : readObject(join(BLOB_DIR, hashInHEAD), Blob.class).getContent();
                byte[] givenContent = (hashInGiven == null) ? new byte[0]
                        : readObject(join(BLOB_DIR, hashInGiven), Blob.class).getContent();

                String conflictContent = "<<<<<<< HEAD\n"
                                        + new String(headContent)
                                        + "=======\n"
                                        + new String(givenContent)
                                        + ">>>>>>>\n";
                Blob conflictedBlob = new Blob(filePath, conflictContent.getBytes(StandardCharsets.UTF_8));
                mergedTree = Tree.update(mergedTree, filePath, conflictedBlob.getHash());
                conflictedBlob.saveBlob();
            }
        }

        Commit mergedCommit = new Commit("Merged " + arg + " into " + getHEADBranchName() + ".", headCommit, mergedTree);
        mergedCommit.setSecondParent(givenCommit);

        mergedCommit.saveCommit();
        mergedTree.saveTree();
        updateHEADBranch(mergedCommit);

        Map<String, String> mergedCommitFiles = mergedCommit.getTree().getAllFilesInTree();
        // 调用 resetCWDandStage 来更新工作目录
        resetCWDandStage(headCommitFiles, mergedCommitFiles);

        if (ifConflict) {
            System.out.println("Encountered a merge conflict.");
        }
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
            } catch (IOException ignore) {
            }
        }
        if (!BRANCH_DIR.exists()) {
            BRANCH_DIR.mkdir();
        }
        if (!headFile.exists()) {
            try {
                headFile.createNewFile();
            } catch (IOException ignore) {
            }
        }
    }

    public static void checkIfGitletExists() {
        if (!GITLET_DIR.exists()) {
            System.out.println("Not in an initialized Gitlet directory.");
            System.exit(0);
        }
    }

    public static String getHEADBranchName() {
        return readContentsAsString(headFile);
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
        String headBranchName = readContentsAsString(headFile);
        writeContents(join(BRANCH_DIR, headBranchName), c.getHash());
    }

    // checkout
    public static void switchHEAD(String branchName) {
        writeContents(headFile, branchName);
    }

    /**
    递归地输出CWD中所有文件路径
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

    private static void checkHasUntrackedFileConflicts(Map<String, String> currentFiles, Map<String, String> targetFiles) {
        List<String> cwdFilePaths = getAllFilePathsInCWD(CWD);
        for (String filePath : cwdFilePaths) {
            // 未跟踪文件是指存在于CWD中，但不被当前提交跟踪，也没有被暂存在add区的文件。
            boolean isTracked = currentFiles.containsKey(filePath);
            boolean isStaged = Stage.loadStageArea().getStagedForAddition().containsKey(filePath);
            if (!isTracked && !isStaged) {
                // 如果这个未跟踪的文件存在于目标分支中，checkout将覆盖它。
                if (targetFiles.containsKey(filePath)) {
                    System.out.println("There is an untracked file in the way; delete it, or add and commit it first.");
                    System.exit(0);
                }
            }
        }
    }

    private static void resetCWDandStage(Map<String, String> currentFiles, Map<String, String> targetFiles) {
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
    }

    private static String findFullCommitHash(String unfullHash) {
        String fullCommitHash = null;
        List<String> allCommitHashes = plainFilenamesIn(COMMIT_DIR);
        if (allCommitHashes != null) {
            for (String hash : allCommitHashes) {
                if (hash.startsWith(unfullHash)) {
                    fullCommitHash = hash;
                    break;
                }
            }
        }

        if (fullCommitHash == null) {
            System.out.println("No commit with that id exists.");
            System.exit(0);
        }
        return fullCommitHash;
    }

    private static void printInfoOfCommit(Commit c) {
        System.out.println("commit " + c.getHash());
        System.out.println("Date: " + c.getTimeStamp());
        System.out.println(c.getMessage());
    }

    private static Commit findSplitPoint(Commit head, Commit given) {
        // 1. 使用 BFS 获取 head 的所有祖先（包括自身），存入 Set 中
        Set<String> headAncestors = new HashSet<>();
        Queue<Commit> queue = new LinkedList<>();
        queue.add(head);

        while (!queue.isEmpty()) {
            Commit c = queue.poll();
            // 如果该提交已访问过，跳过（避免环或重复处理）
            if (headAncestors.contains(c.getHash())) {
                continue;
            }

            headAncestors.add(c.getHash());

            if (c.getParent() != null) {
                queue.add(c.getParent());
            }
            if (c.getSecondParent() != null) {
                queue.add(c.getSecondParent());
            }
        }

        // 2. 对 Given 分支进行 BFS，找到的第一个在 headAncestors 中的节点就是最近公共祖先
        queue.add(given);
        // 重用一个 visited 集合来避免在 Given 分支 BFS 中重复处理
        Set<String> visitedGiven = new HashSet<>();

        while (!queue.isEmpty()) {
            Commit c = queue.poll();
            if (visitedGiven.contains(c.getHash())) {
                continue;
            }
            visitedGiven.add(c.getHash());

            // 找到了！这是距离 Given 最近的、且同时存在于 head 历史中的节点
            if (headAncestors.contains(c.getHash())) {
                return c;
            }

            if (c.getParent() != null) {
                queue.add(c.getParent());
            }
            if (c.getSecondParent() != null) {
                queue.add(c.getSecondParent());
            }
        }

        // 理论上不会运行到这里，因为至少 Initial Commit 是公共的
        return null;
    }
}
