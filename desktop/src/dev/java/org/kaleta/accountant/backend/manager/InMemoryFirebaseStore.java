package org.kaleta.accountant.backend.manager;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.kaleta.accountant.Initializer;
import org.kaleta.accountant.backend.model.FirebaseTransactionModel;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Development stand-in for {@link RealtimeFirebaseStore}, backed by a JSON snapshot on disk
 * instead of the Firebase database. It needs no network and no service key, and it keeps the
 * same wire format as the real database, so a snapshot exported from the Firebase console can
 * be dropped in as seed data.
 * <p>
 * The snapshot file is created from src/dev/resources/firebase.json on first use; its location
 * can be overridden with -Dfirebase.data.file=&lt;path&gt;. Writes are persisted, so state
 * survives the restarts of the dev.sh loop.
 */
public class InMemoryFirebaseStore implements FirebaseStore {

    private static final String DATA_FILE_PROPERTY = "firebase.data.file";
    private static final String SEED_RESOURCE = "/firebase.json";

    private static final String TRANSACTION = "transaction";
    private static final String ACCOUNTS = "accounts";
    private static final String DEBIT = "debit";
    private static final String CREDIT = "credit";

    private final ObjectMapper objectMapper = new ObjectMapper();
    private final Path dataFile;

    private final List<FirebaseTransactionModel> transactionList = new ArrayList<>();
    private final Map<String, String> debitAccounts = new LinkedHashMap<>();
    private final Map<String, String> creditAccounts = new LinkedHashMap<>();

    public InMemoryFirebaseStore() throws ManagerException {
        String configured = System.getProperty(DATA_FILE_PROPERTY);
        dataFile = Path.of(configured != null ? configured : Initializer.getDataSource() + "firebase.json")
                .toAbsolutePath().normalize();
        try {
            createSnapshotIfMissing();
            load();
        } catch (IOException e) {
            throw new ManagerException(e);
        }
        Initializer.LOG.info("Firebase snapshot loaded from '" + dataFile + "': "
                + transactionList.size() + " transaction(s), "
                + debitAccounts.size() + " debit and " + creditAccounts.size() + " credit account(s)");
    }

    private void createSnapshotIfMissing() throws IOException {
        if (Files.exists(dataFile)) {
            return;
        }
        Files.createDirectories(dataFile.getParent());
        try (InputStream seed = InMemoryFirebaseStore.class.getResourceAsStream(SEED_RESOURCE)) {
            if (seed == null) {
                Files.write(dataFile, "{}".getBytes());
            } else {
                Files.copy(seed, dataFile, StandardCopyOption.REPLACE_EXISTING);
            }
        }
        Initializer.LOG.info("Firebase snapshot created: '" + dataFile + "'");
    }

    private void load() throws IOException {
        JsonNode root = objectMapper.readTree(dataFile.toFile());

        JsonNode transactions = root.path(TRANSACTION);
        for (JsonNode node : transactions) {
            FirebaseTransactionModel transaction = new FirebaseTransactionModel();
            transaction.setDate(node.path("date").asText(""));
            transaction.setDescription(node.path("description").asText(""));
            transaction.setAmount(node.path("amount").asText(""));
            transaction.setDebit(node.path("debit").asText(""));
            transaction.setCredit(node.path("credit").asText(""));
            transactionList.add(transaction);
        }

        readAccounts(root.path(ACCOUNTS).path(DEBIT), debitAccounts);
        readAccounts(root.path(ACCOUNTS).path(CREDIT), creditAccounts);
    }

    /**
     * Account keys are stored the way the real database encodes them, with '.' written as 'a'.
     */
    private void readAccounts(JsonNode node, Map<String, String> target) {
        node.fieldNames().forEachRemaining(key -> target.put(key.replace("a", "."), node.path(key).asText()));
    }

    private void persist() {
        ObjectNode root = objectMapper.createObjectNode();

        ArrayNode transactions = root.putArray(TRANSACTION);
        for (FirebaseTransactionModel transaction : transactionList) {
            ObjectNode node = transactions.addObject();
            node.put("date", transaction.getDate());
            node.put("description", transaction.getDescription());
            node.put("amount", transaction.getAmount());
            node.put("debit", transaction.getDebit());
            node.put("credit", transaction.getCredit());
        }

        ObjectNode accounts = root.putObject(ACCOUNTS);
        writeAccounts(accounts.putObject(DEBIT), debitAccounts);
        writeAccounts(accounts.putObject(CREDIT), creditAccounts);

        try {
            objectMapper.writerWithDefaultPrettyPrinter().writeValue(dataFile.toFile(), root);
        } catch (IOException e) {
            Initializer.LOG.warning("Could not persist the Firebase snapshot '" + dataFile + "': " + e);
        }
    }

    private void writeAccounts(ObjectNode node, Map<String, String> accounts) {
        accounts.forEach((id, name) -> node.put(id.replace(".", "a"), name));
    }

    @Override
    public List<FirebaseTransactionModel> getTransactionList() {
        return transactionList;
    }

    @Override
    public void clearTransactions() {
        transactionList.clear();
        persist();
    }

    @Override
    public boolean hasAccount(String accountId, boolean isDebit) {
        return (isDebit ? debitAccounts : creditAccounts).containsKey(accountId);
    }

    @Override
    public void pushAccount(String id, String name, boolean isDebit) {
        Initializer.LOG.info((isDebit ? "Debit" : "Credit") + " account added to Firebase snapshot: id=" + id + " name='" + name + "'");
        (isDebit ? debitAccounts : creditAccounts).put(id, name);
        persist();
    }
}
