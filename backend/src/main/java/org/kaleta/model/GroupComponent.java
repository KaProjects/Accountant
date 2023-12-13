package org.kaleta.model;

import lombok.Data;
import org.kaleta.Utils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;


@Data
public class GroupComponent
{
    private String name;
    private String schemaId;
    private List<AccountComponent> accounts = new ArrayList<>();

    public Integer getInitialValue(){
        Integer initialValue = 0;
        for (AccountComponent account : accounts){
            initialValue += account.getInitialValue();
        }
        return initialValue;
    }

    public Integer[] getMonthlyBalance(){
        Integer[] monthlyBalance = new Integer[12];
        Arrays.fill(monthlyBalance, 0);
        for (AccountComponent account : accounts){
            monthlyBalance = Utils.addIntegerArrays(monthlyBalance, account.getMonthlyBalance());
        }
        return monthlyBalance;
    }

    public Integer getBalance(){
        Integer balance = 0;
        for (AccountComponent account : accounts){
            balance += account.getBalance();
        }
        return balance;
    }

    @Data
    public static class AccountComponent {
        private String name;
        private String schemaId;
        private Integer initialValue = 0;
        private Integer[] monthlyBalance = new Integer[]{0,0,0,0,0,0,0,0,0,0,0,0};

        public void addInitialValue(Integer addition){
            initialValue += addition;
        }

        public void addMonthlyBalance(Integer[] addition){
            monthlyBalance = Utils.addIntegerArrays(monthlyBalance, addition);
        }

        public Integer getBalance(){
            return initialValue + Utils.sumArray(monthlyBalance);
        }
    }
}
