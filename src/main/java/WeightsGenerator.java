import org.codehaus.jackson.map.ObjectMapper;

import java.io.*;
import java.util.*;

public class WeightsGenerator {

    private static int emotionToInt(String label) {
        switch (label){
            case "surprise":
                return 0;
            case "sadness":
                return 1;
            case "joy":
                return 2;
            case "disgust":
                return 3;
            case "fear":
                return 4;
            case "anger":
                return 5;
            default:
                System.err.println("Error with label: "+label);
                System.exit(-1);
                return 6;
        }
    }

    public static Map<String,List<Integer>> getWeightMap(Map<String,String> tweetEmotionMap, boolean lemmatized, int tfNorm) throws IOException {

        Map<String, List<Integer>> weightMap = new HashMap<>();
        Map<String, Term> termMap = new HashMap<>();
        List<String> allNgrams = new ArrayList<>();
        Set<String> allUniqueNgrams;
        String tweetsPath = "";
        switch(System.getProperty("os.name")){
            case "Windows 10":
                tweetsPath = "C:/cygwin/home/Sheryan/EmotionClassifier/TwitterEmotionClassifier/dataset/processed_tweets";
                break;
            case "Linux":
                tweetsPath = "/home/sheryan/IdeaProjects/emotionclassifier/dataset/processed_tweets";
                break;
        }
        List<Integer> emotionDocCounts = new ArrayList<>(Collections.nCopies(6,0));
        List<Integer> emotionTermCounts = new ArrayList<>(Collections.nCopies(6,0));

        ObjectMapper objectMapper = new ObjectMapper();

        for(String id : tweetEmotionMap.keySet()){
            String emotion = tweetEmotionMap.get(id);
            emotionDocCounts.set(emotionToInt(emotion),emotionDocCounts.get(emotionToInt(emotion))+1);
            TweetInfo tweetInfo = objectMapper.readValue(new File(tweetsPath + "/" + id + ".json"), TweetInfo.class);
            if (lemmatized) {
                allNgrams.addAll(tweetInfo.getLemUnigrams());
                allNgrams.addAll(tweetInfo.getLemBigrams());
            }
            else {
                allNgrams.addAll(tweetInfo.getUnigrams());
                allNgrams.addAll(tweetInfo.getBigrams());
            }
            for(String  term: allNgrams){
                emotionTermCounts.set(emotionToInt(emotion),emotionTermCounts.get(emotionToInt(emotion))+1);
                if(termMap.containsKey(term)){
                    Term termObj = termMap.get(term);
                    termObj.tCounts.set(emotionToInt(tweetEmotionMap.get(id)),
                            termObj.tCounts.get(emotionToInt(tweetEmotionMap.get(id)))+1);
                }
                else{
                    Term termObj = new Term();
                    termObj.term = term;
                    termObj.tCounts.set(emotionToInt(tweetEmotionMap.get(id)),1);
                    termMap.put(term, termObj);
                }
            }
            allUniqueNgrams = new HashSet<>(allNgrams);
            for(String uniqueTerm: allUniqueNgrams){
                if(termMap.containsKey(uniqueTerm)){
                    Term termObj = termMap.get(uniqueTerm);
                    termObj.dCounts.set(emotionToInt(tweetEmotionMap.get(id)),
                            termObj.dCounts.get(emotionToInt(tweetEmotionMap.get(id)))+1);
                }
                else{
                    Term termObj = new Term();
                    termObj.term = uniqueTerm;
                    termObj.dCounts.set(emotionToInt(tweetEmotionMap.get(id)),1);
                    termMap.put(uniqueTerm, termObj);
                }
            }
            allUniqueNgrams.clear();
            allNgrams.clear();
        }

        for(Term term : termMap.values()){
            term.computeEmotionValues(emotionDocCounts,emotionTermCounts, tfNorm);
            weightMap.put(term.term, term.emotionValueList);
        }

        return weightMap;
    }
}
