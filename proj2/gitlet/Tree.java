package gitlet;

import java.io.File;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import static gitlet.Utils.*;

/**
 *
 */
public class Tree implements Serializable {
    private Map<String, String> blobs;
    private Map<String, String> subtrees;

    public Tree() {
        blobs = new HashMap<>();
        subtrees = new HashMap<>();
    }

    public Tree(Map<String, String> b, Map<String, String> s) {
        blobs = b;
        subtrees = s;
    }

    public void addBlob(String fileName, String blobHash) {
        blobs.put(fileName, blobHash);
    }

    public void addSubTree(String dirName, String treeHash) {
        subtrees.put(dirName, treeHash);
    }

    public Map<String, String> getBlobs() {
        return blobs;
    }

    public Map<String, String> getSubtrees() {
        return subtrees;
    }

    public String getHash() {
        return sha1(serialize(this));
    }

    public void saveTree() {
        writeObject(Utils.join(Repository.TREE_DIR, this.getHash()),this);
    }

    // Return the hash value of the file Blob at the specified path under a specific Tree. If the file does not exist, return null
    // TODO: Finish the rest.
    // Example of argument : gitlet/Stage.java
    public String getHashOfFile(String relativePath) {
        String[] pathParts = relativePath.split("[/\\\\]");

        Tree currentTree = this;

        for (int i = 0; i < pathParts.length - 1; i++) {
            String dirName = pathParts[i];

            String subTreeHash = currentTree.getSubtrees().get(dirName);

            if (subTreeHash == null) {
                // The file doesn't exist.
                return null;
            }

            currentTree = readObject(join(Repository.TREE_DIR, subTreeHash), Tree.class);
        }

        String fileName = pathParts[pathParts.length - 1];
        return currentTree.getBlobs().get(fileName);
    }


    // ********************** AIGC ******************************
    /**
     * Core auxiliary method: Update or create the hierarchical structure of the Tree based on a complete path.
     * @param rootTree The root Tree to be updated can be null (indicating that it starts from an empty Tree)
     * @param fullPath The complete path of the file, such as "src/gitlet/Main.java"
     * @param blobHash Hash the content of the file. If it is null, it indicates that the file has been deleted.
     * @return A brand-new root Tree object representing the updated state.
     */
    public static Tree update(Tree rootTree, String fullPath, String blobHash) {
        String[] pathParts = fullPath.split("[/\\\\]");
        return updateRecursive(rootTree, pathParts, 0, blobHash);
    }

    private static Tree updateRecursive(Tree currentTree, String[] pathParts,
                                 int index, String blobHash) {
        // If the current node is empty (for example, a new subdirectory needs to be created), a new Tree will be created
        Tree newTree = (currentTree == null)
                ? new Tree()
                : new Tree(new HashMap<>(currentTree.getBlobs()), new HashMap<>(currentTree.getSubtrees()));

        String currentPart = pathParts[index];

        // --- Base Case: The last part of the arrival path, that is, the file name ---
        if (index == pathParts.length - 1) {
            if (blobHash != null) { // add or update files
                newTree.addBlob(currentPart, blobHash);
            } else { // delete files
                newTree.getBlobs().remove(currentPart);
            }
            return newTree;
        }

        // --- Recursive Step: still in the directory path ---
        // obtain the hash of the next level subtree
        String subTreeHash = newTree.getSubtrees().get(currentPart);
        Tree subTree = (subTreeHash == null)
                ? null // if the subtree does not exist, pass in null
                : readObject(join(Repository.TREE_DIR, subTreeHash), Tree.class);

        // Recursive call to update the subtree at the next level
        Tree newSubTree = updateRecursive(subTree, pathParts, index + 1, blobHash);

        // If the updated subtree is empty (for example, all the files within it have been deleted), then directly remove this subdirectory
        if (newSubTree.getBlobs().isEmpty() && newSubTree.getSubtrees().isEmpty()) {
            newTree.getSubtrees().remove(currentPart);
        } else {
            // Otherwise, save the updated subtree and update it to the subtrees mapping of the current tree
            newSubTree.saveTree();
            newTree.addSubTree(currentPart, newSubTree.getHash());
        }

        return newTree;
    }

    public Map<String, String> getAllFilesInTree() {
        Map<String, String> allFiles = new HashMap<>();
        // Add files from the current directory
        for (Map.Entry<String, String> entry : blobs.entrySet()) {
            allFiles.put(entry.getKey(), entry.getValue());
        }

        // Recursively add files from subdirectories
        for (Map.Entry<String, String> entry : subtrees.entrySet()) {
            String dirName = entry.getKey();
            String subTreeHash = entry.getValue();
            Tree subTree = readObject(join(Repository.TREE_DIR, subTreeHash), Tree.class);

            Map<String, String> subTreeFiles = subTree.getAllFilesInTree();
            for (Map.Entry<String, String> subEntry : subTreeFiles.entrySet()) {
                // Prepend the directory name to the file path
                allFiles.put(dirName + "/" + subEntry.getKey(), subEntry.getValue());
            }
        }
        return allFiles;
    }
}