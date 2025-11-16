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

                // 子情况2.1：文件已暂存，但暂存后又被修改（工作目录内容与暂存内容不一致）
                if (stagedAddHash != null && !stagedAddHash.equals(cwdHash)) {
                    modifications.add(filePath + " (modified)");
                }
                // 子情况2.2：文件被跟踪（在HEAD中存在）但未暂存，且工作目录内容与HEAD中内容不一致
                else if (trackedHash != null && stagedAddHash == null && !trackedHash.equals(cwdHash)) {
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
        Stage stage = Stage.loadStageArea();

        // 1. 检查错误条件
        if (!stage.getStagedForAddition().isEmpty() || !stage.getStagedForRemoval().isEmpty()) {
            System.out.println("You have uncommitted changes.");
            return;
        }
        File branchFile = join(BRANCH_DIR, arg);
        if (!branchFile.exists()) {
            System.out.println("A branch with that name does not exist.");
            return;
        }
        String givenBranchName = arg;
        String currentBranchName = getHEADBranchName();
        if (givenBranchName.equals(currentBranchName)) {
            System.out.println("Cannot merge a branch with itself.");
            return;
        }

        // 2. 获取所有相关的 Commit 和 Tree
        String givenCommitHash = readContentsAsString(branchFile);
        Commit givenCommit = readObject(join(COMMIT_DIR, givenCommitHash), Commit.class);
        Commit HEADCommit = getHEADCommit();
        Commit splitPoint = findSplitPoint(HEADCommit, givenCommit);

        // 3. 处理快进 (Fast-forward) 和祖先情况
        if (splitPoint.getHash().equals(givenCommit.getHash())) {
            System.out.println("Given branch is an ancestor of the current branch.");
            return;
        }
        if (splitPoint.getHash().equals(HEADCommit.getHash())) {
            System.out.println("Current branch fast-forwarded.");
            checkoutCommand(new String[]{"checkout", givenBranchName});
            return;
        }

        // 4. 获取三个 commit 的文件映射
        Map<String, String> splitFiles = splitPoint.getTree().getAllFilesInTree();
        Map<String, String> headFiles = HEADCommit.getTree().getAllFilesInTree();
        Map<String, String> givenFiles = givenCommit.getTree().getAllFilesInTree();

        // 5. 检查未跟踪文件的冲突
        List<String> cwdFilePaths = getAllFilePathsInCWD(CWD);
        for (String filePath : cwdFilePaths) {
            if (!headFiles.containsKey(filePath)) { // 未被 HEAD 跟踪
                // 如果该文件在 Given 中被修改/添加，则会覆盖 CWD 中的未跟踪文件
                String givenHash = givenFiles.get(filePath);
                String splitHash = splitFiles.get(filePath);
                if (!Objects.equals(givenHash, splitHash)) {
                    System.out.println("There is an untracked file in the way; delete it, or add and commit it first.");
                    System.exit(0);
                }
            }
        }

        // 6. 核心合并逻辑
        Set<String> allFileNames = new HashSet<>();
        allFileNames.addAll(splitFiles.keySet());
        allFileNames.addAll(headFiles.keySet());
        allFileNames.addAll(givenFiles.keySet());

        boolean conflict = false;
        Tree newTree = HEADCommit.getTree(); // 从 HEAD 树开始
        Stage newStage = new Stage(); // 合并操作将暂存文件

        for (String fileName : allFileNames) {
            String splitHash = splitFiles.get(fileName);
            String headHash = headFiles.get(fileName);
            String givenHash = givenFiles.get(fileName);

            boolean headModified = !Objects.equals(splitHash, headHash);
            boolean givenModified = !Objects.equals(splitHash, givenHash);

            if (headModified && givenModified) {
                // 7.1. 双方都修改了
                if (!Objects.equals(headHash, givenHash)) {
                    // *** 冲突 ***
                    conflict = true;
                    // 读取内容
                    byte[] headContent = (headHash == null) ? new byte[0] : readObject(join(BLOB_DIR, headHash), Blob.class).getContent();
                    byte[] givenContent = (givenHash == null) ? new byte[0] : readObject(join(BLOB_DIR, givenHash), Blob.class).getContent();

                    // 写入带标记的冲突文件到 CWD
                    File f = join(CWD, fileName);
                    if (f.getParentFile() != null) {
                        f.getParentFile().mkdirs();
                    }
                    writeContents(f, "<<<<<<< HEAD\n", headContent, "=======\n", givenContent, ">>>>>>>\n");

                    // 将冲突文件暂存
                    Blob conflictBlob = new Blob(f);
                    conflictBlob.saveBlob();
                    newStage.stageForAddition(fileName, conflictBlob.getHash());
                    newTree = Tree.update(newTree, fileName, conflictBlob.getHash());
                }
                // else: 双方修改一致，保留 HEAD 版本 (newTree 默认如此)，无需操作

            } else if (!headModified && givenModified) {
                // 7.2. 只有 Given 修改了
                if (givenHash == null) {
                    // *** (你失败的测试点) ***
                    // 规则5：Given 中删除，HEAD 未修改 -> 删除
                    newStage.stageForRemoval(fileName, headHash);
                    restrictedDelete(join(CWD, fileName));
                    newTree = Tree.update(newTree, fileName, null);
                } else {
                    // 规则1/7：Given 中修改或添加 -> 检出并暂存
                    File f = join(CWD, fileName);
                    if (f.getParentFile() != null) {
                        f.getParentFile().mkdirs();
                    }
                    Blob b = readObject(join(BLOB_DIR, givenHash), Blob.class);
                    writeContents(f, b.getContent()); // 写入 CWD
                    newStage.stageForAddition(fileName, givenHash); // 暂存
                    newTree = Tree.update(newTree, fileName, givenHash); // 更新树
                }
            }
            // 7.3. 只有 HEAD 修改了 (headModified && !givenModified) -> 保留 HEAD 版本 (newTree 默认如此)，无需操作
            // 7.4. 双方都未修改 (!headModified && !givenModified) -> 保留 HEAD 版本，无需操作
        }

        // 8. 提交或报告冲突
        if (conflict) {
            System.out.println("Encountered a merge conflict.");
            newStage.saveStageArea(); // 保存带有冲突文件的暂存区
        } else {
            // 没有冲突，创建合并提交
            newTree.saveTree();
            String mergeMessage = "Merged " + givenBranchName + " into " + currentBranchName + ".";

            Commit mergeCommit = new Commit(mergeMessage, HEADCommit, newTree);
            mergeCommit.setSecondParent(givenCommit); // 使用我们刚添加的 setter
            mergeCommit.saveCommit();

            // 更新 HEAD 分支并清空暂存区
            updateHEADBranch(mergeCommit);
            new Stage().saveStageArea();
        }
    }

//    public static void mergeCommand(String arg) {
//        checkIfGitletExists();
//
//        if (!join(BRANCH_DIR, arg).exists()) {
//            System.out.println("A branch with that name does not exist.");
//            return;
//        }
//        if (arg.equals(getHEADBranchName())) {
//            System.out.println("Cannot merge a branch with itself.");
//            return;
//        }
//
//        // todo:检查是否有未跟踪文件
//
//        Commit givenCommit = readObject(join(COMMIT_DIR, readContentsAsString(join(BRANCH_DIR, arg))), Commit.class);
//        Commit HEADCommit = getHEADCommit();
//        Commit splitPoint = findSplitPoint(HEADCommit, givenCommit);
//
//
//
//        if (splitPoint.equals(givenCommit)) {
//            System.out.println("Given branch is an ancestor of the current branch.");
//            return;
//        }
//        if (splitPoint.equals(HEADCommit)) {
//            String[] args = new String[]{"checkout", arg};
//            checkoutCommand(args);
//            System.out.println("Current branch fast-forwarded.");
//            return;
//        }
//
//
//    }

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
            System.out.println("Not in an initialized Gitlet directory.");
            System.exit(0);
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

    private static Commit findSplitPoint(Commit currentC, Commit givenC) {
        HashMap<String, Integer> currentCMap = new HashMap<>();
        HashMap<String, Integer> givenCMap = new HashMap<>();

        findSplitPointHelper(currentCMap, currentC);
        findSplitPointHelper(givenCMap, givenC);

        String resultHash = null;
        Integer resultDepth = -1;

        for (String hash : currentCMap.keySet()) {
            Integer newDepth = currentCMap.get(hash);
            if (givenCMap.containsKey(hash)) {
                if (resultDepth == -1) {
                    resultHash = hash;
                    resultDepth = newDepth;
                } else if (resultDepth > newDepth) {
                    resultHash = hash;
                    resultDepth = newDepth;
                }
            }
        }
        return readObject(join(COMMIT_DIR, resultHash), Commit.class);
    }

    private static void findSplitPointHelper(Map<String, Integer> map, Commit c) {
        Commit p = c;
        int depth = 0;
        while (p != null) {
            map.put(p.getHash(), depth);
            p = p.getParent();
            depth++;
        }
    }
}
