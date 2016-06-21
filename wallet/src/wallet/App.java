package wallet;

import org.bitcoinj.core.*;
import org.bitcoinj.core.listeners.TransactionConfidenceEventListener;
import org.bitcoinj.kits.WalletAppKit;
import org.bitcoinj.params.MainNetParams;
import org.bitcoinj.wallet.Wallet;
import org.bitcoinj.wallet.listeners.WalletCoinsReceivedEventListener;
import org.bitcoinj.wallet.listeners.WalletCoinsSentEventListener;

import java.io.*;
import java.math.BigInteger;
import java.util.Scanner;

public class App {
    private static final String fn = "app-blockchain";

    public static void main(String[] args) throws Exception {

        NetworkParameters netParams = MainNetParams.get();

        WalletAppKit appKit = new WalletAppKit(netParams, new File("."), fn);
        appKit.setAutoSave(true);
        appKit.startAsync();
        appKit.awaitRunning();

        Wallet wallet = appKit.wallet();

        if (wallet.getImportedKeys().size() == 0) {
            String privKey;
            System.out.println("Enter private key (silent): ");
            if (System.console() == null) {
                Scanner in = new Scanner(System.in);
                privKey = in.nextLine();
            } else {
                privKey = new String(System.console().readPassword());
            }

            ECKey key = getEcKey(netParams, privKey);
            wallet.importKey(key);
        }

        for (ECKey key : wallet.getImportedKeys()) {
            Address addr = key.toAddress(netParams);
            wallet.addWatchedAddress(addr);
            System.out.println("Public address: " + addr.toString());

//            DumpedPrivateKey pKey = key.getPrivateKeyEncoded(netParams);
//            System.out.println("Private key: " + pKey.toString());
        }

        wallet.addCoinsReceivedEventListener(new WalletCoinsReceivedEventListener() {
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
            public void onTransactionConfidenceChanged(Wallet txWallet, Transaction tx) {
                for (TransactionOutput txo : tx.getOutputs()) {
                    Address oAdr = txo.getScriptPubKey().getToAddress(netParams);
                    for (Address wa : txWallet.getWatchedAddresses()) {
                        if (wa.equals(oAdr)) {
                            System.out.println("-----> watching address: " + wa.toString());
                        }
                    }
                }
                System.out.println("-----> confidence changed: " + tx.getHashAsString());
                System.out.println("-----> new block depth: " + tx.getConfidence().getDepthInBlocks());
                Coin inCoins = tx.getValueSentToMe(txWallet);
                System.out.println("-----> received: " + inCoins.toFriendlyString());
                Coin outCoins = tx.getValueSentFromMe(txWallet);
                System.out.println("-----> sent: " + outCoins.toFriendlyString());
                System.out.println();
            }
        });

        while(true){
            System.out.println("Current balance: " + wallet.getBalance().toFriendlyString());
            Thread.sleep(300000);
        }
    }

    public static ECKey getEcKey(NetworkParameters netParams, String sPrivKey) throws Exception {
        ECKey key;
        if (sPrivKey.length() == 51 || sPrivKey.length() == 52) {
            DumpedPrivateKey dumpedPrivateKey = DumpedPrivateKey.fromBase58(netParams, sPrivKey);
            key = dumpedPrivateKey.getKey();
        } else {
            BigInteger privKey = Base58.decodeToBigInteger(sPrivKey);
            key = ECKey.fromPrivate(privKey);
        }
        return key;
    }
}
