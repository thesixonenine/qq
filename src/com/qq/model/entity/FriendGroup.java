package com.qq.model.entity;

import java.io.Serializable;

/**
 * @author simple
 * @version 1.0
 * @date 2018/12/7 14:48
 * @since 1.0
 */
public class FriendGroup implements Serializable {
    private static final long serialVersionUID = 1L;

    private int id;
    private String name;

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
}
