package org.kaleta.entity.json;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class FinAssetsConfig {
    private List<Group> groups = new ArrayList<>();

    @Data
    public static class Group {
        private String name;

        private List<Account> accounts = new ArrayList<>();

        @Data
        public static class Account {
            private String name;

            private List<Record> records = new ArrayList<>();

            @Data
            public static class Record {
                private String year;
                private String id;
            }
        }
    }
}
