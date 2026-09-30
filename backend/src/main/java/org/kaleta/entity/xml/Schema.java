package org.kaleta.entity.xml;

import io.quarkus.runtime.annotations.RegisterForReflection;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

/**
 * The desktop app keeps a single schema for all years, so this file carries no
 * year of its own; the year is supplied by the caller when syncing.
 */
@Data
@RegisterForReflection
public class Schema
{
    @JacksonXmlElementWrapper(useWrapping = false)
    @JsonProperty("class")
    private List<Schema.Clazz> clazz = new ArrayList<>();

    @Data
    @RegisterForReflection
    public static class Clazz
    {
        private String id;
        private String name;

        @JacksonXmlElementWrapper(useWrapping = false)
        private List<Schema.Clazz.Group> group = new ArrayList<>();

        @Data
        @RegisterForReflection
        public static class Group
        {
            private String id;
            private String name;

            @JacksonXmlElementWrapper(useWrapping = false)
            private List<Schema.Clazz.Group.Account> account = new ArrayList<>();

            @Data
            @RegisterForReflection
            public static class Account
            {
                private String id;
                private String name;
                private String type;
            }
        }
    }
}
