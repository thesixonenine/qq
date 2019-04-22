package com.qq.util;

import com.qq.model.entity.Msg;
import com.qq.model.service.ServiceFactory;

public class MsgUtils {

    @SuppressWarnings("unchecked")
    public static int sendMsgBack(int type, int receiver, int user, String content) {
        Msg msg = new Msg();
        msg.setContent(content);
        msg.setUserId(user);
        msg.setReceiver(receiver);
        msg.setStatus(1);
        msg.setType(2);
        int flag = ServiceFactory.getService("msg").update(msg);
        return flag;
    }
}
