package org.kaleta.entity.xml;

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class Accounts {

    private String year;

    @JacksonXmlElementWrapper(useWrapping = false)
    private List<Accounts.Account> account = new ArrayList<>();

    @Data
    public static class Account {
        private String schemaId;
        private String semanticId;
        private String name;
        private String metadata;
    }
}
