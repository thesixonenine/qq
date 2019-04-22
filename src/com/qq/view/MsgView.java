package com.qq.view;

import com.qq.model.entity.Group;
import com.qq.model.entity.Msg;
import com.qq.model.entity.User;
import com.qq.model.service.ServiceFactory;

import javax.swing.*;
import javax.swing.tree.DefaultMutableTreeNode;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.net.Socket;
import java.net.UnknownHostException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;

/**
 * @author fsc
 * 功能：构建聊天窗口，同时又可以复用为群聊窗口，可以在该界面查找好友或群，点击进入新的聊天窗口，消息接收部分为JScrollPane，还有用户对好友的一系列操作
 * 属性：type――标明该窗口用于单聊还是群聊	receiver――该窗口的消息接收方	self――该窗口的消息发送方	client――当前登录用户的socket
 * map――保存开启当前窗口的接收方和负责该消息窗口信息接收功能的线程	list――保存当前接收者所开的窗口实例化对象	isfirst――该用户登录以来，是否第一次打开和该接收者聊天的窗口，用于判断是否需要读取为读取信息。否则会有重复
 * count――保存当前窗口查看更多记录的时候，从数据库里面读取过的记录痕迹，方便重复每次从数据库里面读取多条不重复的数据
 */
public class MsgView extends JFrame {
    private final static int COLOR = 0xcccccc;
    private String type;
    private User receiver = null;
    private User self = null;
    private final Socket client;
    private static final long serialVersionUID = 1L;
    private final static HashMap<User, Thread> map = new HashMap<>();
    private final static HashMap<User, MsgView> list = new HashMap<>();
    private final static HashMap<String, Boolean> isfirst = new HashMap<>();
    private final static HashMap<String, Integer> count = new HashMap<>();
    private final static HashMap<User, Thread> groupMap = new HashMap<>();

    public MsgView(String name, String type, Socket client) {
        super(name);
        this.type = type;
        this.client = client;
    }

    @SuppressWarnings({"static-access", "unchecked"})
    public static MsgView createMsgView(String name, String type, String chataccount, final Socket client, String useraccount) {
        System.out.println("MsgView of " + type + ":" + Thread.currentThread());
        final MsgView mv = new MsgView(name, type, client);
//		判断是属于个人聊天界面还是群聊界面
        if ("friend".equals(type)) {
            mv.receiver = (User) ServiceFactory.getService("user").findByCondition(chataccount);
        } else {
            Group group = (Group) ServiceFactory.getService("group").findByCondition(chataccount);
            User user = new User();
            user.setId(group.getId());
            user.setAccount(group.getAccount());
            user.setIcon(group.getIcon());
            user.setLabel(group.getLabel());
            user.setLevel(group.getLevel());
            user.setName(group.getName());
            mv.receiver = user;
        }
        mv.self = (User) ServiceFactory.getService("user").findByCondition(useraccount);
        ImageIcon ii = new ImageIcon(System.getProperty("user.dir") + "\\src\\imgs\\msg1.jpg");
        JLabel jl = new JLabel(ii);
        jl.setBounds(0, 0, ii.getIconWidth(), ii.getIconHeight());
        mv.getLayeredPane().setLayout(null);
        mv.getLayeredPane().add(jl, new Integer(Integer.MIN_VALUE));

        JPanel jpcontent = (JPanel) mv.getContentPane();
//		聊天内容区域
        final JPanel jpmsgcontent = new JPanel();
        final JScrollPane jps = new JScrollPane(jpmsgcontent);
        jps.setVisible(true);
        jps.setBounds(0, 70, 550, 400);
        jps.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        jps.setOpaque(false);
        jps.setBorder(new MyBorder(0xeeeeee));
        JViewport test = (JViewport) jps.getComponent(0);
        test.setOpaque(false);

        final JPanel jpsendmsg = new JPanel();
        jpcontent.setOpaque(false);
        jpcontent.setLayout(null);
        jpcontent.add(createTopInfo(mv));

        createMsgContent(jpmsgcontent, mv, jps);
        jpcontent.add(jps);
        jpcontent.add(createMsgRight(mv));
        jpcontent.add(createSendMsg(jpsendmsg, mv, jpmsgcontent, jps));
        jpcontent.add(createEnd());
        mv.setBounds(600, 200, 800, 700);

        mv.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        mv.addWindowListener(new WindowAdapter() {

            /* (non-Javadoc)
             * @see java.awt.event.WindowAdapter#windowClosing(java.awt.event.WindowEvent)
             * 功能：此处绑定聊天界面的关闭事件，关闭事件的时候并没有结束该聊天界面的接收信息线程，会影响到其他线程接收信息，并且同一个接收者聊天窗口多次创建之后，关闭之后，最后一个关闭的
             * 界面接收消息的线程是开启者的，影响消息的下次接收，而且可能会误导消息的状态错误的改变了。
             * 具体实现：利用线程里面对消息的内容判断来实现线程的可控性关闭，此处向服务器发送关闭指令，服务器再转发回来，接收时可能会接收失败（被其他接收线程接收），所以要用循环来发送关闭指令
             * 该循环的判断条件就是该接收线程的状态，当线程死亡时，停止发送关闭指令，此时服务器可能接收到了多条关闭指令，所以在线程接收关闭指令时，要是别指令内容里面的线程名，来实现关闭线程的
             * 准确性，其余多发的信息将会被其他线程忽略掉。
             */
            @Override
            public void windowClosing(WindowEvent e) {
                while (true) {
                    Iterator<Entry<User, Thread>> it = map.entrySet().iterator();
                    System.out.println(map.size());
                    boolean isEnd = true;
                    while (it.hasNext()) {
                        Entry<User, Thread> entry = it.next();
//						找出当前指定接收者窗口的接收信息线程，判断存活状态，后发送关闭指令，并带上线程名标记，并且消息格式为转发信息，交由服务器转发方法处理
                        if (entry.getKey().getAccount().equals(mv.receiver.getAccount())) {
                            System.out.println(entry.getValue().getName() + ":" + entry.getValue().isAlive());
                            if (entry.getValue().isAlive()) {
//								当前没有线程存活时，isEnd为真，此时停止发送关闭指令。
                                System.out.println(isEnd);
                                isEnd = false;
                                try {
                                    BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(client.getOutputStream()));
                                    bw.write("5:" + mv.receiver.getAccount() + ":关闭指令:" + entry.getValue().getName() + "\n");
                                    System.out.println("5:" + mv.receiver.getAccount() + ":关闭指令:" + entry.getValue().getName());
                                    bw.flush();
                                    Thread.yield();
                                } catch (IOException e1) {
                                    System.out.println("发送关闭指令失败");
                                    e1.printStackTrace();
                                }
                                continue;
                            }
                        }
                    }
//					该接收者成功关闭接收信息线程，当第二次打开与该接收者相同的聊天窗口时，isfirst会显示为false，窗口开启时不会读取数据库中离线信息。
                    if (isEnd) {
                        isfirst.remove(mv.receiver.getAccount());
                        isfirst.put(mv.receiver.getAccount(), false);
                        break;
                    }
                }
            }

        });
        mv.setVisible(true);
        mv.setResizable(false);
        mv.addFocusListener(new FocusAdapter() {

            @Override
            public void focusGained(FocusEvent e) {
                mv.validate();
                mv.repaint();
            }
        });
        /*
         * 功能：当新界面开启时，会关闭相同接收者的之间的界面，这个循环就是用来重复发送关闭指令到服务器，关闭上一具有相同接收者的聊天窗口的接收信息线程。多余关闭指令会被其他接收信息
         * 线程忽略。实现开启新的聊天窗口时必定关闭上一个具有相同接收者的聊天窗口，并且也适用于群聊。
         */
        while (true) {
            Iterator<Entry<User, Thread>> it = map.entrySet().iterator();
            System.out.println(map.size());
            boolean isEnd = true;
            while (it.hasNext()) {
                Entry<User, Thread> entry = it.next();
                if (entry.getKey().getAccount().equals(mv.receiver.getAccount())) {
                    System.out.println(entry.getValue().getName() + ":" + entry.getValue().isAlive());
                    if (entry.getValue().isAlive()) {
                        System.out.println(isEnd);
                        isEnd = false;
                        try {
                            BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(client.getOutputStream()));
                            bw.write("5:" + mv.receiver.getAccount() + ":关闭指令:" + entry.getValue().getName() + "\n");
                            System.out.println("5:" + mv.receiver.getAccount() + ":关闭指令:" + entry.getValue().getName());
                            bw.flush();
                            Thread.yield();
                        } catch (IOException e1) {
                            System.out.println("发送关闭指令失败");
                            e1.printStackTrace();
                        }
                        continue;
                    }
                }
            }
            if (isEnd) {
                break;
            }
        }
        /*
         * 功能：该线程是这个聊天窗口的最主要工作机制。用于收发服务器传送回来的各种验证信息及各种消息，各种复杂的消息接收和甄别。
         * 最核心的功能：判断该聊天窗口接收信息的线程接收到的信息是否是属于该窗口接受的信息，倘若验证receiver的account不
         * 能匹配，将会向服务器请求转发信息，服务器接收到消息头为5的转发信息后，再次向该用户的多个接收消息线程发送数据，倘若还是
         * 为正确命中。继续转发。最后正确的线程接收信息后，交付消息窗口显示信息。此功能完全复用于群聊。
         */
        Thread thread = new Thread(new Runnable() {

            @Override
            public void run() {
                try {
                    BufferedReader br = new BufferedReader(new InputStreamReader(mv.client.getInputStream()));
                    JLabel error = (JLabel) jpsendmsg.getComponent(1);
//					rightReceiver用于记录该窗口真正的接收者，用于消息接收错误之后的转发工作，和isFirst来确定是正确的接收人

                    String rightReceiver = null;
                    boolean isFirst = true;
                    while (true) {
                        String msgreceive = null;
                        msgreceive = br.readLine();
                        System.out.println(Thread.currentThread().getName());
                        System.out.println(mv.self.getAccount() + "收到信息：" + msgreceive);
                        String[] msgarr = msgreceive.split(":");
//						倘若收到关闭指令，查看是否匹配到该线程，未匹配则忽略该指令
                        if ("4".equals(msgarr[0]) && "关闭指令".equals(msgarr[2]) && !msgarr[3].equals(Thread.currentThread().getName())) {
                            continue;
                        }
//						消息为4，则进入下一步，此处为删选出有用信息，排除结果信息。
                        if ("4".equals(msgarr[0])) {
//							未匹配该接收窗口则转发信息。
                            if (!msgarr[1].equals(mv.receiver.getAccount())) {
                                if (isFirst) {
                                    rightReceiver = msgarr[1];
                                    isFirst = false;
                                }
                                System.out.println("接收出错，发送重传信号！");
                                BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(mv.client.getOutputStream()));
                                if ("好友当前不在线,转为离线发送".equals(msgarr[2])) {
                                    bw.write("5:" + msgarr[1] + ":" + msgarr[2] + "\n");
                                } else {
                                    bw.write("5:" + rightReceiver + ":" + msgarr[2] + ":" + msgarr[3] + "\n");
                                }
                                bw.flush();
                                continue;
                            }
//							关闭指令成功到达作用地点，此时跳出该接收线程。同时关闭该窗口
                            if ("关闭指令".equals(msgarr[2]) && msgarr[3].equals(Thread.currentThread().getName())) {
                                System.out.println("关闭窗口:" + msgarr[1] + "    " + "关闭线程 :" + Thread.currentThread().getName());
                                mv.setVisible(false);
                                ;
                                break;
                            }
//							此处用于新窗口建立时，旧窗口关闭时系统发送了过多的关闭指令，并且通过了前面的验证，此时不应当当作消息来处理，所以过滤掉
                            if ("关闭指令".equals(msgarr[2]) && !msgarr[3].equals(Thread.currentThread().getName())) {
                                continue;
                            }
//							好友不在线是发送信息会有提示，仅此一个提示。该内容为服务器返回的结果，并且要通过差错重传机制来正确命中该窗口，不容易啊。
                            if ("好友当前不在线,转为离线发送".equals(msgarr[2])) {
                                error.setVisible(true);
                                error.setText(msgarr[2]);
                                error.setForeground(Color.red);
                                continue;
                            }
                            Msg testmsg = (Msg) ServiceFactory.getService("msg").findById(Integer.valueOf(msgarr[3]));
                            System.out.println(testmsg.getStatus());
                            if (testmsg.getStatus() == 2 && "friend".equals(mv.type)) {
                                Thread.yield();
                                continue;
                            }
                            JPanel newMsg = new JPanel();
                            int count = jpmsgcontent.getComponentCount();
                            jpmsgcontent.add(createSingleMsgBlock(newMsg, 65 * (count - 1), 0xeeeeee, 1, mv.receiver.getIcon(), mv.receiver.getName(), msgarr[2], new SimpleDateFormat("EEE  HH:mm:ss").format(testmsg.getDate())));
                            jpmsgcontent.setPreferredSize(new Dimension(530, 65 * count + 35));
                            jpmsgcontent.setBounds(0, 70, 530, 65 * count + 35);
                            jps.getViewport().setViewPosition(new Point(0, 65 * (jpmsgcontent.getComponentCount())));
                            mv.validate();
                            mv.repaint();
//							服务器接收到的消息存入数据库都是状态为1的未读状态，此时已经显示在界面上，所以改为已读
                            int flag = ServiceFactory.getService("msg").updateByConditions("2", "" + testmsg.getId());
                            if (flag > 0) {
                                System.out.println("已经将该消息(id=" + testmsg.getId() + ")状态修改为已读！");
                            }

                        } else {
//							if(!msgreceive.equals("发送成功")&&!msgreceive.equals("重传成功")){
//								error.setVisible(true);
//								error.setText(msgreceive);
//								error.setForeground(Color.red);
//								continue;
//							}else{
//								error.setVisible(false);
//							}
                        }
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });
        thread.start();
        map.put(mv.receiver, thread);
        list.put(mv.receiver, mv);
        count.put(mv.receiver.getAccount(), 0);
//		 要将窗口第一次建立时的标识设置为真，用于第一次建立该接收者窗口时的离线信息读取到消息框的作用
        if (isfirst.get(mv.receiver.getAccount()) == null) {
            isfirst.put(mv.receiver.getAccount(), true);
        }
        Thread.yield();
//		 倘若为第一次打开该接收者的聊天窗口，会读取离线信息，但是会有点小问题。就是也许会重复。
        if (isfirst.get(mv.receiver.getAccount())) {
            List<Msg> listmsg = ServiceFactory.getService("msg").listPart("" + mv.self.getId(), "3", "1", "" + mv.receiver.getId());
            HashMap<Long, Msg> desc = new HashMap<>();
            for (int i = 0; i < listmsg.size(); i++) {
                desc.put(Long.valueOf(listmsg.get(i).getDate().getTime()), listmsg.get(i));
            }
            Iterator<Entry<Long, Msg>> it = desc.entrySet().iterator();
            while (it.hasNext()) {
                Entry<Long, Msg> msg = it.next();
                System.out.println(msg.getValue().getContent() + ":" + msg.getValue().getId() + ":" + msg.getValue().getStatus());
                JPanel newMsg = new JPanel();
                int count = jpmsgcontent.getComponentCount();
                jpmsgcontent.add(createSingleMsgBlock(newMsg, 65 * (count - 1), 0xeeeeee, 1, mv.receiver.getIcon(), mv.receiver.getName(), msg.getValue().getContent(), (new SimpleDateFormat("EEE  HH:mm:ss").format(msg.getValue().getDate()))));
                jpmsgcontent.setPreferredSize(new Dimension(530, 65 * count + 35));
                jpmsgcontent.setBounds(0, 70, 530, 65 * count + 35);
                jps.getViewport().setViewPosition(new Point(0, 65 * (jpmsgcontent.getComponentCount())));
                int flag = ServiceFactory.getService("msg").updateByConditions("2", "" + msg.getValue().getId());
                if (flag > 0) {
                    System.out.println("已经将该消息(id=" + msg.getValue().getId() + ")状态修改为已读！");
                }
                mv.validate();
                mv.repaint();
            }
        }
        return mv;
    }

    public static void main(String[] args) throws UnknownHostException, IOException {
        MsgView.createMsgView("聊天室", "friend", "匿名(111111)", new Socket("127.0.0.1", 1995), "111112");
    }

    public static JPanel createTopInfo(MsgView mv) {
        JPanel jp = new JPanel();
        jp.setLayout(null);
        jp.setVisible(true);
        jp.setOpaque(false);
        jp.setBounds(0, 0, 200, 70);

        jp.add(createIconLabel(10, 15, 50, 50, COLOR, mv));
        jp.add(createNameLabel(70, 15, 200, 25, mv.receiver.getName() + "(" + mv.receiver.getAccount() + ")", COLOR, mv.receiver.getLabel()));
        return jp;
    }

    public static JLabel createIconLabel(int left, int top, int width, int height, int bgcolor, MsgView mv) {
//		ImageIcon ii = new ImageIcon("C:\\Users\\fsc\\Desktop\\icon.png");
        ImageIcon ii = new ImageIcon(System.getProperty("user.dir") + mv.receiver.getIcon());
        ii.setImage(ii.getImage().getScaledInstance(width, height, Image.SCALE_DEFAULT));
        JLabel jl = new JLabel(ii);
        jl.setBounds(left, top, width, height);
        return jl;
    }

    public static JPanel createNameLabel(int left, int top, int width, int height, String name, int bgcolor, String label) {
        JPanel jp = new JPanel();
        jp.setVisible(true);
        jp.setBounds(left, top, 300, height * 2);
        jp.setLayout(null);
        jp.setOpaque(false);

        JLabel jl = new JLabel();
        jl.setText(name);
        jl.setFont(new Font("宋体", Font.BOLD, 15));
        jl.setBounds(0, 0, width, height);

        JLabel jllabel = new JLabel();
        jllabel.setText(label);
        jllabel.setFont(new Font("宋体", Font.ITALIC, 15));
        jllabel.setBounds(0, height, 300, height);

        jp.add(jl);
        jp.add(jllabel);
        return jp;
    }

    public static JPanel createMsgContent(final JPanel jp, final MsgView mv, final JScrollPane jsp) {
        jp.setLayout(null);
        jp.setVisible(true);
        jp.setOpaque(false);
        jp.setBorder(new MyBorder(0xcccccc));
        jp.setBounds(0, 70, 530, 380);
        jp.setPreferredSize(new Dimension(530, 380));

        JButton lastMsg = new JButton("查看更多信息");
        lastMsg.setLayout(null);
        lastMsg.setVisible(true);
        lastMsg.setBackground(Color.white);
        lastMsg.setBounds(210, 5, 150, 30);
        lastMsg.setBorder(null);
        lastMsg.setFont(new Font("宋体", Font.ITALIC, 13));
        lastMsg.setFocusable(false);
        lastMsg.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                int oldcount = count.get(mv.receiver.getAccount()).intValue();
                System.out.println("oldcount:" + oldcount);
                @SuppressWarnings("unchecked")
                List<Msg> list = ServiceFactory.getService("msg").listPart("" + mv.self.getId(), "" + mv.receiver.getId(), "3", "" + oldcount, "" + (oldcount + 5));
                count.remove(mv.receiver.getAccount());
                count.put(mv.receiver.getAccount(), oldcount + 5);
                Iterator<Msg> it = list.iterator();
                System.out.println(list.size());

                while (it.hasNext()) {
                    Msg msg = it.next();
                    JPanel newMsg = new JPanel();
                    int count = jp.getComponentCount();
                    int type = 0;
                    if (msg.getUserId() == mv.self.getId()) {
                        type = 2;
                    } else {
                        type = 1;
                    }
                    jp.add(createSingleMsgBlock(newMsg, 65 * (count - 1), 0xeeeeee, type, mv.receiver.getIcon(), mv.receiver.getName(), msg.getContent(), (new SimpleDateFormat("EEE  HH:mm:ss").format(msg.getDate()))), 0);
                    jp.setPreferredSize(new Dimension(530, 65 * count + 35));
                    jp.setBounds(0, 70, 530, 65 * count + 35);
                    jsp.getViewport().setViewPosition(new Point(0, 65 * (jp.getComponentCount())));
                }
            }
        });

        jp.add(lastMsg);

        return jp;
    }

    private static JPanel createSingleMsgBlock(JPanel jp, int top, int color, int type, String icon, String name, String content, String time) {
        jp.setVisible(true);
        jp.setOpaque(false);
        jp.setBounds(0, top + 35, 530, 65);
        jp.setLayout(null);

        ImageIcon ii = new ImageIcon(System.getProperty("user.dir") + icon);
        ii.setImage(ii.getImage().getScaledInstance(40, 40, Image.SCALE_DEFAULT));
        JLabel jicon = new JLabel(ii);
        jicon.setBounds(10, 10, 40, 40);
        jicon.setVisible(true);

        JLabel jlname = new JLabel();
        jlname.setVisible(true);
        jlname.setText(name + "，" + time);
        jlname.setBounds(55, 5, 420, 30);
        jlname.setFont(new Font("宋体", Font.ITALIC, 14));

        JLabel jlmsg = new JLabel();
        jlmsg.setVisible(true);
        jlmsg.setText(content);
        jlmsg.setBounds(60, 30, 410, 30);
        jlmsg.setFont(new Font("宋体", Font.ITALIC, 13));
        if (type == 2) {
            jlname.setHorizontalAlignment(SwingConstants.RIGHT);
            jlmsg.setHorizontalAlignment(SwingConstants.RIGHT);
            jicon.setBounds(480, 10, 40, 40);

        }
        jp.add(jicon);
        jp.add(jlname);
        jp.add(jlmsg);
        return jp;
    }

    public static JPanel createMsgRight(final MsgView mv) {
        JPanel jp = new JPanel();
        JPanel jpset = new JPanel();
        JPanel jpgroup = new JPanel();
        final JPanel jpresult = new JPanel();
        jp.setLayout(null);
        jp.setVisible(true);
        jp.setOpaque(false);
        jp.setBorder(new MyBorder(0xcccccc));
        jp.setBounds(550, 70, 250, 350);

        final JTextField jtf = new JTextField();
        jtf.setText("查找好友(昵称或账号)");
        jtf.setFont(new Font("宋体", Font.ITALIC, 13));
        jtf.setBounds(10, 10, 150, 30);
        jtf.addFocusListener(new FocusAdapter() {

            @Override
            public void focusGained(FocusEvent e) {
                if ("查找好友(昵称或账号)".equals(jtf.getText())) {
                    jtf.setText("");
                }
            }

            @Override
            public void focusLost(FocusEvent e) {
                if ("".equals(jtf.getText())) {
                    jtf.setText("查找好友(昵称或账号)");
                }
            }
        });

        JButton jb = new JButton("查找");
        jb.setBounds(160, 10, 60, 30);
        jb.setBackground(new Color(0x3CC3F5));
        jb.setForeground(new Color(0xffffff));
        jb.setBorderPainted(false);
        jb.addActionListener(new ActionListener() {

            @SuppressWarnings("unchecked")
            @Override
            public void actionPerformed(ActionEvent e) {
                if ("查找好友(昵称或账号)".equals(jtf.getText())) {
                    return;
                }
                List<User> users = ServiceFactory.getService("user").listPart("" + mv.self.getId(), mv.self.getAccount(), jtf.getText(), jtf.getText());
                if (users.size() > 0) {
                    jpresult.setVisible(true);
                    JPanel jp = (JPanel) jpresult.getComponent(1);
                    JLabel jlname = (JLabel) jp.getComponent(0);
                    JLabel jllabel = (JLabel) jp.getComponent(1);
                    jlname.setText(users.get(0).getName());
                    jllabel.setText(users.get(0).getLabel());

                    JLabel icon = (JLabel) jpresult.getComponent(0);
                    ImageIcon ii = new ImageIcon(System.getProperty("user.dir") + users.get(0).getIcon());
                    ii.setImage(ii.getImage().getScaledInstance(40, 40, Image.SCALE_DEFAULT));
                    icon.setIcon(ii);

                    JLabel jlaccount = (JLabel) jpresult.getComponent(3);
                    jlaccount.setText(users.get(0).getAccount());

                } else {
                    jpresult.setVisible(false);
                }
            }
        });

        createGroupList(jpgroup);
        createSetBlock(jpset);
        jp.add(jb);
        jp.add(jtf);
        jp.add(jpset);
        jp.add(jpgroup);
        jp.add(createSearchResult(mv, jpresult));
        if ("friend".equals(mv.type)) {
            jpset.setVisible(true);
            jpgroup.setVisible(false);
        } else {
            jpset.setVisible(false);
            jpgroup.setVisible(true);
        }
        return jp;
    }

    public static JPanel createSearchResult(final MsgView mv, JPanel jp) {
        jp.setLayout(null);
        jp.setBounds(0, 50, 250, 100);
        jp.setBorder(new MyBorder(0xcccccc));
        jp.setOpaque(false);
        jp.setVisible(false);

        JLabel jlicon = createIconLabel(40, 10, 40, 40, 0xffffff, mv);
        JPanel jpname = createNameLabel(90, 10, 180, 20, "fsc", 0xffffff, "hello");

        final JLabel jl = new JLabel();
        jl.setText("test");

        JButton jb = new JButton("发起会话");
        jb.setVisible(true);
        jb.setBackground(Color.white);
        jb.setBorder(new MyBorder(0xeeeeee));
        jb.setForeground(Color.black);
        jb.setBounds(40, 60, 100, 30);
        jb.setFont(new Font("宋体", Font.ITALIC, 14));
        jb.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                new Thread(new Runnable() {

                    @Override
                    public void run() {
                        MsgView.createMsgView("QQ", "friend", jl.getText(), mv.client, mv.self.getAccount());
                    }
                }).start();
                ;
            }
        });

        jp.add(jlicon);
        jp.add(jpname);
        jp.add(jb);
        jp.add(jl);
        return jp;
    }

    public static JPanel createGroupList(JPanel jp) {
        jp.setLayout(null);
        jp.setBounds(0, 150, 250, 200);
        jp.setBorder(new MyBorder(0xcccccc));
        jp.setOpaque(false);
        jp.setVisible(false);

        JTree tree = null;
        DefaultMutableTreeNode group = new DefaultMutableTreeNode("群成员(10/20)");
        DefaultMutableTreeNode group1 = new DefaultMutableTreeNode("fsc");
        DefaultMutableTreeNode group2 = new DefaultMutableTreeNode("zyf");
        DefaultMutableTreeNode group3 = new DefaultMutableTreeNode("000");
        DefaultMutableTreeNode group4 = new DefaultMutableTreeNode("111");
        group.add(group1);
        group.add(group2);
        group.add(group3);
        group.add(group4);
        tree = new JTree(group);
        tree.setBounds(10, 10, 180, 180);
        tree.setOpaque(false);
        tree.setFont(new Font("宋体", Font.BOLD, 15));

        jp.add(tree);
        return jp;
    }

    public static JPanel createSetBlock(JPanel jp) {
        jp.setLayout(null);
        jp.setBounds(0, 150, 250, 200);
        jp.setBorder(new MyBorder(0xcccccc));
        jp.setOpaque(false);
        jp.setVisible(true);

        JButton jb = new JButton("删除TA");
        jb.setVisible(true);
        jb.setBackground(Color.white);
        jb.setBorder(new MyBorder(0xeeeeee));
        jb.setForeground(Color.black);
        jb.setBounds(120, 20, 100, 30);
        jb.setFont(new Font("宋体", Font.ITALIC, 14));

        JButton jb1 = new JButton("拉黑TA");
        jb1.setVisible(true);
        jb1.setBackground(Color.white);
        jb1.setBorder(new MyBorder(0xeeeeee));
        jb1.setForeground(Color.black);
        jb1.setBounds(120, 60, 100, 30);
        jb1.setFont(new Font("宋体", Font.ITALIC, 14));

        JButton jb2 = new JButton("关注TA");
        jb2.setVisible(true);
        jb2.setBackground(Color.white);
        jb2.setBorder(new MyBorder(0xeeeeee));
        jb2.setForeground(Color.black);
        jb2.setBounds(120, 100, 100, 30);
        jb2.setFont(new Font("宋体", Font.ITALIC, 14));

        JButton jb3 = new JButton("举报TA");
        jb3.setVisible(true);
        jb3.setBackground(Color.white);
        jb3.setBorder(new MyBorder(0xeeeeee));
        jb3.setForeground(Color.black);
        jb3.setBounds(120, 140, 100, 30);
        jb3.setFont(new Font("宋体", Font.ITALIC, 14));

        jp.add(jb);
        jp.add(jb1);
        jp.add(jb2);
        jp.add(jb3);
        return jp;
    }

    public static JPanel createEnd() {
        JPanel jp = new JPanel();
        jp.setLayout(null);
        jp.setBounds(550, 420, 250, 280);
        jp.setBorder(new MyBorder(0xcccccc));
        jp.setOpaque(false);
        jp.setVisible(true);

        JLabel jl = new JLabel("历史消息");
        jl.setBackground(new Color(0xcccccc));
        jl.setFont(new Font("宋体", Font.BOLD, 12));
        jl.setForeground(Color.white);
        jl.setOpaque(true);
        jl.setBounds(0, 10, 250, 30);
        jp.add(jl);

        JTextArea jta = new JTextArea("hello 2016-10-17 \n hello everyone !");
        jta.setBounds(10, 40, 250, 50);
        jta.setEditable(false);
        jp.add(jta);

        JTextArea jta1 = new JTextArea("hello 2016-10-17 \n hello everyone !");
        jta1.setBounds(10, 90, 250, 50);
        jta1.setEditable(false);
        jp.add(jta1);
        return jp;
    }

    private static JPanel createSendMsg(JPanel jp, final MsgView mv, final JPanel jpmsgblock, final JScrollPane jsp) {
        jp.setLayout(null);
        jp.setBounds(0, 470, 551, 230);
        jp.setBorder(new MyBorder(0xcccccc));
        jp.setOpaque(false);
        jp.setVisible(true);

        final JTextArea jtf = new JTextArea("此处输入信息");
        jtf.setBounds(10, 10, 530, 130);
        jtf.setFont(new Font("宋体", Font.ITALIC, 15));
        jtf.addFocusListener(new FocusAdapter() {

            @Override
            public void focusGained(FocusEvent e) {
                if ("此处输入信息".equals(jtf.getText())) {
                    jtf.setText("");
                }
            }

            @Override
            public void focusLost(FocusEvent e) {
                if ("".equals(jtf.getText())) {
                    jtf.setText("此处输入信息");
                }
            }
        });

        JButton jbcancle = new JButton("重置");
        JButton jbsend = new JButton("发送");
        final JLabel jlerror = new JLabel();
        jlerror.setVisible(false);
        jlerror.setBounds(150, 140, 200, 30);
        jlerror.setOpaque(false);
        jlerror.setFont(new Font("宋体", Font.ITALIC, 13));
        jbcancle.setBackground(new Color(0x3CC3F5));
        jbcancle.setForeground(Color.WHITE);
        jbcancle.setBounds(350, 140, 80, 30);
        jbcancle.setBorderPainted(false);
        jbcancle.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                jtf.setText("此处输入信息");
            }
        });

        jbsend.setBackground(new Color(0x3CC3F5));
        jbsend.setForeground(Color.WHITE);
        jbsend.setBounds(440, 140, 80, 30);
        jbsend.setBorderPainted(false);
        jbsend.addActionListener(new ActionListener() {

            /* (non-Javadoc)
             * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
             * 功能：发送信息到服务器，消息格式3为标识符，“：”为分隔符，同时在主界面上打印消息
             */
            @Override
            public void actionPerformed(ActionEvent e) {
                String content = jtf.getText().trim();
                if ("此处输入信息".equals(content)) {
                    return;
                }
                Msg msg = new Msg();
                msg.setContent(content);
                msg.setReceiver(mv.receiver.getId());
                msg.setUserId(mv.self.getId());
                msg.setStatus(1);
                msg.setType(3);
                String sendmsg = "3:" + mv.receiver.getAccount() + ":" + content + "\n";
                try {
                    BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(mv.client.getOutputStream()));
                    bw.write(sendmsg);
                    bw.flush();
//					bw.close();
                    System.out.println(mv.self.getAccount() + "发送消息：" + sendmsg);
                    JPanel ownMsg = new JPanel();
                    jpmsgblock.add(createSingleMsgBlock(ownMsg, 65 * (jpmsgblock.getComponentCount() - 1), 0xffffff, 2, mv.self.getIcon(), mv.self.getName(), content, new SimpleDateFormat("EEE  HH:mm:ss").format(new Date())));
                    jpmsgblock.setPreferredSize(new Dimension(530, 65 * jpmsgblock.getComponentCount() + 35));
                    jpmsgblock.setBounds(0, 70, 530, 65 * jpmsgblock.getComponentCount() + 35);
                    jsp.getViewport().setViewPosition(new Point(0, 65 * (jpmsgblock.getComponentCount())));
                    mv.repaint();

                } catch (IOException e1) {
                    e1.printStackTrace();
                }
            }
        });

        jp.add(jtf);
        jp.add(jlerror);
        jp.add(jbcancle);
        jp.add(jbsend);
        return jp;
    }
}
