package gitlet;

import java.io.File;
import java.io.Serializable;

import static gitlet.Utils.*;

/**
 * Represent the content of a file, including the path (in File Class) and the content.
 */

public class Blob implements Serializable {
    private String fileName;
    private byte[] content;

    public Blob(File f) {
        if (f.isDirectory() || !f.exists()) {
            throw new GitletException("Invalid argument");
        }
        fileName = f.getName();
        content = readContents(f);
    }

    public String getFileName() {
        return fileName;
    }

    public byte[] getContent() {
        return content;
    }

    public String getHash() {
        return sha1(content);
    }
    public void saveBlob() {
        writeObject(Utils.join(Repository.BLOB_DIR, getHash()), this);
    }
}
