#!/bin/bash

if [[ "$OSTYPE" == "linux-gnu" ]]; then
        path="/home/sheryan/IdeaProjects/emotionclassifier"
		ext=""
elif [[ "$OSTYPE" == "cygwin" ]]; then
        path="C:\cygwin/home/Sheryan/EmotionClassifier/TwitterEmotionClassifier"
		ext=".exe"
fi
jarPath="$path/build/libs"
trainPath="$path/dataset/training"
testPath="$path/dataset/testing"
LibSVMPath="$path/LibSVM"
modelPath="$path/dataset/models"
outputPath="$path/dataset/outputs"
IFS=","
gradle jar
while read f1 f2 f3 f4 f5
do
        if [[ "$f5" == "yes" ]]; then
                scaling="_scaled"
        else
                scaling=""
        fi
        [ "$f1" = "Description" ] && continue
        svmParams=()
        prepParams=()
        declare -a "svmParams=($f2)"
        declare -a "prepParams=($f3)"
        java -cp "${jarPath}/emotionclassifier-1.0-SNAPSHOT.jar" FeatureExtractor $f4 ${prepParams[@]}
        END=$(expr ${f4} - 1)
        for ((i=0;i<=END;i++));
                do
                        if [[ "$f5" == "yes" ]]; then
                                ${LibSVMPath}/svm-scale$ext ${trainPath}/file$i.train > ${trainPath}/file${i}_scaled.train
                                ${LibSVMPath}/svm-scale$ext ${testPath}/file$i.test > ${testPath}/file${i}_scaled.test
                        fi
                        ${LibSVMPath}/svm-train$ext ${svmParams[@]} ${trainPath}/file${i}${scaling}.train
                        mv ./file${i}${scaling}.train.model ${modelPath}
                        ${LibSVMPath}/svm-predict$ext ${testPath}/file${i}${scaling}.test ${modelPath}/file${i}${scaling}.train.model ${outputPath}/file$i.output
                done
                java -cp ${jarPath}/emotionclassifier-1.0-SNAPSHOT.jar ResultsGenerator $f4 "$f2" "$f3" "$f1"
				if [[ "$OSTYPE" == "linux-gnu" ]]; then
						rm ${testPath}/*.test ${outputPath}/*.output ${trainPath}/*.train ${modelPath}/*.model
				elif [[ "$OSTYPE" == "cygwin" ]]; then
						rm "C:\cygwin/home/Sheryan/EmotionClassifier/TwitterEmotionClassifier/dataset/models"/*.model
						rm "C:\cygwin/home/Sheryan/EmotionClassifier/TwitterEmotionClassifier/dataset/training"/*.train
						rm "C:\cygwin/home/Sheryan/EmotionClassifier/TwitterEmotionClassifier/dataset/testing"/*.test
						rm "C:\cygwin/home/Sheryan/EmotionClassifier/TwitterEmotionClassifier/dataset/outputs"/*.output
				fi
done < conf.csv
