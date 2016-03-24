import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.util.Scanner;

public class Separator {

    public static void main(String [] args) throws FileNotFoundException, UnsupportedEncodingException {
        String emotions = "surprisesadnessjoydisgustfearanger";
        Scanner scannerIn = new Scanner(new File("/home/sheryan/IdeaProjects/emotionclassifier/dataset/tweetsValidation.txt"));
        String nextSent,extractedSent,id,label;
        PrintWriter labelWriter = new PrintWriter("/home/sheryan/IdeaProjects/emotionclassifier/dataset/labels.txt", "UTF-8");
        PrintWriter sentWriter;
        int startS,endS;
        while(scannerIn.hasNext()){
            nextSent=scannerIn.nextLine();
            startS = nextSent.indexOf(":");
            endS = nextSent.lastIndexOf("::");
            extractedSent = nextSent.substring(startS + 1, endS);
            id = nextSent.substring(0, startS);
            label = nextSent.substring(endS+3);
            sentWriter = new PrintWriter("/home/sheryan/IdeaProjects/emotionclassifier/dataset/tweets/"+id+".txt", "UTF-8");
            sentWriter.println(extractedSent);
            labelWriter.println(id + " " + label);
            sentWriter.close();
        }
        labelWriter.close();
    }
}
