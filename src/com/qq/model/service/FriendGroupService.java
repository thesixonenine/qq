package com.qq.model.service;

import com.qq.model.dao.DaoFactory;
import com.qq.model.entity.FriendGroup;

import java.util.List;

public class FriendGroupService extends CommonSeviceImpl<FriendGroup> {

    @Override
    public FriendGroup findById(int id) {
        return (FriendGroup) DaoFactory.getDao("friendGroup").findById(id);
    }


    @Override
    public FriendGroup findByCondition(String... conditions) {
        return (FriendGroup) DaoFactory.getDao("friendGroup").findByConditions(conditions);
    }

    @Override
    public int updateByConditions(String... conditions) {
        System.out.println("hello");
        return DaoFactory.getDao("friendGroup").updateByCondition(conditions);
    }

    @SuppressWarnings("unchecked")
    @Override
    public List<FriendGroup> listPart(String... conditions) {
        return DaoFactory.getDao("friendGroup").listPart(conditions);
    }


}
