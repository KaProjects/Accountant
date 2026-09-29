package org.kaleta.accountant.backend.manager;

import org.junit.Assert;
import org.junit.Test;
import org.kaleta.accountant.backend.model.PdfTransactionModel;
import org.kaleta.accountant.core.TestParent;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.List;

/**
 * Reading a credit card statement exported as CSV.
 * <p>
 * The export is the bank's, and the bank moves its columns: what identifies a card payment is the
 * note it carries, not the position that note was in the year the parser was written.
 */
public class CsobCreditCsvParserTest extends TestParent {

    private static final String HEADER = "číslo účtu;datum zaúčtování;částka;měna;"
            + "zůstatek;číslo protiúčtu;kód banky;jméno protistrany;adresa;konstantní symbol;"
            + "variabilní symbol;specifický symbol;označení operace;název trvalého příkazu;"
            + "vlastní poznámka;zpráva;kategorie";

    private List<PdfTransactionModel> parse(String... records) throws Exception {
        StringBuilder csv = new StringBuilder("Pohyby na uctu 111111111/0300 dne 01.01.2026\r\n\r\n").append(HEADER).append("\r\n");
        for (String record : records) {
            csv.append(record).append("\r\n");
        }
        File file = file(csv.toString());
        PdfParserManager manager = new PdfParserManager(file, PdfParserManager.CSOB_CREDIT_CSV_PARSER_07_2023);
        manager.loadContent();
        return manager.getTransactions();
    }

    private static File file(String content) throws IOException {
        File file = Files.createTempFile("statement", ".csv").toFile();
        file.deleteOnExit();
        Files.write(file.toPath(), content.getBytes(StandardCharsets.UTF_8));
        return file;
    }

    /** The note used to sit in column 14, then the bank added one and it sat in 15. */
    @Test
    public void thePaymentIsReadWhereverTheBankPutsItsNote() throws Exception {
        String inColumn14 = "111111111/0300;24.09.2026;-2202,00;CZK;-11287,83;;;;;6178;2050;2911;Cerpani uveru;;"
                + "Částka: 2202 CZK 23.09.2026, Místo: A Shop, Prague;;Nakupy";
        String inColumn15 = "111111111/0300;23.09.2026;-147,00;CZK;-9085,83;;;;;6178;2050;2911;Cerpani uveru;;;"
                + "Částka: 147 CZK 22.09.2026, Místo: B Shop, Praha;Potraviny";

        List<PdfTransactionModel> transactions = parse(inColumn14, inColumn15);

        Assert.assertEquals(2, transactions.size());
        Assert.assertEquals("B Shop, Praha", transactions.get(0).getDescription());
        Assert.assertEquals("147", transactions.get(0).getAmount());
        Assert.assertEquals("2309", transactions.get(0).getDate());
        Assert.assertEquals("A Shop, Prague", transactions.get(1).getDescription());
        Assert.assertEquals("2202", transactions.get(1).getAmount());
    }

    /** Only what was spent on the card: the statement also carries its repayments and its fees. */
    @Test
    public void onlyOutgoingCardPaymentsAreRead() throws Exception {
        String repayment = "111111111/0300;20.09.2026;10000,00;CZK;0,00;;;;;;;;Splatka;;;;";
        String feeWithoutANote = "111111111/0300;21.09.2026;-49,00;CZK;-49,00;;;;;;;;Poplatek;;;;";
        String payment = "111111111/0300;22.09.2026;-99,00;CZK;-148,00;;;;;6178;2050;2911;Cerpani uveru;;;"
                + "Částka: 99 CZK 21.09.2026, Místo: C Shop, Brno;Potraviny";

        List<PdfTransactionModel> transactions = parse(repayment, feeWithoutANote, payment);

        Assert.assertEquals(1, transactions.size());
        Assert.assertEquals("C Shop, Brno", transactions.get(0).getDescription());
    }

    /** The credit side is the card itself, and the debit is filled in from the mappings. */
    @Test
    public void theCardIsCreditedAndAKnownDescriptionIsBooked() throws Exception {
        org.kaleta.accountant.service.Service.CONFIG.addDebitMapping("C Shop", "510.0");
        String payment = "111111111/0300;22.09.2026;-99,00;CZK;-148,00;;;;;6178;2050;2911;Cerpani uveru;;;"
                + "Částka: 99 CZK 21.09.2026, Místo: C Shop, Brno;Potraviny";

        PdfTransactionModel transaction = parse(payment).get(0);

        Assert.assertEquals("222.0", transaction.getCredit());
        Assert.assertEquals("510.0", transaction.getDebit());
    }

    /** The title line, the blank line and the column names are not transactions. */
    @Test
    public void whatIsNotAMovementIsNotRead() throws Exception {
        Assert.assertTrue(parse().isEmpty());
    }
}
