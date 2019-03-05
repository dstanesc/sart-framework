# Quick Start

### Boot the APP ./gradlew bootRun

### Open the browser http://localhost:8080/results.html

### Run ./inputDeck.sh

### Run ./result.sh



# Create an input deck

POST http://localhost:8080/inputDeck/1/1/create
Content-Type: application/json

{"inputDeckName": "inputDeck-1", "inputDeckFile": "inputDeck-file-1"}

## Example 

curl --header "Content-Type: application/json" \
     --request POST \
     --data '{"inputDeckName": "inputDeck-1", "inputDeckFile": "inputDeck-file-1"}' \
     http://localhost:8080/inputDeck/1/1/create
     


# Add a result
POST http://localhost:8080/inputDeck/1/1/addResult
Content-Type: application/json

{"resultId": "1", "resultName": "resultName-1", "resultFile": "result-file-1"}

## Example

curl --header "Content-Type: application/json" \
     --request POST \
     --data '{"resultId": "1", "resultName": "resultName-1", "resultFile": "result-file-1"}' \
     http://localhost:8080/inputDeck/1/1/addResult
    

# Update Input Deck File
POST http://localhost:8080/inputDeck/1/1/updateFile   
Content-Type: application/json

{"inputDeckFile": "inputDeck-file-X"}
         
# Get the server event streams

## EventSource url
http://localhost:8080/inputDeck/1/results/sse


