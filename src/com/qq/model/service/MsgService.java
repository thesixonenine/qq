package com.qq.model.service;

import com.qq.model.dao.DaoFactory;
import com.qq.model.entity.Msg;

import java.util.List;

@SuppressWarnings("unchecked")
public class MsgService extends CommonSeviceImpl<Msg> {

    @Override
    public int updateByConditions(String... conditions) {
        return DaoFactory.getDao("msg").updateByCondition(conditions);
    }

    @Override
    public List<Msg> listAll() {
        // TODO Auto-generated method stub
        return super.listAll();
    }

    @Override
    public List<Msg> listPart(String... conditions) {
        return DaoFactory.getDao("msg").listPart(conditions);
    }

    @Override
    public List<Msg> findByConditions(String... conditions) {
        // TODO Auto-generated method stub
        return super.findByConditions(conditions);
    }

    @Override
    public Msg findById(int id) {
        return (Msg) DaoFactory.getDao("msg").findById(id);
    }

    @Override
    public Msg findByCondition(String... conditions) {
        return (Msg) DaoFactory.getDao("msg").findByConditions(conditions);
    }

    @Override
    public int update(Msg t) {
        return DaoFactory.getDao("msg").update(t);
    }

    @Override
    public int deleteById(int id) {
        // TODO Auto-generated method stub
        return super.deleteById(id);
    }

    @Override
    public int delete(Msg t) {
        // TODO Auto-generated method stub
        return super.delete(t);
    }


}
