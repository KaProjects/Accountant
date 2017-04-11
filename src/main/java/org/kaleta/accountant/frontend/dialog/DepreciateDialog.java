package org.kaleta.accountant.frontend.dialog;

import org.kaleta.accountant.frontend.component.year.model.AccountModel;

import java.awt.Component;
import java.util.List;

/**
 * Created by Stanislav Kaleta on 11.04.2017.
 */
public class DepreciateDialog extends Dialog {
    private List<Config> configs;

    public DepreciateDialog(Component parent, List<Config> configs) {
        super(parent, "Depreciate Asset(s) Dialog");
        this.configs = configs;
    }

    @Override
    protected void buildDialog() {

    }

    public static class Config{
        private AccountModel.Account account;
        private AccountModel.Account depAccount;
        private String dateHint;
        private String valueHint;

        public Config(AccountModel.Account account, AccountModel.Account depAccount, String dateHint, String valueHint) {
            this.account = account;
            this.depAccount = depAccount;
            this.dateHint = dateHint;
            this.valueHint = valueHint;
        }

        public AccountModel.Account getAccount() {
            return account;
        }

        public void setAccount(AccountModel.Account account) {
            this.account = account;
        }

        public AccountModel.Account getDepAccount() {
            return depAccount;
        }

        public void setDepAccount(AccountModel.Account depAccount) {
            this.depAccount = depAccount;
        }

        public String getDateHint() {
            return dateHint;
        }

        public void setDateHint(String dateHint) {
            this.dateHint = dateHint;
        }

        public String getValueHint() {
            return valueHint;
        }

        public void setValueHint(String valueHint) {
            this.valueHint = valueHint;
        }
    }

}
