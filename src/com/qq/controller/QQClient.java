package com.qq.controller;

import com.qq.view.Login;

import java.io.*;
import java.net.Socket;
import java.net.UnknownHostException;

/**
 * 客户端的入口，用于启动客户端程序，连接服务器，连接成功后创建一个socket
 *
 * @author simple
 * @version 1.0
 * @date 2018/12/7 14:43
 * @since 1.0
 */
public class QQClient {
    private Socket client = null;

    public static void main(String[] args) {
        try {
            System.out.println(System.getProperty("user.dir"));
            QQClient qqClient = new QQClient();
            qqClient.client = new Socket("127.0.0.1", 1995);
            System.out.println("客户端成功连接服务器");
            System.out.println(qqClient.client.getInetAddress() + ":" + qqClient.client.getPort());
            Login.createLoginView("QQ", qqClient.client);
        } catch (UnknownHostException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}

/**
 * 以下的部分为舍弃代码，用于初期的和服务器联通测试，测试通过即可删除
 * 功能为：创建两个客户端任务，用于接收服务器的数据和向服务器发送数据
 */
class ClientSendMsg implements Runnable {
    private Socket socket = null;

    public ClientSendMsg(Socket s) {
        this.socket = s;
    }

    @Override
    public void run() {
        BufferedWriter bw = null;
        String content = "hello world";
        try {
            bw = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
            bw.write(content + "\n");
            bw.flush();

        } catch (IOException e) {
            System.out.println("客户端写入失败");
        }

    }

}

class ClientAcceptMsg implements Runnable {
    private Socket socket = null;

    public ClientAcceptMsg(Socket s) {
        this.socket = s;
    }

    @Override
    public void run() {
        BufferedReader br = null;
        String content = null;
        try {
            br = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            while (true) {
                System.out.println("hello");
                content = br.readLine();
                System.out.println("接收信息:" + content);
            }

        } catch (IOException e) {
            System.out.println("接收信息失败！");
        }
    }

}