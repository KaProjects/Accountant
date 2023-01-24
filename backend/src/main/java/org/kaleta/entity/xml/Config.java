package org.kaleta.entity.xml;

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class Config
{
    private Years years;
    private Mapping mapping;

    @Data
    public static class Years
    {
        private String active;

        @JacksonXmlElementWrapper(useWrapping = false)
        private List<Config.Years.Year> year = new ArrayList<>();

        @Data
        public static class Year
        {
            private String name;
        }
    }

    @Data
    private static class Mapping
    {
        @JacksonXmlElementWrapper(useWrapping = false)
        private List<Config.Mapping.Debit> debit = new ArrayList<>();

        @Data
        private static class Debit
        {
            private String substring;
            private String account;
        }
    }
}
