#!/bin/sh

curl --header "Content-Type: application/json" \
     --request POST \
     --data '{"inputDeckName": "inputDeck-1", "inputDeckFile": "inputDeck-file-1"}' \
     http://localhost:8080/inputDeck/1/1/create
     
echo "Input Deck Created"

sleep 2  
     
curl --header "Content-Type: application/json" \
     --request POST \
     --data '{"resultId": "1", "resultName": "resultName-1", "resultFile": "result-file-1"}' \
     http://localhost:8080/inputDeck/1/1/0/addResult
     
echo "Result 1 Created"

sleep 2    
     
curl --header "Content-Type: application/json" \
     --request POST \
     --data '{"resultId": "2", "resultName": "resultName-2", "resultFile": "result-file-2"}' \
     http://localhost:8080/inputDeck/1/1/0/addResult
     
echo "Result 2 Created"