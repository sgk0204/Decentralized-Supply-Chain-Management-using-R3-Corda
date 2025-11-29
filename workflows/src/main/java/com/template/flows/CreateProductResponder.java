package com.template.flows;

import com.template.states.ProductState; // Import the State
import co.paralleluniverse.fibers.Suspendable;
import net.corda.core.contracts.ContractState;
import net.corda.core.flows.*;
import net.corda.core.transactions.SignedTransaction;
import net.corda.core.identity.Party;

@InitiatedBy(CreateProductFlow.class)
public class CreateProductResponder extends FlowLogic<SignedTransaction> {

    private final FlowSession counterpartySession;

    public CreateProductResponder(FlowSession counterpartySession) {
        this.counterpartySession = counterpartySession;
    }

    @Override
    @Suspendable
    public SignedTransaction call() throws FlowException {

        return subFlow(new SignTransactionFlow(counterpartySession) {
            @Override
            protected void checkTransaction(SignedTransaction stx) throws FlowException {

                // 1. Extract the State from the transaction
                // We use getCoreTransaction() because the Notary hasn't signed it yet.
                ContractState output = stx.getCoreTransaction().getOutputStates().get(0);

                // 2. Security Check: Is this actually a ProductState?
                if (!(output instanceof ProductState)) {
                    throw new FlowException("This is not a ProductState! I refuse to sign.");
                }

                // Cast it so we can read the fields
                ProductState product = (ProductState) output;

                // 3. Business Check: Do we accept this product name?
                // Example: We are a strict warehouse, we don't accept "Damaged" goods.
                if (product.getProductName().contains("Damaged")) {
                    throw new FlowException("REJECTED: We do not accept damaged goods.");
                }

                // 4. Identity Check: Am I actually the new owner?
                // If PartyA says "New Owner is PartyC", why is PartyA asking ME to sign?
                Party me = getOurIdentity();
                if (!product.getNewOwner().equals(me)) {
                    throw new FlowException("REJECTED: I am not the new owner specified in this state.");
                }

                // If code reaches here, everything is good!
            }
        });
    }
}