#!/bin/bash

BLOCK_INFO=`curl -s https://blockexplorer.com/api/status?q=getInfo`; 

LAST_BLOCK=`echo "import json; j=json.loads('${BLOCK_INFO}');print(j['info']['blocks'])"|python`

echo "\"last_block\": $LAST_BLOCK"

for i in `seq 0 ${LAST_BLOCK}`; do
BHASH_DATA=`curl -s https://blockexplorer.com/api/block-index/${i}`
BHASH=`echo "import json; j=json.loads('${BHASH_DATA}');print(j['blockHash'])"|python`
BHASH_INFO=`curl -s https://blockexplorer.com/api/block/${BHASH}`

echo "import json; j=json.loads('${BHASH_INFO}');print('%s,' % str(j))" | python

done

