package org.kaleta.accountant.frontend.component;

/**
 * Created by Stanislav Kaleta on 10.08.2016.
 */
public class BalanceRow {
    public static final String SUM = "SUM";
    public static final String CLASS = "CLASS";
    public static final String GROUP = "GROUP";
    public static final String ACCOUNT = "ACC";

    private String name;
    private String value;
    private String type;

    public BalanceRow(String name, String value, String type){
        this.name = name;
        this.value = value;
        this.type = type;
    }

    public BalanceRow(){

    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }
}
