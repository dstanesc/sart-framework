#!/bin/sh

if [ -z $1 ]; then
  echo "Please provide resultId as argument";
  exit 1;
else
  echo resultId is $1;
fi

if [ -z $2 ]; then
  echo "Please provide inputDeckVersion as argument";
  exit 1;
else
  echo inputDeckVersion is $2;
fi


curl --header "Content-Type: application/json" \
     --request POST \
     --data '{"resultId": "'$1'", "resultName": "resultName-'$1'", "resultFile": "result-file-'$1'"}' \
     http://localhost:8080/inputDeck/2/1/$2/addResult
     
echo "Result '$1' Created"
