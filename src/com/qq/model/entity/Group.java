package com.qq.model.entity;

import java.io.Serializable;
import java.util.Objects;

/**
 * @author simple
 * @version 1.0
 * @date 2018/12/7 14:50
 * @since 1.0
 */
public class Group implements Serializable {

    private static final long serialVersionUID = 1L;

    private int id;
    private String name;
    private String account;
    private String label;
    private int level;
    private String icon;
    private int status;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getAccount() {
        return account;
    }

    public void setAccount(String account) {
        this.account = account;
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public int getLevel() {
        return level;
    }

    public void setLevel(int level) {
        this.level = level;
    }

    public String getIcon() {
        return icon;
    }

    public void setIcon(String icon) {
        this.icon = icon;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        Group group = (Group) o;
        return id == group.id;
    }

    @Override
    public int hashCode() {

        return Objects.hash(id);
    }
}
