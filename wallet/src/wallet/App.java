package wallet;

import org.bitcoinj.core.*;
import org.bitcoinj.core.listeners.TransactionConfidenceEventListener;
import org.bitcoinj.kits.WalletAppKit;
import org.bitcoinj.net.discovery.DnsDiscovery;
import org.bitcoinj.params.MainNetParams;
import org.bitcoinj.store.BlockStoreException;
import org.bitcoinj.store.SPVBlockStore;
import org.bitcoinj.wallet.UnreadableWalletException;
import org.bitcoinj.wallet.Wallet;
import org.bitcoinj.wallet.WalletExtension;
import org.bitcoinj.wallet.listeners.WalletCoinsReceivedEventListener;
import org.bitcoinj.wallet.listeners.WalletCoinsSentEventListener;

import java.io.*;
import java.util.List;

public class App {
    private static final String fn = "app-blockchain";

    public static void main( String[] args ) throws BlockStoreException, InterruptedException, UnreadableWalletException, FileNotFoundException {

        NetworkParameters netParams = MainNetParams.get();

        WalletAppKit appKit = new WalletAppKit(netParams, new File("."), fn);
        appKit.setAutoSave(true);
        appKit.startAsync();
        appKit.awaitRunning();

        Wallet wallet = appKit.wallet();

        if (wallet.getImportedKeys().size() == 0) {
            ECKey key = new ECKey();
            wallet.importKey(key);
        }

        for (ECKey key : wallet.getImportedKeys()) {
            Address addr = key.toAddress(netParams);
            DumpedPrivateKey pKey = key.getPrivateKeyEncoded(netParams);
            wallet.addWatchedAddress(addr);

            System.out.println("Public address: " + addr.toString());
            System.out.println("Private key: " + pKey.toString());
        }

        SPVBlockStore blockStore = new SPVBlockStore(netParams, new File(fn));

//        BlockStore blockStore = new MemoryBlockStore(netParams);
        BlockChain chain = new BlockChain(netParams, wallet, blockStore);

        PeerGroup peerGroup = new PeerGroup(netParams, chain);
        peerGroup.addPeerDiscovery(new DnsDiscovery(netParams));
        peerGroup.addWallet(wallet);
        peerGroup.start();
        peerGroup.downloadBlockChain();

        wallet.addCoinsReceivedEventListener(
                new WalletCoinsReceivedEventListener() {
                    @Override
                    public void onCoinsReceived(Wallet wallet, Transaction tx, Coin prevBalance, Coin newBalance) {
                        System.out.println("coins received");
                        System.out.println("Previous balance: " + prevBalance.toFriendlyString());
                        System.out.println("New balance: " + newBalance.toFriendlyString());
                    }
                }
        );

        wallet.addCoinsSentEventListener(new WalletCoinsSentEventListener() {
            @Override
            public void onCoinsSent(Wallet wallet, Transaction tx, Coin prevBalance, Coin newBalance) {
                System.out.println("coins sent");
                System.out.println("Previous balance: " + prevBalance.toFriendlyString());
                System.out.println("New balance: " + newBalance.toFriendlyString());
            }
        });
        wallet.addTransactionConfidenceEventListener(new TransactionConfidenceEventListener() {
            @Override
            public void onTransactionConfidenceChanged(Wallet wallet, Transaction tx) {
                System.out.println("-----> confidence changed: " + tx.getHashAsString());
                TransactionConfidence confidence = tx.getConfidence();
                System.out.println("new block depth: " + confidence.getDepthInBlocks());
            }
        });

        while(true){
            List<Address> addresses = wallet.getWatchedAddresses();
            for (Address a : addresses) {
                System.out.println("Watching address: " + a.toString());
            }
            Thread.sleep(300000);
        }
    }
}
