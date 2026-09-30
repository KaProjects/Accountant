package org.kaleta.rest.data;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Writes a complete, self-consistent datasource for the tests in this package to read, in the
 * same shape as the one the desktop app produces: a config and a schema in the root, and a
 * directory per year holding that year's accounts and transactions.
 * <p>
 * The data is generated rather than checked in because the accounting rules the backend
 * validates are relationships between years, and deriving them is the only way to keep them
 * true. Every year opens each balance account with the previous year's closing balance, closes
 * the revenue and expense accounts into the profit account, and carries the result into the
 * next year's accumulated earnings. That makes the sync validator's rules hold by construction
 * instead of by careful editing: each account's debits equal its credits in a closed year, and
 * the running year's assets equal its liabilities plus its profit.
 * <p>
 * The years end with the current one, which is the active year, because the views under test
 * are relative to today: a chart stops at the current month, and the financial assets view
 * trims the months that have not happened yet.
 */
final class FakeDataSource
{
    static final String LOCATION = "target/test-datasource/";

    private static final int YEAR_COUNT = 4;
    private static final int OPENING_CAPITAL = 5000;

    /** Accounts held in the balance sheet, in the order they are opened and closed. */
    private static final Map<String, Character> BALANCE_ACCOUNTS = new LinkedHashMap<>();
    /** Accounts closed into the profit account at the end of a year. */
    private static final Map<String, Character> RESULT_ACCOUNTS = new LinkedHashMap<>();

    static {
        BALANCE_ACCOUNTS.put("000.0", 'A');
        BALANCE_ACCOUNTS.put("090.0", 'L');
        BALANCE_ACCOUNTS.put("100.0", 'A');
        BALANCE_ACCOUNTS.put("200.0", 'A');
        BALANCE_ACCOUNTS.put("210.0", 'A');
        BALANCE_ACCOUNTS.put("220.0", 'L');
        BALANCE_ACCOUNTS.put("230.0", 'A');
        BALANCE_ACCOUNTS.put("231.0", 'A');
        BALANCE_ACCOUNTS.put("300.0", 'A');
        BALANCE_ACCOUNTS.put("310.0", 'L');
        BALANCE_ACCOUNTS.put("400.0", 'L');
        BALANCE_ACCOUNTS.put("401.0", 'L');

        RESULT_ACCOUNTS.put("500.0", 'E');
        RESULT_ACCOUNTS.put("510.0", 'E');
        RESULT_ACCOUNTS.put("520.0", 'E');
        RESULT_ACCOUNTS.put("530.0", 'E');
        RESULT_ACCOUNTS.put("540.0", 'E');
        RESULT_ACCOUNTS.put("548.1-0", 'E');
        RESULT_ACCOUNTS.put("549.0-0", 'E');
        RESULT_ACCOUNTS.put("550.0", 'E');
        RESULT_ACCOUNTS.put("556.0", 'E');
        RESULT_ACCOUNTS.put("560.0", 'E');
        RESULT_ACCOUNTS.put("600.0", 'R');
        RESULT_ACCOUNTS.put("610.0", 'R');
        RESULT_ACCOUNTS.put("620.0", 'R');
        RESULT_ACCOUNTS.put("629.0-0", 'R');
        RESULT_ACCOUNTS.put("630.0", 'R');
        RESULT_ACCOUNTS.put("631.0", 'R');
        RESULT_ACCOUNTS.put("635.0", 'R');
    }

    private static boolean generated;

    private FakeDataSource()
    {
    }

    /** The years of the datasource, oldest first. The last one is the active year. */
    static List<String> years()
    {
        int currentYear = new GregorianCalendar().get(Calendar.YEAR);
        List<String> years = new ArrayList<>();
        for (int i = YEAR_COUNT - 1; i >= 0; i--) years.add(String.valueOf(currentYear - i));
        return years;
    }

    static String activeYear()
    {
        List<String> years = years();
        return years.get(years.size() - 1);
    }

    /**
     * Writes the datasource, once per JVM. Every test class in this package calls this before
     * its first request, because any of them may be the one that runs first.
     */
    static synchronized void generate()
    {
        if (generated) return;
        try {
            write();
            generated = true;
        } catch (IOException e) {
            throw new UncheckedIOException("could not write the test datasource", e);
        }
    }

    private static void write() throws IOException
    {
        Path root = Path.of(LOCATION);
        Files.createDirectories(root);

        List<String> years = years();
        Files.writeString(root.resolve("config.xml"), config(years));
        Files.writeString(root.resolve("schema.xml"), schema());

        Map<String, Integer> opening = new LinkedHashMap<>();
        for (String account : BALANCE_ACCOUNTS.keySet()) opening.put(account, 0);
        opening.put("200.0", OPENING_CAPITAL);
        opening.put("400.0", OPENING_CAPITAL);

        int previousProfit = 0;
        for (int i = 0; i < years.size(); i++)
        {
            String year = years.get(i);
            boolean active = year.equals(activeYear());
            String previousYear = i == 0 ? null : years.get(i - 1);

            Path yearDir = root.resolve(year);
            Files.createDirectories(yearDir);
            Files.writeString(yearDir.resolve("accounts.xml"), accounts(year));

            Year generatedYear = new Year(year, i, opening, previousYear, previousProfit, active);
            Files.writeString(yearDir.resolve("transactions.xml"), generatedYear.transactions());

            opening = generatedYear.closingBalances();
            previousProfit = generatedYear.profit();
        }
    }

    private static String config(List<String> years)
    {
        StringBuilder sb = new StringBuilder(header()).append("<config>\n")
                .append("    <years active=\"").append(activeYear()).append("\">\n");
        for (String year : years) sb.append("        <year name=\"").append(year).append("\"/>\n");
        return sb.append("    </years>\n").append("</config>\n").toString();
    }

    /**
     * The schema is deliberately sparse: a group holds only the accounts the generated
     * transactions use, so a view that asks for an account the schema does not have is
     * exercised too. Groups 55 and 63 are the interesting ones, because the profit views split
     * them into two buckets and a bucket must keep working when only part of it exists.
     */
    private static String schema()
    {
        Map<String, String> groups = new LinkedHashMap<>();
        groups.put("00", "A");
        groups.put("09", "L");
        groups.put("10", "A");
        groups.put("20", "A");
        groups.put("21", "A");
        groups.put("22", "L");
        groups.put("23", "AA");
        groups.put("30", "A");
        groups.put("31", "L");
        groups.put("40", "LL");
        groups.put("50", "E");
        groups.put("51", "E");
        groups.put("52", "E");
        groups.put("53", "E");
        groups.put("54", "E..8E9E");
        groups.put("55", "E..6E");
        groups.put("56", "E");
        groups.put("60", "R");
        groups.put("61", "R");
        groups.put("62", "R..9R");
        groups.put("63", "R1R..5R");
        groups.put("70", "XX");
        groups.put("71", "X");

        StringBuilder sb = new StringBuilder(header()).append("<schema>\n");
        for (int clazz = 0; clazz <= 7; clazz++)
        {
            sb.append("    <class id=\"").append(clazz).append("\" name=\"class").append(clazz).append("\">\n");
            for (Map.Entry<String, String> group : groups.entrySet())
            {
                if (group.getKey().charAt(0) != Character.forDigit(clazz, 10)) continue;
                sb.append("        <group id=\"").append(group.getKey().charAt(1))
                        .append("\" name=\"group").append(group.getKey()).append("\">\n");
                for (Map.Entry<String, Character> account : accountsOf(group.getKey(), group.getValue()).entrySet())
                {
                    sb.append("            <account id=\"").append(account.getKey())
                            .append("\" name=\"account").append(group.getKey()).append(account.getKey())
                            .append("\" type=\"").append(account.getValue()).append("\"/>\n");
                }
                sb.append("        </group>\n");
            }
            sb.append("    </class>\n");
        }
        return sb.append("</schema>\n").toString();
    }

    /**
     * Reads a group's account layout. Consecutive types starting at account 0 are written as
     * just their types ("AA" is accounts 0 and 1), and a gap is written as the account id
     * followed by its type ("E..8E" is account 0 and account 8).
     */
    private static Map<String, Character> accountsOf(String groupId, String layout)
    {
        Map<String, Character> accounts = new LinkedHashMap<>();
        int next = 0;
        for (int i = 0; i < layout.length(); i++)
        {
            char c = layout.charAt(i);
            if (c == '.') continue;
            if (Character.isDigit(c)) {
                accounts.put(String.valueOf(c), layout.charAt(++i));
                next = Character.getNumericValue(c) + 1;
            } else {
                accounts.put(String.valueOf(next++), c);
            }
        }
        return accounts;
    }

    private static String accounts(String year)
    {
        List<String> all = new ArrayList<>(BALANCE_ACCOUNTS.keySet());
        all.addAll(RESULT_ACCOUNTS.keySet());
        all.add("700.0");
        all.add("701.0");
        all.add("710.0");

        StringBuilder sb = new StringBuilder(header())
                .append("<accounts year=\"").append(year).append("\">\n");
        for (String account : all)
        {
            String[] parts = account.split("\\.", 2);
            sb.append("    <account schemaId=\"").append(parts[0])
                    .append("\" semanticId=\"").append(parts[1])
                    .append("\" name=\"account").append(parts[0]).append("\" metadata=\"\"/>\n");
        }
        return sb.append("</accounts>\n").toString();
    }

    private static String header()
    {
        return "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>\n";
    }

    /** One year's transactions, and the balances they leave behind. */
    private static final class Year
    {
        private final String year;
        private final boolean active;
        private final List<String> rows = new ArrayList<>();
        private final Map<String, Integer> balances = new LinkedHashMap<>();
        /** The balance sheet as it stood before the closing entries emptied it. */
        private final Map<String, Integer> carried = new LinkedHashMap<>();
        private int id;
        private int profit;

        private Year(String year, int index, Map<String, Integer> opening, String previousYear,
                     int previousProfit, boolean active)
        {
            this.year = year;
            this.active = active;
            BALANCE_ACCOUNTS.forEach((account, type) -> balances.put(account, 0));
            RESULT_ACCOUNTS.forEach((account, type) -> balances.put(account, 0));

            open(opening, previousYear, previousProfit);
            move(index);
            BALANCE_ACCOUNTS.forEach((account, type) -> carried.put(account, balances.get(account)));
            if (!active) close();
        }

        private void open(Map<String, Integer> opening, String previousYear, int previousProfit)
        {
            BALANCE_ACCOUNTS.forEach((account, type) -> {
                int amount = opening.getOrDefault(account, 0);
                if (type == 'A') add("0101", "initiation", amount, account, "700.0");
                else add("0101", "initiation", amount, "700.0", account);
            });
            if (previousYear != null) {
                add("0101", "Profit from " + previousYear, previousProfit, "700.0", "401.0");
            }
        }

        /**
         * The movements of an ordinary year: a salary every month, the costs it pays for, and
         * the occasional dealing with a bank, an institution or a financial asset. The amounts
         * shift from year to year so that a view which mixes years up cannot pass by accident.
         */
        private void move(int index)
        {
            for (int month = 1; month <= 12; month++)
            {
                String date = date(15, month);
                add(date, "salary", 1000 + 10 * index, "200.0", "600.0");
                add(date, "services", 200, "520.0", "200.0");
                add(date, "consumption", 150, "510.0", "200.0");
                add(date, "insurance", 100, "550.0", "200.0");
                add(date, "operations", 40, "530.0", "200.0");
            }
            add(date(3, 1), "equipment", 250, "000.0", "200.0");
            add(date(3, 1), "supplies", 120, "100.0", "200.0");
            add(date(4, 2), "to savings", 300, "210.0", "200.0");
            add(date(4, 2), "deposit", 400 + 10 * index, "230.0", "200.0");
            add(date(4, 2), "entry fee", 10, "549.0-0", "200.0");
            add(date(4, 2), "bank fee", 20, "540.0", "200.0");
            add(date(5, 3), "borrowed", 500, "200.0", "220.0");
            add(date(5, 3), "health refund", 60, "200.0", "631.0");
            add(date(6, 4), "deposit", 600 + 20 * index, "231.0", "200.0");
            add(date(6, 4), "interest", 15, "200.0", "620.0");
            add(date(7, 5), "lent out", 80, "300.0", "200.0");
            add(date(7, 5), "vehicle refund", 30, "200.0", "630.0");
            add(date(8, 6), "owed", 90, "200.0", "310.0");
            add(date(8, 6), "office fee", 10, "556.0", "200.0");
            add(date(9, 7), "property cost", 35, "560.0", "200.0");
            add(date(9, 8), "property income", 45, "200.0", "610.0");
            add(date(10, 9), "revaluation", 70, "548.1-0", "231.0");
            add(date(10, 9), "travel refund", 25, "200.0", "635.0");
            add(date(11, 10), "repaid", 100, "220.0", "200.0");
            add(date(11, 10), "withdrawal", 150, "200.0", "231.0");
            add(date(12, 11), "revaluation", 120 + 5 * index, "230.0", "629.0-0");
            add(date(12, 12), "depreciation", 50, "500.0", "000.0");
        }

        /**
         * Closing a year: the result accounts go into the profit account, the profit itself
         * goes on to the closing account, and every balance account follows it. The closing
         * account then balances on its own, because assets can only differ from liabilities by
         * the profit that was made.
         */
        private void close()
        {
            int revenues = 0;
            int expenses = 0;
            for (Map.Entry<String, Character> account : RESULT_ACCOUNTS.entrySet())
            {
                int balance = balances.get(account.getKey());
                if (account.getValue() == 'R') {
                    revenues += balance;
                    if (balance != 0) add("3112", "closure", balance, account.getKey(), "710.0");
                } else {
                    expenses += balance;
                    if (balance != 0) add("3112", "closure", balance, "710.0", account.getKey());
                }
            }
            profit = revenues - expenses;
            add("3112", "profit balance -> closing balance", profit, "710.0", "701.0");

            BALANCE_ACCOUNTS.forEach((account, type) -> {
                int balance = balances.get(account);
                if (balance == 0) return;
                if (type == 'A') add("3112", "closure", balance, "701.0", account);
                else add("3112", "closure", balance, account, "701.0");
            });
        }

        private String date(int day, int month)
        {
            return String.format("%02d%02d", day, month);
        }

        private void add(String date, String description, int amount, String debit, String credit)
        {
            rows.add(String.format(
                    "    <transaction id=\"%s\" date=\"%s\" description=\"%s\" amount=\"%d\" debit=\"%s\" credit=\"%s\"/>",
                    year.substring(2) + "-" + id++, date, description, amount, debit, credit));
            record(debit, amount, true);
            record(credit, amount, false);
        }

        private void record(String account, int amount, boolean debit)
        {
            Character type = BALANCE_ACCOUNTS.get(account);
            if (type == null) type = RESULT_ACCOUNTS.get(account);
            if (type == null) return; // an off-balance account carries no balance of its own

            boolean debitSide = type == 'A' || type == 'E';
            balances.merge(account, debitSide == debit ? amount : -amount, Integer::sum);
        }

        private String transactions()
        {
            StringBuilder sb = new StringBuilder(header())
                    .append("<transactions year=\"").append(year).append("\">\n");
            for (String row : rows) sb.append(row).append("\n");
            return sb.append("</transactions>\n").toString();
        }

        /** What the next year opens with: the balance accounts as this year left them. */
        private Map<String, Integer> closingBalances()
        {
            return carried;
        }

        private int profit()
        {
            return active ? 0 : profit;
        }
    }
}
