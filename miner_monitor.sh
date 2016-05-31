#!/bin/bash

MINER_FILE=$HOME/logs/1G4Nr28Js2pGwzhNCaTvnopw1Hyq7wc3oB.log

while true; do 
sleep 35 
echo "============================= `date -R` =============================" >> ${MINER_FILE}

curl -s "http://www.bcmonster.com/index.php?page=api&action=getuserstatus&api_key=2d731511a174af171c4717a2df8200241b47b0ab6759b91c703f260f6a72514b&id=1210" | \ 
	awk -F'"hashrate"' '{hr=substr($2,2); print hr}' | \ 
	awk -F',' '{print $1/1000/1000" Gh/s - 30 seconds"}' >> ${MINER_FILE} 2>/dev/null 

echo "============================= `date -R` =============================" >> ${MINER_FILE} 

#html2text http://eligius.st/~wizkid057/newstats/userstats.php/1G4Nr28Js2pGwzhNCaTvnopw1Hyq7wc3oB | \ 
#	egrep "[0-9] hours|minutes|seconds" | \ 
#	awk -F"|" '{if($2) print $2" - "$1" "}' >> ${MINER_FILE} 2>/dev/null 

done

