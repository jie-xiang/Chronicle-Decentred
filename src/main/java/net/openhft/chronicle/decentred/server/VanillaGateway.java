package net.openhft.chronicle.decentred.server;

import net.openhft.chronicle.decentred.dto.*;
import net.openhft.chronicle.decentred.util.DecentredUtil;
import net.openhft.chronicle.decentred.util.PublicKeyRegistry;

/**
 * This accepts message from the XCLServer and passes them to the appropriate downstream component
 */
public class VanillaGateway implements Gateway {
    private final long address;
    private final long chainAddress;
    private final BlockEngine main;
    private final BlockEngine local;
    private PublicKeyRegistry publicKeyRegistery;

    public VanillaGateway(long address, long chainAddress, BlockEngine main, BlockEngine local) {
        this.address = address;
        this.chainAddress = chainAddress;
        this.main = main;
        this.local = local;
    }


    public static VanillaGateway newGateway(long address, String regionStr, long[] clusterAddresses, int mainPeriodMS, int localPeriodMS) {
        long region = DecentredUtil.parseAddress(regionStr);
        return new VanillaGateway(address,
                region,
                BlockEngine.newMain(address, mainPeriodMS, clusterAddresses),
                BlockEngine.newLocal(address, region, localPeriodMS, clusterAddresses, 2 << 20)
        );
    }

    @Override
    public void createAccountRequest(CreateAddressRequest createAddressRequest) {
        main.chainer().onMessage(createAddressRequest);
    }

    @Override
    public void verificationEvent(VerificationEvent verificationEvent) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void invalidationEvent(InvalidationEvent record) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void transactionBlockEvent(TransactionBlockEvent transactionBlockEvent) {
        long chainAddress = transactionBlockEvent.chainAddress();
        if (isMainChain(chainAddress))
            main.transactionBlockEvent(transactionBlockEvent);
        else if (this.chainAddress == this.chainAddress)
            local.transactionBlockEvent(transactionBlockEvent);
        else
            System.err.println("Unknown chainAddress " + this.chainAddress);
    }

    private boolean isMainChain(long chainAddress) {
        return chainAddress == 0L;
    }

    @Override
    public void transactionBlockGossipEvent(TransactionBlockGossipEvent transactionBlockGossipEvent) {
        long chainAddress = transactionBlockGossipEvent.chainAddress();
        if (isMainChain(chainAddress))
            main.transactionBlockGossipEvent(transactionBlockGossipEvent);
        else if (this.chainAddress == chainAddress)
            local.transactionBlockGossipEvent(transactionBlockGossipEvent);
        else
            System.err.println("Unknown chainAddress " + chainAddress);
    }

    @Override
    public void transactionBlockVoteEvent(TransactionBlockVoteEvent transactionBlockVoteEvent) {
        long chainAddress = transactionBlockVoteEvent.chainAddress();
        if (isMainChain(chainAddress))
            main.transactionBlockVoteEvent(transactionBlockVoteEvent);
        else if (this.chainAddress == this.chainAddress)
            local.transactionBlockVoteEvent(transactionBlockVoteEvent);
        else
            System.err.println("Unknown chainAddress " + this.chainAddress);
    }

    @Override
    public void endOfRoundBlockEvent(EndOfRoundBlockEvent endOfRoundBlockEvent) {
        long chainAddress = endOfRoundBlockEvent.chainAddress();
        if (isMainChain(chainAddress))
            main.endOfRoundBlockEvent(endOfRoundBlockEvent);
        else if (this.chainAddress == chainAddress)
            local.endOfRoundBlockEvent(endOfRoundBlockEvent);
        else
            System.err.println("Unknown chainAddress " + this.chainAddress);
    }

    public void createAccountEvent(CreateAddressEvent createAddressEvent) {
        // received as a weekly event
        checkTrusted(createAddressEvent);
        publicKeyRegistery.register(createAddressEvent.address(),
                createAddressEvent.publicKey());
    }

    private void checkTrusted(SignedMessage message) {

    }

    @Override
    public void start() {
        main.start();
        local.start();
    }

    @Override
    public void close() {
        main.close();
        local.close();
    }
}