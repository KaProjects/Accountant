package org.kaleta.accountant.backend.manager;

import org.kaleta.accountant.backend.model.ConfigModel;
import org.kaleta.accountant.backend.model.StatementTransactionModel;
import org.kaleta.accountant.service.Service;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.text.DateFormatSymbols;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class StatementParserManager {

    /**
     * The shapes of statement this app can read. Which bank each one comes from, what it is called
     * and which account it is about are the user's own business and live in the configuration, so
     * that a public source says only how a file is laid out.
     */
    public static final String CARD_STATEMENT_CSV = "card-statement-csv";
    public static final String ACCOUNT_STATEMENT_CSV = "account-statement-csv";
    public static final String WALLET_STATEMENT_CSV = "wallet-statement-csv";

    private final File file;
    private final String format;
    private final String account;
    private String content;

    /**
     * @param format  one of the formats above
     * @param account the account the statement is about, which is on one side of everything on it
     */
    public StatementParserManager(File file, String format, String account)  {
        this.file = file;
        this.format = format;
        this.account = account;
    }

    /**
     * Reads the file. The exports are UTF-8, and a machine whose default is something else read them
     * as mojibake, which no substring of a description ever matched.
     */
    public String loadContent() throws IOException {
        content = new String(Files.readAllBytes(file.toPath()), StandardCharsets.UTF_8);
        return content;
    }

    public String getContent(){
        return content;
    }

    public List<StatementTransactionModel> getTransactions()  {
        if (content == null) throw new NullPointerException("first load the file content via #loadContent");

        switch (format){
            case WALLET_STATEMENT_CSV: return readWalletStatementCsv();
            case CARD_STATEMENT_CSV: return readCardStatementCsv();
            case ACCOUNT_STATEMENT_CSV: return readAccountStatementCsv();

            default: throw new IllegalArgumentException("Unknown statement format '" + format + "'");
        }
    }

    private List<StatementTransactionModel> readWalletStatementCsv(){
        List<StatementTransactionModel> transactions = new ArrayList<>();

        for (String record: content.split("\n")){

            boolean inString = false;
            StringBuilder RecordSb = new StringBuilder(record);
            for (int i=0; i<RecordSb.length(); i++){
                if (RecordSb.charAt(i) == '"') {
                    inString = !inString;
                } else if (RecordSb.charAt(i) == ',' && !inString) {
                    RecordSb.setCharAt(i, ';');
                }
            }

            String[] values = RecordSb.toString().split(";");

            StatementTransactionModel transaction = null;

            if (values[0].equals("CARD_PAYMENT")) {
                transaction = new StatementTransactionModel();

                transaction.setCredit(account);
                transaction.setDescription(values[4]);

                ConfigModel.Mapping.Entry mapping = Service.CONFIG.getMatchingMapping(transaction.getDescription(), true);
                if (mapping != null) {
                    transaction.setDebit(mapping.getAccount());
                }

            } else if (values[0].equals("TRANSFER")) {
                transaction = new StatementTransactionModel();

                if (values[4].startsWith("From")){
                    transaction.setDebit(account);
                } else if (values[4].startsWith("To")){
                    transaction.setCredit(account);
                }
                transaction.setDescription(values[4]);
            }

            if (transaction != null){
                String[] date = values[2].split(" ")[0].split("-");
                transaction.setDate(date[2] + date[1]);

                Double amount = Double.parseDouble(values[5].replace("-", ""));
                if (values[7].equals("CZK")) {
                    // no change
                } else if (values[7].equals("EUR")) {
                    amount = amount * 25;
                } else if (values[7].equals("USD")) {
                    amount = amount * 23;
                } else {
                    amount = null;
                }

                if (amount != null){
                    transaction.setAmount(String.valueOf(amount.intValue()));
                }

                if (!values[7].equals("CZK")) {
                    transaction.setDescription(values[5].replace("-", "") + values[7] + " " + transaction.getDescription());
                }

                transactions.add(transaction);
            }
        }

        System.out.println("transactions loaded: " + transactions.size());
        return transactions;
    }

    /**
     * The current account's own statement: every movement on it, out and in alike.
     * <p>
     * The account the statement is about sits on one side of every transaction - on the credit when
     * money left it, on the debit when money arrived - and the other side is what the booking is
     * really about. For money spent that side is filled in from the mappings; for money received it
     * is left open, because a mapping names a debit account and an income is not one.
     * <p>
     * The columns are found by their heading rather than by counting, because the bank has already
     * moved them once.
     */
    private List<StatementTransactionModel> readAccountStatementCsv() {
        List<StatementTransactionModel> transactions = new ArrayList<>();
        Map<String, Integer> columns = columnsOf(content);

        for (String record : content.split("\n")) {
            String[] split = record.trim().split(";", -1);
            if (split.length < 3 || !split[0].matches("\\d+/\\d{4}")) continue;

            boolean spent = split[2].startsWith("-");
            String amount = split[2].replace("-", "").split(",")[0];

            StatementTransactionModel transaction = new StatementTransactionModel();
            transaction.setDate(split[1].replace(".", "").substring(0, 4));
            transaction.setAmount(amount);
            transaction.setDescription(describe(split, columns));
            transaction.setCounterSideDebit(spent);

            // whichever side the statement leaves open is the one the mappings fill in: an expense
            // for money spent, and where the money came from for money received
            ConfigModel.Mapping.Entry mapping = Service.CONFIG.getMatchingMapping(transaction.getDescription(), spent);
            if (spent) {
                transaction.setCredit(account);
                if (mapping != null) {
                    transaction.setDebit(mapping.getAccount());
                }
            } else {
                transaction.setDebit(account);
                if (mapping != null) {
                    transaction.setCredit(mapping.getAccount());
                }
            }

            transactions.add(transaction);
        }

        System.out.println("transactions loaded: " + transactions.size());
        Collections.reverse(transactions);
        return transactions;
    }

    /**
     * What the transaction is, from the columns that carry it: who it was with, and what it was for.
     * A standing order gives its own name, a card operation its message, and an internal operation
     * only its label - so the first of each that says anything is taken.
     */
    private static String describe(String[] fields, Map<String, Integer> columns) {
        String who = firstNonEmpty(fields, columns, "jméno protistrany", "označení operace");
        String what = firstNonEmpty(fields, columns, "název trvalého příkazu", "vlastní poznámka", "zpráva");
        String description = join(who, what);

        // a payment to somebody says who and what it was for; a transfer between one's own accounts
        // says only a name, the same name on both sides, and nothing about which account it went to.
        // Those are the movements that carry neither a variable symbol nor a message, and there the
        // account on the other side is the only thing that tells them apart
        if (firstNonEmpty(fields, columns, "variabilní symbol").isEmpty()
                && firstNonEmpty(fields, columns, "zpráva").isEmpty()) {
            description = join(description, counterAccount(fields, columns));
        }
        return description;
    }

    /** The account on the other side, as the statement writes it: number, and the bank it is with. */
    private static String counterAccount(String[] fields, Map<String, Integer> columns) {
        String number = firstNonEmpty(fields, columns, "číslo protiúčtu");
        String bank = firstNonEmpty(fields, columns, "kód banky protiúčtu");
        if (number.isEmpty()) {
            return "";
        }
        return bank.isEmpty() ? number : number + "/" + bank;
    }

    private static String join(String first, String second) {
        if (first.isEmpty()) {
            return second;
        }
        return second.isEmpty() ? first : first + " - " + second;
    }

    private static String firstNonEmpty(String[] fields, Map<String, Integer> columns, String... headings) {
        for (String heading : headings) {
            Integer column = columns.get(heading);
            if (column != null && column < fields.length && !fields[column].isBlank()) {
                return fields[column].trim();
            }
        }
        return "";
    }

    /** Where each column of the export is, by the heading the bank gave it. */
    private static Map<String, Integer> columnsOf(String content) {
        Map<String, Integer> columns = new HashMap<>();
        for (String record : content.split("\n")) {
            String[] split = record.trim().split(";", -1);
            if (split.length > 3 && split[1].equals("datum zaúčtování")) {
                for (int i = 0; i < split.length; i++) {
                    columns.put(split[i].trim(), i);
                }
                break;
            }
        }
        return columns;
    }

    /** The first field that says what it is, whichever column the export happens to put it in. */
    private static String fieldStartingWith(String[] fields, String prefix) {
        for (String field : fields) {
            if (field.startsWith(prefix)) {
                return field;
            }
        }
        return null;
    }

    private List<StatementTransactionModel> readCardStatementCsv() {
        List<StatementTransactionModel> transactions = new ArrayList<>();

        for (String record: content.split("\n")) {
            String[] split = record.trim().split(";");

            // a movement starts with the account it moved on; the export also carries a title line,
            // a blank line and the column names, and any of them would be read as a transaction
            if (split.length < 3 || !split[0].matches("\\d+/\\d{4}")) continue;

            if (!split[2].startsWith("-")) continue;

            String amount = split[2].replace("-", "").split(",")[0];

            // the card's note is found by what it says rather than by which column it is in: the
            // bank has added columns before it, and every fixed index broke the day it did
            String note = fieldStartingWith(split, "Částka:");
            if (note == null || !note.contains("Místo: ")) continue;

            String description = note.split("Místo: ")[1];

            String date = split[1].replace(".", "").substring(0,4);

            StatementTransactionModel transaction = new StatementTransactionModel();
            transaction.setAmount(amount);
            transaction.setDate(date);
            transaction.setDescription(description);
            transaction.setCredit(account);

            ConfigModel.Mapping.Entry mapping = Service.CONFIG.getMatchingMapping(transaction.getDescription(), true);
            if (mapping != null) {
                transaction.setDebit(mapping.getAccount());
            }

            transactions.add(transaction);
        }

        System.out.println("transactions loaded: " + transactions.size());
        Collections.reverse(transactions);
        return transactions;
    }
}
