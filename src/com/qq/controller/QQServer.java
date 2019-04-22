package com.qq.controller;

import com.qq.model.entity.Group;
import com.qq.model.entity.Msg;
import com.qq.model.entity.User;
import com.qq.model.service.ServiceFactory;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.*;
import java.util.concurrent.*;

/**
 * 描述：这是该项目最核心的部分，承担着所有消息的处理转发存储，用户的登录注册验证，所有的数据库操作都与之有关，而且实现了最复杂的消息转发错误重传机制。保存所有用户的在线信息，和群的在线信息。保存所有用户的socket
 * 功能：转发客户端的信息，让客户端可以正常交流，也实现了群聊功能，底层是基于消息标志甄别和重传，复杂度较高。
 * 属性：users――保存用户登录信息，服务器接受到客户端的socket连接后得到的socket与用户登录或注册登录后的用户信息绑定起来，实现消息的准确对应发送。
 * groups――用户登录时，会加载群列表中的群，每个群都对应该用户的socket，从而来实现群消息转发时，根据groups来向多个该群成员的socket发送群消息，此时也会经过错误重传机制，但客户端界面群聊界面完全复用了
 * 单聊界面的所有功能，所以可以被当作普通用户聊天操作。
 *
 * @author simple
 * @version 1.0
 * @date 2018/12/7 14:46
 * @since 1.0
 */
public class QQServer {
    public static ConcurrentHashMap<User, Socket> users = new ConcurrentHashMap<>();
    public static ConcurrentHashMap<Group, Socket> groups = new ConcurrentHashMap<>();
    private static final int LENGTH = 500;
    private static ThreadFactory threadFactory = Executors.defaultThreadFactory();
    private static ExecutorService poolExecutor = new ThreadPoolExecutor(LENGTH, LENGTH * 2,
            10000L, TimeUnit.MILLISECONDS,
            new LinkedBlockingQueue<Runnable>(1024), threadFactory, new ThreadPoolExecutor.AbortPolicy());

    public static void main(String[] args) {
        ServerSocket server = null;
        try {
            server = new ServerSocket(1995);
            System.out.println("服务器启动,等待客户端连接");
            while (true) {
                Socket client = server.accept();
//				System.out.println(client.getInetAddress()+":"+client.getPort());
                System.out.println("用户登录!");
                User newUser = new User();
                poolExecutor.execute(new ServerTask(client, newUser));
                users.put(newUser, client);
                System.out.println("当前在线人数：" + users.size());
            }
        } catch (IOException e) {
            System.out.println("获取服务器端socket失败!");
        } finally {
            poolExecutor.shutdown();
        }
    }
}

class ServerTask implements Runnable {
    private Socket socket;
    private final User user;
    private final List<Group> groups = new ArrayList<>();

    public ServerTask(Socket socket, User user) {
        this.socket = socket;
        this.user = user;
    }

    @Override
    public void run() {
        BufferedReader br = null;
        BufferedWriter bw = null;
        String content = null;
        try {
            br = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            bw = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
            while (true) {
                if (socket.isClosed()) {
                    System.out.println("连接已关闭");
                    return;
                }

                content = br.readLine();
                System.out.println((user.getAccount() != null ? user.getAccount() : "匿名") + " receive msg:" + content);
                System.out.println((user.getAccount() != null ? user.getAccount() : "匿名") + " socket port:" + socket.getPort());
                String[] msg = content.split(":");
                String result = null;
                if ("1".equals(msg[0])) {
                    result = doRegister(msg) + "\n";
                }
                if ("2".equals(msg[0])) {
                    result = doLogin(msg) + "\n";
                }
                if ("3".equals(msg[0])) {
                    result = doSendMsg(msg) + "\n";
                }
                if ("4".equals(msg[0])) {
                    result = doAcceptMsg(msg) + "\n";
                }
                if ("5".equals(msg[0])) {
                    result = doRetrainsmit(msg) + "\n";
                }
                if ("6".equals(msg[0])) {
                    result = doClose(msg);
                }
//				else{
//					bw.write(content+"\n");
//					bw.flush();
//				}
                bw.write(null == result ? "" : result);
                bw.flush();
            }
        } catch (IOException e) {
//			当用户断开连接时，即关闭userview界面时，会断开socket连接，此时判断该用户已经下线，从users里面去掉用户在线信息，同时去除该用户的群信息，完全去开了和该用户通信的socket，
//			防止消息发送的错误。
            System.out.println("服务器获取客户端输入流失败！");
            System.out.println("用户（" + (user.getAccount() != null ? user.getAccount() : "游客") + ") 已经下线!");
            QQServer.users.remove(user);
            for (Group group : groups) {
                QQServer.groups.remove(group);
            }
            System.out.println("在线用户数量：" + QQServer.users.size());
        }

    }

    private String doClose(String[] msg) {
        try {
            BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
            bw.write("4:" + msg[1] + ":" + msg[2] + ":" + msg[3] + "\n");
            bw.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return "重传成功";
    }

    /**
     * 消息重传机制
     *
     * @param msg 消息
     * @return 消息
     */
    private String doRetrainsmit(String[] msg) {
        try {
            BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
            if ("关闭指令".equals(msg[2])) {
                bw.write("4:" + msg[1] + ":" + msg[2] + ":" + msg[3] + "\n");
            } else {
                bw.write("4:" + msg[1] + ":" + msg[2] + ":" + msg[3] + "\n");
            }
            bw.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return "重传成功";
    }

    private String doAcceptMsg(String[] msg) {
//		try {
//			BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
//			System.out.println("write");
//			bw.write(msg[1]+"\n");
//			bw.flush();
//		} catch (IOException e) {
//			e.printStackTrace();
//		}
        return msg[1];
    }

    /**
     * @param msg 消息
     * @return 消息
     * @throws IOException 消息发送方法，包括群聊和单聊。
     */
    @SuppressWarnings("unchecked")
    private String doSendMsg(String[] msg) throws IOException {
        Group group = (Group) ServiceFactory.getService("group").findByCondition(msg[1]);
        User selfuser = (User) ServiceFactory.getService("user").findByCondition(user.getAccount());
//		群聊消息发送，接收方为指定的群的所有在线用户，socket从groups里面获取。
        if (group != null) {
            HashMap<Group, Socket> temp = new HashMap<>(QQServer.groups);
            for (Group group1 : groups) {
                temp.remove(group1);
            }
            Iterator<Map.Entry<Group, Socket>> it = temp.entrySet().iterator();
            HashSet<Socket> groupReceivers = new HashSet<Socket>();
            System.out.println("groupuser count:" + QQServer.groups.size());
            while (it.hasNext()) {
                Map.Entry<Group, Socket> entry = it.next();
                if (entry.getKey().getAccount().equals(group.getAccount())) {
                    groupReceivers.add(entry.getValue());
                }
            }

            System.out.println("groupReceivers count:" + groupReceivers.size());
            Iterator<Socket> receivers = groupReceivers.iterator();
            Msg newmsg = new Msg();
            newmsg.setContent(msg[2]);
            newmsg.setReceiver(group.getId());
            newmsg.setUserId(selfuser.getId());
            newmsg.setStatus(1);
            newmsg.setType(3);
            int flag = ServiceFactory.getService("msg").update(newmsg);
            if (flag > 0) {
                System.out.println("成功存入信息！");
            }
            while (receivers.hasNext()) {
                Socket s = receivers.next();
                BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(s.getOutputStream()));
                bw.write("4:" + group.getAccount() + ":" + msg[2] + ":" + flag + "\n");
                bw.flush();
                System.out.println("转发了用户" + user.getAccount() + " 的群消息：4:" + group.getAccount() + ":" + msg[2] + ":" + flag);
            }
            return "发送成功";
//			单聊的消息发送机制。
        } else {
            String receiverAccount = msg[1];
            User receiveruser = (User) ServiceFactory.getService("user").findByCondition(msg[1]);

            for (Map.Entry<User, Socket> userSocketEntry : QQServer.users.entrySet()) {
                User receiver = userSocketEntry.getKey();
//				选出正确的接收者的socket
                if (receiver.getAccount().equals(receiverAccount)) {
                    Socket socket1 = QQServer.users.get(receiver);
                    if (socket1.isClosed()) {
                        System.out.println("接口已关闭！");
                        return "接口已关闭";
                    }
                    Msg newmsg = new Msg();
                    newmsg.setContent(msg[2]);
                    newmsg.setReceiver(receiveruser.getId());
                    newmsg.setUserId(selfuser.getId());
                    newmsg.setStatus(1);
                    newmsg.setType(3);
//					接收到了就存入数据库，状态为未读。
                    int flag = ServiceFactory.getService("msg").update(newmsg);
                    if (flag > 0) {
                        System.out.println("成功存入信息！");
                    }

                    BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(socket1.getOutputStream()));
                    bw.write("4:" + user.getAccount() + ":" + msg[2] + ":" + flag + "\n");
                    bw.flush();
                    System.out.println("向用户" + receiverAccount + " 发送了：4:" + user.getAccount() + ":" + msg[2] + ":" + flag);
                    return "发送成功";
                }
            }
            Msg newmsg = new Msg();
            newmsg.setContent(msg[2]);
            newmsg.setReceiver(receiveruser.getId());
            newmsg.setUserId(selfuser.getId());
            newmsg.setStatus(1);
            newmsg.setType(3);
            int flag = ServiceFactory.getService("msg").update(newmsg);
            if (flag > 0) {
                System.out.println("成功存入信息！");
            }
            System.out.println("好友当前不在线，转为离线发送");
            return "4:" + msg[1] + ":好友当前不在线,转为离线发送";
        }

    }

    @SuppressWarnings("unchecked")
    private String doLogin(String[] msg) {
        User user = (User) ServiceFactory.getService("user").findByCondition(msg[1]);
        if (user == null) {
            return "该QQ号不存在";
        }
        if (user.getPassword().equals(msg[2])) {
            for (User u : QQServer.users.keySet()) {
                if (u.getAccount() != null) {
                    if (u.getAccount().equals(msg[1])) {
                        return "该用户已登录,不能重复登录";
                    }
                }
            }
//			登录成功后，存入登录的用户信息，用于和该socket匹配，用于以后的消息发送接受
            this.user.setAccount(user.getAccount());
            List<Group> usergroup = ServiceFactory.getService("group").listPart("" + user.getId());
//			登录后，存入用户的群信息，并且绑定该用户的socket，当接收到群消息时，通过groups来查找所有该群的用户的socket来逐个发送消息。
            for (Group anUsergroup : usergroup) {
                groups.add(anUsergroup);
                QQServer.groups.put(anUsergroup, socket);
            }
            return "欢迎:" + user.getAccount();

        } else {
            return "用户名和密码,不匹配";
        }
    }

    @SuppressWarnings("unchecked")
    private String doRegister(String[] msg) {
        User user = (User) ServiceFactory.getService("user").findByCondition(msg[1]);
        if (user != null) {
            return "QQ号已被注册";
        } else {
            User newUser = new User();
            newUser.setAccount(msg[1]);
            newUser.setPassword(msg[2]);
            System.out.println(msg[1] + msg[2]);
            int flag = ServiceFactory.getService("user").update(newUser);
            if (flag > 0) {
                System.out.println("成功注册");
                this.user.setAccount(newUser.getAccount());
                List<Group> usergroup = ServiceFactory.getService("group").listPart("" + newUser.getId());
                for (Group anUsergroup : usergroup) {
                    groups.add(anUsergroup);
                    QQServer.groups.put(anUsergroup, socket);
                }
                return "欢迎:" + newUser.getAccount();
            }
            return "注册失败!";
        }
    }

    public Socket getSocket() {
        return socket;
    }

    public void setSocket(Socket socket) {
        this.socket = socket;
    }
}