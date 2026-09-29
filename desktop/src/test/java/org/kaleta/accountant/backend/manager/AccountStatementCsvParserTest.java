package org.kaleta.accountant.backend.manager;

import org.junit.Assert;
import org.junit.Test;
import org.kaleta.accountant.backend.model.StatementTransactionModel;
import org.kaleta.accountant.core.TestParent;
import org.kaleta.accountant.service.Service;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.List;

/**
 * Reading a current account's statement, which carries what left the account and what arrived on it.
 * <p>
 * The account the statement is about is on one side of every movement - credited when money left,
 * debited when money came in - and the other side is what the booking is about.
 */
public class AccountStatementCsvParserTest extends TestParent {

    private static final String ACCOUNT = "210.0";
    private static final String HEADER = "číslo účtu;datum zaúčtování;částka;měna;"
            + "zůstatek;číslo protiúčtu;kód banky protiúčtu;jméno protistrany;"
            + "adresa protistrany;konstantní symbol;variabilní symbol;specifický symbol;označení operace;"
            + "název trvalého příkazu;vlastní poznámka;zpráva;kategorie";

    /** counterparty, operation, standing order name, message - the columns a description is made of */
    private static String record(String date, String amount, String counterparty, String operation,
                                 String standingOrder, String message) {
        return record(date, amount, counterparty, operation, standingOrder, message, "123", "0300", "5566");
    }

    /** ... and the columns that say whether anything else identifies the movement */
    private static String record(String date, String amount, String counterparty, String operation,
                                 String standingOrder, String message,
                                 String counterAccount, String counterBank, String variableSymbol) {
        return "222260349/0300;" + date + ";" + amount + ";CZK;1000,00;" + counterAccount + ";" + counterBank + ";"
                + counterparty + ";;;" + variableSymbol + ";;" + operation + ";" + standingOrder + ";;" + message + ";Kategorie";
    }

    private List<StatementTransactionModel> parse(String... records) throws Exception {
        StringBuilder csv = new StringBuilder("Pohyby na uctu 222260349/0300 dne 01.01.2026\r\n\r\n").append(HEADER).append("\r\n");
        for (String record : records) {
            csv.append(record).append("\r\n");
        }
        File file = Files.createTempFile("statement", ".csv").toFile();
        file.deleteOnExit();
        Files.write(file.toPath(), csv.toString().getBytes(StandardCharsets.UTF_8));

        StatementParserManager manager = new StatementParserManager(file, StatementParserManager.ACCOUNT_STATEMENT_CSV, ACCOUNT);
        manager.loadContent();
        return manager.getTransactions();
    }

    @Test
    public void moneyThatLeftTheAccountCreditsIt() throws Exception {
        StatementTransactionModel transaction = parse(record("20.09.2026", "-2090,00", "PRAZSKA ENERGETIKA", "Trvaly prikaz", "elektrika", "")).get(0);

        Assert.assertEquals(ACCOUNT, transaction.getCredit());
        Assert.assertNull("the account it was spent on is not known from the statement", transaction.getDebit());
        Assert.assertTrue("the debit is the side left open", transaction.isCounterSideDebit());
        Assert.assertEquals("2090", transaction.getAmount());
        Assert.assertEquals("2009", transaction.getDate());
    }

    @Test
    public void moneyThatArrivedDebitsIt() throws Exception {
        StatementTransactionModel transaction = parse(record("14.09.2026", "508016,00", "ORACLE GLOBAL SERVIC", "Prichozi uhrada", "", "")).get(0);

        Assert.assertEquals(ACCOUNT, transaction.getDebit());
        Assert.assertNull(transaction.getCredit());
        Assert.assertFalse("the credit is the side left open, and no mapping names one",
                transaction.isCounterSideDebit());
        Assert.assertEquals("508016", transaction.getAmount());
    }

    /** Both directions are read: a statement is not only what was spent. */
    @Test
    public void everyMovementIsRead() throws Exception {
        List<StatementTransactionModel> transactions = parse(
                record("20.09.2026", "-2090,00", "PRAZSKA ENERGETIKA", "Trvaly prikaz", "elektrika", ""),
                record("14.09.2026", "508016,00", "ORACLE GLOBAL SERVIC", "Prichozi uhrada", "", ""),
                record("22.09.2026", "-299,77", "", "Sporici operace", "", "Fond - castka vracena"));

        Assert.assertEquals(3, transactions.size());
    }

    @Test
    public void whatWasSpentIsBookedFromTheMappings() throws Exception {
        Service.CONFIG.addMapping("PRAZSKA ENERGETIKA", "520.5", true);

        StatementTransactionModel transaction = parse(record("20.09.2026", "-2090,00", "PRAZSKA ENERGETIKA", "Trvaly prikaz", "elektrika", "")).get(0);

        Assert.assertEquals("520.5", transaction.getDebit());
        Assert.assertEquals(ACCOUNT, transaction.getCredit());
    }

    /** Who it was with and what it was for, from whichever columns carry them. */
    @Test
    public void theDescriptionSaysWhoAndWhat() throws Exception {
        List<StatementTransactionModel> transactions = parse(
                record("20.09.2026", "-2090,00", "PRAZSKA ENERGETIKA", "Trvaly prikaz", "elektrika", ""),
                record("22.09.2026", "-299,77", "", "Sporici operace", "", "Fond - castka vracena"),
                record("14.09.2026", "508016,00", "ORACLE GLOBAL SERVIC", "Prichozi uhrada", "", ""));

        Assert.assertEquals("ORACLE GLOBAL SERVIC", transactions.get(0).getDescription());
        Assert.assertEquals("Sporici operace - Fond - castka vracena", transactions.get(1).getDescription());
        Assert.assertEquals("PRAZSKA ENERGETIKA - elektrika", transactions.get(2).getDescription());
    }

    /** Money received is booked from the credit mappings: an income is not an expense account. */
    @Test
    public void whatArrivedIsBookedFromTheCreditMappings() throws Exception {
        Service.CONFIG.addMapping("AN EMPLOYER", "600.1", false);

        StatementTransactionModel transaction = parse(record("14.09.2026", "508016,00", "AN EMPLOYER", "Prichozi uhrada", "", "")).get(0);

        Assert.assertEquals(ACCOUNT, transaction.getDebit());
        Assert.assertEquals("600.1", transaction.getCredit());
    }

    /** The sides are not crossed: a debit mapping says nothing about money coming in. */
    @Test
    public void aDebitMappingDoesNotBookAnIncome() throws Exception {
        Service.CONFIG.addMapping("AN EMPLOYER", "510.0", true);

        StatementTransactionModel transaction = parse(record("14.09.2026", "508016,00", "AN EMPLOYER", "Prichozi uhrada", "", "")).get(0);

        Assert.assertNull(transaction.getCredit());
    }

    /**
     * A transfer between one's own accounts says only a name - the same name on both sides - so the
     * account on the other side is what tells one from another. Those movements are the ones that
     * carry neither a variable symbol nor a message.
     */
    @Test
    public void aMovementThatIdentifiesItselfWithNothingElseGetsTheOtherAccount() throws Exception {
        String transfer = record("18.09.2026", "-400000,00", "A NAME", "Odchozi uhrada", "", "", "296465607", "0300", "");

        Assert.assertEquals("A NAME - 296465607/0300", parse(transfer).get(0).getDescription());
    }

    @Test
    public void aPaymentThatSaysWhatItIsForDoesNot() throws Exception {
        String withVariableSymbol = record("14.09.2026", "508016,00", "AN EMPLOYER", "Prichozi uhrada", "", "", "64450", "6300", "0000000009");
        String withMessage = record("13.09.2026", "-1551,00", "A TELCO", "Inkaso", "", "zprava o platbe", "217077033", "0300", "");

        Assert.assertEquals("AN EMPLOYER", parse(withVariableSymbol).get(0).getDescription());
        Assert.assertEquals("A TELCO - zprava o platbe", parse(withMessage).get(0).getDescription());
    }

    /** What a standing order is for is still said, and the account is added to it. */
    @Test
    public void whatIsKnownIsKeptAndTheAccountIsAddedToIt() throws Exception {
        String standingOrder = record("18.09.2026", "-21660,00", "A NAME", "Trvaly prikaz", "prevod", "", "1373888019", "3030", "");

        Assert.assertEquals("A NAME - prevod - 1373888019/3030", parse(standingOrder).get(0).getDescription());
    }

    @Test
    public void aMovementWithNoOtherAccountIsLeftAsItIs() throws Exception {
        String internal = record("22.09.2026", "-299,77", "", "Sporici operace", "", "", "", "", "");

        Assert.assertEquals("Sporici operace", parse(internal).get(0).getDescription());
    }

    /** The bank has moved its columns once already, so they are found by their headings. */
    @Test
    public void theColumnsAreFoundByTheirHeadings() throws Exception {
        // the same statement with one column inserted before the counterparty
        String shiftedHeader = HEADER.replace(";jm\u00E9no protistrany;", ";n\u011Bco nov\u00E9ho;jm\u00E9no protistrany;");
        String shiftedRecord = "222260349/0300;20.09.2026;-2090,00;CZK;1000,00;123;0300;NOVY SLOUPEC;PRAZSKA ENERGETIKA;;;;;;;;;";

        StringBuilder csv = new StringBuilder("Pohyby\r\n\r\n").append(shiftedHeader).append("\r\n").append(shiftedRecord).append("\r\n");
        File file = Files.createTempFile("statement", ".csv").toFile();
        file.deleteOnExit();
        Files.write(file.toPath(), csv.toString().getBytes(StandardCharsets.UTF_8));

        StatementParserManager manager = new StatementParserManager(file, StatementParserManager.ACCOUNT_STATEMENT_CSV, ACCOUNT);
        manager.loadContent();

        // it carries no variable symbol, so the other account is added to it as well
        Assert.assertEquals("PRAZSKA ENERGETIKA - 123/0300", manager.getTransactions().get(0).getDescription());
    }

    @Test
    public void whatIsNotAMovementIsNotRead() throws Exception {
        Assert.assertTrue(parse().isEmpty());
    }
}
