package com.template.states;

import com.template.contracts.ProductContract;
import net.corda.core.contracts.BelongsToContract;
import net.corda.core.contracts.LinearState;
import net.corda.core.contracts.UniqueIdentifier;
import net.corda.core.identity.AbstractParty;
import net.corda.core.identity.Party;
import org.jetbrains.annotations.NotNull;
import java.util.Arrays;
import java.util.List;

@BelongsToContract(ProductContract.class)
public class ProductState implements LinearState {

    private final String productName;
    private final String location; // e.g., "New York Warehouse"
    private final Party currentOwner;
    private final Party newOwner; // Target for transfer
    private final UniqueIdentifier linearId;

    // Constructor
    public ProductState(String productName, String location, Party currentOwner, Party newOwner, UniqueIdentifier linearId) {
        this.productName = productName;
        this.location = location;
        this.currentOwner = currentOwner;
        this.newOwner = newOwner;
        this.linearId = linearId;
    }

    // Getters
    public String getProductName() { return productName; }
    public String getLocation() { return location; }
    public Party getCurrentOwner() { return currentOwner; }
    public Party getNewOwner() { return newOwner; }

    @NotNull
    @Override
    public UniqueIdentifier getLinearId() { return linearId; }

    @NotNull
    @Override
    public List<AbstractParty> getParticipants() {
        // Both the current owner and the new owner (if exists) must track this state
        return Arrays.asList(currentOwner, newOwner);
    }
}