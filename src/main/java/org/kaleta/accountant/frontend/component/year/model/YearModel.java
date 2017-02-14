package org.kaleta.accountant.frontend.component.year.model;

/**
 * Created by Stanislav Kaleta on 14.02.2017.
 */
public class YearModel {
    private int yearId;
    private boolean isActive;
    private SchemaModel schemaModel;
    private AccountModel accountModel;

    public int getYearId() {
        return yearId;
    }

    public void setYearId(int yearId) {
        this.yearId = yearId;
    }

    public boolean isActive() {
        return isActive;
    }

    public void setActive(boolean active) {
        isActive = active;
    }

    public SchemaModel getSchemaModel() {
        return schemaModel;
    }

    public void setSchemaModel(SchemaModel schemaModel) {
        this.schemaModel = schemaModel;
    }

    public AccountModel getAccountModel() {
        return accountModel;
    }

    public void setAccountModel(AccountModel accountModel) {
        this.accountModel = accountModel;
    }
}
