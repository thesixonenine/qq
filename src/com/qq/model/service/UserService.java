package com.qq.model.service;

import com.qq.model.dao.DaoFactory;
import com.qq.model.entity.User;

import java.util.List;

@SuppressWarnings("unchecked")
public class UserService extends CommonSeviceImpl<User> {

    @Override
    public User findById(int id) {
        return (User) DaoFactory.getDao("user").findById(id);
    }

    @Override
    public User findByCondition(String... conditions) {
        return (User) DaoFactory.getDao("user").findByConditions(conditions);
    }


    @Override
    public int update(User t) {
        return DaoFactory.getDao("user").update(t);
    }


    @Override
    public List<User> listPart(String... conditions) {
        return DaoFactory.getDao("user").listPart(conditions);
    }

    @Override
    public int deleteById(int id) {
        // TODO Auto-generated method stub
        return super.deleteById(id);
    }

    @Override
    public int delete(User t) {
        // TODO Auto-generated method stub
        return super.delete(t);
    }

}
