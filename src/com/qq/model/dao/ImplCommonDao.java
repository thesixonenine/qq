package com.qq.model.dao;

import java.util.List;

public class ImplCommonDao<T extends java.io.Serializable> implements ICommonDao<T> {

    @Override
    public List<T> listAll() {
        return null;
    }

    @Override
    public List<T> listPart(String... conditions) {
        return null;
    }

    @Override
    public T findById(int id) {
        return null;
    }

    @Override
    public T findByConditions(String... conditions) {
        return null;
    }

    @Override
    public int update(T t) {
        return 0;
    }

    @Override
    public int updateByCondition(String... conditions) {
        return 0;
    }

    @Override
    public int updateById(int id) {
        return 0;
    }

}
