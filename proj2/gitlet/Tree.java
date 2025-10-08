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
}