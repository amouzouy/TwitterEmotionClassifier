
public class FeatureClassDist {
    public static int surpriseCount = 0;
    public static int sadnessCount = 0;
    public static int joyCount = 0;
    public static int disgustCount = 0;
    public static int fearCount = 0;
    public static int angerCount = 0;

    public double percentSurprise = 0;
    public double percentSadness = 0;
    public double percentJoy = 0;
    public double percentDisgust = 0;
    public double percentFear = 0;
    public double percentAnger = 0;

    public static double percentDiff=0.00001; //0.0001 - 0.00001 is where this works

    public FeatureClassDist(String label) {
        updateCounts(label);
    }

    public boolean isIrrelevantFeature(){
        computePercent();
        if(absValLessThanDiff(this.percentSurprise,this.percentSadness)
        && absValLessThanDiff(this.percentSurprise,this.percentJoy)
        && absValLessThanDiff(this.percentSurprise,this.percentDisgust)
        && absValLessThanDiff(this.percentSurprise,this.percentFear)
        && absValLessThanDiff(this.percentSurprise,this.percentAnger)
        && absValLessThanDiff(this.percentSadness,this.percentJoy)
        && absValLessThanDiff(this.percentSadness,this.percentDisgust)
        && absValLessThanDiff(this.percentSadness,this.percentFear)
        && absValLessThanDiff(this.percentSadness,this.percentAnger)
        && absValLessThanDiff(this.percentJoy,this.percentDisgust)
        && absValLessThanDiff(this.percentJoy,this.percentFear)
        && absValLessThanDiff(this.percentJoy,this.percentAnger)
        && absValLessThanDiff(this.percentDisgust,this.percentFear)
        && absValLessThanDiff(this.percentDisgust,this.percentAnger)
        && absValLessThanDiff(this.percentFear,this.percentAnger)){
            return true;
        }
        return false;
    }

    private void computePercent(){
        this.percentSurprise = this.percentSurprise/surpriseCount;
        this.percentSadness = this.percentSadness/sadnessCount;
        this.percentJoy = this.percentJoy/joyCount;
        this.percentDisgust = this.percentDisgust/disgustCount;
        this.percentFear = this.percentFear/fearCount;
        this.percentAnger = this.percentAnger/angerCount;
    }

    private boolean absValLessThanDiff(double p1, double p2){
        return Math.abs((p1-p2)) < percentDiff;
    }

    public void updateCounts(String label) {
        switch (label){
            case "surprise":
                this.percentSurprise++;
                surpriseCount++;
                break;
            case "sadness":
                this.percentSadness++;
                sadnessCount++;
                break;
            case "joy":
                this.percentJoy++;
                joyCount++;
                break;
            case "disgust":
                this.percentDisgust++;
                disgustCount++;
                break;
            case "fear":
                this.percentFear++;
                fearCount++;
                break;
            case "anger":
                this.percentAnger++;
                angerCount++;
                break;
        }
    }
}
