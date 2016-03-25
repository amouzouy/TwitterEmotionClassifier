import org.codehaus.jackson.map.ObjectMapper;

import java.io.*;
import java.util.*;

public class WeightsGenerator {

    public static void main(String [] args) throws IOException {

        Map<String, List<Integer>> weightMap = new HashMap<>();
        Map<String, Term> termMap = new HashMap<>();
        List<String> allNgrams = new ArrayList<>();
        Set<String> allUniqueNgrams;
        Map<String, String> tweetEmotionMap= new HashMap<>();
        String tweetsPath = "/home/sheryan/IdeaProjects/emotionclassifier/dataset/processed_tweets";
        List<Integer> emotionCounts = new ArrayList<>(Collections.nCopies(6,0));

        ObjectMapper objectMapper = new ObjectMapper();
        BufferedReader bufferedReader = new BufferedReader(new FileReader("/home/sheryan/IdeaProjects/emotionclassifier/dataset/labels.txt"));

        String line;
        int index=0;
        while((line=bufferedReader.readLine())!=null){
            if(index==17000){
                break;
            }
            index++;
            tweetEmotionMap.put(line.split(" ")[0],line.split(" ")[1]);
        }

        for(String id : tweetEmotionMap.keySet()){
            String emotion = tweetEmotionMap.get(id);
            emotionCounts.set(emotionToInt(emotion),emotionCounts.get(emotionToInt(emotion))+1);
            TweetInfo tweetInfo = objectMapper.readValue(new File(tweetsPath + "/" + id + ".json"), TweetInfo.class);
//            allNgrams.addAll(tweetInfo.getUnigrams());
            allNgrams.addAll(tweetInfo.getLemUnigrams());
//            allNgrams.addAll(tweetInfo.getBigrams());
            allNgrams.addAll(tweetInfo.getLemBigrams());
            for(String  term: allNgrams){
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
            term.computeEmotionValues(emotionCounts);
            weightMap.put(term.term, term.emotionValueList);
        }
        objectMapper.writeValue(new File("/home/sheryan/IdeaProjects/emotionclassifier/dataset/tfidf_maps/lemma_weights.json"),weightMap);
    }

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
}
