package org.kaleta.accountant;

import javax.persistence.*;

@Entity
@Table(name = "testt")
public class Data {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private int id;

    @Column(name = "_KEY")
    private String key;

    @Column(name = "_VALUE")
    private String value;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }
}
