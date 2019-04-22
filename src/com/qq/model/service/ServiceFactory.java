package com.qq.model.service;

import java.util.WeakHashMap;

@SuppressWarnings("rawtypes")
public class ServiceFactory {

    private final static WeakHashMap<String, ICommonService> MAP = new WeakHashMap<>();

    public static ICommonService getService(String name) {
        ICommonService service = MAP.get(name);
        if (service != null) {
            return service;
        } else {
            return createService(name);
        }
    }

    private static ICommonService createService(String name) {
        ICommonService service = null;
        if ("user".equals(name)) {
            service = new UserService();
            MAP.put("user", service);
        }
        if ("friendGroup".equals(name)) {
            service = new FriendGroupService();
            MAP.put("friendGroup", service);
        }
        if ("friendGroupUser".equals(name)) {
            service = new FriendGroupUserService();
            MAP.put("friendGroupUser", service);
        }
        if ("msg".equals(name)) {
            service = new MsgService();
            MAP.put("msg", service);
        }
        if ("group".equals(name)) {
            service = new GroupService();
            MAP.put("group", service);
        }
        if ("userGroup".equals(name)) {
            service = new UserGroupService();
            MAP.put("userGroup", service);
        }
        return service;
    }
}
