package org.kaleta.entity.xml;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class Schema {
    private String year;

    @JacksonXmlElementWrapper(useWrapping = false)
    @JsonProperty("class")
    private List<Schema.Clazz> clazz = new ArrayList<>();

    @Data
    public static class Clazz {
        private String id;
        private String name;

        @JacksonXmlElementWrapper(useWrapping = false)
        private List<Schema.Clazz.Group> group = new ArrayList<>();

        @Data
        public static class Group {
            private String id;
            private String name;

            @JacksonXmlElementWrapper(useWrapping = false)
            private List<Schema.Clazz.Group.Account> account = new ArrayList<>();

            @Data
            public static class Account {
                private String id;
                private String name;
                private String type;
            }
        }
    }
}
