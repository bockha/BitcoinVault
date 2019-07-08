package iuno.tdm.vault;

import io.swagger.model.*;
import org.bitcoinj.core.*;
import org.bitcoinj.core.Transaction;
import org.bitcoinj.wallet.SendRequest;
import org.bitcoinj.wallet.UnreadableWalletException;
import org.bitcoinj.wallet.Wallet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

/**
 * Created by goergch on 23.05.17.
 */
public class UserWallet {

    private UUID walletId;
    private String userId;
    private Wallet wallet;
    private Context context;
    private static final String PREFIX = "Vault";
    private static final Logger logger = LoggerFactory.getLogger(UserWallet.class);
    private File walletFile;
    private HashMap<UUID, Payout> payouts = new HashMap<>();


    private PeerGroup peerGroup;

    /***
     * This method creates a new UserWallet object instance by creating a new wallet.
     * @param userId the user ID the wallet belongs to
     * @param context bitcoinj context object
     * @param peerGroup bitcoinj peer group
     *
     * @throws IOException if there is something wrong with the wallet file itself
     */
    UserWallet(String userId, Context context, PeerGroup peerGroup) throws IOException {
        String workDir = System.getProperty("user.home") + "/." + PREFIX;
        new File(workDir).mkdirs();

        String walletId = UUID.randomUUID().toString();
        String walletFileName = workDir + "/" + PREFIX + walletId + ".wallet";

        try {
            initWallet(walletId, userId, context, walletFileName, peerGroup);

        } catch (UnreadableWalletException e) {
            logger.error("this should never happen");
            e.printStackTrace();
        }
    }

    /***
     * This method constructs a UserWallet object instance by opening an existing wallet file or creating a new wallet.
     *
     * @param walletId the ID of the wallet
     * @param userId the user ID the wallet belongs to
     * @param context bitcoinj context object
     * @param walletFileName the filename of the wallet;
     *                       if this wallet file does not exist, a new wallet will be created
     * @param peerGroup bitcoinj peer group
     *
     * @throws IOException if there is something wrong with the wallet file itself
     * @throws UnreadableWalletException if the wallet is ûnreadable
     */
    UserWallet(String walletId, String userId, Context context, String walletFileName, PeerGroup peerGroup) throws IOException, UnreadableWalletException {
        initWallet(walletId, userId, context, walletFileName, peerGroup);
    }

    private void initWallet(String walletId, String userId, Context context, String walletFileName, PeerGroup peerGroup) throws IOException, UnreadableWalletException {
        this.peerGroup = peerGroup;
        this.userId = userId;
        this.context = context;
        this.walletId = UUID.fromString(walletId);

        walletFile = new File(walletFileName);

        if (walletFile.exists())
            wallet = Wallet.loadFromFile(walletFile);
        else
            wallet = new Wallet(context);

        startupAutosaveToFile();
    }


    private void startupAutosaveToFile() throws IOException {
        try {
            wallet.autosaveToFile(walletFile, 5, TimeUnit.SECONDS, null).saveNow();
        } catch (IOException e) {
            logger.error(String.format("saving wallet file failed: %s", e.getMessage()));
            throw new IOException(String.format("saving wallet file failed: %s", e.getMessage()));
        }
    }

    /***
     * This method shuts down the autosave feature of the wallet and finally saves the wallet one last time.
     */
    void shutdownAutosaveAndSave() {
        try {
            wallet.shutdownAutosaveAndWait();
            wallet.saveToFile(walletFile);
        } catch (IOException e) {
            logger.error(String.format("saving wallet file failed: %s", e.getMessage()));
            e.printStackTrace();
        }
    }

    public void deleteWallet() {
        if (walletFile.exists()) {
            walletFile.delete();
        }
    }

    public UUID getWalletId() {
        return walletId;
    }

    public String getUserId() {
        return userId;
    }

    public File getWalletFile() {
        return walletFile;
    }

    public Coin getBalance() {
        return wallet.getBalance(Wallet.BalanceType.ESTIMATED_SPENDABLE);
    }

    public Coin getConfirmedBalance() {
        return wallet.getBalance(Wallet.BalanceType.AVAILABLE_SPENDABLE);
    }

    public Wallet getWallet() {
        return wallet;
    }

    public String getFreshAddress() {
        return wallet.freshReceiveAddress().toBase58();
    }


    public String getPublicSeed() {
        return wallet.getActiveKeyChain().getWatchingKey().serializePubB58(context.getParams());
    }

    public TransactionOutput[] getTransactionOutputs() {
        List<TransactionOutput> transactionOutputs = wallet.getUnspents();
        return transactionOutputs.toArray(new TransactionOutput[transactionOutputs.size()]);
    }

    private SendRequest getSendRequestForPayout(Payout payout) {
        // fail if wallet contains less than twice the dust value
        if (wallet.getBalance().isLessThan(Transaction.MIN_NONDUST_OUTPUT.multiply(2))) {
            throw new IllegalArgumentException("Wallet contains less than twice the dust output."); // TODO This error is not about an illegal argument!
        }

        // helper variables to provide readable code
        Coin amount = Coin.valueOf(payout.getAmount());
        Coin balance = wallet.getBalance(Wallet.BalanceType.AVAILABLE_SPENDABLE);
        Coin doubleDust = Transaction.MIN_NONDUST_OUTPUT.multiply(2);
        Address address = Address.fromBase58(context.getParams(), payout.getPayoutAddress());

        SendRequest sendRequest;

        // empty wallet if change output would be dust or emptyWallet is true
        if (payout.getEmptyWallet() || amount.add(doubleDust).isGreaterThan(balance)) {
            sendRequest = SendRequest.emptyWallet(address);

        } else {
            sendRequest = SendRequest.to(address, amount);
        }

        // finalize send request
        sendRequest.feePerKb = Transaction.REFERENCE_DEFAULT_MIN_TX_FEE;
        sendRequest.memo = payout.getReferenceId();
        return sendRequest;
    }

    public Payout addPayout(Payout payout) {
        payout.setPayoutId(UUID.randomUUID());
        SendRequest sendRequest = getSendRequestForPayout(payout);
        logger.debug(sendRequest.tx.getFee().toString());

        try {
            wallet.completeTx(sendRequest);
            wallet.commitTx(sendRequest.tx);
            peerGroup.broadcastTransaction(sendRequest.tx).broadcast();
        } catch (InsufficientMoneyException e) {
            throw new IllegalArgumentException(e.getMessage());
        }
        payouts.put(payout.getPayoutId(), payout);

        return payout;
    }

    public Payout getPayout(UUID payoutId) {
        if (!payouts.containsKey(payoutId)) {
            throw new NullPointerException("There is no payout with the payout ID " + payoutId);
        }

        return payouts.get(payoutId);
    }

    public io.swagger.model.Transaction[] getTransactionsForPayout(UUID payoutId) {
        return null;
    }

    public UUID[] getPayoutIDs() {
        UUID[] uuids = payouts.keySet().toArray(new UUID[payouts.keySet().size()]);
        return uuids;
    }

    public PayoutCheck checkPayout(Payout payout) {
        payout.setPayoutId(UUID.randomUUID()); // Why?
        SendRequest sendRequest = getSendRequestForPayout(payout);

        Coin balance = wallet.getBalance(Wallet.BalanceType.AVAILABLE_SPENDABLE);
        Coin amount = Coin.valueOf(payout.getAmount());
        Coin remaining;
        Coin fee;
        try {
            wallet.completeTx(sendRequest);
            fee = sendRequest.tx.getFee();
            remaining = balance.subtract(amount).subtract(fee);
        } catch (InsufficientMoneyException e) {
            fee = sendRequest.tx.getFee();
            remaining = Coin.ZERO.subtract(e.missing);
        }

        PayoutCheck pc = new PayoutCheck()
                .fee((int) fee.value)
                .remaining((int) remaining.value);
        return pc;
    }
}
