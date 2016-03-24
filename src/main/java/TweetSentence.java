import java.util.ArrayList;
import java.util.List;

public class TweetSentence {

    List<String> POSTags = new ArrayList<>();
    String constTree;
    String depTree;


    public List<String> getPOSTags() {
        return POSTags;
    }

    public void setPOSTags(List<String> POSTags) {
        this.POSTags = POSTags;
    }

    public String getConstTree() {
        return constTree;
    }

    public void setConstTree(String constTree) {
        this.constTree = constTree;
    }

    public String getDepTree() {
        return depTree;
    }

    public void setDepTree(String depTree) {
        this.depTree = depTree;
    }
}
