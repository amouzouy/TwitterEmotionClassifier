import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DTFeatures {

    Map<String,Integer> dependencyCounts = new HashMap<>();
    Map<String,Integer> depRules = new HashMap<>();
    Map<String,Integer> depRulesSimple = new HashMap<>();
    Map<String,Integer> depSkeletons = new HashMap<>();
    List<String> depStrings = new ArrayList<>();

    public Map<String, Integer> getDepSkeletons(int i) {
        if(i==0){
            FeatureExtractor.setAllValuesToZero(this.depSkeletons);
            return depSkeletons;
        }
        return depSkeletons;
    }

    public void setDepSkeletons(Map<String, Integer> depSkeletons) {
        this.depSkeletons = depSkeletons;
    }
    public Map<String, Integer> getDependencyCounts(int i) {
        if(i==0){
            FeatureExtractor.setAllValuesToZero(this.dependencyCounts);
            return dependencyCounts;
        }
        return dependencyCounts;
    }

    public void setDependencyCounts(Map<String, Integer> dependencyCounts) {
        this.dependencyCounts = dependencyCounts;
    }

    public Map<String, Integer> getDepRules(int i) {
        if(i==0){
            FeatureExtractor.setAllValuesToZero(this.depRules);
            return depRules;
        }
        return depRules;
    }

    public void setDepRules(Map<String, Integer> depRules) {
        this.depRules = depRules;
    }

    public Map<String, Integer> getDepRulesSimple(int i) {
        if(i==0){
            FeatureExtractor.setAllValuesToZero(this.depRulesSimple);
            return depRulesSimple;
        }
        return depRulesSimple;
    }

    public void setDepRulesSimple(Map<String, Integer> depRulesSimple) {
        this.depRulesSimple = depRulesSimple;
    }

    DTFeatures (List<String> trees) {
        for(String tree : trees){
            parseStringTree(tree,"topdep");
            for(String depString : depStrings){
                String [] dependencies = depString.split(" ");
                for(int i=0;i<dependencies.length;i++){
                    if(dependencyCounts.containsKey("dep tree "+dependencies[i])){
                        dependencyCounts.put("dep tree "+dependencies[i],dependencyCounts.get("dep tree "+dependencies[i])+1);
                    }
                    else{
                        dependencyCounts.put("dep tree "+dependencies[i],1);
                    }
                }
                if(depSkeletons.containsKey("dep tree "+dependencies.length)){
                    depSkeletons.put("dep tree "+dependencies.length,depSkeletons.get("dep tree "+dependencies.length)+1);
                }
                else{
                    depSkeletons.put("dep tree "+dependencies.length,1);
                }
                if(!depRules.containsKey("dep tree "+depString)){
                    depRules.put("dep tree "+depString,1);
                }
                if(!depRulesSimple.containsKey("dep tree "+dependencies[0]+" "+(dependencies.length-1))){
                    depRulesSimple.put("dep tree "+dependencies[0]+" "+(dependencies.length-1),1);
                }
            }
        }
    }

    private void parseStringTree(String treeString, String head) {
        if(!treeString.contains("[")){
            return;
        }
        String subtree,depString,substring, headString;
        treeString = treeString.substring(treeString.indexOf(" ")+1,treeString.length()-1);
        depString = head;

        int rightP=0,leftP=0;
        int lastIndex=treeString.indexOf("[");
        int lastSpace=0;
        for(int i=0;i<treeString.length();i++){
            if(treeString.charAt(i)=='['){
                leftP++;
            }
            else if(treeString.charAt(i)==']'){
                rightP++;
            }
            if(treeString.charAt(i)==' ' || (i == treeString.length()-1)){
                if(leftP == rightP){
                    substring = treeString.substring(lastSpace,i);
                    depString = depString + " " + substring.substring(0, substring.indexOf('>'));
                    lastSpace = i;
                }
            }
            if((rightP == leftP) && (rightP!=0)){
                subtree = treeString.substring(lastIndex,i+1);
                headString = treeString.substring(0,lastIndex-1);
                headString = headString.substring(headString.lastIndexOf(" ")+1);

                parseStringTree(subtree,headString);
                lastIndex = treeString.indexOf("[",i+1);
                rightP=0;leftP=0;
            }
        }
        depStrings.add(depString);
    }
}
