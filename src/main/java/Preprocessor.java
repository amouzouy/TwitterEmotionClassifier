import edu.stanford.nlp.ling.CoreAnnotations;
import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.pipeline.Annotation;
import edu.stanford.nlp.pipeline.StanfordCoreNLP;
import edu.stanford.nlp.process.DocumentPreprocessor;
import edu.stanford.nlp.semgraph.SemanticGraph;
import edu.stanford.nlp.semgraph.SemanticGraphCoreAnnotations;
import edu.stanford.nlp.trees.Tree;
import edu.stanford.nlp.trees.TreeCoreAnnotations;
import edu.stanford.nlp.util.CoreMap;
import org.codehaus.jackson.map.ObjectMapper;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.List;
import java.util.Properties;
import java.util.Scanner;

public class Preprocessor {

    public static void main(String [] args) throws IOException {

        String labelsPath="", sentencesPath="";
        Properties props = new Properties();
        props.put("annotators", "tokenize, ssplit,pos,lemma,parse");
        StanfordCoreNLP pipeline = new StanfordCoreNLP(props);
        String line;
        StanfordLemmatizer stanfordLemmatizer = new StanfordLemmatizer();

        switch(System.getProperty("os.name")){
            case "Windows 10":
                labelsPath = "C:/cygwin/home/Sheryan/EmotionClassifier/TwitterEmotionClassifier/dataset/labels.txt";
                sentencesPath = "C:/cygwin/home/Sheryan/EmotionClassifier/TwitterEmotionClassifier/dataset/tweets";
                break;
            case "Linux":
                labelsPath = "/home/sheryan/IdeaProjects/emotionclassifier/dataset/labels.txt";
                sentencesPath = "/home/sheryan/IdeaProjects/emotionclassifier/dataset/tweets";
                break;
        }

        String idLabel, id;
        DocumentPreprocessor dp;
        String unigram, lemmaU, bigram="", lemmaB="";
        Scanner scannerIn = new Scanner(new File(labelsPath));

        ObjectMapper mapper = new ObjectMapper();

        while (scannerIn.hasNext()) {

            TweetInfo tweetInfo = new TweetInfo();
            idLabel = scannerIn.nextLine();
            id = idLabel.substring(0, 18);
            BufferedReader reader = new BufferedReader(new FileReader(sentencesPath + "/" + id + ".txt"));
            line = reader.readLine();
            Annotation document = new Annotation(line);
            pipeline.annotate(document);

            List<CoreMap> sentences = document.get(CoreAnnotations.SentencesAnnotation.class);
            for(CoreMap sentence: sentences) {
                TweetSentence tweetSentence = new TweetSentence();
                for (CoreLabel token: sentence.get(CoreAnnotations.TokensAnnotation.class)) {
                    unigram = token.get(CoreAnnotations.TextAnnotation.class);
                    lemmaU = token.get(CoreAnnotations.LemmaAnnotation.class);
                    String pos = token.get(CoreAnnotations.PartOfSpeechAnnotation.class);

                    tweetInfo.getUnigrams().add(unigram);
                    tweetInfo.getLemUnigrams().add(lemmaU);
                    tweetSentence.getPOSTags().add(pos);
                }

                Tree tree = sentence.get(TreeCoreAnnotations.TreeAnnotation.class);
                SemanticGraph dependencies = sentence.get(SemanticGraphCoreAnnotations.CollapsedCCProcessedDependenciesAnnotation.class);
                tweetSentence.setDepTree(dependencies.toCompactString(true));
                tweetSentence.setConstTree(tree.toString());
                tweetInfo.getTweetSentenceList().add(tweetSentence);
            }

            dp = new DocumentPreprocessor(sentencesPath + "/" + id + ".txt");
            for (List sentence : dp) {
                for (int iter = 0; iter < sentence.size(); iter++) {
                    if (iter == 0) {
                        bigram = "<s> " + sentence.get(0);
                        lemmaB = String.join(" ", stanfordLemmatizer.lemmatize(bigram));
                    }
                    if (iter > 0) {
                        bigram = sentence.get(iter - 1) + " " + sentence.get(iter);
                        lemmaB = String.join(" ", stanfordLemmatizer.lemmatize(bigram));
                    }
                    if ((iter == (sentence.size() - 1))) {
                        bigram = sentence.get(iter) + " <s>";
                        lemmaB = String.join(" ", stanfordLemmatizer.lemmatize(bigram));
                    }
                    tweetInfo.getBigrams().add(bigram);
                    tweetInfo.getLemBigrams().add(lemmaB);
                }
            }
            mapper.writeValue(new File("/home/sheryan/IdeaProjects/emotionclassifier/dataset/processed_tweets/"+id+".json"), tweetInfo);
        }
    }
}
