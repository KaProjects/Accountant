package org.kaleta.entity.xml;

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class Transactions {

    private String year;

    @JacksonXmlElementWrapper(useWrapping = false)
    private List<Transaction> transaction = new ArrayList<>();


    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("Transactions{year='" + year + "', transaction=[\n");
        for (Transaction tr : transaction){
            sb.append("    {" +
                    "id='" + tr.id + '\'' +
                    ", date='" + tr.date + '\'' +
                    ", description='" + tr.description + '\'' +
                    ", amount='" + tr.amount + '\'' +
                    ", debit='" + tr.debit + '\'' +
                    ", credit='" + tr.credit + '\'' +
                    "}\n");
        }
        sb.append("]}");
        return sb.toString();
    }

    @Data
    public static class Transaction {
        private String id;
        private String date;
        private String description;
        private String amount;
        private String debit;
        private String credit;
    }
}

