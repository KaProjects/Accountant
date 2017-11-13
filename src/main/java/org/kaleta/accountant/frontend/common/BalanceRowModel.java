package org.kaleta.accountant.frontend.common;

public class BalanceRowModel {
    public static final String SUM = "SUM";
    public static final String CLASS = "CLASS";
    public static final String GROUP = "GROUP";
    public static final String ACCOUNT = "ACC";

    private final String name;
    private String turnover;
    private String value;
    private String schemaId;
    private final String type;

    public BalanceRowModel(String name, String turnover, String value, String type){
        this.name = name;
        this.turnover = turnover;
        this.value = value;
        this.type = type;
    }

    public BalanceRowModel(String name, String turnover, String value, String schemaId, String type){
        this.name = name;
        this.turnover = turnover;
        this.value = value;
        this.schemaId = schemaId;
        this.type = type;
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

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public String getSchemaId() {
        return schemaId;
    }

    public String getType() {
        return type;
    }

}
