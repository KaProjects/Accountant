package org.kaleta.entity.xml;

import io.quarkus.runtime.annotations.RegisterForReflection;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

/**
 * Only the years are read here; the desktop app owns this file and keeps adding
 * sections to it (import sources, credit mappings, ...), so unknown elements are
 * ignored rather than failing the sync.
 */
@Data
@RegisterForReflection
@JsonIgnoreProperties(ignoreUnknown = true)
public class Config
{
    private Years years;

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
}
