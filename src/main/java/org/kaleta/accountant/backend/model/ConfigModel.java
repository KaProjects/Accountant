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
    protected ConfigModel.Years years = new Years();

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

        protected List<ConfigModel.Years.Year> year;
        @XmlAttribute(name = "active", required = true)
        protected String active = "";

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
            protected String name;

            public String getName() {
                return name;
            }

            public void setName(String value) {
                this.name = value;
            }

        }

    }

}
