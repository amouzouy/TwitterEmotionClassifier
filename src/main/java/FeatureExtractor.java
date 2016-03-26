import org.codehaus.jackson.map.ObjectMapper;

import java.io.*;
import java.util.*;

public class FeatureExtractor {

    public static void main(String [] args) throws IOException {

        Map<String,String> labelMap = new HashMap<>();
        Map<String,Integer> unigramMap = new HashMap<>();
        Map<String,Integer> bigramMap = new HashMap<>();
        Map<String,Integer> featureVector = new HashMap<>();
        Map<String,FeatureClassDist> featureClassDistMap = new HashMap<>();
        Map<String,List<Integer>> weightMap = null;
        List<String> docIds = new ArrayList<>();

        boolean ub=false,bb=false,up=false,bp=false,posB=false,ir=false,lemmaT=false, pt=false,dt=false,weights = false;
        int uFreqCutoff = 0, bFreqCutoff = 0, tfNorm=0;
        String labelsPath="", sentencesPath="";

        switch(System.getProperty("os.name")){
            case "Windows 10":
                labelsPath = "C:/cygwin/home/Sheryan/EmotionClassifier/TwitterEmotionClassifier/dataset/labels.txt";
                sentencesPath = "C:/cygwin/home/Sheryan/EmotionClassifier/TwitterEmotionClassifier/dataset/processed_tweets";
                break;
            case "Linux":
                labelsPath = "/home/sheryan/IdeaProjects/emotionclassifier/dataset/labels.txt";
                sentencesPath = "/home/sheryan/IdeaProjects/emotionclassifier/dataset/processed_tweets";
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
                case "wPOS":

                    break;
                case "pos":
                    posB=true;
                    break;
                case "pt":
                    pt=true;
                    break;
                case "ir":
                    ir = true;
                    FeatureClassDist.percentDiff = Double.parseDouble(args[i+1]);
                    break;
                case "dt":
                    dt=true;
                    break;
                case "l":
                    lemmaT = true;
                    i--;
                    break;
            }
        }

        /*
        * -ub n (add unigrams, with cutoff of n)
        * -bb n (add bigrams, with cutoff of n)
        * -up (unigram probabilities)
        * -bp (bigram probabilities)
        * -pos part of speech
        * -pt parse tree
        * -s stemming?
        * -ir n (irrelevance cutoff for ub,bb)
        * -d dependency tree features
        * -l use lemmatization for n-grams
        * */

        Scanner scannerIn;

        //loop over folds, for each fold, skip the 20% test data when creating feature vector,
        //when feature vector is created, loop over test data and populate
        Map<String,String> tweetEmotionMap = new HashMap<>();
        for(int num=0;num<numFolds;num++) {
            labelMap.clear(); unigramMap.clear();bigramMap.clear();featureVector.clear();featureClassDistMap.clear();docIds.clear();
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

                if(ub){
                    if(lemmaT){
                        for(String lug:tweetInfo.getLemUnigrams()){
                            if (unigramMap.containsKey(lug)) {
                                unigramMap.put(lug, unigramMap.get(lug) + 1);
                            } else {
                                unigramMap.put(lug, 1);
                            }
                        }
                    }
                    else{
                        for(String ug:tweetInfo.getUnigrams()){
                            if (unigramMap.containsKey(ug)) {
                                unigramMap.put(ug, unigramMap.get(ug) + 1);
                            } else {
                                unigramMap.put(ug, 1);
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

                if(posB){

                }

                if(pt){

                }

                if(dt){

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
                if(ub){
                    if(lemmaT){
                        for(String lug:tweetInfo.getLemUnigrams()){
                            if (featureVector.containsKey(lug)) {
                                if(weights){
                                    featureVector.put(lug, weightMap.get(lug).indexOf(1) + 1);
                                }
                                else{
                                    featureVector.put(lug, 1);
                                }
                            }
                        }
                    }
                    else{
                        for(String ug:tweetInfo.getUnigrams()){
                            if (featureVector.containsKey(ug)) {
                                if(weights){
                                    featureVector.put(ug, weightMap.get(ug).indexOf(1) + 1);
                                }
                                else{
                                    featureVector.put(ug, 1);
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
                    for (Iterator<Map.Entry<String, Integer>> it = featureVector.entrySet().iterator(); it.hasNext(); ) {
                        Map.Entry<String, Integer> entry = it.next();
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
        System.out.println((endTime-startTime)/1000.000+" seconds");
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

    private static void setAllValuesToZero(HashMap<String, Integer> hm){
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
}
