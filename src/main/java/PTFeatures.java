import java.util.*;

public class PTFeatures {

    Map<String,Integer> ntPOSTags = new HashMap<>();
    Map<String,Integer> childCounts = new HashMap<>();
    Map<String,Integer> childCountsHead = new HashMap<>();
    Map<String,Integer> rewriteRules = new HashMap<>();

    public Map<String, Integer> getChildCountsHead(int i) {
        if (i == 0) {
            FeatureExtractor.setAllValuesToZero(this.childCountsHead);
            return  childCountsHead;
        }
        return childCountsHead;
    }

    public void setChildCountsHead(Map<String, Integer> childCountsHead) {
        this.childCountsHead = childCountsHead;
    }

    public Map<String, Integer> getNtPOSTags(int i) {
        if(i == 0){
            FeatureExtractor.setAllValuesToZero(this.ntPOSTags);
            return ntPOSTags;
        }
        return ntPOSTags;
    }

    public void setNtPOSTags(Map<String, Integer> ntPOSTags) {
        this.ntPOSTags = ntPOSTags;
    }

    public Map<String, Integer> getChildCounts(int i) {
        if(i == 0){
            FeatureExtractor.setAllValuesToZero(this.childCounts);
            return childCounts;
        }
        return childCounts;
    }

    public void setChildCounts(Map<String, Integer> childCounts) {
        this.childCounts = childCounts;
    }

    public Map<String, Integer> getRewriteRules(int i) {
        if(i == 0){
            FeatureExtractor.setAllValuesToZero(this.rewriteRules);
            return rewriteRules;
        }
        return rewriteRules;
    }

    public void setRewriteRules(Map<String, Integer> rewriteRules) {
        this.rewriteRules = rewriteRules;
    }

    PTFeatures (List<String> trees) {
        for(String tree : trees){
            parseStringTree(tree);
        }
    }

    private void parseStringTree(String treeString){
        String ntPos, subtree,words;
        boolean isLeaf = true;
        treeString = treeString.substring(1,treeString.length()-1);
        ntPos = treeString.substring(0,treeString.indexOf(" "));
        words = ntPos;
        if(ntPOSTags.containsKey("parse tree "+ntPos)){
            ntPOSTags.put("parse tree "+ntPos,ntPOSTags.get("parse tree "+ntPos)+1);
        }
        else{
            ntPOSTags.put("parse tree "+ntPos,1);
        }
        int rightP=0,leftP=0;
        int lastIndex=treeString.indexOf(" ");
        for(int i=0;i<treeString.length();i++){
            if(treeString.charAt(i)=='('){
                leftP++;
            }
            else if(treeString.charAt(i)==')'){
                rightP++;
            }
            if((rightP == leftP) && (rightP!=0)){
                subtree = treeString.substring(lastIndex+1,i+1);
                words = words + " " + subtree.substring(1,subtree.indexOf(" "));
                parseStringTree(subtree);
                lastIndex = i+1;
                rightP=0;leftP=0;
                isLeaf=false;
            }
        }

        String childCount = Integer.toString(words.split(" ").length-1);
        if(!isLeaf) {
            if (childCounts.containsKey("parse tree "+childCount)) {
                childCounts.put("parse tree "+childCount, childCounts.get("parse tree "+childCount) + 1);
            } else {
                childCounts.put("parse tree "+childCount, 1);
            }
            if(!childCountsHead.containsKey("parse tree "+words.split(" ")[0]+childCount)){
                childCountsHead.put("parse tree "+words.split(" ")[0]+childCount,1);
            }
            if (!rewriteRules.containsKey("parse tree "+words)) {
                rewriteRules.put("parse tree "+words, 1);
            }
        }
    }
}
