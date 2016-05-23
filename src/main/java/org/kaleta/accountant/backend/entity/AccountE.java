package org.kaleta.accountant.backend.entity;

/**
 * Created by Stanislav Kaleta on 16.04.2016.
 */
public class AccountE {
    private final String type;
    private final int clazz;
    private final int group;
    private final int number;
    private final String name;

    public AccountE(String type, int clazz, int group, int number, String name){
        this.type = type;
        this.clazz = clazz;
        this.group = group;
        this.number = number;
        this.name = name;
    }

    public String getType() {
        return type;
    }

    public int getClazz() {
        return clazz;
    }

    public int getGroup() {
        return group;
    }

    public int getNumber() {
        return number;
    }

    public String getName() {
        return name;
    }

    // TODO: 4/16/16 equals, hash code
}
