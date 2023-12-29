package org.kaleta.model;

import lombok.Data;
import org.kaleta.Utils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Data
public class ClassComponent
{
    private String name;
    private String schemaId;
    private List<GroupComponent> groups = new ArrayList<>();

    public Integer getInitialValue(){
        Integer initialValue = 0;
        for (GroupComponent group : groups){
            initialValue += group.getInitialValue();
        }
        return initialValue;
    }

    public Integer[] getMonthlyBalance(){
        Integer[] monthlyBalance = new Integer[12];
        Arrays.fill(monthlyBalance, 0);
        for (GroupComponent group : groups){
            monthlyBalance = Utils.addIntegerArrays(monthlyBalance, group.getMonthlyBalance());
        }
        return monthlyBalance;
    }

    public Integer getBalance(){
        Integer balance = 0;
        for (GroupComponent group : groups){
            balance += group.getBalance();
        }
        return balance;
    }
}
