import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.process.CoreLabelTokenFactory;
import edu.stanford.nlp.process.DocumentPreprocessor;
import edu.stanford.nlp.process.PTBTokenizer;

import java.io.*;
import java.util.*;

public class Preprocessor {

    public static void main(String [] args) throws FileNotFoundException, UnsupportedEncodingException {


        Map<String,String> labelMap = new HashMap<>();
        Map<String,Integer> unigramMap = new HashMap<>();
        Map<String,Integer> bigramMap = new HashMap<>();
        Map<String,Integer> featureVector = new HashMap<>();
        Map<String,Integer> lemmaMap = new HashMap<>();
        Map<String,FeatureClassDist> featureClassDistMap = new HashMap<>();
        List<String> docIds = new ArrayList<>();

        boolean ub=false,bb=false,up=false,bp=false,pos=false,ir=false,lemmaT=false;
        int uFreqCutoff = 0, bFreqCutoff = 0;
        String labelsPath="", sentencesPath="";

        switch(System.getProperty("os.name")){
            case "Windows 10":
                labelsPath = "C:/cygwin/home/Sheryan/EmotionClassifier/TwitterEmotionClassifier/dataset/labels.txt";
                sentencesPath = "C:/cygwin/home/Sheryan/EmotionClassifier/TwitterEmotionClassifier/dataset/sentences";
                break;
            case "Linux":
                labelsPath = "/home/sheryan/IdeaProjects/emotionclassifier/dataset/labels.txt";
                sentencesPath = "/home/sheryan/IdeaProjects/emotionclassifier/dataset/sentences";
                break;
        }

        String idLabel, id, label;
        PTBTokenizer ptbt;
        DocumentPreprocessor dp;
        String stringLabel, bigramS=null;
        int numFolds;

        numFolds = Integer.parseInt(args[0]);
        String param;
        StanfordLemmatizer stanfordLemmatizer = new StanfordLemmatizer();
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
                case "up":

                    break;
                case "bp":

                    break;
                case "pos":

                    break;
                case "pt":

                    break;
                case "ir":
                    ir = true;
                    FeatureClassDist.percentDiff = Double.parseDouble(args[i+1]);
                    break;
                case "dt":

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
        String lemmaStr;
        //loop over folds, for each fold, skip the 20% test data when creating feature vector,
        //when feature vector is created, loop over test data and populate
        for(int num=0;num<numFolds;num++) {

            labelMap.clear(); unigramMap.clear();bigramMap.clear();featureVector.clear();featureClassDistMap.clear();docIds.clear();
            scannerIn = new Scanner(new File(labelsPath));

            int counter=0;
            while (scannerIn.hasNext()) {
                idLabel = scannerIn.nextLine();
                id = idLabel.substring(0, 18);
                label = idLabel.substring(19);
                labelMap.put(id, label);
                docIds.add(id);
                if ((counter>=(num*3400)) &&  (counter<((num+1)*3400))){
                    counter++;
                    continue;
                }
                if(ub) {
                    ptbt = new PTBTokenizer(new FileReader(sentencesPath + "/" + id + ".txt"), new CoreLabelTokenFactory(), "");
                    for (CoreLabel cLabel; ptbt.hasNext(); ) {
                        cLabel = (CoreLabel) ptbt.next();
                        stringLabel = cLabel.toString();
                        if(lemmaT) {
                            stringLabel = stanfordLemmatizer.lemmatize(stringLabel).get(0);
                        }
                        if (unigramMap.containsKey(stringLabel)) {
                            unigramMap.put(stringLabel, unigramMap.get(stringLabel) + 1);
                        } else {
                            unigramMap.put(stringLabel, 1);
                        }
                        updateFeatureClassDistMap(featureClassDistMap, stringLabel, label);
                    }
                }
                if(bb) {
                    dp = new DocumentPreprocessor(sentencesPath + "/" + id + ".txt");
                    for (List sentence : dp) {
                        for (int iter = 0; iter < sentence.size(); iter++) {
                            if (iter == 0) {
                                bigramS = "<s> " + sentence.get(0);
                            }
                            if (iter > 0 && iter < (sentence.size() - 1)) {
                                bigramS = sentence.get(iter - 1) + " " + sentence.get(iter);
                            }
                            if ((iter == (sentence.size() - 1)) && sentence.size() > 1) {
                                bigramS = sentence.get(sentence.size() - 2) + " <s>";
                            }
                            if ((iter == (sentence.size() - 1)) && sentence.size() == 1) {
                                bigramS = sentence.get(0) + " <s>";
                            }
                            if(lemmaT) {
                                bigramS = String.join(" ", stanfordLemmatizer.lemmatize(bigramS));
                            }
                            if (bigramMap.containsKey(bigramS)) {
                                bigramMap.put(bigramS, bigramMap.get(bigramS) + 1);
                            } else {
                                bigramMap.put(bigramS, 1);
                            }
                            updateFeatureClassDistMap(featureClassDistMap, bigramS, label);
                        }
                    }
                }
                counter++;
            }

            //remove all entries in n-gram maps which have count less then n
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
                if(ub) {
                    ptbt = new PTBTokenizer(new FileReader(sentencesPath + "/" + docId + ".txt"), new CoreLabelTokenFactory(), "");
                    for (CoreLabel cLabel; ptbt.hasNext(); ) {
                        cLabel = (CoreLabel) ptbt.next();
                        stringLabel = cLabel.toString();
                        if(lemmaT) {
                            stringLabel = stanfordLemmatizer.lemmatize(stringLabel).get(0);
                        }
                        if (featureVector.containsKey(stringLabel)) {
                            featureVector.put(stringLabel, 1);
                        }
                    }
                }
                if(bb) {
                    dp = new DocumentPreprocessor(sentencesPath + "/" + docId + ".txt");
                    for (List sentence : dp) {
                        for (int iter = 0; iter < sentence.size(); iter++) {
                            if (iter == 0) {
                                bigramS = "<s> " + sentence.get(0);
                            }
                            if (iter > 0 && iter < (sentence.size() - 1)) {
                                bigramS = sentence.get(iter - 1) + " " + sentence.get(iter);
                            }
                            if ((iter == (sentence.size() - 1)) && sentence.size() > 1) {
                                bigramS = sentence.get(sentence.size() - 2) + " <s>";
                            }
                            if ((iter == (sentence.size() - 1)) && sentence.size() == 1) {
                                bigramS = sentence.get(0) + " <s>";
                            }
                            if(lemmaT) {
                                bigramS = String.join(" ", stanfordLemmatizer.lemmatize(bigramS));
                            }
                            if (featureVector.containsKey(bigramS)) {
                                featureVector.put(bigramS, 1);
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

//        System.out.println((endTime-startTime)/1000.000+" seconds");
//        System.out.println("Feature vector size: "+featureVector.size()+" Lemma vector size: "+lemmaMap.size());
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
