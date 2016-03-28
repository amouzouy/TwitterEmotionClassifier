import org.codehaus.jackson.map.ObjectMapper;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

public class POSTagAnalyzer {

    public static void main(String [] args) throws IOException {

        String labelsPath="",sentencesPath="";
        ObjectMapper objectMapper = new ObjectMapper();
        String idLabel,id,label;
        int counter=0;

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

        Scanner scannerIn = new Scanner(new File(labelsPath));

        List<List<List<Integer>>> posTagCounts = new ArrayList<>();
        Map<Integer,String> posMap = new HashMap<>();
        posTagCounts.add(new ArrayList<>());
        posTagCounts.add(new ArrayList<>());
        posTagCounts.add(new ArrayList<>());
        posTagCounts.add(new ArrayList<>());
        posTagCounts.add(new ArrayList<>());
        posTagCounts.add(new ArrayList<>());
        List<String> allPOSTags;
        while (scannerIn.hasNext()) {
            if (counter == 17000){
                break;
            }
            idLabel = scannerIn.nextLine();
            id = idLabel.substring(0, 18);
            label = idLabel.substring(19);
            TweetInfo tweetInfo = objectMapper.readValue(new File(sentencesPath + "/" + id + ".json"), TweetInfo.class);

            allPOSTags = tweetInfo.getTweetSentenceList().stream().flatMap(l->l.getPOSTags().stream()).collect(Collectors.toList());
            ArrayList<Integer> tweetPosCounts = new ArrayList<>(Collections.nCopies(45,0));
            for(String posTag : allPOSTags){
                tweetPosCounts.set(posToInt(posTag),tweetPosCounts.get(posToInt(posTag))+1);
                posMap.put(posToInt(posTag),posTag);
            }
            posTagCounts.get(emotionToInt(label)).add(tweetPosCounts);
            counter++;
        }


        List<POSTagValues> posTagValuesList = new ArrayList<>();

        for(int i=0;i<45;i++){
            POSTagValues posTagValue = new POSTagValues();
            posTagValue.setPosTagInt(i);
            for(int j=0;j<6;j++) {
                posTagValue.getMeanValues().set(j,average(
                        transpose(posTagCounts.get(j)).get(i))
                );
                posTagValue.getVarValues().set(j,variance(
                        transpose(posTagCounts.get(j)).get(i))
                );
            }
            posTagValuesList.add(posTagValue);
        }
        for(POSTagValues posTagValues : posTagValuesList){
            posTagValues.calculateSeparationMetric(posMap);
        }
        objectMapper.writeValue(new File("/home/sheryan/IdeaProjects/emotionclassifier/POSvalues.json"),posTagValuesList);
    }



    private static <T> List<List<T>> transpose(List<List<T>> table) {
        List<List<T>> ret = new ArrayList<>();
        final int N = table.get(0).size();
        for (int i = 0; i < N; i++) {
            List<T> col = new ArrayList<T>();
            for (List<T> row : table) {
                col.add(row.get(i));
            }
            ret.add(col);
        }
        return ret;
    }

    private static int posToInt(String posTag) {
        switch (posTag){
            case "NN":
                return 0;
            case "JJ":
                return 1;
            case "``":
                return 2;
            case "-LRB-":
                return 3;
            case "WRB":
                return 4;
            case "LS":
                return 5;
            case "PRP":
                return 6;
            case "DT":
                return 7;
            case "FW":
                return 8;
            case "NNP":
                return 9;
            case "NNS":
                return 10;
            case "JJS":
                return 11;
            case "JJR":
                return 12;
            case "UH":
                return 13;
            case "MD":
                return 14;
            case "VBD":
                return 15;
            case "WP":
                return 16;
            case "VBG":
                return 17;
            case "CC":
                return 18;
            case "''":
                return 19;
            case "CD":
                return 20;
            case "PDT":
                return 21;
            case "RBS":
                return 22;
            case "VBN":
                return 23;
            case "#":
                return 24;
            case "RBR":
                return 25;
            case "$":
                return 26;
            case "VBP":
                return 27;
            case "IN":
                return 28;
            case "WDT":
                return 29;
            case "SYM":
                return 30;
            case "NNPS":
                return 31;
            case "WP$":
                return 32;
            case "VB":
                return 33;
            case ",":
                return 34;
            case ".":
                return 35;
            case "VBZ":
                return 36;
            case "RB":
                return 37;
            case "PRP$":
                return 38;
            case "EX":
                return 39;
            case "POS":
                return 40;
            case "-RRB-":
                return 41;
            case ":":
                return 42;
            case "TO":
                return 43;
            case "RP":
                return 44;
            default:
                System.err.println("Error with posTagInt: "+posTag);
                System.exit(-1);
                return 45;
        }
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

    public static double variance(List<Integer> list) {
        double sumDiffsSquared = 0.0;
        double avg = average(list);
        for (int value : list)
        {
            double diff = value - avg;
            diff *= diff;
            sumDiffsSquared += diff;
        }
        return sumDiffsSquared  / (list.size()-1);
    }

    public static double average(List<Integer> list) {
        double average = sum(list)/(double)list.size();
        return average;
    }

    public static int sum(List<Integer> list) {
        int sum = 0;
        for(int i=0; i<list.size(); i++ ){
            sum = sum + list.get(i) ;
        }
        return sum;
    }
}
