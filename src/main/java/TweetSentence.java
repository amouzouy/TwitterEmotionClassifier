import java.util.ArrayList;
import java.util.List;

public class TweetSentence {

    List<String> unigrams = new ArrayList<>();
    List<String> bigrams = new ArrayList<>();
    List<String> lemUnigrams = new ArrayList<>();
    List<String> lemBigrams = new ArrayList<>();
    List<String> POSTags = new ArrayList<>();
    String constTree;
    String depTree;

    public List<String> getUnigrams() {
        return unigrams;
    }

    public void setUnigrams(List<String> unigrams) {
        this.unigrams = unigrams;
    }

    public List<String> getBigrams() {
        return bigrams;
    }

    public void setBigrams(List<String> bigrams) {
        this.bigrams = bigrams;
    }

    public List<String> getLemUnigrams() {
        return lemUnigrams;
    }

    public void setLemUnigrams(List<String> lemUnigrams) {
        this.lemUnigrams = lemUnigrams;
    }

    public List<String> getLemBigrams() {
        return lemBigrams;
    }

    public void setLemBigrams(List<String> lemBigrams) {
        this.lemBigrams = lemBigrams;
    }

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
