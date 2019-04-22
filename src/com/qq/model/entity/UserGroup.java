package com.qq.model.entity;

import java.io.Serializable;

/**
 * @author simple
 * @version 1.0
 * @date 2018/12/7 14:51
 * @since 1.0
 */
public class UserGroup implements Serializable {
    private static final long serialVersionUID = 1L;
    private int userId;
    private int GroupId;

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public int getGroupId() {
        return GroupId;
    }

    public void setGroupId(int groupId) {
        GroupId = groupId;
    }
}
