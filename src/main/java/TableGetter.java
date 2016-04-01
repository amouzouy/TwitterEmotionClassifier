import org.codehaus.jackson.JsonParseException;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.type.TypeReference;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

public class TableGetter {

    public static void main(String [] args) throws IOException {
        String labelsPath = "/home/sheryan/IdeaProjects/emotionclassifier/dataset/labels.txt";
        String sentencesPath = "/home/sheryan/IdeaProjects/emotionclassifier/dataset/processed_tweets";
        Scanner scannerIn = new Scanner(new File(labelsPath));
        String id,idLabel,label;
        Map<String,Integer> ngramMap1 = new HashMap<>(), lemmaNgramMap1 = new HashMap<>();
        Map<String,Integer> ngramMap2 = new HashMap<>(), lemmaNgramMap2 = new HashMap<>();
        Map<String,Double> goodWords = new HashMap<>();
        ObjectMapper objectMapper = new ObjectMapper();
        Map<String,String> tweetEmotionMap = new HashMap<>();
        Map<String,Integer> f1 = new HashMap<>(),f2 = new HashMap<>(),f3 = new HashMap<>(),f4 = new HashMap<>();
        Map<String,Integer> d1 = new HashMap<>(),d2 = new HashMap<>(),d3 = new HashMap<>(),d4 = new HashMap<>();
        while (scannerIn.hasNext()) {
            idLabel = scannerIn.nextLine();
            id = idLabel.substring(0, 18);
            label = idLabel.substring(19);
            tweetEmotionMap.put(id,label);

            TweetInfo tweetInfo = objectMapper.readValue(new File(sentencesPath + "/" + id + ".json"), TweetInfo.class);
            PTFeatures ptFeatures;
            List<String> trees = tweetInfo.getTweetSentenceList().stream()
                    .map(tweetSentence -> tweetSentence.getConstTree())
                    .collect(Collectors.toList());

            ptFeatures = new PTFeatures(trees);

            f1.putAll(ptFeatures.getChildCounts(0));
            f2.putAll(ptFeatures.getNtPOSTags(0));
            f3.putAll(ptFeatures.getChildCountsHead(0));
            f4.putAll(ptFeatures.getRewriteRules(0));

            DTFeatures dtFeatures;
            List<String> depTrees = tweetInfo.getTweetSentenceList().stream()
                    .map(tweetSentence -> tweetSentence.getDepTree())
                    .collect(Collectors.toList());

            dtFeatures = new DTFeatures(depTrees);

            d1.putAll(dtFeatures.getDependencyCounts(0));
            d2.putAll(dtFeatures.getDepSkeletons(0));
            d3.putAll(dtFeatures.getDepRulesSimple(0));
            d4.putAll(dtFeatures.getDepRules(0));
//            for(String lemu : tweetInfo.getLemUnigrams()){
//                if(lemmaNgramMap1.containsKey(lemu)){
//                    lemmaNgramMap1.put(lemu,lemmaNgramMap1.get(lemu)+1);
//                }
//                else{
//                    lemmaNgramMap1.put(lemu,1);
//                }
//            }
//            for(String lemb : tweetInfo.getLemBigrams()){
//                if(lemmaNgramMap2.containsKey(lemb)){
//                    lemmaNgramMap2.put(lemb,lemmaNgramMap2.get(lemb)+1);
//                }
//                else{
//                    lemmaNgramMap2.put(lemb,1);
//                }
//            }
//            for(String u : tweetInfo.getUnigrams()){
//                if(ngramMap1.containsKey(u)){
//                    ngramMap1.put(u,ngramMap1.get(u)+1);
//                }
//                else{
//                    ngramMap1.put(u,1);
//                }
//            }
//            for(String b : tweetInfo.getBigrams()){
//                if(ngramMap2.containsKey(b)){
//                    ngramMap2.put(b,ngramMap2.get(b)+1);
//                }
//                else{
//                    ngramMap2.put(b,1);
//                }
//            }
        }
        System.out.println(f1.size()+" "+f2.size()+" "+f3.size()+" "+f4.size());
        System.out.println(d1.size()+" "+d2.size()+" "+d3.size()+" "+d4.size());
//        HashMap<String,Integer> posCountMap = new HashMap<>();
//        HashMap<String,Double> posValMap = new HashMap<>();
//        Set<String> posFilterSet = new HashSet<>();
//        List<POSTagValues> posTagValuesList;
//        Map<String,Integer> sortedPOS1,sortedPOS2;
//        String posJsonPath = "/home/sheryan/IdeaProjects/emotionclassifier/POS";
//        posCountMap = objectMapper.readValue(new File(posJsonPath + "counts.json"), new TypeReference<Map<String, Integer>>() {
//        });
//        sortedPOS1 = FeatureExtractor.sortByValues(posCountMap, true);
//        posTagValuesList = objectMapper.readValue(new File(posJsonPath+"values.json"), new TypeReference<List<POSTagValues>>() {
//        });
//        posTagValuesList.stream().forEach(posTagValues -> posValMap.put(posTagValues.getPosTag(),posTagValues.getSeparationValue()));
//        sortedPOS2 = FeatureExtractor.sortByValues(posValMap,false);
//        for(Map.Entry<String,Integer> entry: sortedPOS1.entrySet()){
//            System.out.print(entry.getKey()+":"+entry.getValue()+" ");
//        }
//        System.out.println();
//        for(Map.Entry<String,Integer> entry: sortedPOS2.entrySet()){
//            System.out.print(entry.getKey()+":"+entry.getValue()+" ");
//        }

//        List<Integer> l1 = new ArrayList<>();
//        l1.add(1);l1.add(5);l1.add(10);l1.add(20);l1.add(50);l1.add(100);
//
//        for(Integer i : l1){
//            ngramMap1.values().removeIf(val -> val < i);
//            ngramMap2.values().removeIf(val -> val < i);
//            lemmaNgramMap1.values().removeIf(val -> val < i);
//            lemmaNgramMap2.values().removeIf(val -> val < i);
//
//            System.out.println(i+": "+ngramMap1.size() + " " +
//                            ngramMap2.size() + " " +
//                            lemmaNgramMap1.size() + " " +
//                            lemmaNgramMap2.size()
//            );
//        }

//        Map<String,List<Integer>> m0 = WeightsGenerator.getWeightMap(tweetEmotionMap,true,0);
//        Map<String,List<Integer>> m1 = WeightsGenerator.getWeightMap(tweetEmotionMap,true,1);
//        Map<String,List<Integer>> m2 = WeightsGenerator.getWeightMap(tweetEmotionMap,true,2);
//        List<String> list = new ArrayList<>();
//        list.add("amaze");
//        list.add("depressed");
//        list.add("bliss");
//        list.add("detest");
//        list.add("terror");
//        list.add("rage");
//
//
//        for(String word : list){
//            System.out.print("0 "+m0.get(word).toString());
//            System.out.print(" 1 "+m1.get(word).toString());
//            System.out.println(" 2 "+m2.get(word).toString());
//        }

//        for(Map.Entry<String,Integer> entry : lemmaNgramMap.entrySet()){
//            if(ngramMap.containsKey(entry.getKey())){
//                if(entry.getValue()/(double)ngramMap.get(entry.getKey())>50){
//                    goodWords.put(entry.getKey(),entry.getValue()/((double)ngramMap.get(entry.getKey())));
//                }
//            }
//        }
//        goodWords = FeatureExtractor.sortByValues(goodWords, true);
//        for(String key : goodWords.keySet()){
//            System.out.println(key + " " + goodWords.get(key));
//        }
    }
}
