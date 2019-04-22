package com.qq.model.service;

import com.qq.model.dao.DaoFactory;
import com.qq.model.entity.UserGroup;

import java.util.List;

public class UserGroupService extends CommonSeviceImpl<UserGroup> {

    @Override
    public int updateByConditions(String... conditions) {
        return DaoFactory.getDao("userGroup").updateByCondition(conditions);
    }

    @Override
    public List<UserGroup> listPart(String... conditions) {
        // TODO Auto-generated method stub
        return super.listPart(conditions);
    }

    @Override
    public List<UserGroup> findByConditions(String... conditions) {
        // TODO Auto-generated method stub
        return super.findByConditions(conditions);
    }

    @Override
    public UserGroup findByCondition(String... conditions) {
        // TODO Auto-generated method stub
        return super.findByCondition(conditions);
    }

    @Override
    public int update(UserGroup t) {
        // TODO Auto-generated method stub
        return super.update(t);
    }


}
