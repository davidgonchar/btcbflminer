from __future__ import print_function

from time import sleep

import blocktrail

client = blocktrail.APIClient(api_key="731ac7a92afe05e7640b3e89d69ecce514d3ea1d", api_secret="cb816f06d9722244f7489deb71cc65c43b97bccd", network="BTC", testnet=False)

address = client.address('3ETGrUjxeuC1e8nY8EP7U2EUcvffX76AKC')

prev_block = 416075

while True:
    latest_block = client.block_latest()
    last_block = latest_block['height']
    for i in xrange(prev_block, last_block):
        try:
            b = client.block(i)
            print(b)

            fn = '{0:0>6}.json'.format(str(b['height']))
            with open(fn, 'w') as f:
                f.write(str(b))

        except Exception as e:
            print(e)
    sleep(60)
    prev_block = last_block
    print("==== %s ====" % str(prev_block))
