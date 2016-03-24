import java.util.ArrayList;
import java.util.List;

public class TweetInfo {
    List<TweetSentence> tweetSentenceList = new ArrayList<>();
    List<String> unigrams = new ArrayList<>();
    List<String> bigrams = new ArrayList<>();
    List<String> lemUnigrams = new ArrayList<>();
    List<String> lemBigrams = new ArrayList<>();


    public List<TweetSentence> getTweetSentenceList() {
        return tweetSentenceList;
    }

    public void setTweetSentenceList(List<TweetSentence> tweetSentenceList) {
        this.tweetSentenceList = tweetSentenceList;
    }

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

}
