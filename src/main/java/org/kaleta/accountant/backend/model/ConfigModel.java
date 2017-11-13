package org.kaleta.accountant.backend.model;

import javax.xml.bind.annotation.*;
import java.util.ArrayList;
import java.util.List;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "", propOrder = {
    "years"
})
@XmlRootElement(name = "config")
public class ConfigModel {

    @XmlElement(required = true)
    private ConfigModel.Years years = new Years();

    public ConfigModel(){}

    public ConfigModel(ConfigModel configModel){
        this.setYears(new ConfigModel.Years(configModel.getYears()));
    }

    public ConfigModel.Years getYears() {
        return years;
    }

    public void setYears(ConfigModel.Years value) {
        this.years = value;
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

}
