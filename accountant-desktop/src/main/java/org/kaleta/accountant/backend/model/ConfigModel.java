package org.kaleta.accountant.backend.model;

import javax.xml.bind.annotation.*;
import java.util.ArrayList;
import java.util.List;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "", propOrder = {
        "years",
        "mapping"
})
@XmlRootElement(name = "config")
public class ConfigModel {

    @XmlElement(required = true)
    protected ConfigModel.Years years;
    @XmlElement(required = true)
    protected ConfigModel.Mapping mapping;

    public ConfigModel(){}

    public ConfigModel(ConfigModel configModel){
        this.setYears(new ConfigModel.Years(configModel.getYears()));
        this.setMapping(new ConfigModel.Mapping(configModel.getMapping()));
    }

    public ConfigModel.Years getYears() {
        return years;
    }

    public void setYears(ConfigModel.Years value) {
        this.years = value;
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

    @XmlAccessorType(XmlAccessType.FIELD)
    @XmlType(name = "", propOrder = {
            "debit"
    })
    public static class Mapping {

        protected List<ConfigModel.Mapping.Debit> debit;

        public Mapping(){}

        Mapping(Mapping mapping){
            for (ConfigModel.Mapping.Debit debit : mapping.getDebit()){
                this.getDebit().add(new ConfigModel.Mapping.Debit(debit));
            }
        }

        public List<ConfigModel.Mapping.Debit> getDebit() {
            if (debit == null) {
                debit = new ArrayList<>();
            }
            return this.debit;
        }

        @XmlAccessorType(XmlAccessType.FIELD)
        @XmlType(name = "")
        public static class Debit {

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

    }

}
