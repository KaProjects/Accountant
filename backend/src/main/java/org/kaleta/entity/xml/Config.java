package org.kaleta.entity.xml;

import io.quarkus.runtime.annotations.RegisterForReflection;

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
@RegisterForReflection
public class Config
{
    private Years years;
    private Mapping mapping;

    @Data
    @RegisterForReflection
    public static class Years
    {
        private String active;

        @JacksonXmlElementWrapper(useWrapping = false)
        private List<Config.Years.Year> year = new ArrayList<>();

        @Data
        @RegisterForReflection
        public static class Year
        {
            private String name;
        }
    }

    @Data
    @RegisterForReflection
    private static class Mapping
    {
        @JacksonXmlElementWrapper(useWrapping = false)
        private List<Config.Mapping.Debit> debit = new ArrayList<>();

        @Data
        @RegisterForReflection
        private static class Debit
        {
            private String substring;
            private String account;
        }
    }
}
