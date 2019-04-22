package com.qq.model.service;

import java.util.List;

public interface ICommonService<T extends java.io.Serializable> {

    List<T> listAll();

    List<T> listPart(String... conditions);

    List<T> findByConditions(String... conditions);

    T findById(int id);

    T findByCondition(String... conditions);

    int update(T t);

    int updateByConditions(String... conditions);

    int deleteById(int id);

    int delete(T t);


}
