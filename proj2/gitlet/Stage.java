package gitlet;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import static gitlet.Utils.*;

public class Stage implements Serializable {
    private  Map<String, String> stagedForAddition;
    private  Map<String, String> stagedForRemoval;

    public Stage() {
        stagedForAddition = new HashMap<>();
        stagedForRemoval = new HashMap<>();
    }

    public Map<String, String> getStagedForAddition() {
        return stagedForAddition;
    }

    public Map<String, String> getStagedForRemoval() {
        return stagedForRemoval;
    }

    public void stageForAddition(String fileName, String BlobHash) {
        stagedForAddition.put(fileName, BlobHash);
    }

    public void stageForRemoval(String fileName, String BlobHash) {
        stagedForRemoval.put(fileName, BlobHash);
    }

    public void saveStageArea() {
        writeObject(Repository.STAGE_FILE, this);
    }

    public static Stage loadStageArea() {
        Stage stage;
        try {
            stage = readObject(Repository.STAGE_FILE, Stage.class);
        } catch (IllegalArgumentException iae) {
            stage = new Stage();
        }
        return stage;
    }
}
