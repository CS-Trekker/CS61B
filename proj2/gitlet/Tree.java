package gitlet;

import java.io.File;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import static gitlet.Utils.*;

/**
 * 表示Gitlet中的树对象，用于维护文件（blob）和子目录（子树）的层级结构。
 * 树对象是版本控制中目录结构的核心载体，通过哈希值唯一标识自身状态，支持递归更新和持久化存储。
 */
public class Tree implements Serializable {
    // 存储当前目录下的文件映射：键为文件名，值为对应文件内容(blob)的哈希值
    private Map<String, String> blobs;
    // 存储当前目录下的子目录映射：键为目录名，值为对应子树的哈希值
    private Map<String, String> subtrees;

    /**
     * 初始化一个空树对象，包含空的文件和子目录映射。
     */
    public Tree() {
        blobs = new HashMap<>();
        subtrees = new HashMap<>();
    }

    /**
     * 基于已有文件和子目录映射创建树对象，通过深拷贝避免共享引用。
     * （版本控制中需保证树对象不可变，修改时需创建新对象，因此深拷贝是必要的）
     * @param b 已有文件映射（键：文件名，值：blob哈希）
     * @param s 已有子目录映射（键：目录名，值：子树哈希）
     */
    public Tree(Map<String, String> b, Map<String, String> s) {
        // 深拷贝输入的映射，确保新树对象的修改不会影响原对象
        blobs = new HashMap<>(b);
        subtrees = new HashMap<>(s);
    }

    /**
     * 向当前树中添加或更新文件映射。
     * @param fileName 文件名（当前目录下的相对名称）
     * @param blobHash 文件内容对应的blob哈希值
     */
    public void addBlob(String fileName, String blobHash) {
        blobs.put(fileName, blobHash);
    }

    /**
     * 向当前树中添加或更新子目录映射。
     * @param dirName 子目录名（当前目录下的相对名称）
     * @param treeHash 子目录对应的树对象哈希值
     */
    public void addSubTree(String dirName, String treeHash) {
        subtrees.put(dirName, treeHash);
    }

    /**
     * 获取当前树中的所有文件映射。
     * @return 包含文件名与blob哈希的映射表
     */
    public Map<String, String> getBlobs() {
        return blobs;
    }

    /**
     * 获取当前树中的所有子目录映射。
     * @return 包含目录名与子树哈希的映射表
     */
    public Map<String, String> getSubtrees() {
        return subtrees;
    }

    /**
     * 计算当前树对象的唯一哈希值。
     * 哈希基于树的完整内容（文件和子目录映射）生成，用于标识树的状态，支持存储和比较。
     * @return 树对象的SHA-1哈希值
     */
    public String getHash() {
        return sha1(serialize(this));
    }

    /**
     * 将当前树对象持久化到磁盘。
     * 存储路径为仓库的树目录（Repository.TREE_DIR），文件名为树的哈希值，便于后续读取。
     */
    public void saveTree() {
        writeObject(Utils.join(Repository.TREE_DIR, this.getHash()), this);
    }

    /**
     * 根据相对路径查找文件对应的blob哈希值。
     * 例如：路径"a/b.txt"表示在当前树的"a"子目录下查找"b.txt"文件。
     * @param relativePath 相对于当前树的文件路径（如"gitlet/Stage.java"）
     * @return 文件的blob哈希值；若路径不存在（如目录或文件不存在），则返回null
     */
    public String getHashOfFile(String relativePath) {
        String[] pathParts = relativePath.split("[/\\\\]");
        Tree currentTree = this;
        // 逐层遍历目录部分（除最后一个是文件名外）
        for (int i = 0; i < pathParts.length - 1; i++) {
            String dirName = pathParts[i];
            String subTreeHash = currentTree.getSubtrees().get(dirName);
            if (subTreeHash == null) {
                // 中间目录不存在，直接返回null
                return null;
            }
            // 读取子树继续查找
            currentTree = readObject(join(Repository.TREE_DIR, subTreeHash), Tree.class);
        }
        // 查找最终文件名对应的blob哈希
        String fileName = pathParts[pathParts.length - 1];
        return currentTree.getBlobs().get(fileName);
    }

    /**
     * 核心方法：根据完整文件路径更新树的层级结构（添加、更新或删除文件）。
     * 采用不可变设计，始终返回新的树对象，不修改原有树结构。
     * @param rootTree 待更新的根树（可为null，表示从空树开始）
     * @param fullPath 完整文件路径（如"src/gitlet/Main.java"）
     * @param blobHash 文件内容的blob哈希值；若为null，表示删除该文件
     * @return 更新后的全新根树对象
     */
    public static Tree update(Tree rootTree, String fullPath, String blobHash) {
        // 拆分路径为各部分（如"a/b/c.txt"拆分为["a", "b", "c.txt"]）
        String[] pathParts = fullPath.split("[/\\\\]");
        // 调用递归方法处理层级更新
        return updateRecursive(rootTree, pathParts, 0, blobHash);
    }

    /**
     * 递归更新树结构的辅助方法。
     * 每次递归都会创建当前层级的新树对象，确保原有树结构不被修改，符合不可变设计原则。
     * @param currentTree 当前处理的树节点（可为null，表示该层级目录不存在）
     * @param pathParts 拆分后的路径数组（如["a", "b", "c.txt"]）
     * @param index 当前处理的路径索引（从0开始）
     * @param blobHash 文件的blob哈希（null表示删除）
     * @return 处理后当前层级的新树对象
     */
    private static Tree updateRecursive(Tree currentTree, String[] pathParts,
                                        int index, String blobHash) {
        // 创建当前层级的新树对象：若原树存在则深拷贝其内容，否则创建空树
        // Tree采用的是不可变设计，只创建新对象，不修改旧对象
        Tree newTree = (currentTree == null)
                ? new Tree()
                : new Tree(new HashMap<>(currentTree.getBlobs()),
                new HashMap<>(currentTree.getSubtrees()));

        String currentPart = pathParts[index]; // 当前路径部分（目录名或文件名）

        // 基础情况：已到达路径最后一部分（即文件名），直接处理文件
        if (index == pathParts.length - 1) {
            if (blobHash != null) {
                // 添加或更新文件：将文件名与blob哈希关联
                newTree.addBlob(currentPart, blobHash);
            } else {
                // 删除文件：从当前树的文件映射中移除该文件名
                newTree.getBlobs().remove(currentPart);
            }
            return newTree;
        }

        // 递归情况：当前处理的是目录，需要继续处理下一级
        // 1. 获取当前目录对应的子树（若不存在则为null）
        String subTreeHash = newTree.getSubtrees().get(currentPart);
        Tree subTree = (subTreeHash == null)
                ? null // 子目录不存在，从空树开始处理
                : readObject(join(Repository.TREE_DIR, subTreeHash), Tree.class);

        // 2. 递归更新下一级子树
        Tree newSubTree = updateRecursive(subTree, pathParts, index + 1, blobHash);

        // 3. 处理更新后的子树：若为空则移除，否则保存并关联
        if (newSubTree.getBlobs().isEmpty() && newSubTree.getSubtrees().isEmpty()) {
            // 子树为空（无文件也无子目录），从当前树中移除该子目录
            // Git不会跟踪空目录
            newTree.getSubtrees().remove(currentPart);
        } else {
            // 子树非空，保存子树到磁盘，并更新当前树的子目录映射
            newSubTree.saveTree();
            newTree.addSubTree(currentPart, newSubTree.getHash());
        }

        return newTree;
    }

    /**
     * 递归收集当前树及所有子树中的所有文件，生成路径与blob哈希的映射。
     * 用于快速获取整个目录结构中的所有文件信息。
     * @return 键为文件相对路径（如"a/b.txt"），值为对应blob哈希的映射表
     */
    public Map<String, String> getAllFilesInTree() {
        Map<String, String> allFiles = new HashMap<>();

        // 添加当前目录下的文件
        for (Map.Entry<String, String> entry : blobs.entrySet()) {
            allFiles.put(entry.getKey(), entry.getValue());
        }

        // 递归添加子目录中的文件（路径前拼接目录名）
        for (Map.Entry<String, String> entry : subtrees.entrySet()) {
            String dirName = entry.getKey();
            String subTreeHash = entry.getValue();
            Tree subTree = readObject(join(Repository.TREE_DIR, subTreeHash), Tree.class);

            // 获取子树中的所有文件，并拼接路径
            Map<String, String> subTreeFiles = subTree.getAllFilesInTree();
            for (Map.Entry<String, String> subEntry : subTreeFiles.entrySet()) {
                allFiles.put(dirName + "/" + subEntry.getKey(), subEntry.getValue());
            }
        }
        return allFiles;
    }
}
