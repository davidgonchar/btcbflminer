#!/bin/bash

if [ "$#" -ne 1 ]; then
   echo "ERROR (`date`): incorrect number of parameters passed to the script $@"
   echo "USAGE: $0 <file contaning blocks info, e.g. screenlog.0>"
   exit 1
fi

INPUT_FILE=$1

while read line; do 
  echo -e $"from pymongo import MongoClient;c=MongoClient();db=c.blocks;j=${line:0:${#line}-2};db.blocks.insert_one({'_id':j['hash'],'block':j})" | python
done < ${INPUT_FILE}

