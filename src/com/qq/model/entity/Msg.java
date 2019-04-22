package com.qq.model.entity;

import java.io.Serializable;
import java.util.Date;

/**
 * @author simple
 * @version 1.0
 * @date 2018/12/7 14:50
 * @since 1.0
 */
public class Msg implements Serializable {
    private static final long serialVersionUID = 1L;

    private int id;
    private int userId;
    private String content;
    private Date date;
    private int receiver;
    private int status;
    private int type;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public Date getDate() {
        return date;
    }

    public void setData(Date date) {
        this.date = date;
    }

    public int getReceiver() {
        return receiver;
    }

    public void setReceiver(int receiver) {
        this.receiver = receiver;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }
}
