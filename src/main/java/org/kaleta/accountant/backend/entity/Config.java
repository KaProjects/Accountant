//
// This file was generated by the JavaTM Architecture for XML Binding(JAXB) Reference Implementation, v2.2.8-b130911.1802 
// See <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a> 
// Any modifications to this file will be lost upon recompilation of the source schema. 
// Generated on: 2016.12.19 at 01:41:01 AM CET 
//


package org.kaleta.accountant.backend.entity;

import javax.xml.bind.annotation.*;
import java.util.ArrayList;
import java.util.List;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "", propOrder = {
    "years"
})
@XmlRootElement(name = "config")
@Deprecated
public class Config {

    @XmlElement(required = true)
    private Config.Years years = new Years();

    /**
     * Gets the value of the years property.
     * 
     * @return
     *     possible object is
     *     {@link Config.Years }
     *     
     */
    public Config.Years getYears() {
        return years;
    }

    /**
     * Sets the value of the years property.
     * 
     * @param value
     *     allowed object is
     *     {@link Config.Years }
     *     
     */
    public void setYears(Config.Years value) {
        this.years = value;
    }


    @XmlAccessorType(XmlAccessType.FIELD)
    @XmlType(name = "", propOrder = {
        "year"
    })
    public static class Years {

        List<Config.Years.Year> year;
        @XmlAttribute(name = "active", required = true)
        String active = "";

        public List<Config.Years.Year> getYear() {
            if (year == null) {
                year = new ArrayList<>();
            }
            return this.year;
        }

        /**
         * Gets the value of the active property.
         * 
         * @return
         *     possible object is
         *     {@link String }
         *     
         */
        public String getActive() {
            return active;
        }

        /**
         * Sets the value of the active property.
         * 
         * @param value
         *     allowed object is
         *     {@link String }
         *     
         */
        public void setActive(String value) {
            this.active = value;
        }


        @XmlAccessorType(XmlAccessType.FIELD)
        @XmlType(name = "")
        static class Year {

            @XmlAttribute(name = "name", required = true)
            String name;

            /**
             * Gets the value of the name property.
             * 
             * @return
             *     possible object is
             *     {@link String }
             *     
             */
            public String getName() {
                return name;
            }

            /**
             * Sets the value of the name property.
             * 
             * @param value
             *     allowed object is
             *     {@link String }
             *     
             */
            public void setName(String value) {
                this.name = value;
            }

        }

    }

}
