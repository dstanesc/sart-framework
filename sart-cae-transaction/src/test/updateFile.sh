#!/bin/sh

if [ -z $1 ]; then
  echo "Please provide inputDeckFile as argument";
  exit 1;
else
  echo inputDeckFile is $1;
fi

if [ -z $2 ]; then
  echo "Please provide inputDeckVersion as argument";
  exit 1;
else
  echo inputDeckVersion is $2;
fi


curl --header "Content-Type: application/json" \
     --request POST \
     --data '{"inputDeckFile": "inputDeck-file-'$1'"}' \
     http://localhost:8080/inputDeck/3/1/$2/updateFile
     
echo "Result '$1' Created"
