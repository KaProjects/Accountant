package org.kaleta.accountant.backend.manager;

import org.apache.tika.exception.TikaException;
import org.apache.tika.metadata.Metadata;
import org.apache.tika.parser.ParseContext;
import org.apache.tika.parser.pdf.PDFParser;
import org.apache.tika.sax.BodyContentHandler;
import org.kaleta.accountant.backend.model.ConfigModel;
import org.kaleta.accountant.backend.model.PdfTransactionModel;
import org.kaleta.accountant.service.Service;
import org.xml.sax.SAXException;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.text.DateFormatSymbols;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class PdfParserManager {

    public static final String CSOB_CREDIT_PARSER_10_2022 = "CSOB - Kreditka 10/2022";
    public static final String REVOLUT_PARSER_12_2022 = "Revolut 12/2022";

    public static String[] getDataTypeOptions(){
        return new String[]{CSOB_CREDIT_PARSER_10_2022, REVOLUT_PARSER_12_2022};
    }

    private final File file;
    private final String dataType;
    private String content;

    public PdfParserManager(File file, String dataType)  {
        this.file = file;
        this.dataType = dataType;
    }

    public String loadContent() throws IOException, TikaException, SAXException{
        BodyContentHandler contentHandler = new BodyContentHandler();
        FileInputStream fis = new FileInputStream(file);
        Metadata metadata = new Metadata();
        ParseContext context = new ParseContext();
        PDFParser pdfparser = new PDFParser();

        pdfparser.parse(fis, contentHandler, metadata, context);

        content = contentHandler.toString();
        return content;
    }

    public String getContent(){
        return content;
    }

    public List<PdfTransactionModel> getTransactions()  {
        if (content == null) throw new NullPointerException("first load PDF file content via #loadContent");

        switch (dataType){
            case CSOB_CREDIT_PARSER_10_2022: return use_CSOB_CREDIT_PARSER_10_2022();
            case REVOLUT_PARSER_12_2022: return use_REVOLUT_PARSER_12_2022();

            default: throw new IllegalArgumentException("unknown dataType");
        }
    }

    private List<PdfTransactionModel> use_CSOB_CREDIT_PARSER_10_2022(){
        List<PdfTransactionModel> transactions = new ArrayList<>();

        String[] records = content.split("Částka: ");
        for (int i=1;i<records.length;i++){
            String record = records[i];

            PdfTransactionModel transaction = new PdfTransactionModel();
            transaction.setDescription("");

            String firstLine = record.split("\n")[0];

            String[] dateSplits = firstLine.split(" ")[2].split("\\.");
            transaction.setDate(dateSplits[0] + dateSplits[1]);

            int amountLineIndex = 3;

            if (!firstLine.split(" ")[1].equals("CZK")) {
                transaction.setDescription(firstLine.split(" ")[0] + firstLine.split(" ")[1] + " ");
                amountLineIndex = 4;
            }

            if (firstLine.contains("Místo: ")){
                transaction.setDescription(transaction.getDescription() + firstLine.split("Místo: ")[1]);
            } else {
                amountLineIndex = 2;
            }

            String sAmount = record.split("\n")[amountLineIndex]
                    .replace("-","")
                    .replace(" ", "")
                    .replace(",", ".");

            transaction.setAmount(String.valueOf(Math.round(Double.parseDouble(sAmount))));

            transaction.setCredit("222.0");

            for (ConfigModel.Mapping.Debit mapping : Service.CONFIG.getDebitMappings()){
                if (transaction.getDescription().contains(mapping.getSubstring())){
                    transaction.setDebit(mapping.getAccount());
                }
            }

            transactions.add(transaction);
        }
        System.out.println("transactions loaded: " + transactions.size());
        Collections.reverse(transactions);
        return transactions;
    }

    private List<PdfTransactionModel> use_REVOLUT_PARSER_12_2022(){
        List<PdfTransactionModel> transactions = new ArrayList<>();
        System.out.println(content);

        String[] records = content.split("\n\n");
        for (int i=1;i<records.length;i++){
            String[] lines = records[i].split("\n");

            PdfTransactionModel transaction = null;

            if (lines.length == 3 && lines[1].startsWith("Reference: ") && (lines[2].startsWith("From: ") || lines[2].startsWith("To: "))){
                transaction = new PdfTransactionModel();

                transaction.setDescription(lines[1].replace("Reference: ", ""));

                if (lines[2].startsWith("From: ")){
                    transaction.setDebit("210.3");
                } else {
                    transaction.setCredit("210.3");
                }
            }

            if (lines.length == 3 && lines[1].startsWith("To: ") && lines[2].startsWith("Card: ")){
                transaction = new PdfTransactionModel();

                transaction.setDescription(lines[1].replace("To: ", ""));

                transaction.setCredit("210.3");

                for (ConfigModel.Mapping.Debit mapping : Service.CONFIG.getDebitMappings()){
                    if (transaction.getDescription().contains(mapping.getSubstring())){
                        transaction.setDebit(mapping.getAccount());
                    }
                }
            }

            if (transaction != null){
                String[] firstLineSplit = lines[0].split(" ");

                String date = null;
                String[] shortMonths = new DateFormatSymbols().getShortMonths();
                for (int mi=0;mi<shortMonths.length;mi++) {
                    if (shortMonths[mi].equals(firstLineSplit[1])){
                        date = String.format("%02d", Integer.parseInt(firstLineSplit[0])) + String.format("%02d", mi + 1);
                        break;
                    }
                    if (shortMonths[mi].equals(firstLineSplit[0])){
                        date = String.format("%02d", Integer.parseInt(firstLineSplit[1].replace(",",""))) + String.format("%02d", mi + 1);
                        break;
                    }
                }
                transaction.setDate(date);

                String amount;
                String amountRecord;
                if (firstLineSplit[firstLineSplit.length - 1].contains("€")){
                    amountRecord = firstLineSplit[firstLineSplit.length - 2];
                    Double amountCzk = Double.parseDouble(amountRecord.replace("€","")) * 25;
                    amount = String.valueOf(amountCzk.intValue());
                } else if (firstLineSplit[firstLineSplit.length - 1].contains("$")){
                    amountRecord = firstLineSplit[firstLineSplit.length - 2];
                    Double amountCzk = Double.parseDouble(amountRecord.replace("$","")) * 25;
                    amount = String.valueOf(amountCzk.intValue());
                } else if (firstLineSplit[firstLineSplit.length - 1].contains("CZK")){
                    amountRecord = firstLineSplit[firstLineSplit.length - 4];
                    amount = String.valueOf(Integer.parseInt(amountRecord.replace(",","").split("\\.")[0]));
                } else {
                    amount = "";
                    amountRecord = "UNKNOWN CURRENCY " + lines[0];
                }
                transaction.setAmount(amount);
                transaction.setDescription(amountRecord + " " + transaction.getDescription());

                if (!amount.isEmpty() && Integer.parseInt(amount) == 0){
                    continue;
                }

                transactions.add(transaction);
            }
        }

        System.out.println("transactions loaded: " + transactions.size());
        return transactions;
    }

}