package com.qq.model.entity;

import java.io.Serializable;

/**
 * @author simple
 * @version 1.0
 * @date 2018/12/7 14:49
 * @since 1.0
 */
public class FriendGroupUser implements Serializable {
    private static final long serialVersionUID = 1L;

    private int friendGroupId;
    private int userId;

    public int getFriendGroupId() {
        return friendGroupId;
    }

    public void setFriendGroupId(int friendGroupId) {
        this.friendGroupId = friendGroupId;
    }

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }
}
