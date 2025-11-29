Here's your README in proper Markdown format:

```markdown
# Decentralized Supply Chain Management System (R3 Corda)

![Java](https://img.shields.io/badge/Java-JDK_1.8-orange) ![Corda](https://img.shields.io/badge/R3_Corda-Distributed_Ledger-red) ![Gradle](https://img.shields.io/badge/Gradle-Build_Tool-blue)

A decentralized application (**CorDapp**) built on **R3 Corda** to track product ownership and history in a supply chain. This project establishes a "Single Source of Truth" between Manufacturers and Wholesalers, eliminating data silos and enforcing automated business rules via Smart Contracts.

---

## 🚀 Key Features

* **Immutable Ledger:** Products are tracked using `LinearState`, ensuring a permanent, unchangeable history of the asset.
* **Smart Contract Validation:** Automated "Digital Gatekeepers" (`ProductContract`) enforce rules (e.g., Product Name cannot be empty) before any data is written to the ledger.
* **Peer-to-Peer Privacy:** Transaction data is shared *only* between the Manufacturer and the Receiver using Corda's privacy model (Party C cannot see the data).
* **Automated Business Logic:** Custom Responder flows automatically reject transactions that violate specific business policies (e.g., rejecting goods labeled "Damaged").

---

## 🏗️ System Architecture

### 1. The State (`ProductState.java`)
Defines the schema of the asset on the ledger.
* **Type:** `LinearState` (Tracks history via a unique `linearId`).
* **Fields:** `productName`, `location`, `currentOwner`, `newOwner`.
* **Privacy:** `getParticipants()` ensures only the owner and new owner see the transaction.

### 2. The Contract (`ProductContract.java`)
Enforces global validation rules (The "Digital Police").
* **Rule 1:** A `Create` transaction must have 0 inputs and 1 output.
* **Rule 2:** Product Name cannot be empty.
* **Rule 3:** During a `Transfer`, the `linearId` must remain constant (Asset Preservation).

### 3. The Workflow (`CreateProductFlow.java` & `Responder`)
Manages the transaction lifecycle.
* **Initiator:** Builds the transaction, signs it, and requests the counterparty's signature.
* **Responder:** Verifies the transaction and runs local business logic checks (e.g., `if name contains "Damaged" -> REJECT`).

---

## 🛠️ Prerequisites

* **Java 8 (JDK 1.8)** - *Strictly required for Corda.*
* **IntelliJ IDEA** (Recommended IDE).
* **Git**.

---

## ⚡ How to Run the Project

### 1. Clone & Build
```bash
git clone https://github.com/sgk0204/Decentralized-Supply-Chain-Management-using-R3-Corda.git
cd Decentralized-Supply-Chain-Management-using-R3-Corda
```

**Build the Nodes:**

```bash
# Windows
gradlew deployNodes

# Mac/Linux
./gradlew deployNodes
```

### 2. Start the Network

Navigate to the build folder and start the nodes:

```bash
cd build/nodes
runnodes.bat  # (Windows) or ./runnodes (Mac/Linux)
```

*Wait for the `>>>` prompt to appear in the PartyA and PartyB terminals.*

---

## 🧪 Usage & Testing Scenarios

### Scenario A: Successful Creation (Happy Path)

**Goal:** Manufacturer (PartyA) creates a valid product and sends it to Wholesaler (PartyB).

Run this in **PartyA** terminal:

```bash
flow start CreateProductFlow productName: "Samsung Galaxy S24", location: "Noida Factory", receiver: PartyB
```

**Expected Output:**

> `Flow completed with result: SignedTransaction(id=...)`

**Verify Data:**
Run this in **PartyB** terminal to see the ledger update:

```bash
run vaultQuery contractStateType: com.template.states.ProductState
```

---

### Scenario B: Business Logic Rejection (Failing Path)

**Goal:** Test if the **Responder** logic catches bad data. PartyB is configured to reject "Damaged" goods.

Run this in **PartyA** terminal:

```bash
flow start CreateProductFlow productName: "Damaged Samsung Galaxy", location: "Noida Factory", receiver: PartyB
```

**Expected Output:**

> `net.corda.core.flows.FlowException: REJECTED: We do not accept damaged goods.`

*Note: The transaction is blocked instantly and nothing is saved to the ledger.*

---

### Scenario C: Contract Rejection (Invalid Input)

**Goal:** Test if the **Smart Contract** catches empty data.

Run this in **PartyA** terminal:

```bash
flow start CreateProductFlow productName: "", location: "Nowhere", receiver: PartyB
```

**Expected Output:**

> `TransactionVerificationException$ContractRejection: Product name cannot be empty.`

---

## 📂 Project Structure

```
.
├── contracts/src/main/java/com/template/
│   ├── states/
│   │   └── ProductState.java       # The Data Schema
│   └── contracts/
│       └── ProductContract.java    # The Validation Rules
│
└── workflows/src/main/java/com/template/
    └── flows/
        ├── CreateProductFlow.java      # Sender Logic
        └── CreateProductResponder.java # Receiver Logic (Rejection Check)
```

---

