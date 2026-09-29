package org.kaleta.accountant.backend.model;

import jakarta.xml.bind.annotation.*;
import java.util.ArrayList;
import java.util.List;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "", propOrder = {
        "years",
        "imports",
        "mapping"
})
@XmlRootElement(name = "config")
public class ConfigModel {

    @XmlElement(required = true)
    protected ConfigModel.Years years;
    protected ConfigModel.Imports imports;
    @XmlElement(required = true)
    protected ConfigModel.Mapping mapping;

    public ConfigModel(){}

    public ConfigModel(ConfigModel configModel){
        this.setYears(new ConfigModel.Years(configModel.getYears()));
        this.setMapping(new ConfigModel.Mapping(configModel.getMapping()));
        this.setImports(new ConfigModel.Imports(configModel.getImports()));
    }

    public ConfigModel.Years getYears() {
        return years;
    }

    public void setYears(ConfigModel.Years value) {
        this.years = value;
    }

    /** Never null: a file written before the imports were configurable simply has none. */
    public ConfigModel.Imports getImports() {
        if (imports == null) {
            imports = new ConfigModel.Imports();
        }
        return imports;
    }

    public void setImports(ConfigModel.Imports value) {
        this.imports = value;
    }

    public ConfigModel.Mapping getMapping() {
        return mapping;
    }

    public void setMapping(ConfigModel.Mapping value) {
        this.mapping = value;
    }

    @XmlAccessorType(XmlAccessType.FIELD)
    @XmlType(name = "", propOrder = {
        "year"
    })
    public static class Years {

        List<ConfigModel.Years.Year> year;
        @XmlAttribute(name = "active", required = true)
        String active = "";

        public Years(){}

        Years(Years years){
            this.setActive(years.getActive());
            for (ConfigModel.Years.Year year : years.getYearList()){
                this.getYearList().add(new ConfigModel.Years.Year(year));
            }
        }

        public List<ConfigModel.Years.Year> getYearList() {
            if (year == null) {
                year = new ArrayList<>();
            }
            return this.year;
        }

        public String getActive() {
            return active;
        }

        public void setActive(String value) {
            this.active = value;
        }


        @XmlAccessorType(XmlAccessType.FIELD)
        @XmlType(name = "")
        public static class Year {

            @XmlAttribute(name = "name", required = true)
            String name;

            public Year(){}

            Year(Year year){
                this.setName(year.getName());
            }

            public String getName() {
                return name;
            }

            public void setName(String value) {
                this.name = value;
            }

        }

    }

    /**
     * The statements this app knows how to read: what each one is called, which format it is in, and
     * the account it is about. All three belong to whoever keeps the books, not to the app - which
     * bank a statement comes from is theirs to say, and is not written into the source.
     */
    @XmlAccessorType(XmlAccessType.FIELD)
    @XmlType(name = "", propOrder = {
            "source"
    })
    public static class Imports {

        protected List<ConfigModel.Imports.Source> source;

        public Imports(){}

        Imports(Imports imports){
            for (ConfigModel.Imports.Source source : imports.getSource()){
                this.getSource().add(new ConfigModel.Imports.Source(source));
            }
        }

        public List<ConfigModel.Imports.Source> getSource() {
            if (source == null) {
                source = new ArrayList<>();
            }
            return this.source;
        }

        @XmlAccessorType(XmlAccessType.FIELD)
        @XmlType(name = "")
        public static class Source {

            @XmlAttribute(name = "name", required = true)
            protected String name;
            @XmlAttribute(name = "format", required = true)
            protected String format;
            @XmlAttribute(name = "account", required = true)
            protected String account;

            public Source(){}

            Source(Imports.Source source){
                this.setName(source.getName());
                this.setFormat(source.getFormat());
                this.setAccount(source.getAccount());
            }

            /** What the statement is called in the import dialog. */
            public String getName() {
                return name;
            }

            public void setName(String value) {
                this.name = value;
            }

            /** Which parser reads it, as one of the formats the app implements. */
            public String getFormat() {
                return format;
            }

            public void setFormat(String value) {
                this.format = value;
            }

            /** The account the statement is about, which is on one side of everything on it. */
            public String getAccount() {
                return account;
            }

            public void setAccount(String value) {
                this.account = value;
            }
        }
    }

    @XmlAccessorType(XmlAccessType.FIELD)
    @XmlType(name = "", propOrder = {
            "debit",
            "credit"
    })
    public static class Mapping {

        protected List<ConfigModel.Mapping.Debit> debit;
        protected List<ConfigModel.Mapping.Credit> credit;

        public Mapping(){}

        Mapping(Mapping mapping){
            for (ConfigModel.Mapping.Debit debit : mapping.getDebit()){
                this.getDebit().add(new ConfigModel.Mapping.Debit(debit));
            }
            for (ConfigModel.Mapping.Credit credit : mapping.getCredit()){
                this.getCredit().add(new ConfigModel.Mapping.Credit(credit));
            }
        }

        public List<ConfigModel.Mapping.Debit> getDebit() {
            if (debit == null) {
                debit = new ArrayList<>();
            }
            return this.debit;
        }

        public List<ConfigModel.Mapping.Credit> getCredit() {
            if (credit == null) {
                credit = new ArrayList<>();
            }
            return this.credit;
        }

        /**
         * What a mapping says, whichever side of a transaction it names: money spent is booked
         * against a debit account, money received against a credit one, and the rest is the same.
         */
        public interface Entry {
            String getSubstring();

            void setSubstring(String value);

            String getAccount();

            void setAccount(String value);
        }

        @XmlAccessorType(XmlAccessType.FIELD)
        @XmlType(name = "")
        public static class Debit implements Entry {

            @XmlAttribute(name = "substring", required = true)
            protected String substring;
            @XmlAttribute(name = "account", required = true)
            protected String account;

            public Debit(){}

            Debit(Mapping.Debit debit){
                this.setSubstring(debit.getSubstring());
                this.setAccount(debit.getAccount());
            }

            public String getSubstring() {
                return substring;
            }

            public void setSubstring(String value) {
                this.substring = value;
            }

            public String getAccount() {
                return account;
            }

            public void setAccount(String value) {
                this.account = value;
            }

        }

        @XmlAccessorType(XmlAccessType.FIELD)
        @XmlType(name = "")
        public static class Credit implements Entry {

            @XmlAttribute(name = "substring", required = true)
            protected String substring;
            @XmlAttribute(name = "account", required = true)
            protected String account;

            public Credit(){}

            Credit(Mapping.Credit credit){
                this.setSubstring(credit.getSubstring());
                this.setAccount(credit.getAccount());
            }

            public String getSubstring() {
                return substring;
            }

            public void setSubstring(String value) {
                this.substring = value;
            }

            public String getAccount() {
                return account;
            }

            public void setAccount(String value) {
                this.account = value;
            }

        }

    }

}
