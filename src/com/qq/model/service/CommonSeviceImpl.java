package com.qq.model.service;

import java.util.List;

public class CommonSeviceImpl<T extends java.io.Serializable> implements ICommonService<T> {

    @Override
    public int updateByConditions(String... conditions) {
        // TODO Auto-generated method stub
        return 0;
    }

    @Override
    public List<T> listAll() {
        return null;
    }

    @Override
    public List<T> listPart(String... conditions) {
        return null;
    }

    @Override
    public List<T> findByConditions(String... conditions) {
        return null;
    }

    @Override
    public T findById(int id) {
        return null;
    }

    @Override
    public T findByCondition(String... conditions) {
        return null;
    }

    @Override
    public int update(T t) {
        return 0;
    }

    @Override
    public int deleteById(int id) {
        return 0;
    }

    @Override
    public int delete(T t) {
        return 0;
    }

}
