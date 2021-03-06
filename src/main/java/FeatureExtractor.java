import com.google.common.collect.Lists;
import edu.stanford.nlp.semgraph.SemanticGraph;
import edu.stanford.nlp.trees.SimpleTree;
import edu.stanford.nlp.trees.Tree;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.type.TypeReference;

import java.io.*;
import java.util.*;
import java.util.stream.Collectors;

public class FeatureExtractor {

    public static void main(String [] args) throws IOException {

        Map<String,String> labelMap = new HashMap<>();
        Map<String,Integer> unigramMap = new HashMap<>();
        Map<String,Integer> bigramMap = new HashMap<>();
        Map<String,Integer> featureVector = new HashMap<>();
        Map<String,FeatureClassDist> featureClassDistMap = new HashMap<>();
        Map<String,List<Integer>> weightMap = null;
        List<String> docIds = new ArrayList<>();
        Map<String,Integer> sortedPOS;
        Map<String,Integer> totalPOSCount = new HashMap<>();

        boolean ub=false,bb=false,posF=false,posC=false,ir=false,lemmaT=false, pt=false,dt=false,weights = false;
        int uFreqCutoff = 0, bFreqCutoff = 0, tfNorm=0, posCCutoff=0, posFCutoff=0,posCMap=0,posFMap=0;
        String dtFeats="",ptFeats="";
        String labelsPath="", sentencesPath="", posJsonPath="";

        switch(System.getProperty("os.name")){
            case "Windows 10":
                labelsPath = "C:/cygwin/home/Sheryan/EmotionClassifier/TwitterEmotionClassifier/dataset/labels.txt";
                sentencesPath = "C:/cygwin/home/Sheryan/EmotionClassifier/TwitterEmotionClassifier/dataset/processed_tweets";
                posJsonPath = "C:/cygwin/home/Sheryan/EmotionClassifier/TwitterEmotionClassifier/POS";
                break;
            case "Linux":
                labelsPath = "/home/sheryan/IdeaProjects/emotionclassifier/dataset/labels.txt";
                sentencesPath = "/home/sheryan/IdeaProjects/emotionclassifier/dataset/processed_tweets";
                posJsonPath = "/home/sheryan/IdeaProjects/emotionclassifier/POS";
                break;
        }

        String idLabel, id, label;
        int numFolds;

        numFolds = Integer.parseInt(args[0]);
        String param;
        ObjectMapper objectMapper = new ObjectMapper();
        long startTime = System.currentTimeMillis();

        for(int i=1;i<args.length;i=i+2){
            param = args[i].substring(1);
            switch (param){
                case "ub":
                    ub = true;
                    uFreqCutoff = Integer.parseInt(args[i + 1]);
                    break;
                case "bb":
                    bb = true;
                    bFreqCutoff = Integer.parseInt(args[i + 1]);
                    break;
                case "wTI":
                    weights = true;
                    tfNorm = Integer.parseInt(args[i+1]);
                    break;
                case "posF":
                    posF=true;
                    posFCutoff = Integer.parseInt(args[i+1]);
                    posFMap = Integer.parseInt(args[i+2]);
                    i++;
                    break;
                case "posC":
                    posC=true;
                    posCCutoff = Integer.parseInt(args[i+1]);
                    posCMap = Integer.parseInt(args[i+2]);
                    i++;
                    break;
                case "pt":
                    pt=true;
                    ptFeats = args[i+1];
                    break;
                case "ir":
                    ir = true;
                    FeatureClassDist.percentDiff = Double.parseDouble(args[i+1]);
                    break;
                case "dt":
                    dt=true;
                    dtFeats = args[i+1];
                    break;
                case "l":
                    lemmaT = true;
                    i--;
                    break;
            }
        }

        Scanner scannerIn;

        //loop over folds, for each fold, skip the 20% test data when creating feature vector,
        //when feature vector is created, loop over test data and populate
        Map<String,String> tweetEmotionMap = new HashMap<>();
        HashMap<String,Integer> posCountMap = new HashMap<>();
        HashMap<String,Double> posValMap = new HashMap<>();
        Set<String> posFilterSet = new HashSet<>();
        List<POSTagValues> posTagValuesList;

        int countF = 0;

        if(posF){
            if(posFMap==0){
                posCountMap = objectMapper.readValue(new File(posJsonPath+"counts.json"), new TypeReference<Map<String,Integer>>() {
                });
                sortedPOS = sortByValues(posCountMap,true);
            }
            else{
                posTagValuesList = objectMapper.readValue(new File(posJsonPath+"values.json"), new TypeReference<List<POSTagValues>>() {
                });
                posTagValuesList.stream().forEach(posTagValues -> posValMap.put(posTagValues.getPosTag(),posTagValues.getSeparationValue()));
                sortedPOS = sortByValues(posValMap,false);
            }
            for(Map.Entry<String,Integer> entry : sortedPOS.entrySet()){
                if(countF == posFCutoff){
                    break;
                }
                posFilterSet.add(entry.getKey());
                countF++;
            }
        }

        for(int num=0;num<numFolds;num++) {
            labelMap.clear(); unigramMap.clear();bigramMap.clear();featureVector.clear();featureClassDistMap.clear();docIds.clear();
            posCountMap.clear();
            scannerIn = new Scanner(new File(labelsPath));
            int counter=0;
            while (scannerIn.hasNext()) {
                if (counter == 17000){
                    break;
                }
                idLabel = scannerIn.nextLine();
                id = idLabel.substring(0, 18);
                label = idLabel.substring(19);
                labelMap.put(id, label);
                docIds.add(id);
                if ((counter>=(num*3400)) &&  (counter<((num+1)*3400))){
                    counter++;
                    continue;
                }
                else if (weights){
                    tweetEmotionMap.put(id,label);
                }

                TweetInfo tweetInfo = objectMapper.readValue(new File(sentencesPath + "/" + id + ".json"), TweetInfo.class);
                List<String> totalPOSlist = tweetInfo.getTweetSentenceList().stream().flatMap(
                        tweetSentence -> tweetSentence.getPOSTags().stream()).collect(Collectors.toList());
                if(ub){
                    if(lemmaT){
                        for(String lug:tweetInfo.getLemUnigrams()){
                            if(!posF || posFilterSet.contains(totalPOSlist.get(tweetInfo.getLemUnigrams().indexOf(lug)))) {
                                if (unigramMap.containsKey(lug)) {
                                    unigramMap.put(lug, unigramMap.get(lug) + 1);
                                } else {
                                    unigramMap.put(lug, 1);
                                }
                            }
                        }
                    }
                    else{
                        for(String ug:tweetInfo.getUnigrams()){
                            if(!posF || posFilterSet.contains(totalPOSlist.get(tweetInfo.getUnigrams().indexOf(ug)))) {
                                if (unigramMap.containsKey(ug)) {
                                    unigramMap.put(ug, unigramMap.get(ug) + 1);
                                } else {
                                    unigramMap.put(ug, 1);
                                }
                            }
                        }
                    }
                }

                if(bb){
                    if(lemmaT){
                        for(String lbg:tweetInfo.getLemBigrams()){
                            if (bigramMap.containsKey(lbg)) {
                                bigramMap.put(lbg, bigramMap.get(lbg) + 1);
                            } else {
                                bigramMap.put(lbg, 1);
                            }
                        }
                    }
                    else{
                        for(String bg:tweetInfo.getBigrams()){
                            if (bigramMap.containsKey(bg)) {
                                bigramMap.put(bg, bigramMap.get(bg) + 1);
                            } else {
                                bigramMap.put(bg, 1);
                            }
                        }
                    }
                }

                if(posC){
                    for(TweetSentence tweetSent : tweetInfo.getTweetSentenceList()){
                        for(String posTag : tweetSent.getPOSTags()){
                            if(posCountMap.containsKey(posTag)){
                                posCountMap.put(posTag,posCountMap.get(posTag)+1);
                            }
                            else{
                                posCountMap.put(posTag,1);
                            }
                        }
                    }
                }

                if(pt){
                    PTFeatures ptFeatures;
                    List<String> trees = tweetInfo.getTweetSentenceList().stream()
                            .map(tweetSentence -> tweetSentence.getConstTree())
                            .collect(Collectors.toList());

                    ptFeatures = new PTFeatures(trees);

                    if(ptFeats.charAt(0) == '1') {
                        featureVector.putAll(ptFeatures.getChildCounts(0));
                    }
                    if(ptFeats.charAt(1) == '1') {
                        featureVector.putAll(ptFeatures.getNtPOSTags(0));
                    }
                    if(ptFeats.charAt(2) == '1'){
                        featureVector.putAll(ptFeatures.getChildCountsHead(0));
                    }
                    if(ptFeats.charAt(3) == '1'){
                        featureVector.putAll(ptFeatures.getRewriteRules(0));
                    }
                }

                if(dt){
                    DTFeatures dtFeatures;
                    List<String> depTrees = tweetInfo.getTweetSentenceList().stream()
                            .map(tweetSentence -> tweetSentence.getDepTree())
                            .collect(Collectors.toList());

                    dtFeatures = new DTFeatures(depTrees);

                    if(dtFeats.charAt(0) == '1'){
                        featureVector.putAll(dtFeatures.getDependencyCounts(0));
                    }
                    if(dtFeats.charAt(1) == '1'){
                        featureVector.putAll(dtFeatures.getDepSkeletons(0));
                    }
                    if(dtFeats.charAt(2) == '1'){
                        featureVector.putAll(dtFeatures.getDepRulesSimple(0));
                    }
                    if(dtFeats.charAt(3) == '1') {
                        featureVector.putAll(dtFeatures.getDepRules(0));
                    }
                }
                counter++;
            }

            if(ub) {
                final int finalUFreqCutoff = uFreqCutoff;
                unigramMap.values().removeIf(val -> val < finalUFreqCutoff);
                featureVector.putAll(unigramMap);
            }
            if(bb){
                final int finalBFreqCutoff = bFreqCutoff;
                bigramMap.values().removeIf(val -> val < finalBFreqCutoff);
                featureVector.putAll(bigramMap);
            }

            if(weights){
                weightMap = WeightsGenerator.getWeightMap(tweetEmotionMap,lemmaT, tfNorm);
            }

            if(posC){
                if(posCMap == 0) {
                    sortedPOS = sortByValues(posCountMap,true);
                }
                else {
                    posTagValuesList = objectMapper.readValue(new File(posJsonPath+"values.json"), new TypeReference<List<POSTagValues>>() {
                    });
                    posTagValuesList.stream().forEach(posTagValues -> posValMap.put(posTagValues.getPosTag(),posTagValues.getSeparationValue()));
                    sortedPOS = sortByValues(posValMap,false);
                }
                int posCount=0;
                for(Map.Entry entry: sortedPOS.entrySet()){
                    if(posCount == posCCutoff){
                        break;
                    }
                    posCount++;
                    featureVector.put((String)entry.getKey(),0);
                }
            }

            //if time permits
            if(ir) {
                int removedFeatures = 0;
                for (Iterator<Map.Entry<String, Integer>> it = featureVector.entrySet().iterator(); it.hasNext(); ) {
                    Map.Entry<String, Integer> entry = it.next();
                    if (featureClassDistMap.get(entry.getKey()).isIrrelevantFeature()) {
                        it.remove();
                        removedFeatures++;
                    }
                }
                System.out.println("Removed features number: "+removedFeatures);
            }

            counter = 0;
            PrintWriter writer1 = null, writer2 = null;
            switch(System.getProperty("os.name")){
                case "Windows 10":
                    writer1 = new PrintWriter("C:/cygwin/home/Sheryan/EmotionClassifier/TwitterEmotionClassifier/dataset/training/file"+num+".train", "UTF-8");
                    writer2 = new PrintWriter("C:/cygwin/home/Sheryan/EmotionClassifier/TwitterEmotionClassifier/dataset/testing/file"+num+".test", "UTF-8");
                    break;
                case "Linux":
                    writer1 = new PrintWriter("/home/sheryan/IdeaProjects/emotionclassifier/dataset/training/file"+num+".train", "UTF-8");
                    writer2 = new PrintWriter("/home/sheryan/IdeaProjects/emotionclassifier/dataset/testing/file"+num+".test", "UTF-8");
                    break;
            }

            int trainingDocs = 0, testingDocs = 0;
            for (String docId : docIds) {
                counter++;
                setAllValuesToZero((HashMap<String, Integer>) featureVector);
                TweetInfo tweetInfo = objectMapper.readValue(new File(sentencesPath + "/" + docId + ".json"), TweetInfo.class);
                List<String> totalPOSlist = tweetInfo.getTweetSentenceList().stream().flatMap(
                        tweetSentence -> tweetSentence.getPOSTags().stream()).collect(Collectors.toList());
                if(ub){
                    if(lemmaT){
                        for(String lug:tweetInfo.getLemUnigrams()){
                            if(!posF || posFilterSet.contains(totalPOSlist.get(tweetInfo.getLemUnigrams().indexOf(lug)))) {
                                if (featureVector.containsKey(lug)) {
                                    if (weights) {
                                        featureVector.put(lug, weightMap.get(lug).indexOf(1) + 1);
                                    } else {
                                        featureVector.put(lug, 1);
                                    }
                                }
                            }
                        }
                    }
                    else{
                        for(String ug:tweetInfo.getUnigrams()){
                            if (featureVector.containsKey(ug)) {
                                if(!posF || posFilterSet.contains(totalPOSlist.get(tweetInfo.getUnigrams().indexOf(ug)))) {
                                    if (weights) {
                                        featureVector.put(ug, weightMap.get(ug).indexOf(1) + 1);
                                    } else {
                                        featureVector.put(ug, 1);
                                    }
                                }
                            }
                        }
                    }
                }

                if(bb){
                    if(lemmaT){
                        for(String lbg:tweetInfo.getLemBigrams()){
                            if (featureVector.containsKey(lbg)) {
                                if(weights){
                                    featureVector.put(lbg, weightMap.get(lbg).indexOf(1)+1);
                                }
                                else{
                                    featureVector.put(lbg, 1);
                                }
                            }
                        }
                    }
                    else{
                        for(String bg:tweetInfo.getBigrams()){
                            if (featureVector.containsKey(bg)) {
                                if(weights){
                                    featureVector.put(bg, weightMap.get(bg).indexOf(1) + 1);
                                }
                                else{
                                    featureVector.put(bg, 1);
                                }
                            }
                        }
                    }
                }
                if(posC){
                    for(TweetSentence tweetSent : tweetInfo.getTweetSentenceList()){
                        for(String posTag : tweetSent.getPOSTags()){
                            if(featureVector.containsKey(posTag)){
                                featureVector.put(posTag,featureVector.get(posTag)+1);
                            }
                        }
                    }
                }
                if(pt){
                    PTFeatures ptFeatures;
                    List<String> trees = tweetInfo.getTweetSentenceList().stream()
                            .map(tweetSentence -> tweetSentence.getConstTree())
                            .collect(Collectors.toList());
                    ptFeatures = new PTFeatures(trees);

                    for(String childCounts : ptFeatures.getChildCounts(1).keySet()){
                        if(featureVector.containsKey(childCounts)){
                            featureVector.put(childCounts,ptFeatures.getChildCounts(1).get(childCounts));
                        }
                    }
                    for(String rewriteRule : ptFeatures.getRewriteRules(1).keySet()){
                        if(featureVector.containsKey(rewriteRule)){
                            featureVector.put(rewriteRule,ptFeatures.getRewriteRules(1).get(rewriteRule));
                        }
                    }
                    for(String ntPosTag : ptFeatures.getNtPOSTags(1).keySet()){
                        if(featureVector.containsKey(ntPosTag)){
                            featureVector.put(ntPosTag,ptFeatures.getNtPOSTags(1).get(ntPosTag));
                        }
                    }
                    for(String simpleRule : ptFeatures.getChildCountsHead(1).keySet()){
                        if(featureVector.containsKey(simpleRule)){
                            featureVector.put(simpleRule,ptFeatures.getChildCountsHead(1).get(simpleRule));
                        }
                    }
                }
                if(dt){
                    DTFeatures dtFeatures;
                    List<String> depTrees = tweetInfo.getTweetSentenceList().stream()
                            .map(tweetSentence -> tweetSentence.getDepTree())
                            .collect(Collectors.toList());
                    dtFeatures = new DTFeatures(depTrees);

                    for(String depCounts : dtFeatures.getDependencyCounts(1).keySet()){
                        if(featureVector.containsKey(depCounts)){
                            featureVector.put(depCounts,dtFeatures.getDependencyCounts(1).get(depCounts));
                        }
                    }
                    for(String rewriteRule : dtFeatures.getDepRules(1).keySet()){
                        if(featureVector.containsKey(rewriteRule)){
                            featureVector.put(rewriteRule,dtFeatures.getDepRules(1).get(rewriteRule));
                        }
                    }
                    for(String skeleton : dtFeatures.getDepSkeletons(1).keySet()){
                        if(featureVector.containsKey(skeleton)){
                            featureVector.put(skeleton,dtFeatures.getDepSkeletons(1).get(skeleton));
                        }
                    }
                    for(String simpleRule : dtFeatures.getDepRulesSimple(1).keySet()){
                        if(featureVector.containsKey(simpleRule)){
                            featureVector.put(simpleRule,dtFeatures.getDepRulesSimple(1).get(simpleRule));
                        }
                    }
                }
                label = labelMap.get(docId);
                int featureCounter;
                if ((counter>=(num*3400)) &&  (counter<((num+1)*3400))){
                    testingDocs++;
                    writer2.print((int) emotionToDouble(label));
                    featureCounter = 1;
                    for (Iterator<Map.Entry<String, Integer>> it = featureVector.entrySet().iterator(); it.hasNext(); ) {
                        Map.Entry<String, Integer> entry = it.next();
                        writer2.print(" " + featureCounter + ":" + entry.getValue());
                        featureCounter++;
                    }
                    writer2.println();
                }
                else {
                    trainingDocs++;
                    writer1.print((int) emotionToDouble(label));
                    featureCounter = 1;
                    for (Map.Entry<String, Integer> entry : featureVector.entrySet()) {
                        writer1.print(" " + featureCounter + ":" + entry.getValue());
                        featureCounter++;
                    }
                    writer1.println();
                }
            }
            writer1.close();
            writer2.close();
            scannerIn.close();
        }
        long endTime   = System.currentTimeMillis();
        System.out.println((endTime - startTime) / 1000.000 + " seconds");
    }

    private static double emotionToDouble(String label) {
        switch (label){
            case "surprise":
                return 0.0;
            case "sadness":
                return 1.0;
            case "joy":
                return 2.0;
            case "disgust":
                return 3.0;
            case "fear":
                return 4.0;
            case "anger":
                return 5.0;
            default:
                System.err.println("Error with label: "+label);
                System.exit(-1);
                return 6.0;
        }
    }

    public static void setAllValuesToZero(Map<String, Integer> hm){
        for (Map.Entry<String, Integer> entry : hm.entrySet()) {
            hm.put(entry.getKey(), 0);
        }
    }

    private static void updateFeatureClassDistMap(Map<String, FeatureClassDist> featureClassDistMap, String ngram, String label){
        if(featureClassDistMap.containsKey(ngram)){
            FeatureClassDist featureClassDist = featureClassDistMap.get(ngram);
            featureClassDist.updateCounts(label);
            featureClassDistMap.put(ngram,featureClassDist);
        }
        else{
            featureClassDistMap.put(ngram,new FeatureClassDist(label));
        }
    }

    public static HashMap sortByValues(Map map, boolean reverseList) {
        List list = new LinkedList(map.entrySet());
        // Defined Custom Comparator here
        Collections.sort(list, new Comparator() {
            public int compare(Object o1, Object o2) {
                return ((Comparable) ((Map.Entry) (o1)).getValue())
                        .compareTo(((Map.Entry) (o2)).getValue());
            }
        });

        // Here I am copying the sorted list in HashMap
        // using LinkedHashMap to preserve the insertion order
        HashMap sortedHashMap = new LinkedHashMap();
        if(reverseList) {
            list = Lists.reverse(list);
        }
        for (Iterator it = list.iterator(); it.hasNext();) {
            Map.Entry entry = (Map.Entry) it.next();
            sortedHashMap.put(entry.getKey(), entry.getValue());
        }
        return sortedHashMap;
    }
}
