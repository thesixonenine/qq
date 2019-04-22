package com.qq.model.service;

import com.qq.model.dao.DaoFactory;
import com.qq.model.entity.FriendGroupUser;

import java.util.List;

public class FriendGroupUserService extends CommonSeviceImpl<FriendGroupUser> {

    @SuppressWarnings("unchecked")
    @Override
    public List<FriendGroupUser> listPart(String... conditions) {
        return DaoFactory.getDao("friendGroupUser").listPart(conditions);
    }

    @Override
    public int updateByConditions(String... conditions) {
        return DaoFactory.getDao("friendGroupUser").updateByCondition(conditions);
    }

}
