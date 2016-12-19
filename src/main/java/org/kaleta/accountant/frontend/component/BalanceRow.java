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
    private String turnover;
    private String value;
    private String schemaId;
    private String type;

    public BalanceRow(String name, String turnover, String value, String type){
        this.name = name;
        this.turnover = turnover;
        this.value = value;
        this.type = type;
    }

    public BalanceRow(String name, String turnover, String value, String schemaId, String type){
        this.name = name;
        this.turnover = turnover;
        this.value = value;
        this.schemaId = schemaId;
        this.type = type;
    }

    public BalanceRow(){

    }

    public String getTurnover() {
        return turnover;
    }

    public void setTurnover(String turnover) {
        this.turnover = turnover;
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

    public String getSchemaId() {
        return schemaId;
    }

    public void setSchemaId(String schemaId) {
        this.schemaId = schemaId;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }
}
