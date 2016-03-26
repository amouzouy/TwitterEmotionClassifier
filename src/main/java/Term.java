import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Term {

    String term;
    List<Integer> emotionValueList =  new ArrayList<>(Collections.nCopies(6,0));
    List<Integer> tCounts = new ArrayList<>(Collections.nCopies(6, 0));
    List<Integer> dCounts = new ArrayList<>(Collections.nCopies(6, 0));
    List<Double> tfIdfs = new ArrayList<>();

    private void computeTfIDf(List<Integer> emotionDocCounts, List<Integer> emotionTermCounts,int tfNorm){
        double tf, idf;
        for(int i=0;i<6;i++){
            if(tfNorm == 2){
                tf = tCounts.get(i) / (double)emotionTermCounts.get(i);
            }
            else if(tfNorm == 1){
                tf = 1;
            }
            else {
                tf = tCounts.get(i);
            }
            if(dCounts.get(i) != 0) {
                idf = Math.log(emotionDocCounts.get(i) / dCounts.get(i));
            }
            else{
                idf = 0;
            }
            tfIdfs.add(tf*idf);
        }
    }

    public void computeEmotionValues(List<Integer> emotionDocCounts, List<Integer> emotionTermCounts, int tfNorm){
        computeTfIDf(emotionDocCounts,emotionTermCounts, tfNorm);
        for(int i=1;i<7;i++){
            emotionValueList.set(tfIdfs.indexOf(Collections.max(tfIdfs)), i);
            tfIdfs.set(tfIdfs.indexOf(Collections.max(tfIdfs)), -1.0);
        }
    }
}
