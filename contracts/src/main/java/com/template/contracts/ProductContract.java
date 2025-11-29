package com.template.contracts;

import com.template.states.ProductState;
import net.corda.core.contracts.Command; // Changed Import
import net.corda.core.contracts.CommandData;
import net.corda.core.contracts.CommandWithParties; // Keep just in case, but likely unused now
import net.corda.core.contracts.Contract;
import net.corda.core.transactions.LedgerTransaction;
import org.jetbrains.annotations.NotNull;

public class ProductContract implements Contract {
    public static final String ID = "com.template.contracts.ProductContract";

    public interface Commands extends CommandData {
        class Create implements Commands {}
        class Transfer implements Commands {}
    }

    @Override
    public void verify(@NotNull LedgerTransaction tx) {
        // FIX: Use 'Command' instead of 'CommandWithParties'
        final Command<Commands> command = tx.commandsOfType(Commands.class).get(0);

        if (command.getValue() instanceof Commands.Create) {
            if (tx.getInputStates().size() != 0) throw new IllegalArgumentException("No inputs allowed when creating a product.");
            if (tx.getOutputStates().size() != 1) throw new IllegalArgumentException("One output expected.");

            ProductState output = (ProductState) tx.getOutput(0);
            if (output.getProductName().isEmpty()) throw new IllegalArgumentException("Product name cannot be empty.");

        } else if (command.getValue() instanceof Commands.Transfer) {
            if (tx.getInputStates().size() != 1) throw new IllegalArgumentException("One input expected.");
            if (tx.getOutputStates().size() != 1) throw new IllegalArgumentException("One output expected.");

            ProductState input = (ProductState) tx.getInput(0);
            ProductState output = (ProductState) tx.getOutput(0);

            if (!input.getLinearId().equals(output.getLinearId())) throw new IllegalArgumentException("Product ID must remain same.");
            if (input.getCurrentOwner().equals(output.getCurrentOwner())) throw new IllegalArgumentException("Owner must change.");
        }
    }
}