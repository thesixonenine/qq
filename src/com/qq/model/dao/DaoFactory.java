package com.qq.model.dao;

import java.util.WeakHashMap;

@SuppressWarnings("rawtypes")
public class DaoFactory {

    private final static WeakHashMap<String, ICommonDao> MAP = new WeakHashMap<>();

    public static ICommonDao getDao(String name) {
        ICommonDao dao = MAP.get(name);
        if (dao != null) {
            return dao;
        } else {
            return createDao(name);
        }
    }

    private static ICommonDao createDao(String name) {
        ICommonDao dao = null;
        if ("user".equals(name)) {
            dao = new UserDao();
            MAP.put("user", dao);
            return dao;
        }
        if ("friendGroup".equals(name)) {
            dao = new FriendGroupDao();
            MAP.put("friendGroup", dao);
            return dao;
        }
        if ("friendGroupUser".equals(name)) {
            dao = new FriendGroupUserDao();
            MAP.put("friendGroupUser", dao);
            return dao;
        }
        if ("msg".equals(name)) {
            dao = new MsgDao();
            MAP.put("msg", dao);
            return dao;
        }
        if ("group".equals(name)) {
            dao = new GroupDao();
            MAP.put("group", dao);
            return dao;
        }
        if ("userGroup".equals(name)) {
            dao = new UserGroupDao();
            MAP.put("userGroup", dao);
            return dao;
        }
        return null;
    }

}
