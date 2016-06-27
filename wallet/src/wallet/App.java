package wallet;

import org.bitcoinj.core.*;
import org.bitcoinj.core.listeners.TransactionConfidenceEventListener;
import org.bitcoinj.kits.WalletAppKit;
import org.bitcoinj.params.MainNetParams;
import org.bitcoinj.wallet.Wallet;
import org.bitcoinj.wallet.listeners.WalletCoinsReceivedEventListener;
import org.bitcoinj.wallet.listeners.WalletCoinsSentEventListener;

import http.BtcServletRest;

import javax.validation.constraints.NotNull;
import java.io.*;
import java.math.BigInteger;
import java.util.Scanner;

public class App {
    private static final String fn = "app-blockchain";

    public static void main(String[] args) throws Exception {

        NetworkParameters netParams = MainNetParams.get();

        File dir = null;
        if (args.length > 0){
            dir = new File(args[0]);
        } else {
            dir = new File(".");
        }
        WalletAppKit appKit = new WalletAppKit(netParams, dir, fn);
        System.out.println("Please wait for initialization...");
        appKit.setAutoSave(true);
        appKit.startAsync();
        appKit.awaitRunning();

        Wallet wallet = appKit.wallet();

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
//                for (TransactionOutput txo : tx.getOutputs()) {
//                    Address oAdr = txo.getScriptPubKey().getToAddress(netParams);
//                    for (Address wa : txWallet.getWatchedAddresses()) {
//                        if (wa.equals(oAdr)) {
//                            System.out.println("-----> watching address: " + wa.toString());
//                        }
//                    }
//                }
                System.out.println("-----> confidence changed: " + tx.getHashAsString());
                System.out.println("-----> new block depth: " + tx.getConfidence().getDepthInBlocks());
//                Coin inCoins = tx.getValueSentToMe(txWallet);
//                System.out.println("-----> received: " + inCoins.toFriendlyString());
//                Coin outCoins = tx.getValueSentFromMe(txWallet);
//                System.out.println("-----> sent: " + outCoins.toFriendlyString());
//                System.out.println();
            }
        });

        Thread t = new Thread(new CmdHandler(wallet));
        t.start();

        BtcServletRest.run(appKit);
    }

    private static class CmdHandler implements Runnable {
        final private Wallet wallet;

        CmdHandler(@NotNull Wallet wallet) throws Exception {
            this.wallet = wallet;
        }

        @Override
        public void run() {
            String cmd;

            while (!(cmd = readLine()).equals("^^~^^")) {
                String[] params = cmd.split(" ");

                if (params.length > 1 && params[0].equals("?")) {
                    helpCmd(params[1]);
                } else {
                    switch (cmd) {
                        case "help":
                            help();
                            break;
                        case "?":
                            helpCmd("");
                            break;
                        case "private":
                            setPrivateKey();
                            break;
                        case "balance":
                            showBalance();
                        default:
                    }
                }
            }
            System.out.println("Bye, bye...");

            try {
                Thread.sleep(2000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            System.exit(100);
        }

        private String readLine() {
            System.out.print("btc_cmd > ");

            String cmd;
            if (System.console() == null) {
                Scanner in = new Scanner(System.in);
                cmd = in.nextLine();
//                in.close();
            } else {
                cmd = System.console().readLine();
            }
            return cmd;
        }

        private void helpCmd(@NotNull String cmd) {
            if (cmd.isEmpty()) {
                System.out.println("usage: ? <Command>");
                System.out.println("use 'help' for commands set");
                return;
            }

            System.out.println("Usage of " + cmd);
        }

        private void help() {
            System.out.println("Commands: ");
            System.out.println(" \t balance \n \t private \n \t help \n \t ? \n");
        }

        private void showBalance() {
            System.out.println("Balance: " + wallet.getBalance().toFriendlyString());
            for (Address wa : wallet.getWatchedAddresses()) {
                System.out.println("Watching address: " + wa.toString());
            }
            for (Transaction tx : wallet.getTransactionsByTime()) {
                System.out.println("Transaction: " + tx.getHashAsString());
                System.out.println("Received: " + tx.getValueSentToMe(wallet).toFriendlyString());
                System.out.println("Sent: " + tx.getValueSentFromMe(wallet).toFriendlyString());
                System.out.println("Confirmations: " + tx.getConfidence().getDepthInBlocks());
                System.out.println();
            }
        }

        private void setPrivateKey() {
            NetworkParameters netParams = wallet.getParams();

            String privKey;
            System.out.println("Enter private key (silent): ");
            if (System.console() == null) {
                Scanner in = new Scanner(System.in);
                privKey = in.nextLine();
//                in.close();
            } else {
                privKey = new String(System.console().readPassword());
            }

            ECKey key = null;
            try {
                key = getEcKey(netParams, privKey);
            } catch (Exception e) {
                System.out.println("Private key incorrect");
                return;
            }
            wallet.importKey(key);

            Address addr = key.toAddress(netParams);
            wallet.addWatchedAddress(addr);
            System.out.println("Watching public address: " + addr.toString());

//            DumpedPrivateKey pKey = key.getPrivateKeyEncoded(netParams);
//            System.out.println("Private key: " + pKey.toString());
        }

        private ECKey getEcKey(NetworkParameters netParams, String sPrivKey) throws Exception {
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
}
