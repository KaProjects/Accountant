package org.kaleta.accountant.backend.model;


import lombok.Data;

@Data
public class FirebaseAccountModel
{
    private String id;
    private String name;

    public FirebaseAccountModel(String name, String id) {
        this.id = id;
        this.name = name;
    }
}
