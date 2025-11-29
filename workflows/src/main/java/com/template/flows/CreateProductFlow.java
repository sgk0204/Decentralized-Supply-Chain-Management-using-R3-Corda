package com.template.flows;

import com.template.contracts.ProductContract;
import com.template.states.ProductState;
import net.corda.core.contracts.Command;
import net.corda.core.contracts.UniqueIdentifier;
import net.corda.core.flows.*;
import net.corda.core.identity.Party;
import net.corda.core.transactions.SignedTransaction;
import net.corda.core.transactions.TransactionBuilder;
import co.paralleluniverse.fibers.Suspendable;

import java.util.Arrays;
import java.util.Collections;

@InitiatingFlow
@StartableByRPC
public class CreateProductFlow extends FlowLogic<SignedTransaction> {

    private final String productName;
    private final String location;
    private final Party receiver; // Who we are sending it to initially (or keep it self)

    public CreateProductFlow(String productName, String location, Party receiver) {
        this.productName = productName;
        this.location = location;
        this.receiver = receiver;
    }

    @Override
    @Suspendable
    public SignedTransaction call() throws FlowException {
        // 1. Get reference to the notary
        final Party notary = getServiceHub().getNetworkMapCache().getNotaryIdentities().get(0);

        // 2. Create the state
        Party me = getOurIdentity();
        ProductState state = new ProductState(productName, location, me, receiver, new UniqueIdentifier());

        // 3. Build the transaction command
        Command<ProductContract.Commands.Create> command = new Command<>(
                new ProductContract.Commands.Create(),
                Arrays.asList(me.getOwningKey(), receiver.getOwningKey())
        );

        // 4. Build the transaction
        TransactionBuilder builder = new TransactionBuilder(notary)
                .addOutputState(state, ProductContract.ID)
                .addCommand(command);

        // 5. Verify and Sign
        builder.verify(getServiceHub());
        SignedTransaction ptx = getServiceHub().signInitialTransaction(builder);

        // 6. Collect signature from the Receiver (if different party)
        FlowSession receiverSession = initiateFlow(receiver);
        SignedTransaction stx = subFlow(new CollectSignaturesFlow(ptx, Collections.singletonList(receiverSession)));

        // 7. Finalize and record in both vaults
        return subFlow(new FinalityFlow(stx, Collections.singletonList(receiverSession)));
    }
}