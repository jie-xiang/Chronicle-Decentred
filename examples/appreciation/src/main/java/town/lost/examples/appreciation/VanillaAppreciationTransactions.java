package town.lost.examples.appreciation;

import net.openhft.chronicle.decentred.api.MessageRouter;
import net.openhft.chronicle.decentred.dto.SignedMessage;
import town.lost.examples.appreciation.api.AppreciationMessages;
import town.lost.examples.appreciation.api.AppreciationRequests;
import town.lost.examples.appreciation.api.AppreciationResponses;
import town.lost.examples.appreciation.api.AppreciationTransactions;
import town.lost.examples.appreciation.dto.Give;
import town.lost.examples.appreciation.dto.OnBalance;
import town.lost.examples.appreciation.dto.OpeningBalance;
import town.lost.examples.appreciation.dto.Topup;
import town.lost.examples.appreciation.util.BalanceStore;
import town.lost.examples.appreciation.util.Balances;

/**
 * Run from the blockchain.
 */
public class VanillaAppreciationTransactions implements AppreciationTransactions {

    protected MessageRouter<AppreciationResponses> router;
    private final BalanceStore balanceStore;

    public VanillaAppreciationTransactions(
            MessageRouter<AppreciationResponses> router,
            BalanceStore balanceStore) {
        this.router = router;
        this.balanceStore = balanceStore;
    }

    @Override
    public void openingBalance(OpeningBalance openingBalance) {
        balanceStore.setBalance(openingBalance.balanceAddress(), openingBalance.amount());
    }

    @Override
    public void give(Give give) {
        long fromKey = give.address();
        long toKey = give.toAddress();
        if (balanceStore.subtractBalance(fromKey, give.amount())) {
            balanceStore.addBalance(toKey, give.amount());
            OnBalance onBalanceFrom = new OnBalance();
            OnBalance onBalanceTo = new OnBalance();

            onBalanceFrom.timestampUS(give.timestampUS());
            onBalanceTo.timestampUS(give.timestampUS());

            router.to(fromKey)
                    .onBalance(onBalanceFrom.init(fromKey, balanceStore.getBalances(fromKey)));
            router.to(toKey)
                    .onBalance(onBalanceTo.init(toKey, balanceStore.getBalances(toKey)));
        }
    }

    @Override
    public void topup(Topup topup) {
        balanceStore.setFreeBalance(topup.amount());
        OnBalance onBalance = new OnBalance();
        onBalance.timestampUS(topup.timestampUS());
        long address = topup.address();
        Balances balances = balanceStore.getBalances(address);
        if (balances != null)
            router.to(address)
                    .onBalance(onBalance.init(address, balances));
    }
}
