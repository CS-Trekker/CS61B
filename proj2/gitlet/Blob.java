package gitlet;

import java.io.File;
import java.io.Serializable;

import static gitlet.Utils.*;

/**
 * Represent the content of a file, including the path (in File Class) and the content.
 */

public class Blob implements Serializable {
    private File file;

    public Blob(File f) {
        if (f.isDirectory()) {
            throw new GitletException("Invalid argument");
        }
        file = f;
    }

    public String getFileName() {
        return file.getName();
    }

    public String getHash() {
        byte[] content = readContents(file);
        return sha1(content);
    }
    public void saveBlob() {
        writeObject(Utils.join(Repository.BLOB_DIR, getHash()), this);
    }
}
