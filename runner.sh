#!/bin/bash
jarPath="/home/sheryan/IdeaProjects/emotionclassifier/build/libs"
trainPath="/home/sheryan/IdeaProjects/emotionclassifier/dataset/training"
testPath="/home/sheryan/IdeaProjects/emotionclassifier/dataset/testing"
LibSVMPath="/home/sheryan/IdeaProjects/emotionclassifier/LibSVM"
modelPath="/home/sheryan/IdeaProjects/emotionclassifier/dataset/models"
outputPath="/home/sheryan/IdeaProjects/emotionclassifier/dataset/outputs"
IFS=","
gradle jar
while read f1 f2 f3 f4
do
        [ "$f1" = "Description" ] && continue
        svmParams=()
        prepParams=()
        declare -a "svmParams=($f2)"
        declare -a "prepParams=($f3)"
        java -cp "${jarPath}/emotionclassifier-1.0-SNAPSHOT.jar" Preprocessor $f4 ${prepParams[@]}
        END=$(expr ${f4} - 1)
        for ((i=0;i<=END;i++));
                do
                        ${LibSVMPath}/svm-train ${svmParams[@]} ${trainPath}/file$i.train
                        mv ./file${i}.train.model ${modelPath}
                        ${LibSVMPath}/svm-predict ${testPath}/file$i.test ${modelPath}/file$i.train.model ${outputPath}/file$i.output
                done
                java -cp ${jarPath}/emotionclassifier-1.0-SNAPSHOT.jar ResultsGenerator $f4 "$f2" "$f3" "$f1"
                rm ${testPath}/*.test ${outputPath}/*.output ${trainPath}/*.train ${modelPath}/*.model
done < conf.csv