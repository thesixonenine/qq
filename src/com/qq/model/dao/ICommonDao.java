package com.qq.model.dao;

import java.util.List;

public interface ICommonDao<T extends java.io.Serializable> {
    List<T> listAll();

    List<T> listPart(String... conditions);

    T findById(int id);

    T findByConditions(String... conditions);

    int update(T t);

    int updateByCondition(String... conditions);

    int updateById(int id);
}
