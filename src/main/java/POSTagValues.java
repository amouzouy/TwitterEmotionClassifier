import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

public class POSTagValues {

    Integer posTagInt;
    String posTag;

    public String getPosTag() {
        return posTag;
    }

    public void setPosTag(String posTag) {
        this.posTag = posTag;
    }

    List<Double> meanValues = new ArrayList<>(Collections.nCopies(6,0.0));
    List<Double> varValues = new ArrayList<>(Collections.nCopies(6,0.0));
    Double separationValue=0.0;
    private static String rScriptPath = "/home/sheryan/IdeaProjects/emotionclassifier/calcOVL.r";

    public Integer getPosTagInt() {
        return posTagInt;
    }

    public void setPosTagInt(Integer posTagInt) {
        this.posTagInt = posTagInt;
    }

    public List<Double> getMeanValues() {
        return meanValues;
    }

    public void setMeanValues(List<Double> meanValues) {
        this.meanValues = meanValues;
    }

    public List<Double> getVarValues() {
        return varValues;
    }

    public void setVarValues(List<Double> varValues) {
        this.varValues = varValues;
    }

    public Double getSeparationValue() {
        return separationValue;
    }

    public void setSeparationValue(Double separationValue) {
        this.separationValue = separationValue;
    }

    public void calculateSeparationMetric(Map<Integer, String> posMap){
        double iLower,iUpper,jLower,jUpper;
        for(int i=0;i<6;i++) {
            for(int j=i+1;j<6;j++) {
                iLower = meanValues.get(i)-Math.sqrt(varValues.get(i))/10;
                iUpper = meanValues.get(i)+Math.sqrt(varValues.get(i))/10;
                jLower = meanValues.get(j)-Math.sqrt(varValues.get(j))/10;
                jUpper = meanValues.get(j)+Math.sqrt(varValues.get(j))/10;
                if(iUpper < jLower || jUpper < iLower){
                    //dont add anything, no overlap
                }
                else if(iUpper<jUpper && iLower>jLower){
                    this.separationValue+=1;
                }
                else if(iUpper>jUpper && iLower<jLower){
                    this.separationValue+=1;
                }
                else{
                    this.separationValue+=Math.min(iUpper-jLower,jUpper-iLower)/Math.min(jUpper-jLower,iUpper-iLower);
                }
            }
        }
        this.posTag = posMap.get(posTagInt);
    }
}
