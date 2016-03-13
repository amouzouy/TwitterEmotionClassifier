import java.io.*;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Scanner;

public class ResultsGenerator {

    public static void main(String [] args) throws IOException {

        int [][] matrix = new int[6][6];
        String [] emotions = {"Surprise","Sadness","Joy","Disgust","Fear","Anger"};
        int totalCount = 0;
        String actualLabelPath = "",predictedLabelPath = "";
        String svmFlags, preprocessingFlags, description = "N/A";
        int numFolds;
        String actualLabel, predictedLabel;
        Scanner scannerIn1,scannerIn2;

        numFolds = Integer.parseInt(args[0]);
        svmFlags = args[1];
        preprocessingFlags = args[2];

        if(args.length == 4){
            description = args[3];
        }
        PrintWriter writer = null;
        switch (System.getProperty("os.name")){
            case "Linux":
                writer = new PrintWriter(new FileWriter("/home/sheryan/IdeaProjects/emotionclassifier/dataset/results.txt", true));
                break;
            case "Windows 10":
                writer = new PrintWriter(new FileWriter("C:/cygwin/home/Sheryan/EmotionClassifier/TwitterEmotionClassifier/dataset/results.txt",true));
        }

        for(int num=0;num<numFolds;num++) {
            switch (System.getProperty("os.name")){
                case "Linux":
                    actualLabelPath = "/home/sheryan/IdeaProjects/emotionclassifier/dataset/testing/file"+num+".test";
                    predictedLabelPath = "/home/sheryan/IdeaProjects/emotionclassifier/dataset/outputs/file"+num+".output";
                    break;
                case "Windows 10":
                    actualLabelPath = "C:/cygwin/home/Sheryan/EmotionClassifier/TwitterEmotionClassifier/dataset/testing/file"+num+".test";
                    predictedLabelPath = "C:/cygwin/home/Sheryan/EmotionClassifier/TwitterEmotionClassifier/dataset/outputs/file"+num+".output";
                    break;
            }



            scannerIn1 = new Scanner(new File(actualLabelPath));
            scannerIn2 = new Scanner(new File(predictedLabelPath));

            while (scannerIn1.hasNext()) {
                totalCount++;
                actualLabel = scannerIn1.nextLine();
                actualLabel = actualLabel.substring(0, 1);
                predictedLabel = scannerIn2.nextLine();
                matrix[Integer.parseInt(actualLabel)][(int) Double.parseDouble(predictedLabel)]++;
            }
        }

//        totalCount=totalCount/numFolds;
//        for(int i=0;i<6;i++){
//            for(int j=0;j<6;j++){
//                matrix[i][j]/=numFolds;
//            }
//        }

        DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
        Date date = new Date();
        writer.println("\n\n\nResults for date: " + dateFormat.format(date) +
                "\nDescription: " + description +
                "\nSVM flags: " + svmFlags +
                "\nPreprocessing flags: " + preprocessingFlags +
                "\nCV folds: " + numFolds + "\n");

        writer.format("\t\t\t%10s%10s%8s%13s%8s%11s%10s%12s%10s\n", emotions[0], emotions[1], emotions[2], emotions[3], emotions[4], emotions[5],
                "Recall", "Precision", "F1-Score");
        for (int i = 0; i < 6; i++) {
            writer.format("%10s%10d%10d%10d%10d%10d%10d%12.3f%10.3f%10.3f\n",
                    emotions[i],
                    matrix[i][0],
                    matrix[i][1],
                    matrix[i][2],
                    matrix[i][3],
                    matrix[i][4],
                    matrix[i][5],
                    calculateRecall(matrix, i),
                    calculatePrecision(matrix, i),
                    calculateF1(matrix, i));
        }
        writer.println("\n Total Accuracy: " + (double) calculateCorrectCount(matrix) / totalCount);
        writer.close();
    }


    private static double calculateRecall(int [][] matrix, int diagIndex){
        int sum=0;
        for(int i=0;i<6;i++){
            sum+=matrix[diagIndex][i];
        }
        return (double)matrix[diagIndex][diagIndex]/sum;
    }

    private static double calculatePrecision(int [][] matrix, int diagIndex){
        int sum=0;
        for(int i=0;i<6;i++){
            sum+=matrix[i][diagIndex];
        }
        return (double)matrix[diagIndex][diagIndex]/sum;
    }

    private static double calculateF1(int [][] matrix, int diagIndex){
        return 2*(calculatePrecision(matrix,diagIndex)*calculateRecall(matrix,diagIndex))
                /(calculatePrecision(matrix,diagIndex)+calculateRecall(matrix,diagIndex));
    }

    private static int calculateCorrectCount(int [][] matrix){
        return matrix[0][0]+matrix[1][1]+matrix[2][2]
                +matrix[3][3]+matrix[4][4]+matrix[5][5];
    }

}
