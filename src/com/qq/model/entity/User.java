package com.qq.model.entity;

import java.io.Serializable;
import java.util.Objects;

/**
 * @author simple
 * @version 1.0
 * @date 2018/12/7 14:51
 * @since 1.0
 */
public class User implements Serializable {

    private static final long serialVersionUID = 1L;
    private int id;
    private String name;
    private String account;
    private String password;
    private String icon;
    private String label;
    private int sendMsg;
    private int sendText;
    private int login;
    private int all;
    private int level;
    private int type;
    private String sex;


    public String getSex() {
        return sex;
    }

    public void setSex(String sex) {
        this.sex = sex;
    }

    public String getAccount() {
        return account;
    }

    public void setAccount(String account) {
        this.account = account;
    }

    public int getSendText() {
        return sendText;
    }

    public void setSendText(int sendText) {
        this.sendText = sendText;
    }

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

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getIcon() {
        return icon;
    }

    public void setIcon(String icon) {
        this.icon = icon;
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public int getSendMsg() {
        return sendMsg;
    }

    public void setSendMsg(int sendMsg) {
        this.sendMsg = sendMsg;
    }

    public int getLogin() {
        return login;
    }

    public void setLogin(int login) {
        this.login = login;
    }

    public int getAll() {
        return all;
    }

    public void setAll(int all) {
        this.all = all;
    }

    public int getLevel() {
        return level;
    }

    public void setLevel(int level) {
        this.level = level;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        User user = (User) o;
        return id == user.id;
    }

    @Override
    public int hashCode() {

        return Objects.hash(id);
    }
}
